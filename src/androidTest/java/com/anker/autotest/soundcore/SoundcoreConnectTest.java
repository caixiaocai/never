package com.anker.autotest.soundcore;

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
 * 连接/断连压力测试
 */
@RunWith(AndroidJUnit4.class)
public class SoundcoreConnectTest {
    private UiDevice mDevice = null;
    private int testCount = 0;
    private int connectCount = 0;
    private int disconnectCount = 0;
    private int errorCount = 0;
    private String floderPath;
    private String filePath;


    @Before
    public void setUp() throws Exception {
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
        if (!mDevice.getCurrentPackageName().equals("com.oceanwing.soundcore")) {
            mDevice.executeShellCommand("am start -n com.oceanwing.soundcore/.activity.SelectCategoryNewActivity -W");
            mDevice.executeShellCommand("am start -n com.oceanwing.soundcore/.activity.WelcomeActivity -W");
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
                waitObjects(new UiSelector[]{new UiSelector().resourceId("com.oceanwing.soundcore:id/st_negative"),
                        new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_bluetooth"),
                        new UiSelector().className("android.widget.TextView").text("An error occured. Please try again.")}, 30000);
                if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/st_negative")).exists()// 断开连接
                        || mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/handle")).exists()) {
                    disconnectCount++;
                    XLog.i(String.format("第【 %d 】次断开连接", disconnectCount));
                    row.createCell(1).setCellValue(disconnectCount);
                    row.createCell(2).setCellValue("disconnect");
                    clickHandle();
                    row.createCell(3).setCellValue(DateUtils.getSystemTime());//断开连接的开始时间
                    if (clickDisconnect()) {
                        row.createCell(5).setCellValue("pass");
                    } else {
                        row.createCell(5).setCellValue("fail");
                    }
                    row.createCell(4).setCellValue(DateUtils.getSystemTime());//断开连接的结束时间
                } else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_bluetooth")).exists()) {// 连接
                    connectCount++;
                    XLog.i(String.format("第【 %d 】次连接", connectCount));
                    row.createCell(1).setCellValue(connectCount);
                    row.createCell(2).setCellValue("connect");
                    clickBle();
                    row.createCell(3).setCellValue(DateUtils.getSystemTime());//连接的开始时间
                    if (connect()) {
                        row.createCell(5).setCellValue("pass");
                    } else {
                        row.createCell(5).setCellValue("fail");
                    }
                    cancleUpdate();
                    row.createCell(4).setCellValue(DateUtils.getSystemTime());//连接的结束时间
                } else if (mDevice.findObject(new UiSelector().className("android.widget.TextView").text("An error occured. Please try again.")).exists()) {//异常
                    errorCount++;
                    XLog.i(String.format("第【 %d 】次出现error", errorCount));
                    row.createCell(1).setCellValue(errorCount);
                    row.createCell(2).setCellValue("An error occured");
                    row.createCell(3).setCellValue(DateUtils.getSystemTime());
                    mDevice.takeScreenshot(new File(floderPath + testCount + "_" + DateUtils.getDateTime() + ".png"));
                } else {
                    XLog.i("界面异常");
                    row.createCell(2).setCellValue("界面异常");
                    row.createCell(3).setCellValue(DateUtils.getSystemTime());
                    mDevice.takeScreenshot(new File(floderPath + testCount + "_" + DateUtils.getDateTime() + ".png"));
                }
            } catch (Exception e) {
                XLog.e(e.toString());
                row.createCell(6).setCellValue(e.toString());
                mDevice.takeScreenshot(new File(floderPath + testCount + "_" + DateUtils.getDateTime() + ".png"));
            } finally {
                FileOutputStream os = new FileOutputStream(filePath);
                wb.write(os);
                os.close();
                wb.close();
                XLog.i("结果写入完成");
            }
        }
    }

    /**
     * 等待控件元素
     *
     * @param selectors
     * @param timeout
     * @return
     */
    private boolean waitObjects(UiSelector[] selectors, long timeout) {
        Date start = new Date();
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (UiSelector selector : selectors) {
                if (mDevice.findObject(selector).exists()) {
                    return true;
                }
            }
            Date end = new Date();
            long time = end.getTime() - start.getTime();
            if (time > timeout) {
                return false;
            }
        }
    }

    /***
     * 点击more
     */
    private void clickHandle() {
        while (true) {
            if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/st_negative")).exists()) {
                try {
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/st_negative")).clickAndWaitForNewWindow();
                } catch (UiObjectNotFoundException e) {
                    XLog.e(e.toString());
                }
            }
            if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/handle")).exists()) {
                try {
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/handle")).clickAndWaitForNewWindow();
                    return;
                } catch (UiObjectNotFoundException e) {
                    XLog.e(e.toString());
                }
            } else {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    XLog.e(e.toString());
                }
            }
        }
    }

    /***
     * 点击断开连接
     * @throws UiObjectNotFoundException
     */
    private boolean clickDisconnect() throws UiObjectNotFoundException {
        mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/tv_disconnect")), 10000);
        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/tv_disconnect")).clickAndWaitForNewWindow();
        mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/rightBtn")), 30000);
        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/rightBtn")).click();
        mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/st_positive")), 10000);
        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/st_positive")).clickAndWaitForNewWindow();
        mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/sb_bluetooth")), 30000);
        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_bluetooth")).exists()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 点击打开蓝牙按钮
     *
     * @throws UiObjectNotFoundException
     */
    private void clickBle() throws UiObjectNotFoundException {
        mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/sb_bluetooth")), 30000);
        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_bluetooth")).clickAndWaitForNewWindow();
    }

    /***
     * 点击蓝牙连接
     * @throws UiObjectNotFoundException
     */
    private boolean connect() throws UiObjectNotFoundException {
        int i = 0;
        do {
            i++;
            mDevice.wait(Until.findObject(By.res("android:id/title").text("Soundcore Life NC")), 30000);
            mDevice.findObject(new UiSelector().resourceId("android:id/title").text("Soundcore Life NC")).click();
            if (mDevice.findObject(new UiSelector().resourceId("android:id/button2")).exists()) {
                mDevice.findObject(By.res("android:id/button2")).click();
            }
        } while (!waitBleConnect(60000) && i < 10);
        mDevice.pressBack();
        return checkConnect();
    }

    /**
     * 判断连接是否成功
     *
     * @return
     */
    private boolean checkConnect() {
        waitObjects(new UiSelector[]{new UiSelector().resourceId("com.oceanwing.soundcore:id/handle"),
                new UiSelector().resourceId("com.oceanwing.soundcore:id/tv_title").text("Update Firmware"),
                new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_bluetooth"),
                new UiSelector().className("android.widget.TextView").text("An error occured. Please try again.")}, 30000);
        UiObject handle = mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/handle"));
        UiObject update = mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/tv_title").text("Update Firmware"));
        UiObject ble = mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_bluetooth"));
        UiObject error = mDevice.findObject(new UiSelector().className("android.widget.TextView").text("An error occured. Please try again."));
        if (handle.exists() || update.exists()) {
            return true;
        } else if (ble.exists() || error.exists()) {
            return false;
        } else {
            return false;
        }
    }

    /**
     * 等待蓝牙连接完成
     *
     * @param timeout
     * @return
     */
    private boolean waitBleConnect(long timeout) {
        Date start = new Date();
        while (true) {
            while (mDevice.findObject(new UiSelector().resourceId("android:id/summary").text("Connecting…")).exists()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mDevice.findObject(new UiSelector().resourceId("android:id/summary").text("已连接")).exists()
                    || mDevice.findObject(new UiSelector().resourceId("android:id/summary").text("Connected")).exists()) {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mDevice.findObject(new UiSelector().resourceId("android:id/summary").text("已连接")).exists()
                        || mDevice.findObject(new UiSelector().resourceId("android:id/summary").text("Connected")).exists()) {
                    return true;
                }
            }
            Date end = new Date();
            long time = end.getTime() - start.getTime();
            if (time > timeout) {
                return false;
            }
        }
    }

    /**
     * 弹出升级提示框，点掉
     *
     * @throws UiObjectNotFoundException
     */
    private void cancleUpdate() throws UiObjectNotFoundException {
        mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/tv_title").text("Update Firmware")), 10000);
        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/tv_title").text("Update Firmware")).exists()) {
            mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/st_positive")).clickAndWaitForNewWindow();
            mDevice.pressBack();
        }
    }
}

