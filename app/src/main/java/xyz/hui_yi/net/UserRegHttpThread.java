package xyz.hui_yi.net;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import xyz.hui_yi.bean.UserRegBean;
import xyz.hui_yi.constants.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by LiFen on 2018/2/7.
 * 签到用户注册线程类
 */

public class UserRegHttpThread extends Thread {
    private static final String TAG = "UserRegHttpThread";
    private UserRegBean userRegBean;
    private String url;
    private Handler mHandler;
    private ByteArrayOutputStream baos;

    public UserRegHttpThread(Handler handler, String url, UserRegBean userRegBean) {
        this.mHandler = handler;
        this.url = url;
        this.userRegBean = userRegBean;
    }

    private void doPost() {
        try {
            URL httpUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Connection", "Keep-Alive");
            // 设置可向服务器输出
            conn.setDoOutput(true);
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(10000);
            // 打开连接后，向服务端写要提交的参数
            // 参数格式：“name=asdasdas&age=123123”
        /*String content = "uid=" + userRegBean.getUid() +"&pwd=" + userRegBean.getPwd()+
                "&bname=" + userRegBean.getCompany()+"&uname=" + userRegBean.getName()+
                "&uphone=" + userRegBean.getPhone()+"&upid=" + userRegBean.getPid();*/
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append("uid=").append(URLEncoder.encode(userRegBean.getUid(), "utf-8"))
                    .append("&pwd=").append(URLEncoder.encode(userRegBean.getPwd(), "utf-8"))
                    .append("&uname=").append(URLEncoder.encode(userRegBean.getName(), "utf-8"))
                    .append("&upid=").append(URLEncoder.encode(userRegBean.getPid(), "utf-8"))
                    .append("&bname=").append(URLEncoder.encode(userRegBean.getCompany(), "utf-8"))
                    .append("&uphone=").append(URLEncoder.encode(userRegBean.getPhone(), "utf-8"));
//            conn.setRequestProperty("Charset","UTF-8");
//            conn.setRequestProperty("Content-Length", String.valueOf(stringBuilder.toString().getBytes()));
//            conn.setRequestProperty("Content-type","application/json");
            // 获取向服务器写数据的输出流
            Log.i(TAG, "doPost: " + stringBuilder.toString());
//            conn.setRequestProperty("Content-Length", String.valueOf(stringBuilder.toString().getBytes().length));
            // 打开连接
            try {
                conn.connect();
            }catch (Exception e){
                sendMessage(1025);
            }
            conn.getOutputStream().write(stringBuilder.toString().getBytes());

            byte[] result = null;
            Log.i(TAG, "Code" + conn.getResponseCode());
            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                Log.i(TAG, "getNetData: " + baos.toString());
                result = baos.toByteArray();
                Log.i(TAG, "doPost: " + baos.toString());
                JSONObject resultt = new JSONObject(baos.toString());
                int code = Integer.parseInt(resultt.getString("code"));
                sendMessage(code);
            } else {
                sendMessage(30000);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //向UI线程发送代表登录结果的消息
    private void sendMessage(int code) {
        Message message = new Message();
        switch (code) {
            case 20002:
                message.what = Constants.REG_SUCCESS;
                break;
            case 10000:
                message.what = Constants.REG_UID_EXISTS;
                break;
            case 10004:
                message.what = Constants.REG_BNAME_NOT_EXISTS;
                break;
            case 30000:
                message.what = Constants.NO_SOTER_SUPPORT;
            case 1025:
                message.what = Constants.NET_ERROR;
                break;
        }
        mHandler.sendMessage(message);
    }

    public void run() {
        doPost();
    }

}
