package com.anker.autotest.eufysecurity;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
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
 * 反复点击restart按钮重启设备
 */
@RunWith(AndroidJUnit4.class)
public class RestartDeviceTest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0;
    private int timeout;

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
        XLog.i(String.format("timeout: %s", timeout));

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);
        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("时间");
            row.createCell(2).setCellValue("结果");
            row.createCell(3).setCellValue("timeout=" + timeout);
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
        boolean failed, restarting;
        String shot_name;

        XLog.i("start test");
        while (true) {
            testCount++;
            XLog.i(String.format("第【 %d 】次测试", testCount));
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);
            time_start = System.currentTimeMillis();
            row.createCell(1).setCellValue(DateUtils.getSystemTime(time_start));
            if (!"com.oceanwing.battery.cam".equals(mDevice.getCurrentPackageName())) {
                mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W");
                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/item_single_camera_cam_setting_icon")), 10000);
                mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/item_single_camera_cam_setting_icon")).clickAndWaitForNewWindow();
            }
            if (mDevice.findObject(new UiSelector().scrollable(true)).exists()) {
                UiScrollable scroll = new UiScrollable(new UiSelector().scrollable(true));
                scroll.setAsVerticalList();
                scroll.scrollToEnd(3);
//                scroll.scrollIntoView(new UiSelector().resourceId("com.oceanwing.battery.cam:id/tv_restart_device"));
            }
            mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/tv_restart_device")).clickAndWaitForNewWindow();
            mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/btn_right")), 1000);
            failed = false;
            restarting = false;
            if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/btn_right").text("Restart")).exists()) {
                mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/btn_right").text("Restart")).click();
                failed = true;
                restarting = true;
            } else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/btn_yes").text("LED is flashing yellow.")).exists()) {
                mDevice.pressBack();
                row.createCell(2).setCellValue("LED is flashing yellow.");
            } else {
                shot_name = floderPath + testCount + "_" + DateUtils.getDateTime() + ".png";
                mDevice.takeScreenshot(new File(shot_name));
                row.createCell(2).setCellValue("请查看截图");
            }

            while (true) {
                if (failed && mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/tv_title").text("Restart Failed")).exists()) {
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/btn_single").text("Got it")).click();
                    row.createCell(2).setCellValue("Restart Failed");
                    failed = false;
                }
                if (restarting && mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/tv_title").text("Restarting...")).exists()) {
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/btn_single").text("Got it")).click();
                    row.createCell(2).setCellValue("Restarting");
                    restarting = false;
                }
                time_end = System.currentTimeMillis();
                if (time_end - time_start > timeout * 1000) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");
        }
    }
}
