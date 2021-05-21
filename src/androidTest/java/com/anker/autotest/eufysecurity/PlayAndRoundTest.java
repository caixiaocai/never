package com.anker.autotest.eufysecurity;

import android.graphics.Rect;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
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

@RunWith(AndroidJUnit4.class)
public class PlayAndRoundTest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0;
    private int waitidle = 5;

    @Before
    public void setUp() throws Exception {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");

        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);

        Bundle extras = InstrumentationRegistry.getArguments();
        if (extras.containsKey("waitidle")) {
            waitidle = Integer.parseInt(extras.getString("waitidle"));
        }
        XLog.i(String.format("waitidle: %s", waitidle));

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
            row.createCell(4).setCellValue("waitidle=" + waitidle);
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
        XLog.i("start test");
        mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");
        mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/item_single_camera_img_live")), 60000);
        mDevice.findObject(By.res("com.oceanwing.battery.cam:id/item_single_camera_img_live")).click();

        XLog.i("查看实时流");
        playVideo();
        XLog.i("出流成功");
        UiObject round = mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/roundMenuView"));
        Rect bounds = round.getVisibleBounds();
        int centerX = bounds.centerX();
        int centerY = bounds.centerY();
        int left = (bounds.left * 4 + centerX)/5;
        int y = (centerY * 2 + bounds.bottom)/3;
        int right = (centerX + bounds.right * 4)/5;
        int top = (bounds.top * 4 + centerY)/5;
        int bottom = (centerY + bounds.bottom * 4)/5;

        HSSFWorkbook wb;
        HSSFSheet sheet;
        FileOutputStream os;

        while (true) {
            wb = new HSSFWorkbook(new FileInputStream(filePath));
            sheet = wb.getSheetAt(0);
            for(int i=0; i<2; i++) {
                testCount++;
                XLog.i(String.format("第【 %d 】次测试", testCount));
                HSSFRow row = sheet.createRow(testCount);
                row.createCell(0).setCellValue(testCount);
                row.createCell(1).setCellValue(DateUtils.getSystemTime(System.currentTimeMillis()));
                row.createCell(2).setCellValue("left");
                mDevice.click(left, y);  // left
                XLog.i("left");
                XLog.i(left + ", " + y);
                try {
                    Thread.sleep(waitidle * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                playVideo();
            }
            os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");

            wb = new HSSFWorkbook(new FileInputStream(filePath));
            sheet = wb.getSheetAt(0);
            for(int i=0; i<2; i++) {
                testCount++;
                XLog.i(String.format("第【 %d 】次测试", testCount));
                HSSFRow row = sheet.createRow(testCount);
                row.createCell(0).setCellValue(testCount);
                row.createCell(1).setCellValue(DateUtils.getSystemTime(System.currentTimeMillis()));
                row.createCell(2).setCellValue("right");
                mDevice.click(right, y);  // right
                XLog.i("right");
                XLog.i(right + ", " + y);
                try {
                    Thread.sleep(waitidle * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                playVideo();
            }
            os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");

            wb = new HSSFWorkbook(new FileInputStream(filePath));
            sheet = wb.getSheetAt(0);
            for(int i=0; i<2; i++) {
                testCount++;
                XLog.i(String.format("第【 %d 】次测试", testCount));
                HSSFRow row = sheet.createRow(testCount);
                row.createCell(0).setCellValue(testCount);
                row.createCell(1).setCellValue(DateUtils.getSystemTime(System.currentTimeMillis()));
                row.createCell(2).setCellValue("top");
                mDevice.click(centerX, top);  // top
                XLog.i("top");
                XLog.i(centerX + ", " + top);
                try {
                    Thread.sleep(waitidle * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                playVideo();
            }
            os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");

            wb = new HSSFWorkbook(new FileInputStream(filePath));
            sheet = wb.getSheetAt(0);
            for(int i=0; i<2; i++) {
                testCount++;
                XLog.i(String.format("第【 %d 】次测试", testCount));
                HSSFRow row = sheet.createRow(testCount);
                row.createCell(0).setCellValue(testCount);
                row.createCell(1).setCellValue(DateUtils.getSystemTime(System.currentTimeMillis()));
                row.createCell(2).setCellValue("bottom");
                mDevice.click(centerX, bottom);  // bottom
                XLog.i("bottom");
                XLog.i(centerX + ", " + bottom);
                try {
                    Thread.sleep(waitidle * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                playVideo();
            }
            os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");
        }
    }

    private void playVideo() throws UiObjectNotFoundException {
        UiObject obj;
        while (true) {
            obj = mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/item_super_video_layout"));
            if(obj.exists() && obj.getChildCount()==0){
                break;
            }else if(mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/cb_cellular_dialog_remind")).exists()){
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/cb_cellular_dialog_remind")).click();
            }else if(mDevice.hasObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure").text("Continue"))){
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_custom_sure").text("Continue")).click();
            }else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/camera_preview_video_btn_retry")).exists()) {
                mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/camera_preview_video_btn_retry")).click();
            }else{
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
