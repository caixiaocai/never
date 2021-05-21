package com.anker.autotest.soundcore;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
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
 * PowerConf ota测试（新首页）
 */
@RunWith(AndroidJUnit4.class)
public class PowerConfOTATest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0;
    private int updatetimeout = 1800;
    private int restarttimeout = 30;
    private int idletime = 5;
    private boolean restart=false;

    @Before
    public void setUp() throws Exception {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");

        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);

        Bundle extras = InstrumentationRegistry.getArguments();
        if (extras.containsKey("updatetimeout")) {
            updatetimeout = Integer.parseInt(extras.getString("updatetimeout"));
        }
        if (extras.containsKey("restarttimeout")) {
            restarttimeout = Integer.parseInt(extras.getString("restarttimeout"));
        }
        if (extras.containsKey("idletime")) {
            idletime = Integer.parseInt(extras.getString("idletime"));
        }
        if (extras.containsKey("restart")) {
            restart = Boolean.parseBoolean(extras.getString("restart"));
        }
        XLog.i(String.format("updatetimeout: %s", updatetimeout));
        XLog.i(String.format("restarttimeout: %s", restarttimeout));
        XLog.i(String.format("idletime: %s", idletime));
        XLog.i(String.format("restart: %s", restart));

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("测试次数");
            row.createCell(1).setCellValue("检测新版本");
            row.createCell(2).setCellValue("开始时间");
            row.createCell(3).setCellValue("下载");
            row.createCell(4).setCellValue("下载结束时间");
            row.createCell(5).setCellValue("升级");
            row.createCell(6).setCellValue("升级结束时间");
            row.createCell(7).setCellValue("回连");
            row.createCell(8).setCellValue("回连结束时间");
            row.createCell(9).setCellValue("updatetimeout=" + updatetimeout);
            row.createCell(10).setCellValue("restarttimeout=" + restarttimeout);
            row.createCell(11).setCellValue("idletime=" + idletime);
            row.createCell(12).setCellValue("restart= "+ restart);
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
        }
    }

    @After
    public void tearDown() {
        String screenshotFile = floderPath + "fail_" + DateUtils.getDateTime()+ ".png";
        mDevice.takeScreenshot(new File(screenshotFile));
        XLog.i(screenshotFile);
    }

    @Test
    public void otaTest() throws Exception {
        XLog.i("start test");
        int x = mDevice.getDisplayWidth()/2;
        int y_start = mDevice.getDisplayHeight()/5*4;
        int y_end = mDevice.getDisplayHeight()/2;

        mDevice.executeShellCommand("am start -n com.oceanwing.soundcore/.activity.SelectCategoryNewActivity -W");
        mDevice.executeShellCommand("am start -n com.oceanwing.soundcore/.activity.WelcomeActivity -W");

        while (true) {
            testCount++;
            XLog.i(String.format("第【 %d 】次测试", testCount));
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);

            try {
                if(mDevice.wait(Until.hasObject(By.res("com.oceanwing.soundcore:id/navigation_devices")), 5000)){
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/navigation_devices")).click();
                }

                if (mDevice.wait(Until.hasObject(By.text("Connect")), 5000)) {
                    mDevice.findObject(new UiSelector().text("Connect")).click();
                }
                if (mDevice.wait(Until.hasObject(By.res("com.oceanwing.soundcore:id/st_positive").text("More")), 10000)) {
                    Thread.sleep(2000);
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/st_positive").text("More")).clickAndWaitForNewWindow();
                } else {
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/linear_right3")).clickAndWaitForNewWindow();
                    mDevice.swipe(x, y_start, x, y_end, 500);
                    mDevice.findObject(new UiSelector().text("Update Firmware")).clickAndWaitForNewWindow();
                }
                if (mDevice.wait(Until.hasObject(By.res("com.oceanwing.soundcore:id/sb_download")), 30000)) {//有新版本
                    XLog.i("有新版本");
                    if (mDevice.hasObject(By.res("com.oceanwing.soundcore:id/sb_download").text("Download"))) {
                        row.createCell(1).setCellValue("pass");
                        row.createCell(2).setCellValue(DateUtils.getSystemTime());
                        mDevice.findObject(By.res("com.oceanwing.soundcore:id/sb_download").text("Download")).click();
                    }
                    while(true) {
                        if (mDevice.hasObject(By.res("com.oceanwing.soundcore:id/progress"))) {
                            mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/sb_download").text("Install")), 5000);
                        }else if(mDevice.hasObject(By.res("com.oceanwing.soundcore:id/sb_download").text("Download"))) {
                            mDevice.findObject(By.res("com.oceanwing.soundcore:id/sb_download").text("Download")).click();
                            mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/sb_download").text("Install")), 5000);
                        } else {
                            break;
                        }
                    }
                    if (mDevice.hasObject(By.res("com.oceanwing.soundcore:id/sb_download").text("Install"))) {//下载成功
                        XLog.i("下载成功");
                        row.createCell(3).setCellValue("pass");
                        row.createCell(4).setCellValue(DateUtils.getSystemTime());
                        mDevice.findObject(By.res("com.oceanwing.soundcore:id/sb_download").text("Install")).click();
                        for (int i = 0; i < updatetimeout; i++) {
                            if (mDevice.hasObject(By.res("com.oceanwing.soundcore:id/sb_download").text("Install"))) {
                                mDevice.findObject(By.res("com.oceanwing.soundcore:id/sb_download").text("Install")).click();
                            } else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/progress")).exists()|| mDevice.hasObject(By.textStartsWith("Installing..."))) {
                                Thread.sleep(1000);
                            } else {
                                break;
                            }
                        }
                        if (mDevice.wait(Until.hasObject(By.res("com.oceanwing.soundcore:id/frame_install_complete")), 15000)) {//升级成功
                            XLog.i("升级成功");
                            row.createCell(5).setCellValue("pass");
                            row.createCell(6).setCellValue(DateUtils.getSystemTime());
                            UiObject2 done = mDevice.findObject(By.res("com.oceanwing.soundcore:id/frame_install_complete"));
                            done.wait(Until.clickable(true), restarttimeout*1000);
                            mDevice.findObject(By.res("com.oceanwing.soundcore:id/frame_install_complete")).click();
                            if(mDevice.wait(Until.hasObject(By.res("com.oceanwing.soundcore:id/linear_right3")), 30000) || mDevice.hasObject(By.res("com.oceanwing.soundcore:id/st_positive").text("More"))){
                                XLog.i("回连成功");
                                row.createCell(7).setCellValue("pass");
                                row.createCell(8).setCellValue(DateUtils.getSystemTime());
                            }else{
                                XLog.i("回连异常");
                                row.createCell(7).setCellValue("ERROR");
                                row.createCell(8).setCellValue(DateUtils.getSystemTime());
                                mDevice.takeScreenshot(new File(floderPath + testCount + "_" + DateUtils.getDateTime()+ "_回连异常.png"));
                                while(true){
                                    if(mDevice.hasObject(By.text("Retry"))){
                                        mDevice.findObject(By.text("Retry")).click();
                                    }else if(mDevice.hasObject(By.text("Connect"))){
                                        mDevice.findObject(By.text("Connect")).click();
                                    }
                                    if(mDevice.wait(Until.hasObject(By.res("com.oceanwing.soundcore:id/linear_right3")), 30000) || mDevice.hasObject(By.res("com.oceanwing.soundcore:id/st_positive").text("More"))) {
                                        break;
                                    }
                                }
                            }
                        }else {//install失败
                            XLog.i("升级异常");
                            row.createCell(5).setCellValue("ERROR");
                            row.createCell(6).setCellValue(DateUtils.getSystemTime());
                            mDevice.takeScreenshot(new File(floderPath + testCount + "_" + DateUtils.getDateTime()+ "_升级异常.png"));
                        }
                    } else {//下载失败
                        XLog.i("下载异常");
                        row.createCell(3).setCellValue("ERROR");
                        row.createCell(4).setCellValue(DateUtils.getSystemTime());
                        mDevice.takeScreenshot(new File(floderPath + testCount + "_" + DateUtils.getDateTime()+ "_下载异常.png"));
                    }
                } else {//没有新版本
                    XLog.i("没有新版本");
                    row.createCell(1).setCellValue("fail");
                    mDevice.takeScreenshot(new File(floderPath + testCount + "_" + DateUtils.getDateTime()+ "_没有新版本.png"));
                }
                if(mDevice.hasObject(By.text("Back"))){
                    mDevice.findObject(By.text("Back")).click();
                }
                if(mDevice.wait(Until.hasObject(By.textStartsWith("Firmware Version")), 3000)){
                    mDevice.pressBack();
                }
                Thread.sleep(idletime * 1000);
                if(restart){
                    mDevice.executeShellCommand("am start -n com.oceanwing.soundcore/.activity.SelectCategoryNewActivity -W -S");
                    mDevice.executeShellCommand("am start -n com.oceanwing.soundcore/.activity.WelcomeActivity -W");
//                    mDevice.executeShellCommand("am force-stop com.oceanwing.soundcore");
                }

            } catch (Exception e) {
                XLog.e(e.toString());
                row.createCell(9).setCellValue(e.toString());
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
