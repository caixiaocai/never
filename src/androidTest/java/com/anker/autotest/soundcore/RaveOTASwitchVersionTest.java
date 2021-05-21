package com.anker.autotest.soundcore;

import android.os.Bundle;
import android.os.Environment;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import com.anker.autotest.utils.Common;
import com.anker.autotest.utils.DateUtils;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

/***
 * Rave ota测试，可以测试Rave Mini
 */
@RunWith(AndroidJUnit4.class)
public class RaveOTASwitchVersionTest {
    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0;
    private int count = 0;
    private int updatetimeout = 1800;
    private int restarttimeout = 30;
    private int idletime = 5;

    @Before
    public void setUp() throws Exception {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");

        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);

        Bundle extras = InstrumentationRegistry.getArguments();
        if (extras.containsKey("updatetimeout")) {
            updatetimeout = Integer.parseInt(extras.getString("updatetimeout"));
        }
        if (extras.containsKey("restarttimeout")) {
            restarttimeout = Integer.parseInt(extras.getString("restarttimeout"));
        }
        if (extras.containsKey("idletime")) {
            idletime = Integer.parseInt(extras.getString("idletime"));
        }
        XLog.i(String.format("updatetimeout: %s", updatetimeout));
        XLog.i(String.format("restarttimeout: %s", restarttimeout));
        XLog.i(String.format("idletime: %s", idletime));

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("测试次数");
            row.createCell(1).setCellValue("开始时间");
            row.createCell(2).setCellValue("检测新版本");
            row.createCell(3).setCellValue("下载");
            row.createCell(4).setCellValue("升级");
            row.createCell(5).setCellValue("结束时间");
            row.createCell(6).setCellValue("接口返回的版本号");
            row.createCell(7).setCellValue("升级前app上显示版本号");
            row.createCell(8).setCellValue("升级后app上显示版本号");
            row.createCell(9).setCellValue("E and (H != I)");
            row.createCell(10).setCellValue("升级前的版本号+接口返回版本号");
            row.createCell(12).setCellValue("updatetimeout=" + updatetimeout);
            row.createCell(13).setCellValue("restarttimeout=" + restarttimeout);
            row.createCell(14).setCellValue("idletime=" + idletime);
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

    private String switch_version(String account, String conf1, String conf2) throws JSONException {
        String conf;
        if(count%2==1){
            conf = conf1;
            XLog.i("conf1");
        }else{
            conf = conf2;
            XLog.i("conf2");
        }
        CookieManager manager = new CookieManager();
        //设置cookie策略，只接受与你对话服务器的cookie，而不接收Internet上其它服务器发送的cookie
        manager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        CookieHandler.setDefault(manager);
        Common.doPost("https://omc-ci.eufylife.com/omc/session/login", account);
        String res = Common.doPost("https://omc-ci.eufylife.com/omc/upgrade/save_package", conf);
        XLog.i(res);
        if(res==null){
            res = "{\"url\":\"error\"}";
        }
        JSONObject j = new JSONObject(res);
        String url = (String)j.get("url");
        if(url.contains("/")){
            return url.substring(url.lastIndexOf("/")+1);
        }
        return url;
    }

