package com.anker.autotest.eufysecurity;

import android.graphics.Rect;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/***
 * 轮流查看所有设备的视频流，查看完一轮杀掉重启app
 */
@RunWith(AndroidJUnit4.class)
public class PlayTurnsScreenshotWithLightTest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0, testRound = 0;
    private boolean flag = false;
    private int loadingtimeout = 15;
    private int waitidle = 2;
    private int playtime = 3;
    private int uiconfig = 1;
    private String info;

    private String LINE_BTN =  "com.oceanwing.battery.cam:id/item_single_camera_img_live";
    private String NAME_VIEW = "com.oceanwing.battery.cam:id/item_single_camera_cam_name";
    private String TALK_BACK = "com.oceanwing.battery.cam:id/vb_talkback";
    private String ADD_DEVICE =  "com.oceanwing.battery.cam:id/add_device";
    private String SCREENSHOT = "com.oceanwing.battery.cam:id/iv_event_cut_img";


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
        XLog.i(String.format("loadingtimeout: %s", loadingtimeout));
        XLog.i(String.format("waitidle: %s", waitidle));
        XLog.i(String.format("playtime: %s", playtime));
        XLog.i(String.format("uiconfig: %s", uiconfig));

        if(uiconfig==1){
            LINE_BTN =  "com.oceanwing.battery.cam:id/item_single_camera_img_live";
            NAME_VIEW = "com.oceanwing.battery.cam:id/item_single_camera_cam_name";
        }else if(uiconfig==2){
            LINE_BTN = "com.oceanwing.battery.cam:id/iv_live";
            NAME_VIEW = "com.oceanwing.battery.cam:id/tv_device_name";
        }else if(uiconfig==4){
            TALK_BACK = "com.oceanwing.battery.cam:id/iv_talkback";
        }

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("名字");
            row.createCell(2).setCellValue("灯状态");
            row.createCell(3).setCellValue("开始时间");
            row.createCell(4).setCellValue("截图时间");
            row.createCell(5).setCellValue("出流耗时");
            row.createCell(6).setCellValue("异常信息");
            row.createCell(7).setCellValue("loadingtimeout=" + loadingtimeout);
            row.createCell(8).setCellValue("waitidle=" + waitidle);
            row.createCell(9).setCellValue("playtime=" + playtime);
            row.createCell(10).setCellValue("uiconfig=" + uiconfig);
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
        String del, shot_name, status;
        String light="offline";
        Rect rect;
        int y, top;
        int x = mDevice.getDisplayWidth() / 2;
        int y_start = mDevice.getDisplayHeight()/5;
        int y_end = y_start*4;
        UiObject2 status_obj;

        XLog.i("start test");

        ArrayList<String> nameList = getName();
        int len = nameList.size();
        mDevice.executeShellCommand("am force-stop com.oceanwing.battery.cam");
        UiObject2 nameView;
        List<UiObject2> playView;
        while (true) {
            testRound++;  //第几轮
            XLog.i(String.format("第【 %d 】轮测试", testRound));
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);

            mDevice.executeShellCommand("am start -n com.eufylife.smarthome/com.oceanwing.eufyhome.main.WelcomeActivity -W -S");
            mDevice.wait(Until.findObject(By.res("com.eufylife.smarthome:id/device_status")), 30000);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            status_obj = mDevice.findObject(By.res("com.eufylife.smarthome:id/device_status"));
            status = status_obj.getText();
            XLog.i(status);
            while ("Offline".equals(status)){
                XLog.i("灯离线，刷新");
                mDevice.swipe(x, y_start, x, y_end, 50);
//                mDevice.executeShellCommand("am start -n com.eufylife.smarthome/com.oceanwing.eufyhome.main.WelcomeActivity -W -S");
//                mDevice.wait(Until.findObject(By.res("com.eufylife.smarthome:id/device_status")), 30000);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                status_obj = mDevice.findObject(By.res("com.eufylife.smarthome:id/device_status"));
                status = status_obj.getText();
            }
            if("OFF".equals(status)){
                XLog.i("开灯");
                mDevice.findObject(By.res("com.eufylife.smarthome:id/switch_sitchview")).click();
                mDevice.wait(Until.findObject(By.res("com.eufylife.smarthome:id/device_status").text("ON")), 30000);
                light = "on";
            }else if("ON".equals(status)){
                XLog.i("关灯");
                mDevice.findObject(By.res("com.eufylife.smarthome:id/switch_sitchview")).click();
                mDevice.wait(Until.findObject(By.res("com.eufylife.smarthome:id/device_status").text("OFF")), 30000);
                light = "off";
            }
            try {
                Thread.sleep(waitidle*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W");
            for (int i = 0; i < 40000; i++) {
                if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_cancel").text("Not Now")).exists()) {
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_cancel").text("Not Now")).click();
                }
                formatPopUp();
                if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/cameraRecyclerView")).exists()) {
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            XLog.i("轮流查看实时流");
            for (int index=0;index<len;index++) {
                String name = nameList.get(index);
                testCount++;  //第几行/第几次点播放
                XLog.i(String.format("第【 %d 】次查看实时流", testCount));
                XLog.i(String.format("查看设备【 %s 】实时流", name));
                HSSFRow row = sheet.createRow(testCount);
                row.createCell(0).setCellValue(testRound);
                row.createCell(1).setCellValue(name);
                row.createCell(2).setCellValue(light);
                int bottom = mDevice.getDisplayHeight();
                boolean clickPlay=false;
                boolean existed=false;
                find:
                for (int i = 0; i < len * 2; i++) {
                    if (mDevice.findObject(new UiSelector().resourceId(NAME_VIEW).text(name)).exists()) {
                        existed=true;
                        playView = mDevice.findObjects(By.res(LINE_BTN));

                        nameView = mDevice.findObject(By.res(NAME_VIEW).text(name));
                        top = nameView.getVisibleBounds().centerY();
                        if(index<len-1 && mDevice.findObject(new UiSelector().resourceId(NAME_VIEW).text(nameList.get(index+1))).exists()){
                            nameView = mDevice.findObject(By.res(NAME_VIEW).text(nameList.get(index+1)));
                            bottom = nameView.getVisibleBounds().centerY();
                        }
                        for (UiObject2 view : playView) {
                            y = view.getVisibleBounds().centerY();
                            if (top < y && y < bottom) {
                                view.click();
                                clickPlay = true;
                                break find;
                            }
                        }
                        if (top < mDevice.getDisplayHeight() / 2) {
                            clickPlay = false;
                            break find;
                        }
                    }else if(existed){  // 这个设备名存在过，现在不存在了，所以判断为划过去了还没有播放按钮
                        mDevice.swipe(x, mDevice.getDisplayHeight() / 4, x, mDevice.getDisplayHeight() * 3 / 4, 200);
                        clickPlay = false;
                        break find;
                    }
                    mDevice.swipe(x, mDevice.getDisplayHeight() * 3 / 4, x, mDevice.getDisplayHeight() / 4, 200);
                }
                if(clickPlay) {
                    time_start = System.currentTimeMillis();
                    row.createCell(3).setCellValue(DateUtils.getSystemTime(time_start));
                    flag = waitPlay(loadingtimeout * 1000);
                    if (flag) { //成功
                        XLog.i("出流成功");
                        try {
                            Thread.sleep(playtime * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        XLog.i("截图");
                        mDevice.findObject(By.res(SCREENSHOT)).click();
                        time_end = System.currentTimeMillis();
                        del = String.format("%d s %d ms", (time_end - time_start) / 1000, (time_end - time_start) % 1000);
                        row.createCell(4).setCellValue(DateUtils.getSystemTime(time_end));
                        row.createCell(5).setCellValue(del);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else { //超时或者失败
                        XLog.i("出流超时或失败");
                        shot_name = floderPath + testCount + "_" + DateUtils.getDateTime() + ".png";
                        mDevice.takeScreenshot(new File(shot_name));
                        row.createCell(4).setCellValue("fail");
                        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/camera_preview_txt_remind_title")).exists()) {
                            info = mDevice.findObject(By.res("com.oceanwing.battery.cam:id/camera_preview_txt_remind_title")).getText();
                            row.createCell(6).setCellValue(info);
                        }
                    }
                    if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/iv_close")).exists()) {
                        mDevice.findObject(By.res("com.oceanwing.battery.cam:id/iv_close")).click();
                    } else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/activity_camera_preview_back")).exists()) {
//                    mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/activity_camera_preview_back")), 30000);
                        mDevice.findObject(By.res("com.oceanwing.battery.cam:id/activity_camera_preview_back")).click();
                    } else if (mDevice.findObject(new UiSelector().resourceId(TALK_BACK)).exists()) {
                        mDevice.pressBack();
                    }

                }else{
                    row.createCell(4).setCellValue("没有找到播放按钮");
                }
                mDevice.pressBack();
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
        }
    }

    public ArrayList<String> getName() throws UiObjectNotFoundException, IOException {
        String name;
        int x = mDevice.getDisplayWidth()/2;
        int startY = mDevice.getDisplayHeight()/3*2;
        int endY = mDevice.getDisplayHeight()/3;
        ArrayList<String> nameList = new ArrayList<>();
        mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");
        for (int i = 0; i < 40000; i++) {
            if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_cancel").text("Not Now")).exists()) {
                mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_cancel").text("Not Now")).click();
            }
            formatPopUp();
            if (mDevice.findObject(new UiSelector().resourceId(NAME_VIEW)).exists()) {
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < 50; i++) {
            List<UiObject2> objs = mDevice.findObjects(By.res(NAME_VIEW));
            for (UiObject2 obj : objs) {
                name = obj.getText();
                if(nameList.indexOf(name)==-1) {
                    nameList.add(name);
                }
            }
            if (mDevice.findObject(new UiSelector().resourceId(ADD_DEVICE)).exists() || !mDevice.findObject(new UiSelector().scrollable(true)).exists()) {
                break;
            } else {
                mDevice.swipe(x, startY, x, endY, 200);
//                mDevice.findObject(new UiSelector().scrollable(true)).swipeUp(250);
            }
        }
        return nameList;
    }

    public boolean waitPlay(int timeout) throws UiObjectNotFoundException {
        /**
         * return true表示成功
         * return false表示超时或者失败
         */
        Date start = new Date();
        UiObject obj;
        while (true) {
            if(uiconfig==3){
                obj = mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/item_super_video_layout"));
                if(obj.exists() && obj.getChildCount()==0){
                    break;
                }else{
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }else{
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
                        Thread.sleep(1000);
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
