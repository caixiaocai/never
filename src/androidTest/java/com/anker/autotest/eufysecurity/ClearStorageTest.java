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
 * 测试清除缓存
 * 通过配置参数restart设置单次测试后是否杀掉app重启
 * 可用于：安防摄像头，门铃
 */
@RunWith(AndroidJUnit4.class)
public class ClearStorageTest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0;
    private boolean flag = false;
    private int interval = 3600;
    private int uiconfig = 2;
    private boolean restart = false;
    private String info;

    private String LINE_BTN =  "com.oceanwing.battery.cam:id/item_single_camera_img_live";
    private String TALK_BACK = "com.oceanwing.battery.cam:id/vb_talkback";


    @Before
    public void setUp() throws Exception {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");

        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);

        Bundle extras = InstrumentationRegistry.getArguments();

        if (extras.containsKey("interval")) {
            interval = Integer.parseInt(extras.getString("interval"));
        }
        if (extras.containsKey("uiconfig")) {
            uiconfig = Integer.parseInt(extras.getString("uiconfig"));
        }
        if (extras.containsKey("restart")) {
            restart = Boolean.parseBoolean(extras.getString("restart"));
        }

        XLog.i(String.format("interval: %s", interval));
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
            row.createCell(1).setCellValue("时间");
            row.createCell(2).setCellValue("清除前");
            row.createCell(3).setCellValue("清除后");
            row.createCell(4).setCellValue("结果");
            row.createCell(5).setCellValue("restart device");
            row.createCell(6).setCellValue("interval=" + interval);
            row.createCell(7).setCellValue("restart=" + restart);
            row.createCell(8).setCellValue("uiconfig=" + uiconfig);
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
        XLog.i("start test");
        String before_text, after_text;
        float before, after;
        long start, end;
        int x = mDevice.getDisplayWidth()/2;
        int y = mDevice.getDisplayHeight();
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
            mDevice.findObject(By.res(LINE_BTN)).click();
            XLog.i("查看实时流");
            tryPlay();
            XLog.i("出流成功");
            mDevice.pressBack();
            mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/iv_settings")), 3000);
            mDevice.findObject(By.res("com.oceanwing.battery.cam:id/iv_settings")).click();
            mDevice.wait(Until.findObject(By.text("Camera Storage")), 3000);
            mDevice.findObject(By.text("Camera Storage")).click();
            mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/tv_available")), 3000);
            before_text = mDevice.findObject(By.res("com.oceanwing.battery.cam:id/tv_available")).getText();
            XLog.i(before_text);
//            before = Float.parseFloat(before_text.split(" ")[0]);
            start = System.currentTimeMillis();
            row.createCell(1).setCellValue(DateUtils.getSystemTime(start));
            mDevice.findObject(By.res("com.oceanwing.battery.cam:id/tv_delete_clips")).click();
            mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/btn_positive")), 30000);
            mDevice.findObject(By.res("com.oceanwing.battery.cam:id/btn_positive")).click();
            mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/tv_available")), 30000);
            after_text = mDevice.findObject(By.res("com.oceanwing.battery.cam:id/tv_available")).getText();
            XLog.i(after_text);
//            after = Float.parseFloat(after_text.split(" ")[0]);
            row.createCell(2).setCellValue(before_text);
            row.createCell(3).setCellValue(after_text);
            flag = !before_text.equals(after_text);
            XLog.i(flag);
            if(flag){
                row.createCell(4).setCellValue("pass");
                mDevice.pressBack();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mDevice.pressBack();
            }else {
                row.createCell(4).setCellValue("fail");
                mDevice.pressBack();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mDevice.swipe(x, y / 10 * 8, x, y / 10, 100);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                XLog.i("restart device");
                row.createCell(5).setCellValue(1);
                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/station_setting_btn_restart")), 30000);
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/station_setting_btn_restart")).click();
                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/btn_positive")), 30000);
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/btn_positive")).click();
                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/btn_positive")), 30000);
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/btn_positive")).click();
            }

            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");

            if(!flag){
                testCount++;
                XLog.i("清除失败后重试");
                XLog.i(String.format("第【 %d 】次测试", testCount));
                wb = new HSSFWorkbook(new FileInputStream(filePath));
                sheet = wb.getSheetAt(0);
                row = sheet.createRow(testCount);
                row.createCell(0).setCellValue(testCount);
                mDevice.wait(Until.findObject(By.res(LINE_BTN)), 60000);
                mDevice.findObject(By.res(LINE_BTN)).click();
                tryPlay();
                mDevice.pressBack();
                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/iv_settings")), 3000);
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/iv_settings")).click();
                mDevice.wait(Until.findObject(By.text("Camera Storage")), 3000);
                mDevice.findObject(By.text("Camera Storage")).click();
                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/tv_available")), 3000);
                before_text = mDevice.findObject(By.res("com.oceanwing.battery.cam:id/tv_available")).getText();
                XLog.i(before_text);
//                before = Float.parseFloat(before_text.split(" ")[0]);
                start = System.currentTimeMillis();
                row.createCell(1).setCellValue(DateUtils.getSystemTime(start));
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/tv_delete_clips")).click();
                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/btn_positive")), 30000);
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/btn_positive")).click();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                after_text = mDevice.findObject(By.res("com.oceanwing.battery.cam:id/tv_available")).getText();
                XLog.i(after_text);
//                after = Float.parseFloat(after_text.split(" ")[0]);
                row.createCell(2).setCellValue(before_text);
                row.createCell(3).setCellValue(after_text);
                flag = !before_text.equals(after_text);
                XLog.i(flag);
                if(flag){
                    row.createCell(4).setCellValue("pass");
                }else {
                    row.createCell(4).setCellValue("fail");
                }

                os = new FileOutputStream(filePath);
                wb.write(os);
                os.close();
                wb.close();
                XLog.i("结果写入完成");
                mDevice.pressBack();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mDevice.pressBack();

            }
            end = start + interval * 1000;
            XLog.i("开始瞎点消磨时间");
            XLog.i(DateUtils.getSystemTime(end));
            while(true) {
                if(System.currentTimeMillis() > end){
                    XLog.i("时间到，结束当前操作，进行下一次清除操作");
                    break;
                }
                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/iv_settings")), 3000);
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/iv_settings")).click();
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mDevice.pressBack();
            }

            if (restart) {
                mDevice.executeShellCommand("am force-stop com.oceanwing.battery.cam");
            }
            try {
                Thread.sleep(3 * 1000);
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

    public void tryPlay() throws UiObjectNotFoundException {
        UiObject obj;
        while (true) {
            if(uiconfig==1 || uiconfig==2){
                if (mDevice.findObject(new UiSelector().resourceId(TALK_BACK)).exists()) {
                    try {
                        if (mDevice.findObject(new UiSelector().resourceId(TALK_BACK)).isEnabled()) {
                            return;
                        }
                    } catch (UiObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/camera_bottom_menu_btn_mike")).exists()) {
                    try {
                        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/camera_bottom_menu_btn_mike")).isEnabled()) {
                            return;
                        }
                    } catch (UiObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/iv_voice")).exists()) {
                    try {
                        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/iv_voice")).isEnabled()) {
                            return;
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
                    return;
                }else{
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/camera_preview_video_btn_retry")).exists()) {
                mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/camera_preview_video_btn_retry")).click();
            }
            else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/video_player_error_view")).exists()) {
                mDevice.pressBack();
                mDevice.wait(Until.findObject(By.res(LINE_BTN)), 60000);
                mDevice.findObject(By.res(LINE_BTN)).click();
            }
        }
    }
}
