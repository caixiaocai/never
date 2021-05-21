package com.anker.autotest.nebula;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObjectNotFoundException;
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
import java.io.IOException;

/***
 * 监测连接稳定性
 */
@RunWith(AndroidJUnit4.class)
public class NebulaConnectTest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    String timeData;

    @Before
    public void setUp() throws IOException, UiObjectNotFoundException {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");

        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("时间");
            row.createCell(2).setCellValue("状态");
            row.createCell(3).setCellValue("时间间隔(ms)");
            row.createCell(4).setCellValue("点击reconnect次数");
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
        long nowTime;
        int disConnectCount = 0;
        int reconnect;
        int rowNum = 1;
        HSSFWorkbook wb;
        HSSFSheet sheet;
        HSSFRow row;
        FileOutputStream os;
        XLog.i("Start test");
        long lastTime = System.currentTimeMillis();
        while (true) {
            if (mDevice.findObject(new UiSelector().resourceId("com.zhixin.connecter:id/connectBtn")).exists()) {//断连
                disConnectCount++;//记录
                XLog.i(String.format("第【 %d 】次断连", disConnectCount));
                wb = new HSSFWorkbook(new FileInputStream(filePath));
                sheet = wb.getSheetAt(0);
                row = sheet.createRow(rowNum++);
                nowTime = System.currentTimeMillis();
                row.createCell(0).setCellValue(disConnectCount);
                row.createCell(1).setCellValue(DateUtils.getSystemTime(nowTime));
                row.createCell(2).setCellValue("disconnect");
                row.createCell(3).setCellValue(nowTime - lastTime);
                lastTime = nowTime;
                os = new FileOutputStream(filePath);
                wb.write(os);
                os.close();
                wb.close();
                reconnect = 0;
                while (true) {
                    if (mDevice.findObject(new UiSelector().resourceId("com.zhixin.connecter:id/connectBtn")).exists()) {
                        if (mDevice.findObject(new UiSelector().resourceId("com.zhixin.connecter:id/progress_bar")).exists()) {//正在重连
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {//重连失败，显示断开连接，点击按钮重连
                            try {
                                mDevice.findObject(new UiSelector().resourceId("com.zhixin.connecter:id/connectBtn")).click();
                                reconnect++;
                                XLog.i(String.format("第【 %d 】次点击连接按钮", reconnect));
                                Thread.sleep(500);
                            } catch (UiObjectNotFoundException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (mDevice.findObject(new UiSelector().resourceId("com.zhixin.connecter:id/backBtn")).exists()) {
                        mDevice.findObject(new UiSelector().resourceId("com.zhixin.connecter:id/backBtn")).clickAndWaitForNewWindow();
                    } else {
                        wb = new HSSFWorkbook(new FileInputStream(filePath));
                        sheet = wb.getSheetAt(0);
                        row = sheet.createRow(rowNum++);
                        nowTime = System.currentTimeMillis();
                        row.createCell(0).setCellValue(disConnectCount);
                        row.createCell(1).setCellValue(DateUtils.getSystemTime(nowTime));
                        row.createCell(2).setCellValue("reconnect");
                        row.createCell(3).setCellValue(nowTime - lastTime);
                        lastTime = nowTime;
                        row.createCell(4).setCellValue(reconnect);
                        os = new FileOutputStream(filePath);
                        wb.write(os);
                        os.close();
                        wb.close();
                        XLog.i("结果写入完成");
                        break;
                    }
                }
            } else {
                Thread.sleep(500);
            }
        }
    }
}
