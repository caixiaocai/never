package com.anker.autotest.eufysecurity;

import android.graphics.Rect;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/***
 * 轮流查看所有设备的视频流,且点击对讲，查看完一轮杀掉重启app
 */
@RunWith(AndroidJUnit4.class)
public class PlayTurnsAndSpeakerTest {

    private class Item{
        int page;
        int y;
        UiSelector nameView;
        BySelector playView;
        UiSelector checkView;
        BySelector talkView;
        UiSelector speakView = null;
    }

    private UiDevice mDevice = null;
    private String floderPath;
    private String filePath;
    private int testCount = 0, testRound = 0;
    private boolean flag = false;
    private int loadingtimeout = 15;
    private int waitidle = 2;
    private int speaktime = 30;
    private boolean restart = false;
    private String info;
    private int scroll_count;

    private List<String> NAME_VIEW = Arrays.asList(
            "com.oceanwing.battery.cam:id/item_single_camera_cam_name",
            "com.oceanwing.battery.cam:id/tv_device_name");

    private List<String> LIVE_BTN = Arrays.asList(
            "com.oceanwing.battery.cam:id/item_single_camera_img_live",
            "com.oceanwing.battery.cam:id/iv_live");

    private List<String> TALK_BACK = Arrays.asList(
            "com.oceanwing.battery.cam:id/vb_talkback",
            "com.oceanwing.battery.cam:id/iv_talkback",
            "com.oceanwing.battery.cam:id/iv_voice",
            "com.oceanwing.battery.cam:id/camera_bottom_menu_btn_mike");

    private List<String> SPEAK_VIEW = Arrays.asList(
            "com.oceanwing.battery.cam:id/iv_speak_anim");

    private String ADD_DEVICE =  "com.oceanwing.battery.cam:id/add_device";


