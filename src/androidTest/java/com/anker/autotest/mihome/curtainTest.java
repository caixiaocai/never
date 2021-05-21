package com.anker.autotest.mihome;

import android.graphics.Rect;
import android.os.Bundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class curtainTest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0;
    private int timedelta = 15;

    @Rule
    public TestName testName = new TestName(); //获取当前测试方法名

    @Before
    public void setUp() throws IOException, UiObjectNotFoundException {
        XLog.init(LogLevel.ALL);

        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);
        Bundle extras = InstrumentationRegistry.getArguments();
        if (extras.containsKey("timedelta")) {
            timedelta = Integer.parseInt(extras.getString("timedelta"));
        }

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";

        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet("MiHomeTest");
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("时间");
            row.createCell(2).setCellValue("操作");
            row.createCell(3).setCellValue("结果");
            row.createCell(5).setCellValue("timedelta=" + timedelta);
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
        }
    }


    @Test
    public void test() throws IOException, UiObjectNotFoundException {
        String status = "open";
        String result = "pass";
        boolean flag;
        Rect rect;
        int centerX = mDevice.getDisplayWidth()/2;
        XLog.i("屏幕中心横坐标："+ centerX);
        mDevice.executeShellCommand("am start -n com.xiaomi.smarthome/.SmartHomeMainActivity -S -W");
        mDevice.wait(Until.findObject(By.res("com.xiaomi.smarthome:id/tv_device_name").text("test")),30000);
        mDevice.findObject(By.res("com.xiaomi.smarthome:id/tv_device_name").text("test")).click();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (true) {
            testCount++;
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);
            row.createCell(1).setCellValue(DateUtils.getSystemTime());
            List<UiObject2> l = mDevice.findObjects(By.clazz("android.view.ViewGroup"));
            rect = l.get(3).getVisibleBounds();  // [237,912][491,1032]
            XLog.i("控件横坐标：" + rect.right);

            l = mDevice.findObjects(By.clazz("android.view.ViewGroup"));
            if(rect.right > centerX) {
                flag=true;
                status = "open";
                if(mDevice.findObject(new UiSelector().resourceId("com.xiaomi.gateway:id/curtain_open_icon")).exists()) {
                    mDevice.findObject(By.res("com.xiaomi.gateway:id/curtain_open_icon")).click();
                }else{
                    rect = l.get(6).getVisibleBounds();
                    XLog.i(rect.left);
                    XLog.i(rect.top);
                    XLog.i(rect.width());
                    XLog.i(rect.height());
                    l.get(6).click();
                }
            }else{
                flag=false;
                status = "close";
                if(mDevice.findObject(new UiSelector().resourceId("com.xiaomi.gateway:id/curtain_close_icon")).exists()) {
                    mDevice.findObject(By.res("com.xiaomi.gateway:id/curtain_close_icon")).click();
                }else{
                    rect = l.get(8).getVisibleBounds();
                    XLog.i(rect.left);
                    XLog.i(rect.top);
                    XLog.i(rect.width());
                    XLog.i(rect.height());
                    l.get(8).click();
                }
            }
            XLog.i(status);
            row.createCell(2).setCellValue(status);
            XLog.i(DateUtils.getSystemTime());
            XLog.i("等待 " + timedelta + "秒");
            try {
                Thread.sleep(timedelta * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            XLog.i(DateUtils.getSystemTime());
            XLog.i("等待结束");
            l = mDevice.findObjects(By.clazz("android.view.ViewGroup"));
            rect = l.get(3).getVisibleBounds();
            if((flag && rect.right < centerX) || (!flag && rect.right > centerX)) {
                result = "pass";
            }else{
                result = "fail";
            }
            XLog.i(result);
            row.createCell(3).setCellValue(result);

            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果保存成功");
        }
    }
}
