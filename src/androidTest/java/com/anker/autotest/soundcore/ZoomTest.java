package com.anker.autotest.soundcore;

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
import java.util.Date;

/***
 *
 */
@RunWith(AndroidJUnit4.class)
public class ZoomTest {
    private UiDevice mDevice = null;
    private int testCount = 0;
    private int connectCount = 0;
    private int disconnectCount = 0;
    private int errorCount = 0;
    private String floderPath;
    private String filePath;
    private String zoomid="802380249";
    private int time=60;


    @Before
    public void setUp() throws Exception {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");

        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);
        Bundle extras = InstrumentationRegistry.getArguments();
        if (extras.containsKey("zoomid")) {
            zoomid = extras.getString("zoomid");
        }
        XLog.i(String.format("zoomid: %s", zoomid));
        if (extras.containsKey("time")) {
            time = Integer.parseInt(extras.getString("time"));
        }
        XLog.i(String.format("time: %s", time));

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);
        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("计数");
            row.createCell(2).setCellValue("操作");
            row.createCell(3).setCellValue("开始时间");
            row.createCell(4).setCellValue("结束时间");
            row.createCell(5).setCellValue("结果");
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
        XLog.i("start test");
        if (!mDevice.getCurrentPackageName().equals("us.zoom.videomeetings")) {
            mDevice.executeShellCommand("am start -n us.zoom.videomeetings/com.zipow.videobox.WelcomeActivity -W");
            Thread.sleep(5);
        }
        while (true) {
            testCount++;
            XLog.i(String.format("第【 %d 】次测试", testCount));
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);
            try {
                mDevice.executeShellCommand("am start -n us.zoom.videomeetings/com.zipow.videobox.LauncherActivity -W");
                mDevice.wait(Until.findObject(By.res("us.zoom.videomeetings:id/btnJoinConf")), 10000);
                mDevice.findObject(By.res("us.zoom.videomeetings:id/btnJoinConf")).click();
                mDevice.wait(Until.findObject(By.res("us.zoom.videomeetings:id/edtConfNumber")), 5000);
                mDevice.findObject(By.res("us.zoom.videomeetings:id/edtConfNumber")).setText(zoomid);
                Thread.sleep(200);
                mDevice.findObject(By.res("us.zoom.videomeetings:id/btnJoin")).click();
                Thread.sleep(time * 1000);
            } catch (Exception e) {
                XLog.e(e.toString());
                row.createCell(6).setCellValue(e.toString());
                mDevice.takeScreenshot(new File(floderPath + testCount + "_" + DateUtils.getDateTime() + ".png"));
            } finally {
                mDevice.executeShellCommand("am force-stop us.zoom.videomeetings");
                FileOutputStream os = new FileOutputStream(filePath);
                wb.write(os);
                os.close();
                wb.close();
                XLog.i("结果写入完成");
            }
        }
    }
}

