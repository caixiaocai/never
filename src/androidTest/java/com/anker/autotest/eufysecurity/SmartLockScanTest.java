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

/***
 * smartlock的扫描二维码专项
 */
@RunWith(AndroidJUnit4.class)
public class SmartLockScanTest {
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
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("开始时间");
            row.createCell(2).setCellValue("结果");
            row.createCell(3).setCellValue("耗时");
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
        String text;
        XLog.i("start test");

        while (true) {
            if (!mDevice.getCurrentPackageName().equals("com.oceanwing.battery.cam")) {
                mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W");
                mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/add_device")), 15000);
            }
            if(mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/add_device")).exists()) {
                XLog.i("点击Add Device");
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/add_device")).click();
                break;
            }else{
                int x = mDevice.getDisplayWidth()/2;
                int y = mDevice.getDisplayHeight();
                mDevice.swipe(x, y /10 * 8, x, y/10, 100);
            }
        }

        while (true) {
            testCount++;
            XLog.i(String.format("第【 %d 】次测试", testCount));
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);
            row.createCell(1).setCellValue(DateUtils.getSystemTime());
            while (true) {
                if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_custom_content")).exists()) {
                    XLog.i("扫描成功");
                    text = mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_content")).getText();
                    XLog.i(text);
                    mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure")).click();
                    row.createCell(2).setCellValue("pass");
                    row.createCell(3).setCellValue(text.split("：")[1]);
                    FileOutputStream os = new FileOutputStream(filePath);
                    wb.write(os);
                    os.close();
                    wb.close();
                    XLog.i("结果写入完成");
                    break;
                }else{
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/img_back")),5000);
            mDevice.findObject(By.res("com.oceanwing.battery.cam:id/img_back")).click();
        }
    }
}
