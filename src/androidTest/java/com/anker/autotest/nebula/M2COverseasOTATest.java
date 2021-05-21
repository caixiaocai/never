package com.anker.autotest.nebula;

import android.os.RemoteException;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiSelector;

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


/***
 * M2C海外版ota升级
 */
@RunWith(AndroidJUnit4.class)
public class M2COverseasOTATest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;

    @Before
    public void setUp() throws Exception {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");

        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + "M2COverseasOTATest.xls";
        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("时间");
            row.createCell(2).setCellValue("点击升级时间点");
            row.createCell(3).setCellValue("本次升级前是否已重启");
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
        long start;
        boolean flag = false;
        XLog.i("Start test");
        // 打开setting
        try {
            if (!mDevice.isScreenOn()) {
                mDevice.wakeUp();//唤醒屏幕
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();
        while (true) {
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            int rowNum = sheet.getLastRowNum();
            HSSFRow row = sheet.createRow(rowNum + 1);
            row.createCell(0).setCellValue(rowNum + 1);
            start = System.currentTimeMillis();
            row.createCell(1).setCellValue(DateUtils.getSystemTime(start));

            while (System.currentTimeMillis() - start < 300 * 1000) {
                if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.ota:id/reboot")).exists()
                        && mDevice.findObject(new UiSelector().resourceId("com.oceanwing.ota:id/reboot")).isEnabled()) {
                    break;
                } else {
                    Thread.sleep(1000);
                }
            }
            row.createCell(2).setCellValue(DateUtils.getSystemTime());
            if (flag) {
                row.createCell(3).setCellValue("false");
            } else {
                row.createCell(3).setCellValue("true");
            }
            flag = true;
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");
            mDevice.findObject(new UiSelector().resourceId("com.oceanwing.ota:id/reboot")).click();
            XLog.i("点击升级");
            Thread.sleep(600000);//等待三分钟
        }
    }
}
