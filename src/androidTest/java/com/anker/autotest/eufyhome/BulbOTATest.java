package com.anker.autotest.eufyhome;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
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


/***
 * 球灯泡ota升级
 */
@RunWith(AndroidJUnit4.class)
public class BulbOTATest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0;
    private int timeout = 60;
    private int waitidle = 10;

    @Before
    public void setUp() throws Exception {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");

        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);

        Bundle extras = InstrumentationRegistry.getArguments();
        if (extras.containsKey("timeout")) {
            timeout = Integer.parseInt(extras.getString("timeout"));
        }
        if (extras.containsKey("waitidle")) {
            waitidle = Integer.parseInt(extras.getString("waitidle"));
        }
        XLog.i(String.format("timeout: %s", timeout));
        XLog.i(String.format("waitidle: %s", waitidle));

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);
        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("开始时间");
            row.createCell(2).setCellValue("结束时间");
            row.createCell(3).setCellValue("结果");
            row.createCell(4).setCellValue("OTA耗时(ms)");
            row.createCell(5).setCellValue("timeout=" + timeout);
            row.createCell(6).setCellValue("waitidle=" + waitidle);
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
        long beforeTime = 0;
        long endTime = 0;
        XLog.i("Start test");
        if (!mDevice.getCurrentPackageName().equals("com.eufylife.smarthome")) {
            mDevice.executeShellCommand("am start -n com.eufylife.smarthome/com.oceanwing.eufyhome.main.WelcomeActivity -W");
//            mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/device_name").text("My Smart Bulb")).clickAndWaitForNewWindow();
        }
        if (mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/device_img")).exists()) {
            mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/device_img")).clickAndWaitForNewWindow();
            mDevice.wait(Until.findObject(By.res("com.eufylife.smarthome:id/common_header_end_icon")), 10000);
        }
        if (mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/common_header_end_icon")).exists()) {
            mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/common_header_end_icon")).clickAndWaitForNewWindow();
        }
        while (true) {
            testCount++;
            XLog.i(String.format("第【 %d 】次测试", testCount));

            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);

            mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/update_firmware")).clickAndWaitForNewWindow();
            mDevice.wait(Until.findObject(By.res("com.eufylife.smarthome:id/update_progress")), 10000);
            if (!mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/update_firmware")).exists()) {
                mDevice.pressBack();
                Thread.sleep(15000);
                mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/update_firmware")).clickAndWaitForNewWindow();
                mDevice.wait(Until.findObject(By.res("com.eufylife.smarthome:id/update_progress")), 10000);
            }
            mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/update_progress")).clickAndWaitForNewWindow();
            XLog.i("开始升级");
            beforeTime = System.currentTimeMillis();
            row.createCell(1).setCellValue(DateUtils.getSystemTime(beforeTime));
            while (true) {
                mDevice.wait(Until.findObject(By.res("com.eufylife.smarthome:id/no_update_progress")), 1000 * timeout);
                if (mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/no_update_progress")).exists()) {//升级成功
                    XLog.i("升级成功");
                    row.createCell(3).setCellValue("pass");
                    mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/common_header_start_icon")).clickAndWaitForNewWindow();
                    break;
                } else {//未显示升级成功
                    mDevice.pressBack();
                    if (mDevice.findObject(new UiSelector().text("Update in progress. Do not exit Eufyhome.")).exists()) {//正在升级中
                        mDevice.findObject(new UiSelector().resourceId("com.eufylife.smarthome:id/dialog_btn").text("OK")).click();
                    } else {//升级失败
                        XLog.i("升级失败");
                        row.createCell(3).setCellValue("fail");
                        break;
                    }
                }
            }
            endTime = System.currentTimeMillis();
            row.createCell(2).setCellValue(DateUtils.getSystemTime(endTime));
            row.createCell(4).setCellValue(endTime - beforeTime);
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");
            Thread.sleep(waitidle * 1000);
        }
    }
}