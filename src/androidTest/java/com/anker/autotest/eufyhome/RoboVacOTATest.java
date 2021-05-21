package com.anker.autotest.eufyhome;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

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
import java.util.Date;

/***
 * 扫地机ota升级
 */
@RunWith(AndroidJUnit4.class)
public class RoboVacOTATest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0;

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
            row.createCell(0).setCellValue("测试次数");
            row.createCell(1).setCellValue("开始时间");
            row.createCell(2).setCellValue("测试结果");
            row.createCell(3).setCellValue("结束时间");
            row.createCell(4).setCellValue("OTA耗时(ms)");
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
    public void test() throws Exception {
        XLog.i("Start test");

        long beforeTime = 0;
        long endTime = 0;

        while (true) {
            testCount++;
            XLog.i(String.format("第【 %d 】次测试", testCount));

            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);

            try {
                int flag = waitForUpdate(600);
                if (flag == 0) {
                    pressUpdate();
                    beforeTime = System.currentTimeMillis();
                    String startTime = DateUtils.getSystemTime();
                    row.createCell(1).setCellValue(startTime);
                    XLog.i("点击Update升级");

                    int result = waitForUiObject2("Try Again");

                    if (result == 0) {
                        XLog.i("升级成功");
                        row.createCell(2).setCellValue("pass");
                        endTime = System.currentTimeMillis();
                        long testTime = endTime - beforeTime;
                        String stopTime = DateUtils.getSystemTime();
                        row.createCell(3).setCellValue(stopTime);
                        row.createCell(4).setCellValue(testTime);

                    }
                    if (result == 1) {
                        XLog.i("下载失败");
                        row.createCell(2).setCellValue("fail");
                        String stopTime = DateUtils.getSystemTime();
                        row.createCell(3).setCellValue(stopTime);
                        row.createCell(4).setCellValue("下载失败");
                        Thread.sleep(5000);
                    }
                    if (result == 2) {
                        XLog.i("升级失败");
                        row.createCell(2).setCellValue("fail");
                        String stopTime = DateUtils.getSystemTime();
                        row.createCell(3).setCellValue(stopTime);
                        row.createCell(4).setCellValue("升级失败");
                        Thread.sleep(180000);
                    }
                }
                if (flag == 1) {
                    XLog.i("下载失败");
                    row.createCell(4).setCellValue("下载失败");
                    Thread.sleep(5000);
                }
                if (flag == 2) {
                    XLog.i("升级失败");
                    row.createCell(4).setCellValue("升级失败");
                    Thread.sleep(60000);
                } else {
                    XLog.i("APP界面异常");
                    row.createCell(4).setCellValue("APP界面异常，未出现Update按键");
                    Thread.sleep(5000);
                }
            } catch (Exception e) {
                XLog.e(e.toString());

            }
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");
        }
    }

    private void pressUpdate() {
        UiObject appViews = mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/updateStatus"));
        try {
            if (appViews.getText().equals("Update")) {
                appViews.click();
            } else if (appViews.getText().equals("Failed")) {
                UiObject back = mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/back"));
                back.clickAndWaitForNewWindow();
                UiObject upgrade = mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/updateFrame"));
                upgrade.clickAndWaitForNewWindow();
            }
        } catch (UiObjectNotFoundException e) {
            XLog.e(e.toString());
            try {
                appViews.clickAndWaitForNewWindow();
            } catch (UiObjectNotFoundException e1) {
                XLog.e(e1.toString());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e2) {
                    XLog.e(e2.toString());
                }
                pressUpdate();
            }
        }
    }

    private int waitForUpdate(int timeout) throws UiObjectNotFoundException {
        Date start = new Date();
        while (true) {
            UiObject it2 = mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/cancel").text("Not Now"));
//            UiObject it3 = mDevice.findObject(new UiSelector ().resourceId ("com.eufylife.smarthome:id/cancel").text ("Later"));
            UiObject text2 = mDevice.findObject(new UiSelector().text("Failed to download the new version. Please check your network connection and try again."));
            UiObject text3 = mDevice.findObject(new UiSelector().text("Unable to find your device. Please make sure your device is turned on and connected to the same Wi-Fi network that your phone is connected to."));

            try {
                if (it2.exists() && text2.exists()) {
                    it2.clickAndWaitForNewWindow(2400);
                    XLog.i("点击Not now并等待5S");
                    Thread.sleep(1000);
                    return 1;
                }
                if (it2.exists() && text3.exists()) {
                    it2.clickAndWaitForNewWindow(2400);
                    XLog.i("点击Not now并等待5S");
                    Thread.sleep(1000);
                    return 2;
                }
            } catch (InterruptedException | UiObjectNotFoundException e) {
                XLog.e(e.toString());
                e.printStackTrace();
            }
            UiObject retry = mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/bt_load_again"));
            if (retry.exists()) {
                retry.clickAndWaitForNewWindow();
            }
            UiObject failed = mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/updateStatus").text("Failed"));
            if (failed.exists()) {
                UiObject back = mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/back"));
                back.clickAndWaitForNewWindow();
                UiObject upgrade = mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/updateFrame"));
                upgrade.clickAndWaitForNewWindow();
            }
            UiObject it = mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/updateStatus").text("Update"));
            if (it.exists()) {
                return 0;
            }

            Date end = new Date();
            long time = end.getTime() - start.getTime();
            if (time > timeout * 1000) {
                return 3;
            }
        }
    }

    public int waitForUiObject2(String text) throws UiObjectNotFoundException {//等待对象出现
        UiObject text2 = mDevice.findObject(new UiSelector().text("Failed to download the new version. Please check your network connection and try again."));
        UiObject text3 = mDevice.findObject(new UiSelector().text("Unable to find your device. Please make sure your device is turned on and connected to the same Wi-Fi network that your phone is connected to."));

        while (true) {
            UiObject it2 = mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/cancel").text("Not Now"));
            if (it2.exists()) {
                it2.clickAndWaitForNewWindow(2400);
                if (text2.exists()) {
                    XLog.i("下载失败，点击Not now");
                    return 1;//下载失败
                }
                if (text3.exists()) {
                    XLog.i("升级失败，点击Not now");
                    return 2;//升级失败
                }
            }
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            UiObject it = mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/cancel"));
//            UiObject it = mDevice.findObject(new UiSelector ().resourceId("com.eufylife.smarthome:id/ok").text (text));
            if (it.exists()) {
                it.clickAndWaitForNewWindow();
                return 0;
            }
            UiObject failed = mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/updateStatus").text("Failed"));
            if (failed.exists()) {
                XLog.i("升级失败，显示Failed");
                return 1;
            }
        }
    }
}
