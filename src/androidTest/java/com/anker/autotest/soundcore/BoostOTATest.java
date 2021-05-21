package com.anker.autotest.soundcore;

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
 * boost ota测试
 */
@RunWith(AndroidJUnit4.class)
public class BoostOTATest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0;
    private int downloadtimeout = 180;
    private int updatetimeout = 600;
    private int restarttimeout = 60;
    private int idletime = 5;

    @Before
    public void setUp() throws Exception {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");
        XLog.i(this.getClass().getName());
        XLog.i(this.getClass().getName());

        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);

        Bundle extras = InstrumentationRegistry.getArguments();
        if (extras.containsKey("downloadtimeout")) {
            downloadtimeout = Integer.parseInt(extras.getString("downloadtimeout"));
        }
        if (extras.containsKey("updatetimeout")) {
            updatetimeout = Integer.parseInt(extras.getString("updatetimeout"));
        }
        if (extras.containsKey("restarttimeout")) {
            restarttimeout = Integer.parseInt(extras.getString("restarttimeout"));
        }
        if (extras.containsKey("idletime")) {
            idletime = Integer.parseInt(extras.getString("idletime"));
        }
        XLog.i(String.format("downloadtimeout: %s", downloadtimeout));
        XLog.i(String.format("updatetimeout: %s", updatetimeout));
        XLog.i(String.format("restarttimeout: %s", restarttimeout));
        XLog.i(String.format("idletime: %s", idletime));

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("测试次数");
            row.createCell(1).setCellValue("开始时间");
            row.createCell(2).setCellValue("检测新版本");
            row.createCell(3).setCellValue("下载");
            row.createCell(4).setCellValue("升级");
            row.createCell(5).setCellValue("结束时间");
            row.createCell(6).setCellValue("downloadtimeout=" + downloadtimeout);
            row.createCell(7).setCellValue("updatetimeout=" + updatetimeout);
            row.createCell(8).setCellValue("restarttimeout=" + restarttimeout);
            row.createCell(9).setCellValue("idletime=" + idletime);
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
        XLog.i("start test");

        while (true) {
            testCount++;
            XLog.i(String.format("第【 %d 】次测试", testCount));
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);

            try {
                mDevice.executeShellCommand("am start -n com.oceanwing.soundcore/.activity.SelectCategoryNewActivity -W");
                mDevice.executeShellCommand("am start -n com.oceanwing.soundcore/.activity.WelcomeActivity -W");
                if(mDevice.wait(Until.hasObject(By.res("com.oceanwing.soundcore:id/navigation_devices")), 5000)){
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/navigation_devices")).click();
                }

                if(mDevice.wait(Until.hasObject(By.res("com.oceanwing.soundcore:id/btn_connect")), 5000)){
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/btn_connect")).click();
                }
                Thread.sleep(5000);
                mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/st_positive").text("More")), 5000);
                Thread.sleep(2000);
                row.createCell(1).setCellValue(DateUtils.getSystemTime());
                if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/st_positive").text("More")).exists()) {
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/st_positive").text("More")).clickAndWaitForNewWindow();
                } else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/moreBtn")).exists()) {
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/moreBtn")).clickAndWaitForNewWindow();
                    mDevice.findObject(new UiSelector().text("Update Firmware")).clickAndWaitForNewWindow();
                }
                mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/sb_download")), 30000);
                Thread.sleep(1000);
                if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download")).exists()) {//有新版本
                    XLog.i("有新版本");
                    if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download").text("Download")).exists()) {
                        row.createCell(2).setCellValue("pass");
                    }
                    for (int i = 0; i < downloadtimeout; i++) {
                        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download").text("Download")).exists()) {
                            Thread.sleep(1000);
                            XLog.i("点击下载");
                            mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download").text("Download")).click();
                        } else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/progress")).exists()) {
                            Thread.sleep(1000);
                        } else {
                            break;
                        }
                    }
                    if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download").text("Install")).exists()) {//下载成功
                        XLog.i("下载成功");
                        row.createCell(3).setCellValue("pass");
                        Thread.sleep(1000);
                        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download").text("Install")).click();
                        for (int i = 0; i < updatetimeout; i++) {
                            if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download").text("Install")).exists()) {
                                XLog.i("点击升级");
                                mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download").text("Install")).click();
                            } else if (mDevice.findObject(new UiSelector().textStartsWith("Installing...")).exists()) {
                                Thread.sleep(1000);
                            } else {
                                break;
                            }
                        }
                        if (mDevice.findObject(new UiSelector().textStartsWith("Installation Complete")).exists()) {//升级完成
                            XLog.i("升级成功");
                            row.createCell(4).setCellValue("pass");
                            row.createCell(5).setCellValue(DateUtils.getSystemTime());
                            mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/frame_install_complete")), 15000);
                            Thread.sleep(restarttimeout * 1000);
                        } else {//install失败
                            XLog.i("升级失败");
                            row.createCell(4).setCellValue("fail");
                            row.createCell(5).setCellValue(DateUtils.getSystemTime());
                        }

                    } else {//下载失败
                        XLog.i("下载失败");
                        row.createCell(3).setCellValue("fail");
                    }
                } else {//没有新版本
                    XLog.i("没有新版本");
                    row.createCell(2).setCellValue("fail");
                    mDevice.takeScreenshot(new File(floderPath + testCount + "_" + DateUtils.getDateTime() + ".png"));
                }
                XLog.i("杀app");
                mDevice.executeShellCommand("am force-stop com.oceanwing.soundcore");
                Thread.sleep(idletime * 1000);
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
}