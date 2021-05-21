package com.anker.autotest.nebula;

import android.os.RemoteException;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Direction;
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


/***
 * M2C项目ota升级
 */
@RunWith(AndroidJUnit4.class)
public class M2AndroidSystemOTATest {
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
        filePath = floderPath + "M2COTATest.xls";
        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("时间");
            row.createCell(2).setCellValue("检测升级包");
            row.createCell(3).setCellValue("下载");
            row.createCell(4).setCellValue("点击升级时间点");
            row.createCell(5).setCellValue("本次升级前是否已重启");
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
        boolean flag = false;
        UiObject2 obj;
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
            mDevice.pressHome();
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            int rowNum = sheet.getLastRowNum();
            HSSFRow row = sheet.createRow(rowNum + 1);
            row.createCell(0).setCellValue(rowNum + 1);
            row.createCell(1).setCellValue(DateUtils.getSystemTime());
            mDevice.executeShellCommand("am start -n com.android.tv.settings/.MainSettings -W");
            obj = mDevice.wait(Until.findObject(By.text("Device Preferences")), 5000);
            obj.click();
            obj = mDevice.wait(Until.findObject(By.text("About")), 5000);
            obj.click();
            obj = mDevice.wait(Until.findObject(By.text("System update")), 5000);
            obj.click();
            obj = mDevice.wait(Until.findObject(By.res("com.android.tv.settings:id/on_button_result_btn").text("Download Now")), 30000);
            if (obj == null) {
                obj = mDevice.wait(Until.findObject(By.res("com.microcast.tv.settings:id/on_button_result_btn").text("Check Update")), 10000);
                if(obj == null){
                    XLog.i("界面异常");
                    row.createCell(2).setCellValue("ERROR");
                }else{
                    XLog.i("没有新版本");
                    row.createCell(2).setCellValue("fail");
                    obj.click();
                }
            } else {
                XLog.i("点击下载");
                row.createCell(2).setCellValue("pass");
                obj.click();
                obj = mDevice.wait(Until.findObject(By.res("com.android.tv.settings:id/download_progress_tv").text("100%")), 900000);
                if (obj == null) {
                    row.createCell(3).setCellValue("fail");
                } else {
                    XLog.i("升级");
                    row.createCell(3).setCellValue("pass");
                    obj = mDevice.wait(Until.findObject(By.res("com.android.tv.settings:id/upgrade_btn").text("Upgrade")), 10000);
                    obj.click();
                    row.createCell(4).setCellValue(DateUtils.getSystemTime());
                    if (flag) {
                        row.createCell(5).setCellValue("false");
                    } else {
                        row.createCell(5).setCellValue("true");
                    }
                    flag = true;
                    FileOutputStream os = new FileOutputStream(filePath);
                    wb.write(os);
                    os.close();
                    wb.close();
                    XLog.i("结果写入完成");
                    Thread.sleep(300000);//等待5分钟
                }
            }
            row.createCell(6).setCellValue("流程异常，重新执行升级流程");
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");
            for (int i = 0; i < 5; i++) {
                mDevice.pressBack();
                Thread.sleep(500);
            }
        }
    }
}
