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
import java.util.ArrayList;
import java.util.List;

/***
 * smartlock的ota升级
 */
@RunWith(AndroidJUnit4.class)
public class SmartLockOtaTest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0;
    private int checkupdatetimeout = 30;
    private int updatetimeout = 180;
    private int reconnecttimeout = 60;
    private boolean restart = true;

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
        if (extras.containsKey("reconnecttimeout")) {
            reconnecttimeout = Integer.parseInt(extras.getString("reconnecttimeout"));
        }
        if (extras.containsKey("restart")) {
            restart = Boolean.parseBoolean(extras.getString("restart"));
        }
        XLog.i(String.format("updatetimeout: %d", updatetimeout));
        XLog.i(String.format("reconnecttimeout: %d", reconnecttimeout));
        XLog.i(String.format("restart: %s", restart));

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
            row.createCell(5).setCellValue("下载耗时");
            row.createCell(6).setCellValue("传输耗时");
            row.createCell(7).setCellValue("共耗时");
            row.createCell(8).setCellValue("自动回连");
            row.createCell(9).setCellValue("checkupdatetimeout=" + checkupdatetimeout);
            row.createCell(10).setCellValue("updatetimeout=" + updatetimeout);
            row.createCell(11).setCellValue("reconnecttimeout=" + reconnecttimeout);
            row.createCell(12).setCellValue("restart=" + restart);
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
        String text;
        String[] del;
        int flag;
        int x = mDevice.getDisplayWidth()/2;
        int height = mDevice.getDisplayHeight();
        XLog.i("start test");

        mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");

        while (true) {
            testCount++;
            XLog.i(String.format("第【 %d 】次测试", testCount));
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);
            mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/itemSmartLockRoot")), 15000);
            mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/itemSmartLockRoot")).click();

//            while (true) {
//                if (!mDevice.getCurrentPackageName().equals("com.oceanwing.battery.cam")) {
//                    mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W");
//                    mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/itemSmartLockRoot")), 15000);
//                } else if(mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/itemSmartLockRoot")).exists()){
//                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/itemSmartLockRoot")).click();
//                    break;
//                }
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }

            while (true) {
                if(mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_custom_sure")).exists()){
                    mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure")).click();
                    break;
                } else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/imgFail")).exists()) {
                    mDevice.findObject(By.res("com.oceanwing.battery.cam:id/imgFail")).click();
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
//            mDevice.wait(Until.hasObject(By.text("Locked")), 30000);
            }
            mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/imgSetting")).clickAndWaitForNewWindow();
            mDevice.swipe(x, (int)(height *0.8),x,height/5,100);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/ll_esl_Firmware_Update")).clickAndWaitForNewWindow();
            mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/esl_view_update_firmware_need_update_tv_upgrade")), checkupdatetimeout*1000);
            if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/esl_view_update_firmware_need_update_tv_upgrade")).exists()) {
                XLog.i("检测新版本成功");
                row.createCell(1).setCellValue("pass");
                mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/esl_view_update_firmware_need_update_tv_upgrade")).click();
                time_start = System.currentTimeMillis();
                row.createCell(2).setCellValue(DateUtils.getSystemTime(time_start));
                XLog.i("等待升级");
                while(true) {
//                    mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure")), updatetimeout * 1000);
                    if(System.currentTimeMillis()-time_start > updatetimeout * 1000){
                        flag = -2; // 超时
                        break;
                    }
                    if(mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_custom_sure")).exists()){
                        flag = 1; // pass
                        break;
                    }
                    if(mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/esl_view_update_firmware_update_fail_tv_try")).exists()){
                        flag = 2; // fail
                        break;
                    }
                    if(mDevice.findObject(new UiSelector().text("Updating")).exists()){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
//                    else{
//                        flag = -1; // 界面异常
//                        break;
//                    }
                }

                if (flag==1) {
                    XLog.i("弹窗");
                    text = mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_custom_content")).getText();
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_custom_sure")).click();
                    if(text.contains("耗时")) {
                        del = text.split("\n");
                        //下载耗时：42777
                        //传输耗时：38236
                        //共耗时：81013
                        XLog.i(text);
                        row.createCell(3).setCellValue(DateUtils.getSystemTime());
                        row.createCell(4).setCellValue("pass");
                        for (String str : del) {
                            if ("".equals(str)) {
                            } else if (str.contains("下载耗时：")) {
                                row.createCell(5).setCellValue(Integer.parseInt(str.split("：")[1]));
                            } else if (str.contains("传输耗时：")) {
                                row.createCell(6).setCellValue(Integer.parseInt(str.split("：")[1]));
                            } else if (str.contains("共耗时：")) {
                                row.createCell(7).setCellValue(Integer.parseInt(str.split("：")[1]));
                            }
                        }

                        mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/esl_view_update_firmware_update_success_tv_continue")), 5000);
                        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/esl_view_update_firmware_update_success_tv_continue")).clickAndWaitForNewWindow();
                        XLog.i("等待回连");
//                    mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/esl_activity_on_off_lock_tv_lock_status")), reconnecttimeout * 1000);
                        mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure")), reconnecttimeout * 1000);

                        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/esl_activity_on_off_lock_tv_lock_status")).exists() ||
                                mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_custom_sure")).exists()) {
                            XLog.i("回连成功");
                            row.createCell(8).setCellValue("pass");
                        } else {
                            XLog.i("回连失败");
                            row.createCell(8).setCellValue("fail");
                        }
                    }else{
                        row.createCell(3).setCellValue(DateUtils.getSystemTime());
                        row.createCell(4).setCellValue("fail");
                    }
                } else if(flag==-2){
                    XLog.i("升级超时");
                    row.createCell(4).setCellValue("TIMEOUT");
                }else if(flag==2){
                    XLog.i("升级失败");
                    row.createCell(4).setCellValue("fail");
                }
//                else{
//                    XLog.i("界面异常");
//                    row.createCell(4).setCellValue("error");
//                }

            } else {
                XLog.i("检测新版本失败");
                row.createCell(1).setCellValue("fail");
            }
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");

            if(restart) {
                mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");
            }else{
                if(mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_custom_sure")).exists()){
                    mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure")).click();
                }
                for (int i = 0; i < 3; i++) {
                    if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/img_back")).exists()) {
                        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/img_back")).clickAndWaitForNewWindow();
                    } else {
                        break;
                    }
                }
            }
        }
    }
}
