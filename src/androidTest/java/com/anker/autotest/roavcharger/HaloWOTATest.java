package com.anker.autotest.roavcharger;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
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
import java.util.Date;
import java.util.List;

/***
 * HaloW项目ota升级
 */
@RunWith(AndroidJUnit4.class)
public class HaloWOTATest {
    private UiDevice mDevice = null;
    private int count = 0;
    private String floderPath;
    private String filePath;
    private int updatetimeout;

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
        XLog.i(String.format("updatetimeout: %s", updatetimeout));

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);
        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("开始时间");
            row.createCell(2).setCellValue("检测新版本");
            row.createCell(3).setCellValue("Update");
            row.createCell(4).setCellValue("结束时间");
            row.createCell(5).setCellValue("updatetimeout=" + updatetimeout);
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
    public void test() throws UiObjectNotFoundException, IOException, InterruptedException {
        boolean isMain, connect, setting, update, result;
        XLog.i("start test");
//        registerEnjoyUiWatcher();
//        mDevice.runWatchers();
        if (!mDevice.getCurrentPackageName().equals("com.chipsguide.app.roav.fmplayer")) {
            if (!mDevice.getCurrentPackageName().equals(mDevice.getLauncherPackageName())) {
                mDevice.pressBack();
                mDevice.pressBack();
            }
            mDevice.executeShellCommand("am start -n com.chipsguide.app.roav.fmplayer/.activity.SplashActivity -W");
            mDevice.waitForWindowUpdate("com.chipsguide.app.roav.fmplayer", 10000);
        }
        clickEnjoyPopup(10000);
        clickUpdatePopup(10000);
        while (true) {
            count++;
            XLog.i(String.format("第【 %d 】次测试", count));
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(count);
            row.createCell(0).setCellValue(count);

            isMain = isInMainActivity();

            if (isMain) {
                XLog.i("app页面在主页面，开始新一轮的ota测试");
                //判断蓝牙是否连接状态
                connect = isBluetoothConnected();
                while (!connect) {
                    Thread.sleep(10000);
                    XLog.i("蓝牙重新连接");

                    if (isBluetoothConnected()) {
                        break;
                    }
                }
                setting = clickSettingPage();
                while (!setting) {
                    Thread.sleep(5000);
                    if (clickSettingPage()) {
                        break;
                    }
                    XLog.i("再次滑动至setting页面");
                }
                UiObject fiveText = null;
                if (mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/ota_update_container")).exists()) {
                    fiveText = mDevice.findObject(new UiSelector()
                            .resourceId("com.chipsguide.app.roav.fmplayer:id/ota_update_container"));
                    fiveText.clickAndWaitForNewWindow();
                    update = waitForUiObjectText("com.chipsguide.app.roav.fmplayer:id/bt_update_device",
                            "Update Device", 30000);
                } else {
                    fiveText = mDevice.findObject(new UiSelector()
                            .resourceId("com.chipsguide.app.roav.fmplayer:id/rl_hardware_version"));
                    fiveText.clickAndWaitForNewWindow();
                    if (mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/update_now_tv").text("Update Now")).exists()) {
                        UiObject updateNow = mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/update_now_tv").text("Update Now"));
                        updateNow.clickAndWaitForNewWindow();
                    }
                    update = waitForUiObjectText("com.chipsguide.app.roav.fmplayer:id/tv_btn",
                            "UPDATE NOW", 30000);
                }

                if (update) {
                    UiObject it = null;
                    if (mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/bt_update_device")).exists()) {
                        it = mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/bt_update_device"));
                    } else {
                        it = mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/tv_btn").text("UPDATE NOW"));
                    }
                    if (!it.exists()) {
                        XLog.i("未找到新版本");
                        row.createCell(1).setCellValue(DateUtils.getSystemTime());
                        row.createCell(2).setCellValue("未找到新版本");
                    } else {
                        it.clickAndWaitForNewWindow();
                        row.createCell(1).setCellValue(DateUtils.getSystemTime());
                        row.createCell(2).setCellValue("pass");
                        Thread.sleep(5000);
                        result = waitForUiObjectText5(updatetimeout);//Update Successfully
                        if (result) {
                            XLog.i("测试成功");
                            row.createCell(3).setCellValue("pass");
                        } else {
                            XLog.i("测试失败");
                            row.createCell(3).setCellValue("fail");
                        }
                        row.createCell(4).setCellValue(DateUtils.getSystemTime());
                        Thread.sleep(1000);
                    }
                    for (int i = 0; i < 5; i++) {
                        if (mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/bt_update_device")).exists()
                                || mDevice.findObject(new UiSelector().text("Update Successfully")).exists()
                                || mDevice.findObject(new UiSelector().text("Update Complete")).exists()) {
                            mDevice.pressBack();
                            Thread.sleep(1000);
                        } else {
                            break;
                        }
                    }
                    XLog.i("Thread.sleep 30秒");
                    Thread.sleep(30000);
                    if (mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/tv_later").text("Later")).exists()) {
                        UiObject later = mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/tv_later").text("Later"));
                        later.clickAndWaitForNewWindow();
                    }
                    clickConnectPage();
                } else {
                    XLog.i("未找到新版本");
                    row.createCell(1).setCellValue(DateUtils.getSystemTime());
                    row.createCell(2).setCellValue("未找到新版本");
                    mDevice.pressBack();
                }
            }
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            XLog.i("结果写入完成");
            killAppAndOpen();
            Thread.sleep(3000);
        }
    }

    private boolean isBluetoothConnected() throws UiObjectNotFoundException {
        clickUpdatePopup(10000);
        if (mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/charge_connection_button")
                .className("android.widget.Button")).exists()) {
            UiObject connectButton = mDevice.findObject(new UiSelector()
                    .resourceId("com.chipsguide.app.roav.fmplayer:id/charge_connection_button")
                    .className("android.widget.Button"));
            String text = connectButton.getText().trim();
            if (text.equals("Connect")) {
                connectButton.click();
                return false;
            } else {
                return true;
            }
        } else if (mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/tv_has_connected")).exists()) {
            return true;
        } else {
            UiObject connectButton = mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/connect_device_pb"));
            connectButton.click();
            clickUpdatePopup(30000);
            return false;
        }
    }

    private void killAppAndOpen() throws IOException, InterruptedException {
        mDevice.pressHome();
        mDevice.executeShellCommand("am force-stop com.chipsguide.app.roav.fmplayer");
        Thread.sleep(2000);
        mDevice.executeShellCommand("am start -n com.chipsguide.app.roav.fmplayer/.activity.SplashActivity -W");
        mDevice.waitForWindowUpdate("com.chipsguide.app.roav.fmplayer", 60000);
    }

    private boolean isInMainActivity() throws UiObjectNotFoundException {
        if (mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/back_iv")).exists()) {
            mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/back_iv")).clickAndWaitForNewWindow();
        }
        UiObject connectButton = mDevice.findObject(new UiSelector()
                .resourceId("com.chipsguide.app.roav.fmplayer:id/charge_connection_button")
                .className("android.widget.Button"));
        if (connectButton.exists() && connectButton.isEnabled()) {
            return true;
        }
        UiObject hasConnected = mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/tv_has_connected"));
        if (hasConnected.exists()) {
            return true;
        }
        UiObject connectDevice = mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/connect_device_pb"));
        if (connectDevice.exists()) {
            return true;
        }
        return clickConnectPage();
    }

    private boolean clickConnectPage() throws UiObjectNotFoundException {
        if (!(mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/tab_equipment_rb")).exists())) {
            if (!(mDevice.findObject(new UiSelector().className("android.support.v7.app.ActionBar$Tab")).exists())) {
                if (!mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/home_custom_tab_icon")).exists()) {
                    clickUpdatePopup(3000);
                }
            }
        }
        if (mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/tab_equipment_rb")).exists()) {
            UiObject btn = mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/tab_equipment_rb"));
            btn.click();
            UiObject hasConnected = mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/tv_has_connected"));
            UiObject connectDevice = mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/connect_device_pb"));
            return (hasConnected.exists() || connectDevice.exists());
        } else if (mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/home_custom_tab_icon")).exists()) {
            UiObject btn = mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/home_custom_tab_icon"));
            btn.click();
            UiObject connectDevice = mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/charge_connection_button"));
            return (connectDevice.exists());
        } else if (mDevice.findObject(new UiSelector().className("android.support.v7.app.ActionBar$Tab")).exists()) {
//            UiObject tabLayout = mDevice.findObject(new UiSelector()
//                    .resourceId("com.chipsguide.app.roav.fmplayer:id/tabLayout"));
//            UiObject icon = tabLayout.getChild(new UiSelector().className("android.support.v7.app.ActionBar$Tab").index(0));
            List<UiObject2> tabs = mDevice.findObjects(By.clazz("android.support.v7.app.ActionBar$Tab"));
            UiObject2 icon = tabs.get(0);

            icon.click();
            UiObject connectButton = mDevice.findObject(new UiSelector()
                    .resourceId("com.chipsguide.app.roav.fmplayer:id/charge_connection_button"));
            return connectButton.exists() && connectButton.isEnabled();
        }
        return false;
    }

    private boolean clickSettingPage() throws UiObjectNotFoundException {
        if (mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/tab_setting_rb")).exists()) {
            UiObject btn = mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/tab_setting_rb"));
            btn.click();
            UiObject connectButton = mDevice.findObject(new UiSelector()
                    .resourceId("com.chipsguide.app.roav.fmplayer:id/rl_hardware_version"));
            return connectButton.exists() && connectButton.isEnabled();
        } else {
//            UiObject tabLayout = mDevice.findObject(new UiSelector()
//                    .resourceId("com.chipsguide.app.roav.fmplayer:id/tabLayout"));
//            UiObject icon = tabLayout.getChild(new UiSelector().className("android.support.v7.app.ActionBar$Tab").index(4));
            List<UiObject2> tabs = mDevice.findObjects(By.clazz("android.support.v7.app.ActionBar$Tab"));
            UiObject2 icon = tabs.get(tabs.size() - 1);
            icon.click();
            UiObject connectButton = mDevice.findObject(new UiSelector()
                    .resourceId("com.chipsguide.app.roav.fmplayer:id/ota_update_container"));
            return connectButton.exists() && connectButton.isEnabled();
        }
    }

    public boolean waitForUiObjectText(String rsid, String text, int timeout) {//等待对象出现
        Date start = new Date();
        while (true) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            UiObject it = mDevice.findObject(new UiSelector().resourceId(rsid).text(text));

            if (it.exists()) {
                return true;
            }

            Date end = new Date();
            long time = end.getTime() - start.getTime();
            if (time > timeout) {
                return false;
            }
        }
    }

    private boolean waitForUiObjectText5(int updatetimeout) {//等待对象出现resourceId("com.chipsguide.app.roav.fmplayer:id/tv_update_device_state")
        Date start = new Date();
        UiObject it = mDevice.findObject(new UiSelector().text("Update Successfully"));
        UiObject it2 = mDevice.findObject(new UiSelector().text("Update Complete"));
        UiObject okBtn = mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/tv_btn").text("OK"));

        UiObject fail = mDevice.findObject(new UiSelector().resourceId("android:id/button2"));
        UiObject fail2 = mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/tv_try_later"));
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (fail.exists()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    fail.clickAndWaitForNewWindow();
                } catch (UiObjectNotFoundException e) {
                    e.printStackTrace();
                }
                return false;
            }
            if (fail2.exists()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    fail2.clickAndWaitForNewWindow();
                    mDevice.pressBack();
                } catch (UiObjectNotFoundException e) {
                    e.printStackTrace();
                }
                return false;
            }
