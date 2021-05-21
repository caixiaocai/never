package com.anker.autotest.eufysecurity;

import android.os.Bundle;

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
 * smartlock的ota升级
 */
@RunWith(AndroidJUnit4.class)
public class SmartLock8510OtaTest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0;
    private int checkupdatetimeout = 30;
    private int updatetimeout = 180;
//    private int reconnecttimeout = 60;
//    private boolean restart = false;

    @Before
    public void setUp() throws Exception {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);

        Bundle extras = InstrumentationRegistry.getArguments();
        if (extras.containsKey("checkupdatetimeout")) {
            checkupdatetimeout = Integer.parseInt(extras.getString("checkupdatetimeout"));
        }
        if (extras.containsKey("updatetimeout")) {
            updatetimeout = Integer.parseInt(extras.getString("updatetimeout"));
        }
//        if (extras.containsKey("reconnecttimeout")) {
//            reconnecttimeout = Integer.parseInt(extras.getString("reconnecttimeout"));
//        }
//        if (extras.containsKey("restart")) {
//            restart = Boolean.parseBoolean(extras.getString("restart"));
//        }
        XLog.i(String.format("updatetimeout: %d", updatetimeout));
//        XLog.i(String.format("reconnecttimeout: %d", reconnecttimeout));
//        XLog.i(String.format("restart: %s", restart));

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("检测新版本");
            row.createCell(2).setCellValue("开始时间");
            row.createCell(3).setCellValue("结束时间");
            row.createCell(4).setCellValue("结果");
            row.createCell(5).setCellValue("下载耗时");
            row.createCell(6).setCellValue("传输耗时");
            row.createCell(7).setCellValue("共耗时");
            row.createCell(8).setCellValue("自动回连");
            row.createCell(9).setCellValue("checkupdatetimeout=" + checkupdatetimeout);
            row.createCell(10).setCellValue("updatetimeout=" + updatetimeout);
//            row.createCell(11).setCellValue("reconnecttimeout=" + reconnecttimeout);
//            row.createCell(12).setCellValue("restart=" + restart);
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
    public void test() throws IOException, UiObjectNotFoundException, InterruptedException {
        long time_start=0;
        boolean checkFlag;
        int x = mDevice.getDisplayWidth()/2;
        int height = mDevice.getDisplayHeight();
        XLog.i("start test");

        mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");
        mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/itemSmartLockRoot")), 15000);
        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/itemSmartLockRoot")).click();

        while (true) {
            if(mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/esl_activity_on_off_lock_tv_lock_status")).exists()){
                break;
            } else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/imgFail")).exists()) {
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/imgFail")).click();
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/imgSetting")).clickAndWaitForNewWindow();

        while (true) {

            testCount++;
            XLog.i(String.format("第【 %d 】次测试", testCount));
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);

            mDevice.swipe(x, height /5 * 4,x,height/5,100);
            mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/ll_esl_Firmware_Update")), 3000);
            mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/ll_esl_Firmware_Update")).clickAndWaitForNewWindow();
            mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/esl_view_update_firmware_need_update_tv_upgrade")), checkupdatetimeout*1000);
            checkFlag = false;
            for(int i=0; i<10; i++){
                if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/esl_view_update_firmware_check_fail_img_fail_retry")).exists()) {
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/esl_view_update_firmware_check_fail_img_fail_retry")).click();
                    mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/esl_view_update_firmware_need_update_tv_upgrade")), 60000);
                }else if(mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/esl_view_update_firmware_need_update_tv_upgrade")).exists()){
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/esl_view_update_firmware_need_update_tv_upgrade")).click();
                    mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/esl_view_update_firmware_updating_progress_bar")), 60000);
                    if(mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/esl_view_update_firmware_updating_progress_bar")).exists()){
                        XLog.i("检测新版本成功");
                        row.createCell(1).setCellValue("pass");
                        time_start = System.currentTimeMillis();
                        row.createCell(2).setCellValue(DateUtils.getSystemTime(time_start));
                        checkFlag = true;
                        break;
                    }
                }

            }
            if (checkFlag) {
//                for(int i=0; i<10; i++) {
//                    if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/esl_view_update_firmware_need_update_tv_upgrade")).exists()) {
//                        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/esl_view_update_firmware_need_update_tv_upgrade")).click();
//                        Thread.sleep(3000);
//                    }
//                }
//                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/esl_view_update_firmware_updating_progress_bar")), 120000);
//                time_start = System.currentTimeMillis();
//                row.createCell(2).setCellValue(DateUtils.getSystemTime(time_start));
                XLog.i("等待升级");
                while(true) {
                    if(System.currentTimeMillis()-time_start > updatetimeout * 1000){
                        XLog.i("升级超时");
                        row.createCell(4).setCellValue("timeout");
                        break;
                    }
                    if(mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/esl_view_update_firmware_update_fail_tv_try")).exists()){
                        XLog.i("升级失败");
                        row.createCell(4).setCellValue("fail");
                        break;
                    }
                    if(mDevice.findObject(new UiSelector().text("Updating")).exists()){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                XLog.i("检测新版本失败");
                row.createCell(1).setCellValue("fail");
            }
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");

            for (int i = 0; i < 3; i++) {
                if (!mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/ll_esl_Firmware_Update")).exists()) {
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/img_back")).clickAndWaitForNewWindow();
                    mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/ll_esl_Firmware_Update")), 5000);
                } else {
                    break;
                }
            }


//            if(restart) {
//                mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");
//            }else{
//                for (int i = 0; i < 3; i++) {
//                    if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/img_back")).exists()) {
//                        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/img_back")).clickAndWaitForNewWindow();
//                    } else {
//                        break;
//                    }
//                }
//            }
        }
    }
}
