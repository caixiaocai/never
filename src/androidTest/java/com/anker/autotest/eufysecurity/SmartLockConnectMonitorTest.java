package com.anker.autotest.eufysecurity;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
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
public class SmartLockConnectMonitorTest {
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
            row.createCell(3).setCellValue("自动回连");
            row.createCell(4).setCellValue("点击Retry次数");

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
        int clickCount;
        XLog.i("start test");

        while (true) {
            if (!mDevice.getCurrentPackageName().equals("com.oceanwing.battery.cam")) {
                mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W");
                mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/itemSmartLockRoot")), 15000);
            } else if(mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/itemSmartLockRoot")).exists()){
                mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/itemSmartLockRoot")).clickAndWaitForNewWindow();
            }else if(mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/imgSetting")).exists()){
                break;
            }else {
                mDevice.pressBack();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while (true) {
            clickCount = 0;
            if (!mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/esl_activity_on_off_lock_tv_lock_status")).exists()) {
                testCount++;
                XLog.i(String.format("第【 %d 】次断连", testCount));
                HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
                HSSFSheet sheet = wb.getSheetAt(0);
                HSSFRow row = sheet.createRow(testCount);
                row.createCell(0).setCellValue(testCount);
                row.createCell(1).setCellValue(DateUtils.getSystemTime());
                row.createCell(2).setCellValue("disconnect");

                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/esl_activity_on_off_lock_tv_lock_status")), 30000);
                if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/esl_activity_on_off_lock_tv_lock_status")).exists()) {
                    XLog.i("自动回连成功");
                    row.createCell(3).setCellValue("pass");
                }else{
                    XLog.i("自动回连失败");
                    row.createCell(3).setCellValue("fail");
                    while(true) {
                        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/imgFail")).exists()) {  // Retry
                            clickCount ++;
                            XLog.i("点击Retry");
                            mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/imgFail")).click();
                            mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/esl_activity_on_off_lock_tv_lock_status")), 15000);
                        }
                        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/arcView")).exists()
                                || mDevice.findObject(new UiSelector().text("Connecting the device")).exists()) {
                            XLog.i("正在连接");
                            mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/esl_activity_on_off_lock_tv_lock_status")), 5000);
                        }
                        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/esl_activity_on_off_lock_tv_lock_status")).exists()){
                            row.createCell(4).setCellValue(clickCount);
                            break;
                        }
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
}
