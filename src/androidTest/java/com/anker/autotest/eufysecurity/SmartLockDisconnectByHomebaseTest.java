package com.anker.autotest.eufysecurity;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.StaleObjectException;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
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
 * smartlock的从基站断连后再连接专项
 */
@RunWith(AndroidJUnit4.class)
public class SmartLockDisconnectByHomebaseTest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0;
//    private int connnectTimeout = 60;
//    private boolean restart = false;

    @Before
    public void setUp() throws Exception {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);

//        Bundle extras = InstrumentationRegistry.getArguments();
//
//        if (extras.containsKey("connnectTimeout")) {
//            connnectTimeout = Integer.parseInt(extras.getString("connnectTimeout"));
//        }
//        XLog.i(String.format("connnectTimeout: %d", connnectTimeout));
//
//        if (extras.containsKey("restart")) {
//            restart = Boolean.parseBoolean(extras.getString("restart"));
//        }
//        XLog.i(String.format("restart: %s", restart));

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("断连开始时间");
            row.createCell(2).setCellValue("断连结束时间");
            row.createCell(3).setCellValue("结果");
            row.createCell(4).setCellValue("连接开始时间");
            row.createCell(5).setCellValue("连接结束时间");
            row.createCell(6).setCellValue("结果");
            row.createCell(7).setCellValue("Retry");
            row.createCell(8).setCellValue("Retest");
            row.createCell(9).setCellValue("备注");
            row.createCell(10).setCellValue("Start Test异常文案");
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
    public void test() throws IOException, InterruptedException {
        int clickCount, flag;
        String[] textList;
        String text, tips;
        int width = mDevice.getDisplayWidth();
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

            mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/item_single_camera_cam_setting_icon")), 15000);
            Thread.sleep(1000);
            XLog.i("主界面刷新");
            mDevice.swipe(width / 2, height / 5, width / 2, height, 80);
            Thread.sleep(3000);
            // 断开连接
            mDevice.findObject(By.res("com.oceanwing.battery.cam:id/item_single_camera_cam_setting_icon")).click();
            mDevice.wait(Until.hasObject(By.text("Paired Devices")), 5000);
            boolean clicked = true;
            for(int kk=0;kk<5;kk++){
                try {
                    mDevice.findObject(By.text("Paired Devices")).click();
                }catch (StaleObjectException e){
                    clicked = false;
                }
                if(clicked){
                    break;
                }
            }

            XLog.i("点击断连");
            row.createCell(1).setCellValue(DateUtils.getSystemTime());
            mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/tv_disconnect")), 3000).click();
            mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/btn_positive")), 3000).click();

            XLog.i("等待loading结束");
            // 等待loading结束
            mDevice.wait(Until.hasObject(By.clazz("android.widget.ProgressBar")), 5000);
            mDevice.wait(Until.gone(By.clazz("android.widget.ProgressBar")), 60000);
            row.createCell(2).setCellValue(DateUtils.getSystemTime());
            Thread.sleep(1000);
            // 判断门锁还在Paired Devices列表页面，返回到主界面刷新后再检查
            if (mDevice.hasObject(By.res("com.oceanwing.battery.cam:id/img_lock_icon"))) {
                XLog.i("门锁还在Paired Devices列表，返回主界面刷新后重新进入检查");
                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/activity_add_select_back")), 15000).click();
                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/imv_back")), 3000).click();
                Thread.sleep(1000);
                XLog.i("主界面刷新");
                mDevice.swipe(width / 2, height / 5, width / 2, height, 80);
                Thread.sleep(3000);
                // 断开连接
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/item_single_camera_cam_setting_icon")).click();
                mDevice.wait(Until.findObject(By.text("Paired Devices")), 3000).click();

                if (mDevice.hasObject(By.res("com.oceanwing.battery.cam:id/img_lock_icon"))) {
                    XLog.i("返回主界面刷新后门锁还在Paired Devices列表，杀掉app进行下一轮");
                    row.createCell(3).setCellValue("fail(主界面刷新后)");
                    mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");
                    FileOutputStream os = new FileOutputStream(filePath);
                    wb.write(os);
                    os.close();
                    wb.close();
                    XLog.i("结果写入完成");

                    continue;
                }else{
                    XLog.i("返回主界面刷新后门锁不在Paired Devices列表");
                    row.createCell(3).setCellValue("pass(主界面刷新后)");
                    break;
                }
            } else {
                XLog.i("断开连接成功");
                row.createCell(3).setCellValue("pass");
            }

            mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/activity_add_select_back")), 15000).click();
            mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/imv_back")), 3000).click();

            XLog.i("开始进行连接部分");
            int retry=0, retest = 0;
            boolean connectFlag = false;
            boolean connectResult = false;
            for(int ttt=0;ttt<10;ttt++) {
                // 连接
                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/itemSmartLockRoot")), 5000).click();
                if (mDevice.wait(Until.hasObject(By.res("com.android.packageinstaller:id/permission_allow_button")), 1000)) {
                    mDevice.findObject(By.res("com.android.packageinstaller:id/permission_allow_button")).click();
                }
                XLog.i("等待蓝牙连接");

                while (true) {
                    mDevice.wait(Until.hasObject(By.text("Connecting")), 5000);
                    mDevice.wait(Until.gone(By.text("Connecting")), 120000);
                    if (mDevice.hasObject(By.text("Drag to lock")) || mDevice.hasObject(By.text("Drag to unlock"))) {
                        XLog.i("蓝牙连接成功");
                        break;
                    } else if (mDevice.hasObject(By.text("Retry"))) {
                        XLog.i("点击Retry");
                        mDevice.findObject(By.text("Retry")).click();
                    }
                }
//            clickCount = 0;
//            flag = 0;
//            while (true) {
//                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure")), connnectTimeout*1000);
//                if (mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure"))!=null) {
//                    XLog.i("连接弹窗");
//                    text = mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_content")).getText();
//                    if(text.contains("scanTime")){
//                        XLog.i("解析弹窗内容");
//                        textList = text.split("\n");
//                        for(String str: textList){
//                            if("".equals(str)){
//                            }else if(str.contains("scanTime：")){
//                                row.createCell(5).setCellValue(Integer.parseInt(str.split("：")[1]));
//                            }else if(str.contains("connectTime：")){
//                                row.createCell(6).setCellValue(Integer.parseInt(str.split("：")[1]));
//                            }else if(str.contains("verifyTime：")){
//                                row.createCell(7).setCellValue(Integer.parseInt(str.split("：")[1]));
//                            }else if(str.contains("rssi：")){
//                                row.createCell(8).setCellValue(Integer.parseInt(str.split("：")[1]));
//                            }else if(str.contains("alltime：")){
//                                row.createCell(9).setCellValue(Integer.parseInt(str.split("：")[1]));
//                            }
//                        }
//                    }else{
//                        XLog.i("无法解析弹窗内容");
//                        row.createCell(10).setCellValue(text);
//                    }
//                    mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure")).click();
//                    try {
//                        Thread.sleep(3000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    if (mDevice.findObject(By.res("com.oceanwing.battery.cam:id/imgFail"))!=null){
//                        if(flag==0) {
//                            flag = 2;  // 失败
//                        }
//                    }else {
//                        if(flag==0) {
//                            flag = 1;  // 成功
//                        }
//                    }
//                    break;
//                } else if (mDevice.findObject(By.res("com.oceanwing.battery.cam:id/imgFail"))!=null) {  // Retry
//                    XLog.i("连接失败");
//                    if(flag==0) {
//                        flag = 2;  // 失败
//                    }
//                    clickCount++;
//                    XLog.i("点击Retry，连接超时重新计时");
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    mDevice.findObject(By.res("com.oceanwing.battery.cam:id/imgFail")).click();
//                } else if(mDevice.findObject(By.res("com.oceanwing.battery.cam:id/arcView"))!=null
//                        || mDevice.findObject(By.text("Connecting the device"))!=null){
//                    XLog.i("连接中...");
//                    if(flag==0) {
//                        flag = 3;  // 超时
//                    }
//                    break;
//                }
//            }
//            row.createCell(2).setCellValue(DateUtils.getSystemTime());
//            switch (flag){
//                case 1:
//                    row.createCell(3).setCellValue("pass");
//                    break;
//                case 2:
//                    row.createCell(3).setCellValue("fail");
//                    row.createCell(4).setCellValue(clickCount);
//                    break;
//                case 3:
//                    row.createCell(3).setCellValue("TIMEOUT");
//                    break;
//            }
                XLog.i("进到设置界面");
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/imgSetting")).click();
                Thread.sleep(1000);
                mDevice.swipe(width/2, (int)(height*0.8), width/2, 10, 80);
                if(!mDevice.wait(Until.hasObject(By.text("Connect to Wi-Fi Bridge")), 3000)){
                    row.createCell(9).setCellValue("门锁设置界面没有Connect to Wi-Fi Bridge选项");
                    XLog.i("门锁设置界面没有找到Connect to Wi-Fi Bridge选项");
                    XLog.i("杀掉app进行下一轮测试");
                    mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");
                    break;
                }
                mDevice.findObject(By.text("Connect to Wi-Fi Bridge")).click();
                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/btn_bottom").text("Connect to Wi-Fi Bridge")), 3000).click();

                row.createCell(4).setCellValue(DateUtils.getSystemTime());
                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/ll_item_view")), 3000).click();

                for(int ii=0;ii<3;ii++) {
                    XLog.i("等待连接");
                    mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/ll_connect_root")), 30000); // "com.oceanwing.battery.cam:id/spreadView"
                    mDevice.wait(Until.gone(By.res("com.oceanwing.battery.cam:id/ll_connect_root")), 110000);

                    if (!mDevice.hasObject(By.res("com.oceanwing.battery.cam:id/sv_start_test").text("Start Test"))) {
                        if (!connectResult) {
                            XLog.i("Test 失败记录到表格");
                            connectResult = true;
                            row.createCell(6).setCellValue("fail");
                        }
                        mDevice.findObject(By.text("Retry")).click();
                        retry++;
                    } else {
                        XLog.i("点击Start Test");
                        mDevice.findObject(By.res("com.oceanwing.battery.cam:id/sv_start_test").text("Start Test")).click();
                        for (int jjj = 0; jjj < 3; jjj++) {
                            mDevice.wait(Until.hasObject(By.text("Done")), 60000);
                            if (mDevice.hasObject(By.text("Great!"))) {
                                XLog.i("Test 成功");
                                if (!connectResult) {
                                    XLog.i("Test 成功记录到表格");
                                    connectResult = true;
                                    row.createCell(6).setCellValue("pass");
                                }
                                connectFlag = true;
                                break;
                            } else {
                                XLog.i("Retest");
                                if (!connectResult) {
                                    XLog.i("Test 失败记录到表格");
                                    connectResult = true;
                                    row.createCell(6).setCellValue("fail");
                                }
                                if (jjj == 0) {
                                    tips = mDevice.findObject(By.res("com.oceanwing.battery.cam:id/tv_testing_tip1")).getText();
                                    row.createCell(10).setCellValue(tips);
                                }
                                mDevice.findObject(By.text("Retest")).click();
                                retest++;
                            }
                        }
                        if (mDevice.hasObject(By.text("Done"))) {
                            XLog.i("点击Done");
                            mDevice.findObject(By.text("Done")).click();
                        }
                    }
                    if (connectFlag) {
                        break;
                    }
                }
                if (connectFlag) {
                    break;
                }else{
                    XLog.i("连接失败，杀掉app重试");
                    mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");
                }

            }
            if(!connectResult){
                XLog.i("Test 失败记录到表格");
                row.createCell(6).setCellValue("fail");
            }
            XLog.i("retry: " + retry);
            XLog.i("retest: " + retest);
            row.createCell(5).setCellValue(DateUtils.getSystemTime());
            row.createCell(7).setCellValue(retry);
            row.createCell(8).setCellValue(retest);

            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");
//            if(restart) {
//                mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");
//            }
        }
    }
}
