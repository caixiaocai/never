package com.anker.autotest.eufysecurity;

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

/***
 * 轮流查看所有设备的视频流，查看完一轮杀掉重启app
 */
@RunWith(AndroidJUnit4.class)
public class PlayEventsTest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testRound = 0;

    private String HISTORY_ITEM = "com.oceanwing.battery.cam:id/history_item";
    private String PLAY_BTN = "com.oceanwing.battery.cam:id/imv_history_play";
    private String TIME_LONG = "com.oceanwing.battery.cam:id/item_history_camera_time_long";
    private String DEVICES_NAME = "com.oceanwing.battery.cam:id/item_history_devices_name";
    private String EVENT_TIME = "com.oceanwing.battery.cam:id/item_history_event_time";
    private String PLAY_END = "com.oceanwing.battery.cam:id/item_history_video_preview_btn_play";
    private String RETRY_BTN = "com.oceanwing.battery.cam:id/camera_preview_video_btn_retry";


    @Before
    public void setUp() throws Exception {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");

        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("轮次");
            row.createCell(1).setCellValue("序号");
            row.createCell(2).setCellValue("时间");
            row.createCell(3).setCellValue("名字");
            row.createCell(4).setCellValue("结果");
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
        int testCount=0;
        String name;
        UiObject item, time_long, devices_name, event_time, play_btn;

        int x = mDevice.getDisplayWidth() / 2;

        XLog.i("start test");
        while (true) {
            testRound++;  //第几轮
            XLog.i(String.format("第【 %d 】轮测试", testRound));

            mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W");
            for (int i = 0; i < 40000; i++) {
                if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_cancel").text("Not Now")).exists()) {
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_cancel").text("Not Now")).click();
                }
                if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/item_tab_txt").text("Events")).exists()) {
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/item_tab_txt").text("Events")).click();
                    mDevice.wait(Until.findObject(By.res(HISTORY_ITEM)), 10000);
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            int top = mDevice.findObject(new UiSelector().resourceId(HISTORY_ITEM)).getBounds().centerY();
            String endName = "";

            XLog.i("轮流查看历史视频");
            int swipe_count = 0;
            int pageCount = 0;
            while (true) {
                pageCount++;  //第几行/第几次点播放
                XLog.i(String.format("第【 %d 】页历史视频", pageCount));


                int count = mDevice.findObjects(By.res(HISTORY_ITEM)).size();
                int bottom = mDevice.findObject(new UiSelector().resourceId(HISTORY_ITEM).index(count-1)).getBounds().centerY();
                boolean clickable = false, clicked = false;
                if (pageCount == 1) {
                    clickable = true;
                }
                for (int j = 0; j < count; j++) {
                    item = mDevice.findObject(new UiSelector().resourceId(HISTORY_ITEM).index(j));
                    try {
                        time_long = item.getChild(new UiSelector().resourceId(TIME_LONG));
                        devices_name = item.getChild(new UiSelector().resourceId(DEVICES_NAME));
                        event_time = item.getChild(new UiSelector().resourceId(EVENT_TIME));
                        play_btn = item.getChild(new UiSelector().resourceId(PLAY_BTN));
                        name = String.format("%s %s %s", devices_name.getText(), event_time.getText(), time_long.getText());
                    }catch (UiObjectNotFoundException e){
                        continue;
                    }
                    if (clickable) {
                        testCount ++;
                        clicked = true;
                        bottom = item.getBounds().centerY();
                        XLog.i(String.format("查看【 %s 】历史视频", name));
                        HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
                        HSSFSheet sheet = wb.getSheetAt(0);
                        HSSFRow row = sheet.createRow(testCount);
                        row.createCell(0).setCellValue(testRound);
                        row.createCell(1).setCellValue(testCount);
                        row.createCell(2).setCellValue(DateUtils.getSystemTime());
                        row.createCell(3).setCellValue(name);

                        play_btn.click();
                        if(waitPlay()){
                            row.createCell(4).setCellValue("pass");
                        }else{
                            row.createCell(4).setCellValue("fail");
                        }
                        mDevice.pressBack();
                        endName = name;

                        FileOutputStream os = new FileOutputStream(filePath);
                        wb.write(os);
                        os.close();
                        wb.close();
                        XLog.i("结果写入完成");
                    }else if (name.equals(endName)) {
                        clickable = true;
                        continue;
                    }
                }
                if (!clicked) {
                    for (int k = 0; k < swipe_count + 1; k++) {
                        mDevice.swipe(x, top, x, bottom, 100);
                    }
                    break;
                }
                swipe_count++;
                mDevice.swipe(x, bottom, x, top, 100);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(mDevice.findObject(new UiSelector().className("android.widget.ProgressBar")).exists()){
                    mDevice.wait(Until.gone(By.clazz("android.widget.ProgressBar")), 120000);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for(int t=0; t<3;t++) {
                        swipe_count++;
                        mDevice.swipe(x, bottom, x, top, 100);
                    }
                }
            }
        }
    }

    public boolean waitPlay() {
        /**
         * return true表示成功
         * return false表示超时或者失败
         */
        while (true) {
            if (mDevice.findObject(new UiSelector().resourceId(RETRY_BTN)).exists()) {
                return false;
            }
            if (mDevice.findObject(new UiSelector().resourceId(PLAY_END)).exists()) {
                return true;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
