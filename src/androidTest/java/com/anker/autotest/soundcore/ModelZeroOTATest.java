package com.anker.autotest.soundcore;

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

/***
 * Model Zero ota测试
 */
@RunWith(AndroidJUnit4.class)
public class ModelZeroOTATest {
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
    public void otaTest() throws Exception {
        boolean complete;
        XLog.i("start test");

        while (true) {
            testCount++;
            XLog.i(String.format("第【 %d 】次测试", testCount));
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);

            try {
                clickHandle();
                clickFirmwareUpdate(300);
                row.createCell(1).setCellValue(DateUtils.getSystemTime());
                clickInstall(300);
                complete = clickComplete(300);
                if (!complete) {
                    XLog.i("升级失败");
                    row.createCell(3).setCellValue("升级失败");
                    row.createCell(2).setCellValue(DateUtils.getSystemTime());
                } else {
                    XLog.i("升级成功");
                    row.createCell(3).setCellValue("升级成功");
                    row.createCell(2).setCellValue(DateUtils.getSystemTime());
                    XLog.i("点击Complete");
                }
            } catch (Exception e) {
                XLog.e(e.toString());
            } finally {
                FileOutputStream os = new FileOutputStream(filePath);
                wb.write(os);
                os.close();
                wb.close();
                XLog.i("结果写入完成");
            }
        }
    }

    private void clickHandle() {
        while (true) {
            if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/st_negative")).exists()) {
                try {
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/st_negative")).clickAndWaitForNewWindow();
                } catch (UiObjectNotFoundException e) {
                    XLog.e(e.toString());
                }
            }
            if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/handle")).exists()) {
                try {
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/handle")).clickAndWaitForNewWindow();
                    return;
                } catch (UiObjectNotFoundException e) {
                    XLog.e(e.toString());
                }
            } else {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    XLog.e(e.toString());
                }
            }
        }
    }

    private void clickFirmwareUpdate(int timeout) {
        mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/linear_firmware_update")), timeout * 1000);
        try {
            mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/linear_firmware_update")).clickAndWaitForNewWindow();
        } catch (UiObjectNotFoundException e) {
            XLog.e(e.toString());
        }
    }

    private void waitForProgress() {
        while (true) {
            if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/progress")).exists()) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    XLog.e(e.toString());
                }
            } else {
                return;
            }
        }
    }

    private void clickInstall(int timeout) {
        while (!mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/frame_install_complete")).exists()) {
            try {
                mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/sb_download")), timeout * 1000);
                UiObject obj = mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download"));
                while (!obj.isEnabled()) {
                    Thread.sleep(200);
                }
                obj.clickAndWaitForNewWindow();
                waitForProgress();
            } catch (Exception e) {
                XLog.e(e.toString());
            }
        }
    }

    private boolean clickComplete(int timeout) {
        mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/frame_install_complete")), timeout * 1000);
        try {
            UiObject obj = mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/frame_install_complete"));
            while (!obj.isEnabled()) {
                Thread.sleep(200);
            }
            obj.clickAndWaitForNewWindow();
            return true;
        } catch (Exception e) {
            XLog.e(e.toString());
            return false;
        }
    }
}

