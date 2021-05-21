package com.anker.autotest.eufysecurity;

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

/***
 * doorbell设置项sensitivity压测
 */
@RunWith(AndroidJUnit4.class)
public class DoorbellSensitivityTest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0;
    private int waitidle = 60;


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
            row.createCell(2).setCellValue("设置");
            row.createCell(3).setCellValue("是否生效");
            row.createCell(5).setCellValue("waitidle=" + waitidle);
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

        String RES_1 = "com.oceanwing.battery.cam:id/rbtn_1";
        String RES_2 = "com.oceanwing.battery.cam:id/rbtn_2";
        String RES_3 = "com.oceanwing.battery.cam:id/rbtn_3";
        String RES_4 = "com.oceanwing.battery.cam:id/rbtn_4";
        String RES_5 = "com.oceanwing.battery.cam:id/rbtn_5";
        String RES = RES_1;
        java.util.Random r=new java.util.Random();
        int number, yu;
        boolean result = false;

        while (true) {
            testCount++;
            XLog.i(String.format("第【 %d 】次测试", testCount));
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);

            yu = testCount % 5;

            if (yu == 0){
                RES = RES_1;
                row.createCell(2).setCellValue(1);
            }else if(yu == 1){
                RES = RES_2;
                row.createCell(2).setCellValue(2);
            }else if(yu == 2){
                RES = RES_3;
                row.createCell(2).setCellValue(3);
            }else if(yu == 3){
                RES = RES_4;
                row.createCell(2).setCellValue(4);
            }else if(yu == 4){
                RES = RES_5;
                row.createCell(2).setCellValue(5);
            }

            row.createCell(1).setCellValue(DateUtils.getSystemTime(System.currentTimeMillis()));
            mDevice.findObject(By.res(RES)).click();
            mDevice.wait(Until.gone(By.clazz("android.widget.ProgressBar")), 30000);
            mDevice.wait(Until.findObject(By.res(RES)), 10000);
            result = mDevice.findObject(By.res(RES)).isChecked();

            if (result){
                row.createCell(3).setCellValue("pass");
            }else{
                row.createCell(3).setCellValue("fail");
            }

            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");

            try {
                Thread.sleep(waitidle * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
