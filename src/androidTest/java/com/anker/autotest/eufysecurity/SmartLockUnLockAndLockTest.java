package com.anker.autotest.eufysecurity;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.SearchCondition;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import com.anker.autotest.R;
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
public class SmartLockUnLockAndLockTest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0;
    private int testRound = 0;
    private boolean popup = true;
    private boolean restart = false;

    @Before
    public void setUp() throws Exception {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");

        Bundle extras = InstrumentationRegistry.getArguments();
        if (extras.containsKey("popup")) {
            popup = Boolean.parseBoolean(extras.getString("popup"));
        }
        if (extras.containsKey("restart")) {
            restart = Boolean.parseBoolean(extras.getString("restart"));
        }

        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("轮次");
            row.createCell(1).setCellValue("序号");
            row.createCell(2).setCellValue("时间");
            row.createCell(3).setCellValue("操作");
            row.createCell(4).setCellValue("结果");
            row.createCell(5).setCellValue("耗时");
            row.createCell(6).setCellValue("popup="+popup);
            row.createCell(7).setCellValue("restart="+restart);
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
    public void test() throws IOException, InterruptedException, UiObjectNotFoundException {
        boolean flag;
        String status, txt;
        UiObject2 lockTv;
        XLog.i("start test");
        int x = mDevice.getDisplayWidth()/2;
        int y = mDevice.getDisplayHeight();
        SearchCondition<UiObject2> untilUnlocked, untilLocked;
        if(popup){
            untilUnlocked = Until.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_content"));
            untilLocked = Until.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_content"));
        }else{
            untilUnlocked = Until.findObject(By.res("com.oceanwing.battery.cam:id/tv_lock_status").text("Unlocked"));
            untilLocked = Until.findObject(By.res("com.oceanwing.battery.cam:id/tv_lock_status").text("Locked"));
        }

        mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");
        mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/itemSmartLockRoot")), 15000);
        mDevice.swipe(x, y/4, x, y/10 * 9, 100);
        Thread.sleep(5000);
        while (true) {
            testRound++;
            mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/itemSmartLockRoot")), 15000);
            mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/itemSmartLockRoot")).click();
            for(int tt=0; tt<3; tt++){
                Thread.sleep(1000);
                if(mDevice.hasObject(By.res("com.oceanwing.battery.cam:id/itemSmartLockRoot"))) {
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/itemSmartLockRoot")).click();
                }else{
                    break;
                }
            }
            mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/tv_lock_status")), 300000);

            for(int i=0; i<2; i++) {
                testCount++;
                XLog.i(String.format("第【 %d 】次测试", testCount));
                lockTv = mDevice.findObject(By.res("com.oceanwing.battery.cam:id/tv_lock_status"));
                status = lockTv.getText();
                XLog.i("当前锁的状态：" + status);
                HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
                HSSFSheet sheet = wb.getSheetAt(0);
                HSSFRow row = sheet.createRow(testCount);
                row.createCell(0).setCellValue(testRound);
                row.createCell(1).setCellValue(testCount);
                row.createCell(2).setCellValue(DateUtils.getSystemTime());
                XLog.i("滑动");

                mDevice.swipe(x, y / 10 * 4, x, y, 100);
                if (status.equals("Locked")) {
                    XLog.i("unlock");
                    row.createCell(3).setCellValue("unlock");
                    if(mDevice.wait(untilUnlocked, 30000)!=null){
                        if(popup){
                            txt = mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_content")).getText();
                            XLog.i(txt);
                            row.createCell(5).setCellValue(txt.split("：")[1].trim());
                            mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure")).click();
                        }
                        flag = true;
                    } else {
                        flag = false;
                    }
                } else {
                    XLog.i("lock");
                    row.createCell(3).setCellValue("lock");
                    if(mDevice.wait(untilLocked, 30000)!=null){
                        if(popup){
                            txt = mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_content")).getText();
                            XLog.i(txt);
                            row.createCell(5).setCellValue(txt.split("：")[1].trim());
                            mDevice.findObject(By.text("OK")).click();
                        }
                        flag = true;
                    } else {
                        flag = false;
                    }
                }
                if (flag) {
                    XLog.i("操作成功");
                    row.createCell(4).setCellValue("pass");
                } else {
                    XLog.i("操作失败");
                    row.createCell(4).setCellValue("fail");
                }
                FileOutputStream os = new FileOutputStream(filePath);
                wb.write(os);
                os.close();
                wb.close();
                XLog.i("结果写入完成");
                if (mDevice.wait(Until.findObject(By.text("OK")), 10000)!=null) {
                    mDevice.findObject(By.text("OK")).click();
                }
                for(int kk=0; kk<3; kk++) {
                    if (mDevice.wait(Until.findObject(By.text("OK")), 5000)!=null) {
                        mDevice.findObject(By.text("OK")).click();
                    }else{
                        break;
                    }
                }
            }
            Thread.sleep(100);
            if(restart){
                XLog.i("重启app");
                mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");
            }else{
                XLog.i("返回");
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/img_back")).click();
            }
        }
    }
}