    @Test
    public void otaTest() throws Exception {
        boolean flag = true;
        String response, beforeVersion, afterVersion, getText;
        int x = mDevice.getDisplayWidth()/2;
        int y_start = mDevice.getDisplayHeight()/5*4;
        int y_end = mDevice.getDisplayHeight()/2;
        XLog.i("start test");
        response = "";

        String account = Common.readJsonFile(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "account.json");
        String conf1 = Common.readJsonFile(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "conf1.json");
        String conf2 = Common.readJsonFile(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "conf2.json");

        mDevice.executeShellCommand("am start -n com.oceanwing.soundcore/.activity.SelectCategoryNewActivity -W");
        mDevice.executeShellCommand("am start -n com.oceanwing.soundcore/.activity.WelcomeActivity -W");
        while (true) {
            testCount++;

            XLog.i(String.format("第【 %d 】次测试", testCount));

            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);
            if (flag) {
                response = switch_version(account, conf1, conf2);
                XLog.i(response);
                row.createCell(6).setCellValue(response);
            }
            flag = false;

            try {
                if(mDevice.wait(Until.hasObject(By.text("Connect")), 5000)){
                    mDevice.findObject(By.text("Connect")).click();
                }
                mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/st_negative").text("Cancel")), 10000);
                if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/st_negative").text("Cancel")).exists()) {
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/st_negative").text("Cancel")).clickAndWaitForNewWindow();
                }
                mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/linear_right3")).clickAndWaitForNewWindow();
                mDevice.wait(Until.findObject(By.textStartsWith("Firmware Version")), 10000);
                getText = mDevice.findObject(By.textStartsWith("Firmware Version")).getText();
                beforeVersion = getText.split(":")[1];
                XLog.i(beforeVersion.trim());
                row.createCell(7).setCellValue(beforeVersion);
                mDevice.swipe(x, y_start, x, y_end, 500);
                mDevice.findObject(By.text("Update Firmware")).click();

                mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/sb_download")), 30000);
                row.createCell(1).setCellValue(DateUtils.getSystemTime());
                if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download")).exists()) {//有新版本
                    XLog.i("有新版本");
                    if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download").text("Download")).exists()) {
                        row.createCell(2).setCellValue("pass");
                    }
                    while (true) {
                        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download").text("Download")).exists()) {
                            mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download").text("Download")).clickAndWaitForNewWindow();
                            mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/sb_download").text("Install")), 60000);
                        } else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/progress")).exists()) {
                            mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/sb_download").text("Install")), 60000);
                        } else {
                            break;
                        }
                    }

                    if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download").text("Install")).exists()) {//下载成功
                        XLog.i("下载成功");
                        row.createCell(3).setCellValue("pass");
                        mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download").text("Install")).clickAndWaitForNewWindow();
                        for (int i = 0; i < updatetimeout; i++) {
                            if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download").text("Install")).exists()) {
                                mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/sb_download").text("Install")).clickAndWaitForNewWindow();
                            } else if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/progress")).exists()
                                    || mDevice.findObject(new UiSelector().text("Installing...")).exists()) {
                                Thread.sleep(1000);
                            } else {
                                break;
                            }
                        }
                        mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/frame_install_complete")), 15000);
                        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/frame_install_complete")).exists()) {//升级完成
                            flag = true;
                            count++;
                            XLog.i("升级成功");
                            row.createCell(4).setCellValue("pass");
                            row.createCell(5).setCellValue(DateUtils.getSystemTime());
                            UiObject2 done = mDevice.findObject(By.res("com.oceanwing.soundcore:id/frame_install_complete"));
                            done.wait(Until.clickable(true), 60000);
                            mDevice.findObject(By.res("com.oceanwing.soundcore:id/frame_install_complete")).click();
                            Thread.sleep(restarttimeout * 1000);
//                            mDevice.wait(Until.findObject(By.res("com.oceanwing.soundcore:id/linear_right3")), 60000);
                            if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/st_negative").text("Cancel")).exists()) {
                                mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/st_negative").text("Cancel")).clickAndWaitForNewWindow();
                            }
                            mDevice.findObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/linear_right3")).clickAndWaitForNewWindow();
                            mDevice.wait(Until.findObject(By.textStartsWith("Firmware Version")), 10000);
                            getText = mDevice.findObject(By.textStartsWith("Firmware Version")).getText();
                            mDevice.pressBack();
                            afterVersion = getText.split(":")[1];
                            XLog.i(afterVersion);
                            row.createCell(8).setCellValue(afterVersion);
                            if(beforeVersion.equals(afterVersion)){
                                row.createCell(9).setCellValue("fail");
                            }else{
                                row.createCell(9).setCellValue("pass");
                            }
                            row.createCell(10).setCellValue(beforeVersion+"_"+response);

                        } else {//install失败
                            XLog.i("升级失败");
                            row.createCell(4).setCellValue("fail");
                            row.createCell(5).setCellValue(DateUtils.getSystemTime());
                        }
                    } else {//下载失败
                        XLog.i("下载失败");
                        row.createCell(3).setCellValue("fail");
                    }
                } else {//没有新版本
                    XLog.i("没有新版本");
                    row.createCell(2).setCellValue("fail");
                    mDevice.takeScreenshot(new File(floderPath + testCount + "_" + DateUtils.getDateTime() + ".png"));
                    mDevice.executeShellCommand("am start -n com.oceanwing.soundcore/.activity.SelectCategoryNewActivity -W -S");
                    mDevice.executeShellCommand("am start -n com.oceanwing.soundcore/.activity.WelcomeActivity -W");
                }
                if(mDevice.hasObject(By.text("Back"))){
                    mDevice.findObject(By.text("Back")).click();
                }
                if(mDevice.wait(Until.hasObject(By.textStartsWith("Firmware Version")), 3000)){
                    mDevice.pressBack();
                }
//                mDevice.executeShellCommand("am force-stop com.oceanwing.soundcore");
                Thread.sleep(idletime * 1000);
            } catch (Exception e) {
                XLog.e(e.toString());
            } finally {
                FileOutputStream os = new FileOutputStream(filePath);
                wb.write(os);
                os.close();
                wb.close();
                XLog.i("结果写入完成");
            }
        }
    }
}