package com.anker.autotest.eufysecurity;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import com.anker.autotest.utils.Common;
import com.anker.autotest.utils.DateUtils;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/***
 * 测试出流成功后截图
 * 通过配置参数restart设置单次测试后是否杀掉app重启
 * 可用于：安防摄像头，门铃
 */
@RunWith(AndroidJUnit4.class)
public class PlayAndScreenshotTest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0;
    private boolean flag = false;
    private boolean playFlag = false;
    private int loadingtimeout = 60;
    private int waitidle = 5;
    private int playtime = 5;
    private int uiconfig = 3;
    private boolean restart = false;
    private String info;

    private String LINE_BTN =  "com.oceanwing.battery.cam:id/item_single_camera_img_live";
    private String TALK_BACK = "com.oceanwing.battery.cam:id/vb_talkback";
    private String SCREENSHOT = "com.oceanwing.battery.cam:id/iv_screenshot";


    @Before
    public void setUp() throws Exception {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");

        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);

        Bundle extras = InstrumentationRegistry.getArguments();
        if (extras.containsKey("loadingtimeout")) {
            loadingtimeout = Integer.parseInt(extras.getString("loadingtimeout"));
        }
        if (extras.containsKey("waitidle")) {
            waitidle = Integer.parseInt(extras.getString("waitidle"));
        }
        if (extras.containsKey("playtime")) {
            playtime = Integer.parseInt(extras.getString("playtime"));
        }
        if (extras.containsKey("uiconfig")) {
            uiconfig = Integer.parseInt(extras.getString("uiconfig"));
        }
        if (extras.containsKey("restart")) {
            restart = Boolean.parseBoolean(extras.getString("restart"));
        }

        XLog.i(String.format("loadingtimeout: %s", loadingtimeout));
        XLog.i(String.format("waitidle: %s", waitidle));
        XLog.i(String.format("playtime: %s", playtime));
        XLog.i(String.format("restart: %s", restart));
        XLog.i(String.format("uiconfig: %s", uiconfig));

        if(uiconfig==1){
            LINE_BTN =  "com.oceanwing.battery.cam:id/item_single_camera_img_live";

        }else if(uiconfig==2){
            LINE_BTN = "com.oceanwing.battery.cam:id/iv_live";
        }

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);
        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("开始时间");
            row.createCell(2).setCellValue("出流时间");
            row.createCell(3).setCellValue("结果");
            row.createCell(4).setCellValue("出流耗时(ms)");
            row.createCell(5).setCellValue("异常信息");
            row.createCell(6).setCellValue("播放失败时间点");
            row.createCell(7).setCellValue("loadingtimeout=" + loadingtimeout);
            row.createCell(8).setCellValue("waitidle=" + waitidle);
            row.createCell(9).setCellValue("playtime=" + playtime);
            row.createCell(10).setCellValue("restart=" + restart);
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
        }
    }

    @After
    public void tearDown() {
        String screenshotFile = floderPath + "fail_" + DateUtils.getDateTime() + ".png";
        mDevice.takeScreenshot(new File(screenshotFile));
        XLog.i(screenshotFile);
    }

    @Test
    public void test() throws IOException, UiObjectNotFoundException {
        long time_start, time_end;
        String del, shot_name;
        XLog.i("start test");
        if (!restart) {
            mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");
            mDevice.wait(Until.findObject(By.res(LINE_BTN)), 60000);
        }
        while (true) {
            testCount++;
            XLog.i(String.format("第【 %d 】次测试", testCount));
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);
            if (restart) {
                mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W");
                mDevice.wait(Until.findObject(By.res(LINE_BTN)), 60000);
            }
            formatPopUp();
            mDevice.findObject(By.res(LINE_BTN)).click();
            time_start = System.currentTimeMillis();
            row.createCell(1).setCellValue(DateUtils.getSystemTime(time_start));
            flag = waitPlay(loadingtimeout * 1000);
            if (flag) { //成功
                XLog.i("出流成功");
                time_end = System.currentTimeMillis();
                row.createCell(2).setCellValue(DateUtils.getSystemTime(time_end));
                row.createCell(3).setCellValue("pass");
                row.createCell(4).setCellValue(time_end - time_start);
                playFlag = play(playtime * 1000);
                if(!playFlag){
                    XLog.i("播放过程中出现异常");
                    row.createCell(6).setCellValue(DateUtils.getSystemTime());
                    shot_name = floderPath + testCount + "_" + DateUtils.getDateTime() + ".png";
                    mDevice.takeScreenshot(new File(shot_name));
                }else{
                    mDevice.findObject(By.res(SCREENSHOT)).click();
                }
            } else { //超时或者失败
                XLog.i("出流超时或失败");
                shot_name = floderPath + testCount + "_" + DateUtils.getDateTime() + ".png";
                mDevice.takeScreenshot(new File(shot_name));
                row.createCell(3).setCellValue("fail");
                if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/camera_preview_txt_remind_title")).exists()) {
                    info = mDevice.findObject(By.res("com.oceanwing.battery.cam:id/camera_preview_txt_remind_title")).getText();
                    row.createCell(5).setCellValue(info);
                }
            }
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");
            mDevice.pressBack();

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (restart) {
                mDevice.executeShellCommand("am force-stop com.oceanwing.battery.cam");
            }
            try {
                Thread.sleep(waitidle * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean waitPlay(int timeout) throws UiObjectNotFoundException {
        /**
         * return true表示成功
         * return false表示超时或者失败
         */
        Date start = new Date();
        UiObject obj;
        while (true) {
            if(uiconfig==1 || uiconfig==2){
                if (mDevice.findObject(new UiSelector().resourceId(TALK_BACK)).exists()) {
                    try {
                        if (mDevice.findObject(new UiSelector().resourceId(TALK_BACK)).isEnabled()) {
                            break;
                        }
                    } catch (UiObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/camera_bottom_menu_btn_mike")).exists()) {
                    try {
                        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/camera_bottom_menu_btn_mike")).isEnabled()) {
                            break;
                        }
                    } catch (UiObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/iv_voice")).exists()) {
                    try {
                        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/iv_voice")).isEnabled()) {
                            break;
                        }
                    } catch (UiObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }else if(uiconfig==3){
                obj = mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/item_super_video_layout"));
                if(obj.exists() && obj.getChildCount()==0){
                    break;
                }else{
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            Date end = new Date();
            long time = end.getTime() - start.getTime();
            if (time > timeout) {
                return false;
            }
        }
        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/camera_preview_video_btn_retry")).exists()) {
            return false;
        }
        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/video_player_error_view")).exists()) {
            return false;
        }
        return true;
    }

    private boolean play(int timeout){
        Date start = new Date();
        while(true) {
            Date end = new Date();
            long time = end.getTime() - start.getTime();
            if (time > timeout) {
                return true;
            }
            if(mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/cb_cellular_dialog_remind")).exists()){
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/cb_cellular_dialog_remind")).click();
            }
            if(mDevice.hasObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure").text("Continue"))){
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure").text("Continue")).click();
            }
            if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/camera_preview_video_btn_retry")).exists()) {
                return false;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void formatPopUp(){
        if(mDevice.hasObject(By.textContains("format HomeBase microSD Card"))){
            if(mDevice.hasObject(By.res("com.oceanwing.battery.cam:id/dialog_cancel"))){
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_cancel")).click();
            }
            else if(mDevice.hasObject(By.res("com.oceanwing.battery.cam:id/dialog_ok"))){
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_ok")).click();
                mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/activity_storage_method_back")), 30000);
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/activity_storage_method_back")).click();
            }
        }
    }
}
