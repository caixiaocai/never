package com.anker.autotest.eufysecurity;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
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
 * smartlock的ota升级
 */
@RunWith(AndroidJUnit4.class)
public class SmartLockOta2Test {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0;
    private int checkupdatetimeout = 30;
    private int updatetimeout = 300;
    private boolean homebase = false;
    private int reconnect = 60;
    private boolean restart = false;

    @Before
    public void setUp() throws Exception {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);

        Bundle extras = InstrumentationRegistry.getArguments();
        if (extras.containsKey("checkupdatetimeout")) {
            checkupdatetimeout = Integer.parseInt(extras.getString("checkupdatetimeout"));
        }
        if (extras.containsKey("updatetimeout")) {
            updatetimeout = Integer.parseInt(extras.getString("updatetimeout"));
        }
        if (extras.containsKey("restart")) {
            restart = Boolean.parseBoolean(extras.getString("restart"));
        }
        if (extras.containsKey("homebase")) {
            homebase = Boolean.parseBoolean(extras.getString("homebase"));
        }
        if (extras.containsKey("reconnect")) {
            reconnect = Integer.parseInt(extras.getString("reconnect"));
        }
        XLog.i(String.format("updatetimeout: %d", updatetimeout));
        XLog.i(String.format("restart: %s", restart));
        XLog.i(String.format("homebase: %s", homebase));
        XLog.i(String.format("reconnect: %s", reconnect));

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("检测新版本");
            row.createCell(2).setCellValue("开始时间");
            row.createCell(3).setCellValue("结束时间");
            row.createCell(4).setCellValue("结果");
            row.createCell(5).setCellValue("downloadTime");
            row.createCell(6).setCellValue("ota time");
            row.createCell(7).setCellValue("all time");
            row.createCell(8).setCellValue("升级异常弹窗内容");
            row.createCell(9).setCellValue("回连");
            row.createCell(10).setCellValue("回连重试次数");
            row.createCell(11).setCellValue("scanTime");
            row.createCell(12).setCellValue("connectTime");
            row.createCell(13).setCellValue("verifyTime");
            row.createCell(14).setCellValue("rssi");
            row.createCell(15).setCellValue("alltime");
            row.createCell(16).setCellValue("回连异常弹窗内容");
            row.createCell(17).setCellValue("checkupdatetimeout=" + checkupdatetimeout);
            row.createCell(18).setCellValue("updatetimeout=" + updatetimeout);
            row.createCell(19).setCellValue("restart=" + restart);
            row.createCell(20).setCellValue("homebase=" + homebase);
            row.createCell(21).setCellValue("reconnect=" + reconnect);
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
    public void test() throws IOException, UiObjectNotFoundException, InterruptedException {
        long time_start, time_end;
        UiObject2 waitReturn;
        String text, shot_name;
        String[] del;
        boolean timeoutFlag;
        int flag, retry;
        int x = mDevice.getDisplayWidth()/2;
        int height = mDevice.getDisplayHeight();
        XLog.i("start test");

        mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");

        while (true) {
            testCount++;
            timeoutFlag = false;
            XLog.i(String.format("第【 %d 】次测试", testCount));
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);
            XLog.i("点击设备");
            mDevice.swipe(x, height/4, x, height/10 * 9, 100);
            Thread.sleep(5000);
            while(true) {
                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/itemSmartLockRoot")), 10000);
                mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/itemSmartLockRoot")).click();
                for(int tt=0; tt<3; tt++){
                    Thread.sleep(1000);
                    if(mDevice.hasObject(By.res("com.oceanwing.battery.cam:id/itemSmartLockRoot"))) {
                        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/itemSmartLockRoot")).click();
                    }else{
                        break;
                    }
                }
                mDevice.wait(Until.gone(By.text("Connecting")), 30000);
                if (mDevice.wait(Until.hasObject(By.text("Drag to lock")), 5000)) {
                    break;
                }
                if (mDevice.wait(Until.hasObject(By.text("Drag to unlock")), 5000)) {
                    break;
                }
                if (mDevice.hasObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure"))){
                    mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure")).click();
                    break;
                }else if(mDevice.hasObject(By.text("OK"))){
                    mDevice.findObject(By.text("OK")).click();
                    break;
                }else{
                    if(homebase){
                        try {
                            Thread.sleep(reconnect*1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/img_back")).clickAndWaitForNewWindow();
                }
            }
            XLog.i("点击设置");
            waitReturn = mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/imgSetting")), 5000);
            waitReturn.click();
            for(int i=0;i<3;i++) {
                if (!mDevice.wait(Until.hasObject(By.text("About Device")), 3000)) {
                    XLog.i("滑动");
                    mDevice.swipe(x, (int) (height * 0.8), x, height / 5, 100);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else{
                    break;
                }
            }
            XLog.i("点击About Device");
            mDevice.findObject(new UiSelector().text("About Device")).clickAndWaitForNewWindow();
            XLog.i("点击检查升级");
            boolean fff = mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/tv_check_firmware_update")), 10000);
            if (fff) {
                mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/tv_check_firmware_update")).clickAndWaitForNewWindow();
                waitReturn = mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/esl_view_update_firmware_need_update_tv_upgrade")), checkupdatetimeout * 1000);
                if (waitReturn != null) {
                    XLog.i("检测新版本成功");
                    waitReturn.click();
                    row.createCell(1).setCellValue("pass");
                    time_start = System.currentTimeMillis();
                    row.createCell(2).setCellValue(DateUtils.getSystemTime(time_start));
                    XLog.i("等待升级");
                    while (true) {
                        if (System.currentTimeMillis() - time_start > updatetimeout * 1000) {
                            flag = -2; // 超时
                            break;
                        }
                        if (mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure")), 30000)) {
                            shot_name = floderPath + testCount + "_弹窗.png";
                            XLog.i(String.format("弹窗，截图 %s", shot_name));
                            flag = 1; // pass
                            break;
                        }
                    }
                    row.createCell(3).setCellValue(DateUtils.getSystemTime());
                    if (flag == 1) {
                        XLog.i("弹窗");
                        text = mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_custom_content")).getText();
                        XLog.i(text);
                        XLog.i("点击弹窗上的按钮");
                        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_custom_sure")).click();
                        if (mDevice.wait(Until.hasObject(By.text("Done")), 5000)) {
                            mDevice.findObject(By.text("Done")).click();
                        } else if (mDevice.hasObject(By.res("com.oceanwing.battery.cam:id/esl_view_update_firmware_update_success_tv_continue"))) {
                            mDevice.findObject(By.res("com.oceanwing.battery.cam:id/esl_view_update_firmware_update_success_tv_continue")).click();
                        }
                        if (text.contains("ota time")) {
                            XLog.i("解析弹窗内容");
                            del = text.split("\n");
                            //downloadTime：13
                            //ota time：30859
                            //all time：30872
                            row.createCell(4).setCellValue("pass");
                            for (String str : del) {
                                if ("".equals(str)) {
                                } else if (str.contains("downloadTime：")) {
                                    row.createCell(5).setCellValue(Integer.parseInt(str.split("：")[1]));
                                } else if (str.contains("ota time：")) {
                                    row.createCell(6).setCellValue(Integer.parseInt(str.split("：")[1]));
                                } else if (str.contains("all time：")) {
                                    row.createCell(7).setCellValue(Integer.parseInt(str.split("：")[1]));
                                }
                            }
                            XLog.i("返回到连接界面");
                            for (int jjj = 0; jjj < 5; jjj++) {
                                if (mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/llDownEventList")), 1500)) {
                                    break;
                                } else {
                                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/img_back")).clickAndWaitForNewWindow();
                                }
                            }
                            long start = new Date().getTime();
                            long end;
                            XLog.i("等待回连");
                            retry = 0;
                            while (true) {
                                end = new Date().getTime();
                                if (end - start > reconnect * 1000) {
                                    if (mDevice.hasObject(By.text("Drag to lock")) || mDevice.hasObject(By.text("Drag to unlock"))) {
                                        XLog.i("回连成功");
                                        XLog.i("没有弹窗");
                                        row.createCell(9).setCellValue("pass");
                                        row.createCell(10).setCellValue(retry);
                                        row.createCell(16).setCellValue("没有弹窗");
                                        break;
                                    } else {
                                        XLog.i("TIMEOUT");
                                        row.createCell(9).setCellValue("TIMEOUT");
                                        row.createCell(10).setCellValue(retry);
                                        break;
                                    }
                                } else if (mDevice.wait(Until.hasObject(By.text("OK")), 3000)) {
                                    if (mDevice.hasObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_content"))) {
                                        text = mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_custom_content")).getText();
                                        if (text.contains("alltime")) {
                                            XLog.i("回连成功");
                                            XLog.i("解析弹窗内容");
                                            row.createCell(9).setCellValue("pass");
                                            row.createCell(10).setCellValue(retry);
                                            del = text.split("\n");
                                            //scanTime：17651
                                            //connectTime：5696
                                            //verifyTime：1633
                                            //rssi：-72
                                            //alltime：24980
                                            for (String str : del) {
                                                if ("".equals(str)) {
                                                } else if (str.contains("scanTime：")) {
                                                    row.createCell(11).setCellValue(Integer.parseInt(str.split("：")[1]));
                                                } else if (str.contains("connectTime：")) {
                                                    row.createCell(12).setCellValue(Integer.parseInt(str.split("：")[1]));
                                                } else if (str.contains("verifyTime：")) {
                                                    row.createCell(13).setCellValue(Integer.parseInt(str.split("：")[1]));
                                                } else if (str.contains("rssi：")) {
                                                    row.createCell(14).setCellValue(Integer.parseInt(str.split("：")[1]));
                                                } else if (str.contains("alltime：")) {
                                                    row.createCell(15).setCellValue(Integer.parseInt(str.split("：")[1]));
                                                }
                                            }
                                        } else {
                                            XLog.i("回连异常");
                                            XLog.i("无法解析弹窗内容");
                                            row.createCell(9).setCellValue("ERROR");
                                            row.createCell(10).setCellValue(retry);
                                            row.createCell(16).setCellValue(text);
                                        }
                                    }
                                    mDevice.findObject(By.text("OK")).click();
                                    break;
                                } else if (mDevice.hasObject(By.text("Retry")) && retry < 2) {
                                    retry++;
                                    XLog.i(String.format("Retry: %d", retry));
                                    start = new Date().getTime();
                                    mDevice.findObject(By.text("Retry")).click();
                                }
                            }
                        } else {
                            shot_name = floderPath + testCount + "_无法解析弹窗内容.png";
                            XLog.i(String.format("无法解析弹窗内容，截图 %s", shot_name));
                            mDevice.takeScreenshot(new File(shot_name));
                            row.createCell(4).setCellValue("ERROR");
                            row.createCell(8).setCellValue(text);
                        }
                    } else {
                        shot_name = floderPath + testCount + "_升级超时.png";
                        XLog.i(String.format("升级超时，截图 %s", shot_name));
                        mDevice.takeScreenshot(new File(shot_name));
                        row.createCell(4).setCellValue("TIMEOUT");
                        if (mDevice.hasObject(By.res("com.oceanwing.battery.cam:id/esl_view_update_firmware_update_fail_tv_try"))) {
                            mDevice.findObject(By.res("com.oceanwing.battery.cam:id/esl_view_update_firmware_update_fail_tv_try")).click();
                        } else if (mDevice.hasObject(By.text("Done"))) {
                            mDevice.findObject(By.text("Done")).click();
                        }
                        XLog.i("重启app");
                        timeoutFlag = true;
                        mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");
                    }
                } else {
                    XLog.i("检测新版本失败");
                    row.createCell(1).setCellValue("fail");
                }
            }else{
                XLog.i("没有检测新版本的按钮");
                row.createCell(1).setCellValue("没有检测新版本的按钮");
            }
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");

            if(!timeoutFlag){
                if(restart) {
                    mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");
                }else{
                    for (int i = 0; i < 5; i++) {
                        if (mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/img_back")), 3000)) {
                            mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/img_back")).clickAndWaitForNewWindow();
                        } else {
                            break;
                        }
                    }
                }
            }
            XLog.i("结束");
        }
    }
}
