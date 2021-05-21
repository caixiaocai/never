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

/***
 * smartlock的连接断连专项
 */
@RunWith(AndroidJUnit4.class)
public class SmartLockEventsTest {
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

        Bundle extras = InstrumentationRegistry.getArguments();

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("时间");
            row.createCell(2).setCellValue("操作");
            row.createCell(3).setCellValue("结果");
            row.createCell(4).setCellValue("事件个数");
            row.createCell(5).setCellValue("【1】");
            row.createCell(6).setCellValue("【2】");
            row.createCell(7).setCellValue("【3】");
            row.createCell(9).setCellValue("【1】");
            row.createCell(10).setCellValue("【2】");
            row.createCell(11).setCellValue("【3】");
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
        String text, status, total, item1, item2, item3, old1="", old2="", old3="";
        String[] list;
        UiObject2 lockTv;
        XLog.i("start test");
        int x = mDevice.getDisplayWidth()/2;
        int y = mDevice.getDisplayHeight();

        mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");
        mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/itemSmartLockRoot")), 15000);
        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/itemSmartLockRoot")).click();
        mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure")), 30000);
        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_custom_sure")).click();

        while (true) {
            testCount++;
            lockTv = mDevice.findObject(By.res("com.oceanwing.battery.cam:id/esl_activity_on_off_lock_tv_lock_status"));
            status = lockTv.getText();
            XLog.i("################################");
            XLog.i(status);
            XLog.i("################################");
            XLog.i(String.format("第【 %d 】次测试", testCount));
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);
            row.createCell(1).setCellValue(DateUtils.getSystemTime());
            XLog.i("滑动");

            mDevice.swipe(x, y /10 * 4, x, y, 100);
            if(status.equals("Locked")) {
                XLog.i("unlock");
                row.createCell(2).setCellValue("unlock");
                Thread.sleep(10000);
//                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/esl_activity_on_off_lock_tv_lock_status").text("Unlocked")), 30000);
//                if(mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/esl_activity_on_off_lock_tv_lock_status").text("Unlocked")).exists()){
                if(mDevice.findObject(new UiSelector().text("Unlocked")).exists()){
                    flag = true;
                }else{
                    flag = false;
                }
            }else{
                XLog.i("lock");
                row.createCell(2).setCellValue("lock");
                Thread.sleep(10000);
//                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/esl_activity_on_off_lock_tv_lock_status").text("Locked")), 30000);
//                if(mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/esl_activity_on_off_lock_tv_lock_status").text("Locked")).exists()){
                if(mDevice.findObject(new UiSelector().text("Locked")).exists()){
                    flag = true;
                }else{
                    flag = false;
                }
            }
            if(flag){
                XLog.i("滑动操作成功");
                row.createCell(3).setCellValue("pass");
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/llDownEventList")).click();
                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_content")), 30000);
                text = mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_content")).getText();
                XLog.i(text);
                list = text.split("\n");
                total = list[0].split("：")[1];
                XLog.i(total);
                item1 = list[1].concat(list[2]);
                item2 = list[3].concat(list[4]);
                item3 = list[5].concat(list[6]);
                XLog.i(item1);
                XLog.i(item2);
                XLog.i(item3);
                row.createCell(4).setCellValue(Integer.parseInt(total));
                row.createCell(9).setCellValue(item1);
                row.createCell(10).setCellValue(item2);
                row.createCell(11).setCellValue(item3);
                if(Integer.parseInt(total)<512){
                    if(item1.equals(old1)){
                        XLog.i("pass");
                        row.createCell(5).setCellValue("pass");
                    }else{
                        XLog.i("error");
                        row.createCell(5).setCellValue("error");
                    }
                    if(item2.equals(old2)){
                        XLog.i("pass");
                        row.createCell(6).setCellValue("pass");
                    }else{
                        XLog.i("error");
                        row.createCell(6).setCellValue("error");
                    }
                    if(item3.equals(old3)){
                        XLog.i("pass");
                        row.createCell(7).setCellValue("pass");
                    }else{
                        XLog.i("error");
                        row.createCell(7).setCellValue("error");
                    }
                }else{
                    if((!item1.equals(old1)) && (!item1.equals(old2)) && (!item1.equals(old3))){
                        XLog.i("pass");
                        row.createCell(5).setCellValue("pass");
                    }else{
                        XLog.i("error");
                        row.createCell(5).setCellValue("error");
                    }
                    if(item2.equals(old1)){
                        XLog.i("pass");
                        row.createCell(6).setCellValue("pass");
                    }else{
                        XLog.i("error");
                        row.createCell(6).setCellValue("error");
                    }
                    if(item3.equals(old2)){
                        XLog.i("pass");
                        row.createCell(7).setCellValue("pass");
                    }else{
                        XLog.i("error");
                        row.createCell(7).setCellValue("error");
                    }
                }
                old1 = item1;
                old2 = item2;
                old3 = item3;
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure")).click();  // event弹窗
                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/img_back")),5000);
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/img_back")).click();
                mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/esl_activity_on_off_lock_tv_lock_status")), 5000);
            }else{
                XLog.i("滑动操作失败");
                row.createCell(3).setCellValue("fail");
            }
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");
        }
    }
}
