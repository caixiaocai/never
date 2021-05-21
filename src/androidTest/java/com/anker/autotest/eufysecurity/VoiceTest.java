package com.anker.autotest.eufysecurity;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
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
 * 点击查看实时流，出流成功后点击进行对讲
 */
@RunWith(AndroidJUnit4.class)
public class VoiceTest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0;
    private boolean flag = false;
    private int loadingtimeout = 30;
    private int waitidle = 10;
    private int playtime = 60;
    private String info;

    @Before
    public void setUp() throws Exception {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
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
        XLog.i(String.format("loadingtimeout: %s", loadingtimeout));
        XLog.i(String.format("waitidle: %s", waitidle));
        XLog.i(String.format("playtime: %s", playtime));

        Common.allowPermission(mDevice);
        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("开始对讲");
            row.createCell(2).setCellValue("结束对讲");
            row.createCell(3).setCellValue("备注");
            row.createCell(4).setCellValue("loadingtimeout=" + loadingtimeout);
            row.createCell(5).setCellValue("waitidle=" + waitidle);
            row.createCell(6).setCellValue("playtime=" + playtime);
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
    public void test() throws IOException {
        String shot_name;
        XLog.i("start test");

        mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W");
        mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/item_single_camera_img_live")), 60000);
        while (true) {
            testCount++;
            XLog.i(String.format("第【 %d 】次测试", testCount));
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);

            while (!mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/item_single_camera_img_live")).exists()) {
                if (!mDevice.getCurrentPackageName().equals("com.oceanwing.battery.cam")) {
                    mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W");
                    mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/item_single_camera_img_live")), 60000);
                } else {
                    mDevice.pressBack();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mDevice.findObject(By.res("com.oceanwing.battery.cam:id/item_single_camera_img_live")).click();
            flag = waitPlay(loadingtimeout * 1000);

            if (flag) { //成功
                XLog.i("出流成功");
                row.createCell(1).setCellValue(DateUtils.getSystemTime());
                try {
                    Thread.sleep(playtime * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                row.createCell(2).setCellValue(DateUtils.getSystemTime());
                if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/vb_talkback")).exists()) {
                    try {
                        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/vb_talkback")).isEnabled()) {
                            mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/vb_talkback")).click();
                        }
                    } catch (UiObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/iv_voice")).exists()) {
                    try {
                        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/iv_voice")).isEnabled()) {
                            mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/iv_voice")).click();
                        }
                    } catch (UiObjectNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } else { //超时或者失败
                XLog.i("出流超时或者失败");
                shot_name = floderPath + testCount + "_" + DateUtils.getDateTime() + ".png";
                mDevice.takeScreenshot(new File(shot_name));
                row.createCell(3).setCellValue("出流失败");
                if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/camera_preview_txt_remind_title")).exists()) {
                    info = mDevice.findObject(By.res("com.oceanwing.battery.cam:id/camera_preview_txt_remind_title")).getText();
                    row.createCell(4).setCellValue(info);
                }
            }
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");

            if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/iv_close")).exists()) {
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/iv_close")).click();
            } else {
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/activity_camera_preview_back")).click();
            }
            try {
                Thread.sleep(waitidle * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public boolean waitPlay(int timeout) {
        /**
         * return true表示成功
         * return false表示超时或者失败
         */
        Date start = new Date();
        while (true) {
            if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/vb_talkback")).exists()) {
                try {
                    if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/vb_talkback")).isEnabled()) {
                        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/vb_talkback")).click();
                        return true;
                    }
                } catch (UiObjectNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/iv_voice")).exists()) {
                try {
                    if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/iv_voice")).isEnabled()) {
                        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/iv_voice")).click();
                        return true;
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
            Date end = new Date();
            long time = end.getTime() - start.getTime();
            if (time > timeout) {
                return false;
            }
        }
    }
}