//            XLog.i("wait for success ....");
            if (it.exists()) {
                return true;
            }
            if (it2.exists()) {
                try {
                    okBtn.clickAndWaitForNewWindow();
                } catch (UiObjectNotFoundException e) {
                    e.printStackTrace();
                }
                return true;
            }
            Date end = new Date();
            long time = end.getTime() - start.getTime();
            if (time > updatetimeout * 1000) {
                return false;
            }
        }
    }

    private void clickEnjoyPopup(int timeout) throws UiObjectNotFoundException {
        if (waitForUiObjectText("android:id/alertTitle", "Enjoy Roav Charger?", timeout)) {
//        if(mDevice.findObject(new UiSelector().resourceId("android:id/alertTitle").text("Enjoy Roav Charger?")).exists()){
            try {
                UiObject btn = mDevice.findObject(new UiSelector().resourceId("android:id/button2").text("NOT REALLY"));
                btn.click();
                mDevice.wait(Until.hasObject(By.res("android:id/button2").text("NO,THANKS")), 1000);
                if (mDevice.findObject(new UiSelector().resourceId("android:id/button2").text("NO,THANKS")).exists()) {
                    UiObject btn2 = mDevice.findObject(new UiSelector().resourceId("android:id/button2").text("NO,THANKS"));
                    btn2.click();
                }
            } catch (UiObjectNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void clickUpdatePopup(int timeout) {
        if (waitForUiObjectText("com.chipsguide.app.roav.fmplayer:id/tv_later", "Later", timeout)) {
            try {
                UiObject later = mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/tv_later").text("Later"));
                later.clickAndWaitForNewWindow();
            } catch (UiObjectNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

//    private void registerEnjoyUiWatcher(){
//        /**
//         * Enjoy Roav Charger
//         */
//        mDevice.registerWatcher("enjoy",new UiWatcher() {
//            @Override
//            public boolean checkForCondition() {
//                if(mDevice.findObject(new UiSelector().resourceId("android:id/alertTitle").text("Enjoy Roav Charger?")).exists()){
//                    try {
//                        UiObject btn = mDevice.findObject(new UiSelector().resourceId("android:id/button2").text("NOT REALLY"));
//                        btn.click();
//                        mDevice.wait(Until.hasObject(By.res("android:id/button2").text("NO,THANKS")),1000);
//                        if(mDevice.findObject(new UiSelector().resourceId("android:id/button2").text("NO,THANKS")).exists()){
//                            UiObject btn2 = mDevice.findObject(new UiSelector().resourceId("android:id/button2").text("NO,THANKS"));
//                            btn2.click();
//                        }
//                    } catch (UiObjectNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                }
//                if(waitForUiObjectText("com.chipsguide.app.roav.fmplayer:id/tv_later","Later",3000)){
//                    try {
//                        UiObject later = mDevice.findObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/tv_later").text("Later"));
//                        later.clickAndWaitForNewWindow();
//                    } catch (UiObjectNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                }
//                return false;
//            }
//        });
//    }
}
