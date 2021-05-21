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

/***
 * 测试floodlight切换灯光成功率
 */
@RunWith(AndroidJUnit4.class)
public class LiveSwitchLightTest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0;
    private String PLAY_BTN="";
    private boolean isON;
    private int onCount=0, offCount=0;

    private final String ON = "ON";
    private final String OFF = "OFF";
    private final String PASS = "pass";
    private final String FAIL = "fail";

    private int waitTime = 3;

    private String LIGHT_BTN = "com.oceanwing.battery.cam:id/iv_light_switch";

    private String[] LINE_BTN = {"com.oceanwing.battery.cam:id/item_single_camera_img_live",
            "com.oceanwing.battery.cam:id/iv_live"};

    private String REFRESH_BTN = "com.oceanwing.battery.cam:id/item_single_camera_txt_retry";

    @Before
    public void setUp() throws Exception {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");

        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);

        Bundle extras = InstrumentationRegistry.getArguments();
        if (extras.containsKey("waitTime")) {
            waitTime = Integer.parseInt(extras.getString("waitTime"));
        }

        XLog.i(String.format("waitTime: %s", waitTime));

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);
        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("次数");
            row.createCell(2).setCellValue("操作");
            row.createCell(3).setCellValue("开始时间");
            row.createCell(4).setCellValue("结束时间");
            row.createCell(5).setCellValue("结果");
            row.createCell(6).setCellValue("耗时");
            row.createCell(7).setCellValue("waitTime=" + waitTime);
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
        int x = mDevice.getDisplayWidth()/2;
        int height = mDevice.getDisplayHeight();
        long start, end, del;
        XLog.i("start test");
        mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W");
        mDevice.wait(Until.findObject(By.text("eufySecurity")), 30000);

        int length = LINE_BTN.length;
        for(int i=0; i<length; i++) {
            if (mDevice.hasObject(By.res(LINE_BTN[i]))) {
                PLAY_BTN = LINE_BTN[i];
                break;
            }
        }
        mDevice.findObject(By.res(PLAY_BTN)).click();
        if(!mDevice.wait(Until.hasObject(By.res(LIGHT_BTN)), 5000)) {
            mDevice.swipe(x, height - 3, x, height / 2, 50);
        }
        mDevice.wait(Until.findObject(By.res(LIGHT_BTN).clickable(true)), 120000);

        while (true) {
            testCount++;
            XLog.i(String.format("第【 %d 】次测试", testCount));
            XLog.i("检查出流状态");
            while(true) {
                if (mDevice.hasObject(By.res("com.oceanwing.battery.cam:id/camera_preview_video_btn_retry"))) {
                    mDevice.findObject(By.res("com.oceanwing.battery.cam:id/camera_preview_video_btn_retry")).click();
                }else if (mDevice.hasObject(By.res("com.oceanwing.battery.cam:id/item_super_video_preview_remind"))) {
                    mDevice.wait(Until.gone(By.res("com.oceanwing.battery.cam:id/item_super_video_preview_remind")), 60000);
                }else if(mDevice.hasObject(By.text("Preparing to play..."))){
                    mDevice.wait(Until.gone(By.text("Preparing to play...")), 60000);
                }else if(!mDevice.hasObject(By.res(LIGHT_BTN))){
                    mDevice.swipe(x, height-3, x, height/2, 50);
                } else if(mDevice.hasObject(By.res(LIGHT_BTN).clickable(true))){
                    break;
                }
            }
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);

            if(mDevice.findObject(By.res(LIGHT_BTN)).isSelected()){ // 已经是打开状态，执行关闭操作
                offCount++;
                XLog.i(String.format("关闭 %d", offCount));
                isON = false;
                row.createCell(1).setCellValue(offCount);
                row.createCell(2).setCellValue(OFF);
            }else{ // 已经是关闭状态，执行打开操作
                onCount++;
                XLog.i(String.format("打开 %d", onCount));
                isON = true;
                row.createCell(1).setCellValue(onCount);
                row.createCell(2).setCellValue(ON);
            }
            XLog.i("点击");
            mDevice.findObject(By.res(LIGHT_BTN)).click();
            start = System.currentTimeMillis();
            row.createCell(3).setCellValue(DateUtils.getSystemTime(start));

            while(true){
                if(mDevice.wait(Until.hasObject(By.res(LIGHT_BTN)), 60000)){
                    break;
                }
            }
            end = System.currentTimeMillis();
            row.createCell(4).setCellValue(DateUtils.getSystemTime(end));
            del = end-start;
            row.createCell(6).setCellValue(del/1000 + " s "+ del%1000 + " ms");
            mDevice.wait(Until.hasObject(By.res(LIGHT_BTN)), 10000);
            boolean selected = mDevice.findObject(By.res(LIGHT_BTN)).isSelected();
            if(selected==isON && !mDevice.hasObject(By.res("com.oceanwing.battery.cam:id/tip_top").text("Failed to switch lights."))){
                XLog.i("成功");
                row.createCell(5).setCellValue(PASS);
                Thread.sleep(1000 * waitTime);
            }else{
                XLog.i("失败");
                row.createCell(5).setCellValue(FAIL);
                String screenshotFile = floderPath + testCount+ "_switch_lights_failed_"+ DateUtils.getDateTime() + ".png";
                mDevice.takeScreenshot(new File(screenshotFile));
                if(mDevice.hasObject(By.res("com.oceanwing.battery.cam:id/tips_close"))){
                    mDevice.findObject(By.res("com.oceanwing.battery.cam:id/tips_close")).click();
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
