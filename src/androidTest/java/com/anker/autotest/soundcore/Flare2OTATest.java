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
 * Flare2 ota测试（新首页）
 */
@RunWith(AndroidJUnit4.class)
public class Flare2OTATest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0;
    private int downloadtimeout = 180;
    private int updatetimeout = 600;
    private int restarttimeout = 30;
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
            row.createCell(6).setCellValue("完成按钮可点击");
            row.createCell(8).setCellValue("restarttimeout时间内完成按钮完成loading");
            row.createCell(9).setCellValue("downloadtimeout=" + downloadtimeout);
            row.createCell(10).setCellValue("updatetimeout=" + updatetimeout);
            row.createCell(11).setCellValue("restarttimeout=" + restarttimeout);
            row.createCell(12).setCellValue("idletime=" + idletime);
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
        long start;
        boolean updateFlag;
        String shot_name;

        while (true) {
            testCount++;
            updateFlag = true;
            XLog.i(String.format("第【 %d 】次测试", testCount));
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);
            try{
                mDevice.executeShellCommand("am start -n com.oceanwing.soundcore/.activity.SelectCategoryNewActivity -W");
                mDevice.executeShellCommand("am start -n com.oceanwing.soundcore/.activity.WelcomeActivity -W");

                if(mDevice.wait(Until.hasObject(By.res("com.oceanwing.soundcore:id/navigation_devices")), 5000)){
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/navigation_devices")).click();
                }
                if(mDevice.wait(Until.hasObject(By.res("com.oceanwing.soundcore:id/btn_connect")), 5000)){
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/btn_connect")).click();
                }
                mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/st_positive").text("More")), 5000);
                Thread.sleep(2000);
                row.createCell(1).setCellValue(DateUtils.getSystemTime());
                if (mDevice.hasObject(By.res("com.oceanwing.soundcore:id/st_positive").text("More"))) {
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/st_positive").text("More")).clickAndWaitForNewWindow();
                } else{
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/ivAutoTestMore")).clickAndWaitForNewWindow();
                    if(mDevice.wait(Until.hasObject(By.res("com.oceanwing.soundcore:id/llA3117Update").clickable(true)), 2000)) {
                        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/llA3117Update")).clickAndWaitForNewWindow();
                    }else if(mDevice.wait(Until.hasObject(By.res("com.oceanwing.soundcore:id/llUpdateFirmware").clickable(true)), 2000)) {
                        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/llUpdateFirmware")).clickAndWaitForNewWindow();
                    }else if(mDevice.wait(Until.hasObject(By.text("Update Firmware").clickable(true)), 2000)) {
                        mDevice.findObject(new UiSelector().text("Update Firmware")).clickAndWaitForNewWindow();
                    }else{
                        updateFlag = false;
                    }
                }
                if(!updateFlag){
                    shot_name = floderPath + testCount + "_Update Firmware按钮不可点击.png";
                    XLog.i(String.format("Update Firmware按钮不可点击，截图 %s", shot_name));
                    mDevice.takeScreenshot(new File(shot_name));
                    row.createCell(2).setCellValue("Update Firmware按钮不可点击");
                }else {
                    mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/sb_download")), 30000);
                    if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download")).exists()) {//有新版本
                        XLog.i("有新版本");
                        if (mDevice.hasObject(By.res("com.oceanwing.soundcore:id/sb_download").text("Download"))) {
                            row.createCell(2).setCellValue("pass");
                        }
                        for (int i = 0; i < downloadtimeout; i++) {
                            if (mDevice.hasObject(By.res("com.oceanwing.soundcore:id/sb_download").text("Download"))) {
                                Thread.sleep(1000);
                                mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download").text("Download")).click();
//                            mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/sb_download").text("Install")), 60000);
                            } else if (mDevice.hasObject(By.res("com.oceanwing.soundcore:id/progress"))) {
                                Thread.sleep(1000);
//                            mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/sb_download").text("Install")), 60000);
                            } else {
                                break;
                            }
                        }
                        if (mDevice.hasObject(By.res("com.oceanwing.soundcore:id/sb_download").text("Install"))) {//下载成功
                            XLog.i("下载成功");
                            row.createCell(3).setCellValue("pass");
                            mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download").text("Install")).click();
                            start = System.currentTimeMillis();
                            int resultFlag = 0;
                            while (true) {
                                if (System.currentTimeMillis() - start > updatetimeout * 1000) {
                                    resultFlag = 1;
                                    break;
                                } else if (mDevice.hasObject(By.res("com.oceanwing.soundcore:id/sb_download").text("Install"))) {
                                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download").text("Install")).click();
                                } else if (mDevice.hasObject(By.textStartsWith("Installing..."))) {
//                                mDevice.wait(Until.findObject(By.text("Installation Complete")), 60000);
                                    Thread.sleep(1000);
                                } else {
                                    resultFlag = 2;
                                    break;
                                }
                            }
                            if (mDevice.wait(Until.hasObject(By.text("Installation Complete")), 5000)) {//升级完成
                                XLog.i("升级成功");
                                row.createCell(4).setCellValue("pass");
                                row.createCell(5).setCellValue(DateUtils.getSystemTime());
                                mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/frame_install_complete")), 15000);
                                XLog.i(String.format("等待 %d s", restarttimeout));
                                Thread.sleep(restarttimeout * 1000);
                                if(mDevice.hasObject(By.res("com.oceanwing.soundcore:id/frame_install_complete").clickable(true))){
                                    mDevice.findObject(By.res("com.oceanwing.soundcore:id/frame_install_complete")).click();
                                    XLog.i("完成按钮可点击");
                                    row.createCell(6).setCellValue("pass");
                                }else{
                                    XLog.i("完成按钮不可点击");
                                    row.createCell(6).setCellValue("fail");
                                }
                            } else {//install失败
                                XLog.i("升级失败");
                                if(resultFlag == 1){
                                    row.createCell(7).setCellValue("升级超时");
                                    mDevice.takeScreenshot(new File(floderPath + testCount + "_升级超时_" + DateUtils.getDateTime() + ".png"));
                                }else if(resultFlag == 2){
                                    row.createCell(7).setCellValue("界面异常");
                                    mDevice.takeScreenshot(new File(floderPath + testCount + "_界面异常_" + DateUtils.getDateTime() + ".png"));
                                }
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
                        mDevice.takeScreenshot(new File(floderPath + testCount + "_没有新版本.png"));
                    }
                }
                mDevice.executeShellCommand("am force-stop com.oceanwing.soundcore");
                XLog.i(String.format("等待 %d s", idletime));
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