package com.anker.autotest.nebula;

import android.os.RemoteException;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
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
 * M2C项目ota升级
 */
@RunWith(AndroidJUnit4.class)
public class M2COTATest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;

    @Before
    public void setUp() throws Exception {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");

        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + "M2COTATest.xls";
        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("时间");
            row.createCell(2).setCellValue("检测升级包");
            row.createCell(3).setCellValue("下载");
            row.createCell(4).setCellValue("点击升级时间点");
            row.createCell(5).setCellValue("本次升级前是否已重启");
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
        boolean flag = false;
        UiObject recheck, download, upgrade;
        UiObject2 obj;
        XLog.i("Start test");

        // 打开setting
        try {
            if (!mDevice.isScreenOn()) {
                mDevice.wakeUp();//唤醒屏幕
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();
        while (true) {
            mDevice.pressHome();
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            int rowNum = sheet.getLastRowNum();
            HSSFRow row = sheet.createRow(rowNum + 1);
            row.createCell(0).setCellValue(rowNum + 1);
            row.createCell(1).setCellValue(DateUtils.getSystemTime());
            mDevice.executeShellCommand("am start -n com.oceanwing.ota/.ui.UpgradeActivity -W");
            mDevice.waitForWindowUpdate("com.oceanwing.ota", 5 * 2000);//等待app
            for (int i = 0; i < 5; i++) {
                if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.ota:id/upgradeLayout")).exists()) {  // 点击检查更新
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.ota:id/upgradeLayout")).click();
                    break;
                } else {
                    Thread.sleep(1000);
                }
            }
            Thread.sleep(3000);
            XLog.i("等待检测升级");
            while (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.ota:id/progress_bar")).exists()) {
                Thread.sleep(1000);
            }

            while (true) {
                if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.ota:id/currentVersion").text("当前已经是最新版本")).exists() ||
                        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.ota:id/currentVersion").text("Current version is the latest version")).exists()) {  //当前已是最新版本
                    XLog.i("检测新版本失败");
                    row.createCell(2).setCellValue("fail");
                    break;
                }

                if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.ota:id/tryAgain")).exists()) {  // 重试
                    XLog.i("重试");
                    recheck = mDevice.findObject(new UiSelector().resourceId("com.oceanwing.ota:id/tryAgain"));
                    if (recheck.isEnabled()) {
                        recheck.click();
                        Thread.sleep(5000);
                    } else {
                        Thread.sleep(1000);
                    }
                } else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.ota:id/download")).exists()) {  //点击下载
                    XLog.i("点击下载");
                    row.createCell(2).setCellValue("pass");
                    download = mDevice.findObject(new UiSelector().resourceId("com.oceanwing.ota:id/download"));
                    if (download.isEnabled()) {
                        download.click();
                        Thread.sleep(5000);
                    } else {
                        Thread.sleep(1000);
                    }
                } else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.ota:id/reboot")).exists()) {  // 升级
                    XLog.i("升级");
                    row.createCell(3).setCellValue("pass");
                    upgrade = mDevice.findObject(new UiSelector().resourceId("com.oceanwing.ota:id/reboot"));
                    if (upgrade.isEnabled()) {
                        break;
                    } else {
                        Thread.sleep(1000);
                    }
                } else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.ota:id/progress_bar")).exists()) {  // 下载进度条
                    Thread.sleep(5000);
                } else {
                    break;
                }
            }
            if (flag) {
                row.createCell(5).setCellValue("false");
            } else {
                row.createCell(5).setCellValue("true");
            }
            flag = true;
            if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.ota:id/reboot")).exists()
                    && mDevice.findObject(new UiSelector().resourceId("com.oceanwing.ota:id/reboot")).isEnabled()) {
                row.createCell(4).setCellValue(DateUtils.getSystemTime());
                FileOutputStream os = new FileOutputStream(filePath);
                wb.write(os);
                os.close();
                wb.close();
                XLog.i("结果写入完成");
                mDevice.findObject(new UiSelector().resourceId("com.oceanwing.ota:id/reboot")).click();
                Thread.sleep(600000);//等待三分钟
            } else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.ota:id/currentVersion").text("当前已经是最新版本")).exists() ||
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.ota:id/currentVersion").text("Current version is the latest version")).exists()) {
                row.createCell(6).setCellValue("当前已经是最新版本");
                FileOutputStream os = new FileOutputStream(filePath);
                wb.write(os);
                os.close();
                wb.close();
                XLog.i("结果写入完成");
                for (int i = 0; i < 5; i++) {
                    mDevice.pressBack();
                    Thread.sleep(500);
                }
            } else {
                row.createCell(6).setCellValue("界面异常，重新执行升级流程");
                FileOutputStream os = new FileOutputStream(filePath);
                wb.write(os);
                os.close();
                wb.close();
                XLog.i("结果写入完成");
                for (int i = 0; i < 5; i++) {
                    mDevice.pressBack();
                    Thread.sleep(500);
                }
            }
        }
    }
}