    @Before
    public void setUp() throws Exception {
        XLog.init(LogLevel.ALL);
        XLog.i("setUp");

        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Common.allowPermission(mDevice);

        Bundle extras = InstrumentationRegistry.getArguments();
        if (extras.containsKey("loadingtimeout")) {
            loadingtimeout = Integer.parseInt(extras.getString("loadingtimeout"));
        }
        if (extras.containsKey("waitidle")) {
            waitidle = Integer.parseInt(extras.getString("waitidle"));
        }
        if (extras.containsKey("speaktime")) {
            speaktime = Integer.parseInt(extras.getString("speaktime"));
        }
        if (extras.containsKey("restart")) {
            restart = Boolean.parseBoolean(extras.getString("restart"));
        }
        XLog.i(String.format("loadingtimeout: %s", loadingtimeout));
        XLog.i(String.format("waitidle: %s", waitidle));
        XLog.i(String.format("speaktime: %s", speaktime));
        XLog.i(String.format("restart: %s", restart));

        floderPath = Common.createDirs(this.getClass().getName(), this.getClass().getSimpleName());
        filePath = floderPath + DateUtils.getDateTime() + ".xls";
        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(this.getClass().getSimpleName());
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("名字");
            row.createCell(2).setCellValue("点击出流");
            row.createCell(3).setCellValue("出流时间");
            row.createCell(4).setCellValue("出流结果");
            row.createCell(5).setCellValue("出流耗时");
            row.createCell(6).setCellValue("异常信息");
            row.createCell(7).setCellValue("点击对讲");
            row.createCell(8).setCellValue("结束对讲");
            row.createCell(9).setCellValue("对讲时长");
            row.createCell(10).setCellValue("loadingtimeout=" + loadingtimeout);
            row.createCell(11).setCellValue("waitidle=" + waitidle);
            row.createCell(12).setCellValue("speaktime=" + speaktime);
            row.createCell(13).setCellValue("restart=" + restart);
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
        long time_start, time_end, start, end;
        String del, del2, shot_name;
        Item item;
        Rect rect;
        int y, top;
        int x = mDevice.getDisplayWidth() / 2;
        int liveLength = LIVE_BTN.size();
        int startY = mDevice.getDisplayHeight()/3;
        int endY = mDevice.getDisplayHeight()/3*2;

        XLog.i("先获取设备列表");
        mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W");

        ArrayList<Item> nameList = getName();
        nameList = getCheckBtn(nameList);
        int len = nameList.size();
        mDevice.executeShellCommand("am force-stop com.oceanwing.battery.cam");
        UiObject nameView;
        List<UiObject2> playView;
        while (true) {
            testRound++;  //第几轮
            XLog.i(String.format("第【 %d 】轮测试", testRound));
            if(testCount==0 || restart) {
                mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W");
                mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/dialog_cancel").text("Not Now")), 15000);
                if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_cancel").text("Not Now")).exists()) {
                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_cancel").text("Not Now")).click();
                }
            }

            formatPopUp();
            XLog.i("轮流查看实时流");
            for (int index=0;index<len;index++) {
                item = nameList.get(index);
                testCount++;  //第几行/第几次点播放
                XLog.i(String.format("第【 %d 】次查看实时流", testCount));
                XLog.i(String.format("查看设备【 %s 】实时流", item.nameView.toString()));
                HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
                HSSFSheet sheet = wb.getSheetAt(0);
                HSSFRow row = sheet.createRow(testCount);
                row.createCell(0).setCellValue(testRound);
                row.createCell(1).setCellValue(item.nameView.toString());
                int bottom = mDevice.getDisplayHeight();
                boolean clickPlay=false;
                boolean existed=false;
                find:
                for (int i = 0; i < len * 2; i++) {
                    if (mDevice.findObject(item.nameView).exists()) {
                        existed=true;
                        nameView = mDevice.findObject(item.nameView);
                        top = nameView.getVisibleBounds().centerY();
                        if(index<len-1 && mDevice.findObject(nameList.get(index+1).nameView).exists()){
                            nameView = mDevice.findObject(nameList.get(index+1).nameView);
                            bottom = nameView.getVisibleBounds().centerY();
                        }

                        playView = mDevice.findObjects(item.playView);
                        for (UiObject2 view : playView) {
                            y = view.getVisibleBounds().centerY();
                            if (top < y && y < bottom) {
                                XLog.i("找到播放按钮并点击");
                                view.click();
                                clickPlay = true;
                                break find;
                            }
                        }

                        if(mDevice.findObject(new UiSelector().resourceId(ADD_DEVICE)).exists()){
                            clickPlay = false;
                            break find;
                        }
                        if (top < mDevice.getDisplayHeight() / 2) {
                            clickPlay = false;
                            break find;
                        }
                    }else if(existed){  // 这个设备名存在过，现在不存在了，所以判断为划过去了还没有播放按钮
                        mDevice.swipe(x, mDevice.getDisplayHeight() / 4, x, mDevice.getDisplayHeight() * 3 / 4, 200);
                        clickPlay = false;
                        break find;
                    }
                    mDevice.swipe(x, mDevice.getDisplayHeight() * 3 / 4, x, mDevice.getDisplayHeight() / 4, 200);
                }
                if(clickPlay) {
                    time_start = System.currentTimeMillis();
                    row.createCell(2).setCellValue(DateUtils.getSystemTime(time_start));
                    flag = waitPlay(item.checkView, loadingtimeout * 1000);
                    if (flag) { //成功
                        XLog.i("出流成功");
                        time_end = System.currentTimeMillis();
                        del = String.format("%d s %d ms", (time_end - time_start) / 1000, (time_end - time_start) % 1000);
                        row.createCell(3).setCellValue(DateUtils.getSystemTime(time_end));
                        row.createCell(4).setCellValue("pass");
                        row.createCell(5).setCellValue(del);
                        XLog.i(String.format("开始对讲：%s s", speaktime));
                        if(item.speakView!=null){
                            mDevice.findObject(item.talkView).click();
                            start = System.currentTimeMillis();
                            if(mDevice.findObject(new UiSelector().resourceId("com.android.permissioncontroller:id/permission_allow_button")).exists()){
                                mDevice.findObject(By.res("com.android.permissioncontroller:id/permission_allow_button")).click();
                                mDevice.findObject(item.talkView).click();
                                start = System.currentTimeMillis();
                            }

                            row.createCell(7).setCellValue(DateUtils.getSystemTime(start));
                            while(true){
                                if(System.currentTimeMillis()-start>speaktime*1000){
                                    break;
                                } else if(!mDevice.findObject(item.speakView).exists()){
                                    break;
                                }else{
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }else{
                            start = System.currentTimeMillis();
                            mDevice.findObject(item.talkView).click(speaktime * 1000);
                            if(mDevice.findObject(new UiSelector().resourceId("com.android.permissioncontroller:id/permission_allow_button")).exists()){
                                mDevice.findObject(By.res("com.android.permissioncontroller:id/permission_allow_button")).click();
                                start = System.currentTimeMillis();
                                mDevice.findObject(item.talkView).click(speaktime * 1000);
                            }
                            row.createCell(7).setCellValue(DateUtils.getSystemTime(start));
                        }
                        end = System.currentTimeMillis();
                        row.createCell(8).setCellValue(DateUtils.getSystemTime(end));
                        del2 = String.format("%d s %d ms", (end - start) / 1000, (end - start) % 1000);
                        row.createCell(9).setCellValue(del2);
                        XLog.i("结束对讲");
                    } else { //超时或者失败
                        XLog.i("出流超时或失败");
                        shot_name = floderPath + testCount + "_" + DateUtils.getDateTime() + ".png";
                        mDevice.takeScreenshot(new File(shot_name));
                        row.createCell(4).setCellValue("fail");
                        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/camera_preview_txt_remind_title")).exists()) {
                            info = mDevice.findObject(By.res("com.oceanwing.battery.cam:id/camera_preview_txt_remind_title")).getText();
                            row.createCell(6).setCellValue(info);
                        }
                    }
                    for(int back=0; back<3; back++) {
                        if (mDevice.findObject(item.checkView).exists()) {
                            mDevice.pressBack();
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }else{
                    row.createCell(4).setCellValue("没有找到播放按钮");
                }
                FileOutputStream os = new FileOutputStream(filePath);
                wb.write(os);
                os.close();
                wb.close();
                XLog.i("结果写入完成");
            }
            try {
                Thread.sleep(waitidle * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(restart) {
//            mDevice.executeShellCommand("am start -W -n com.oceanwing.battery.cam/.main.SplashActivity -S -a android.intent.action.MAIN -c android.intent.category.LAUNCHER");
                mDevice.executeShellCommand("am force-stop com.oceanwing.battery.cam");
            }else{
                for(int scroll=0; scroll<scroll_count+1;scroll++){
                    mDevice.swipe(x, startY, x, endY, 200);
                }
            }

        }
    }

    public ArrayList<Item> getName() throws Exception {
        XLog.i("获取设备列表");
        List<UiObject2> objs;
        int x = mDevice.getDisplayWidth()/2;
        int startY = mDevice.getDisplayHeight()/3*2;
        int endY = mDevice.getDisplayHeight()/3;
        int nameLength = NAME_VIEW.size();
        int len;
        Item getItem;
        boolean added;
        ArrayList<Item> nameList = new ArrayList<>();
        mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");
        mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/dialog_cancel")), 15000);
        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_cancel").text("Not Now")).exists()) {
            mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_cancel").text("Not Now")).click();
        }
        formatPopUp();
        XLog.i("循环获取首页的所有名字");
        HashSet set = new HashSet();
        for (int i = 0; i < 50; i++) {
            for(int j=0; j<nameLength; j++) {
                objs = mDevice.findObjects(By.res(NAME_VIEW.get(j)));
                for (UiObject2 obj : objs) {
                    Item item = new Item();
                    item.page = i;
                    item.y = obj.getVisibleCenter().y;
                    item.nameView = new UiSelector().resourceId(obj.getResourceName()).text(obj.getText());
                    item.playView = null;
                    item.checkView = null;
                    if (set.add(item.nameView.toString())) {  // 去重
                        added = false;
                        len = nameList.size();
                        for(int k = 0; k < len; k++){  // 排序
                            getItem = nameList.get(k);
                            if(item.page < getItem.page ||(item.page == getItem.page && item.y < getItem.y)){
                                if(k==0){
                                    nameList.add(0, item);
                                }else {
                                    nameList.add(k - 1, item);
                                }
                                added = true;
                                break;
                            }
                        }
                        if(!added) {
                            nameList.add(item);
                        }
                    }
                }
            }
            if (mDevice.findObject(new UiSelector().resourceId(ADD_DEVICE)).exists() || !mDevice.findObject(new UiSelector().scrollable(true)).exists()) {
                break;
            } else {
                scroll_count++;
                mDevice.swipe(x, startY, x, endY, 200);
//                mDevice.findObject(new UiSelector().scrollable(true)).swipeUp(500);
            }
        }
        XLog.i(nameList.toString());
        if (nameList.size() == 0){
            throw new Exception("没有读到首页设备名称，脚本停止");
        }
        return nameList;
    }

    public ArrayList<Item> getCheckBtn(ArrayList<Item> nameList) throws IOException, UiObjectNotFoundException {
        XLog.i("确定每一个设备的出流界面的按钮");
        int liveLength = LIVE_BTN.size();
        UiObject nameView;
        List<UiObject2> playView;
        String lll, sss;
        int checkLength=TALK_BACK.size();
        int speakLength=SPEAK_VIEW.size();
        int y, top;
        int x = mDevice.getDisplayWidth() / 2;
        mDevice.executeShellCommand("am start -n com.oceanwing.battery.cam/.main.SplashActivity -W -S");
        mDevice.wait(Until.findObject(By.res("com.oceanwing.battery.cam:id/dialog_cancel").text("Not Now")), 15000);
        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_cancel").text("Not Now")).exists()) {
            mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_cancel").text("Not Now")).click();
        }
        for (int index=0;index<nameList.size();index++) {
            formatPopUp();
            Item item = nameList.get(index);
            XLog.i("确定设备"+ item.nameView.toString() +"的出流界面的按钮");
            int bottom = mDevice.getDisplayHeight();
            boolean existed=false;
            boolean clickPlay=false;
            find:
            for (int i = 0; i < nameList.size() * 2; i++) {
                if (mDevice.findObject(item.nameView).exists()) {
                    existed=true;
                    nameView = mDevice.findObject(item.nameView);
                    top = nameView.getVisibleBounds().centerY();
                    if(index<nameList.size()-1 && mDevice.findObject(nameList.get(index+1).nameView).exists()){
                        nameView = mDevice.findObject(nameList.get(index+1).nameView);
                        bottom = nameView.getVisibleBounds().centerY();
                    }
                    for(int k=0; k<liveLength; k++) {
                        playView = mDevice.findObjects(By.res(LIVE_BTN.get(k)));
                        for (UiObject2 view : playView) {
                            y = view.getVisibleBounds().centerY();
                            if (top < y && y < bottom) {
                                item.playView = By.res(LIVE_BTN.get(k));
                                view.click();
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if(mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/cb_cellular_dialog_remind")).exists()){
                                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/cb_cellular_dialog_remind")).click();
                                }
                                if(mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_custom_sure")).exists()){
                                    mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/dialog_custom_sure")).click();
                                }
                                for(int t=0; t<checkLength; t++){
                                    lll = TALK_BACK.get(t);
                                    if(mDevice.findObject(new UiSelector().resourceId(lll)).exists()){
                                       item.checkView = new UiSelector().resourceId(lll);
                                       item.talkView = By.res(lll);
                                       mDevice.findObject(item.talkView).click();
                                       for(int s=0; s<speakLength; s++){
                                           sss = SPEAK_VIEW.get(s);
                                           if(mDevice.wait(Until.findObject(By.res(sss)), 10000)!=null){
//                                           if(mDevice.findObject(new UiSelector().resourceId(sss)).exists()){
                                               item.speakView = new UiSelector().resourceId(sss);
                                               break;
                                           }
                                       }
                                        break;
                                    }
                                }
                                clickPlay = true;
                                for(int back=0; back<3; back++) {
                                    if (mDevice.findObject(item.checkView).exists()) {
                                        mDevice.pressBack();
                                        try {
                                            Thread.sleep(200);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                break find;
                            }
                        }
                    }
                    if(mDevice.findObject(new UiSelector().resourceId(ADD_DEVICE)).exists()){
                        clickPlay = false;
                        break find;
                    }
                    if (top < mDevice.getDisplayHeight() / 2) {
                        clickPlay = false;
                        break find;
                    }
                }else if(existed){  // 这个设备名存在过，现在不存在了，所以判断为划过去了还没有播放按钮
                    mDevice.swipe(x, mDevice.getDisplayHeight() / 4, x, mDevice.getDisplayHeight() * 3 / 4, 200);
                    clickPlay = false;
                    break find;
                }
                mDevice.swipe(x, mDevice.getDisplayHeight() * 3 / 4, x, mDevice.getDisplayHeight() / 4, 200);
            }
            if(!clickPlay){
                XLog.i("移除设备 "+ item.nameView.toString() );
                nameList.remove(item);
                index = index-1;
            }
            mDevice.pressBack();
        }
        return nameList;
    }

    public boolean waitPlay(UiSelector checkView, int timeout) throws UiObjectNotFoundException {
        /**
         * return true表示成功
         * return false表示超时或者失败
         */
        Date start = new Date();
        while (true) {
            if (mDevice.findObject(checkView).exists()) {
                try {
                    if (mDevice.findObject(checkView).isEnabled()) {
                        break;
                    }
                } catch (UiObjectNotFoundException e) {
                    e.printStackTrace();
                }
            }else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Date end = new Date();
            long time = end.getTime() - start.getTime();
            if (time > timeout) {
                return false;
            }
        }
        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/camera_preview_video_btn_retry")).exists()) {
            return false;
        }
        if (mDevice.findObject(new UiSelector().resourceId("com.oceanwing.battery.cam:id/video_player_error_view")).exists()) {
            return false;
        }
        return true;
    }

    private void formatPopUp(){
        if(mDevice.hasObject(By.textContains("format HomeBase microSD Card"))){
            if(mDevice.hasObject(By.res("com.oceanwing.battery.cam:id/dialog_cancel"))){
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_cancel")).click();
            }
            else if(mDevice.hasObject(By.res("com.oceanwing.battery.cam:id/dialog_ok"))){
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/dialog_ok")).click();
                mDevice.wait(Until.hasObject(By.res("com.oceanwing.battery.cam:id/activity_storage_method_back")), 30000);
                mDevice.findObject(By.res("com.oceanwing.battery.cam:id/activity_storage_method_back")).click();
            }
        }
    }
}
