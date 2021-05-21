package com.anker.autotest.eufysecurity;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

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
 * 反复杀掉重启app
 */
@RunWith(AndroidJUnit4.class)
public class KillAndRestartTest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int count = 100;
    private int waitidle = 10;
    private int interval = 10;

    @Before
    public void setUp() throws Exception {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");

        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);

        Bundle extras = InstrumentationRegistry.getArguments();
        if (extras.containsKey("count")) {
            waitidle = Integer.parseInt(extras.getString("count"));
        }
        if (extras.containsKey("waitidle")) {
            waitidle = Integer.parseInt(extras.getString("waitidle"));
        }
        if (extras.containsKey("interval")) {
            interval = Integer.parseInt(extras.getString("interval"));
        }
        XLog.i(String.format("count: %s", count));
        XLog.i(String.format("waitidle: %s", waitidle));
        XLog.i(String.format("interval: %s", interval));

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);
        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("launch时间");
            row.createCell(2).setCellValue("force-stop时间");
            row.createCell(3).setCellValue("count=" + count);
            row.createCell(4).setCellValue("waitidle=" + waitidle);
            row.createCell(5).setCellValue("interval=" + interval);
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
        long start;
        for (int i = 1; i < count + 1; i++) {
            XLog.i(String.format("第【 %d 】次测试", i));
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(i);
            row.createCell(0).setCellValue(i);
            XLog.i("启动应用");
            mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W");
            start = System.currentTimeMillis();
            row.createCell(1).setCellValue(DateUtils.getSystemTime(start));
            while (System.currentTimeMillis() - start < waitidle * 1000) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            XLog.i("杀应用");
            mDevice.executeShellCommand("am force-stop com.oceanwing.battery.cam");
            start = System.currentTimeMillis();
            row.createCell(2).setCellValue(DateUtils.getSystemTime(start));
            while (System.currentTimeMillis() - start < interval * 1000) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");
        }
    }
}
