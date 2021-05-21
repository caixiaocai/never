package com.anker.autotest.eufysecurity;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
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
 * smartlock的连接断连专项
 */
@RunWith(AndroidJUnit4.class)
public class SmartLockConnectTest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0;
    private int connnectTimeout = 60;
    private boolean restart = false;

    @Before
    public void setUp() throws Exception {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);

        Bundle extras = InstrumentationRegistry.getArguments();

        if (extras.containsKey("connnectTimeout")) {
            connnectTimeout = Integer.parseInt(extras.getString("connnectTimeout"));
        }
        XLog.i(String.format("connnectTimeout: %d", connnectTimeout));

        if (extras.containsKey("restart")) {
            restart = Boolean.parseBoolean(extras.getString("restart"));
        }
        XLog.i(String.format("restart: %s", restart));

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("开始时间");
            row.createCell(2).setCellValue("结束时间");
            row.createCell(3).setCellValue("结果");
            row.createCell(4).setCellValue("点击次数");
            row.createCell(5).setCellValue("scanTime");
            row.createCell(6).setCellValue("connectTime");
            row.createCell(7).setCellValue("verifyTime");
            row.createCell(8).setCellValue("rssi");
            row.createCell(9).setCellValue("alltime");
            row.createCell(10).setCellValue("异常弹窗内容");
            row.createCell(11).setCellValue("connnectTimeout=" + connnectTimeout);
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
    public void test() throws IOException {
        int clickCount, flag;
        String[] textList;
        XLog.i("start test");
        mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");

        while (true) {
            String text;
            testCount++;
            XLog.i(String.format("第【 %d 】次测试", testCount));
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);

            mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/itemSmartLockRoot")), 15000);
            mDevice.findObject(By.res("com.oceanwing.battery.cam:id/itemSmartLockRoot")).click();
            row.createCell(1).setCellValue(DateUtils.getSystemTime());
            XLog.i("等待连接");
            clickCount = 0;
            flag = 0;
            while (true) {
                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure")), connnectTimeout*1000);
                if (mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure"))!=null) {
                    XLog.i("连接弹窗");
                    text = mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_content")).getText();
                    if(text.contains("scanTime")){
                        XLog.i("解析弹窗内容");
                        textList = text.split("\n");
                        for(String str: textList){
                            if("".equals(str)){
                            }else if(str.contains("scanTime：")){
                                row.createCell(5).setCellValue(Integer.parseInt(str.split("：")[1]));
                            }else if(str.contains("connectTime：")){
                                row.createCell(6).setCellValue(Integer.parseInt(str.split("：")[1]));
                            }else if(str.contains("verifyTime：")){
                                row.createCell(7).setCellValue(Integer.parseInt(str.split("：")[1]));
                            }else if(str.contains("rssi：")){
                                row.createCell(8).setCellValue(Integer.parseInt(str.split("：")[1]));
                            }else if(str.contains("alltime：")){
                                row.createCell(9).setCellValue(Integer.parseInt(str.split("：")[1]));
                            }
                        }
                    }else{
                        XLog.i("无法解析弹窗内容");
                        row.createCell(10).setCellValue(text);
                    }
                    mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure")).click();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mDevice.findObject(By.res("com.oceanwing.battery.cam:id/imgFail"))!=null){
                        if(flag==0) {
                            flag = 2;  // 失败
                        }
                    }else {
                        if(flag==0) {
                            flag = 1;  // 成功
                        }
                    }
                    break;
                } else if (mDevice.findObject(By.res("com.oceanwing.battery.cam:id/imgFail"))!=null) {  // Retry
                    XLog.i("连接失败");
                    if(flag==0) {
                        flag = 2;  // 失败
                    }
                    clickCount++;
                    XLog.i("点击Retry，连接超时重新计时");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mDevice.findObject(By.res("com.oceanwing.battery.cam:id/imgFail")).click();
                } else if(mDevice.findObject(By.res("com.oceanwing.battery.cam:id/arcView"))!=null
                        || mDevice.findObject(By.text("Connecting the device"))!=null){
                    XLog.i("连接中...");
                    if(flag==0) {
                        flag = 3;  // 超时
                    }
                    break;
                }
            }
            row.createCell(2).setCellValue(DateUtils.getSystemTime());
            switch (flag){
                case 1:
                    row.createCell(3).setCellValue("pass");
                    break;
                case 2:
                    row.createCell(3).setCellValue("fail");
                    row.createCell(4).setCellValue(clickCount);
                    break;
                case 3:
                    row.createCell(3).setCellValue("TIMEOUT");
                    break;
            }

            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");
            if(restart) {
                mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");
            }else if (mDevice.findObject(By.res("com.oceanwing.battery.cam:id/img_back"))!=null) {
                    mDevice.findObject(By.res("com.oceanwing.battery.cam:id/img_back")).click();
            }
        }
    }
}
