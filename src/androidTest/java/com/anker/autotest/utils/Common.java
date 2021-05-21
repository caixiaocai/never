package com.anker.autotest.utils;

import android.content.pm.PackageManager;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.elvishew.xlog.XLog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class Common {
    /***
     * 打开权限
     * @param mDevice
     * @throws UiObjectNotFoundException
     * @throws IOException
     */
    public static void allowPermission(UiDevice mDevice) throws UiObjectNotFoundException, IOException {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(InstrumentationRegistry.getInstrumentation().getTargetContext(), "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                mDevice.executeShellCommand("am start -n com.anker.autotest/.MainActivity -W");
                mDevice.waitForWindowUpdate("com.anker.autotest",10000);
                if(new UiObject(new UiSelector().resourceId("com.android.packageinstaller:id/permission_allow_button")).exists()){
                    UiObject btn = mDevice.findObject(new UiSelector().resourceId("com.android.packageinstaller:id/permission_allow_button"));
                    btn.click();
                }
                for(int i=0;i<3;i++){
                    if (new UiObject(new UiSelector().resourceId("com.android.permissioncontroller:id/permission_allow_button")).exists()) {
                        UiObject btn = mDevice.findObject(new UiSelector().resourceId("com.android.permissioncontroller:id/permission_allow_button"));
                        btn.click();
                    }
                }
            }
        }
    }

    /**
     * 保存结果的文件夹
     * @return
     */
    public static String createDirs(String pkgName, String simpleName){
        boolean result;
        String path;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            path = InstrumentationRegistry.getInstrumentation().getTargetContext().getDataDir().getAbsolutePath() + File.separator + simpleName + "_" + DateUtils.getDateTime();
            File destDir = new File(path);
            if (!destDir.exists()) {
                result = destDir.mkdirs();  // 如果路径已经存在，mkdirs()返回值是false
                XLog.i(result);
            }
        }else{
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "autotest" + File.separator + simpleName + "_" + DateUtils.getDateTime();
            File destDir = new File(path);
            if (!destDir.exists()) {
                result = destDir.mkdirs();
                XLog.i(result);
            }
        }
        String dir = path + File.separator;
        return dir;
    }

    /***
     * get请求
     * @return
     * @throws IOException
     */
    public static String doGet(String getUrl) throws IOException {
        try {
            URL url = new URL(getUrl);
            //得到connection对象。
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //设置请求方式
            connection.setRequestMethod("GET");
            //连接
            connection.connect();
            //得到响应码
            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                //得到响应流
                InputStream inputStream = connection.getInputStream();
                //将响应流转换成字符串
                String result = inputStream.toString();//将流转换为字符串。
                XLog.d("kwwl","result============="+result);
                return result;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * post请求
     * @return
     * @throws IOException
     */
    public static String doPost(String getUrl, String jsonString) {
        String USER_AGENT = "Mozilla/5.0";
//        String getUrl="https://omc-ci.eufylife.com/omc/session/login";
//        String jsonString = Common.readJsonFile(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + dataFile);

        try {
            URL url = new URL(getUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            //添加请求头
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            connection.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            connection.setConnectTimeout(180000);

            connection.connect();
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8"); // utf-8编码
            out.append(jsonString);
            out.flush();
            out.close();

            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                InputStream inputStream = connection.getInputStream();

                StringBuilder sb = new StringBuilder();
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                String result = sb.toString();
                return result;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    //打印cookie信息
    public static void printCookie(CookieStore cookieStore){
        List<HttpCookie> listCookie = cookieStore.getCookies();
        listCookie.forEach(httpCookie -> {
            System.out.println("value      : "+httpCookie.getValue());
            System.out.println("httpCookie : "+httpCookie);
        });
    }


    /**
     * 读取json文件，返回json串
     * @param fileName
     * @return
     */
    public static String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);

            Reader reader = new InputStreamReader(new FileInputStream(jsonFile),"utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
