package io.github.coiby.ustcwlt;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by coiby on 11/16/15.
 */
public class WifiIntentService extends IntentService {

    public static final String STATE = "STATE";
    public static int InternetAccess=1;
    public static int NoInternetAccess=2;
    public static int NOT_USTCNet=3;
    public static int USTCNet=4;
    public static int TIMEOUT=5;

    Handler handler=new Handler(Looper.getMainLooper());

    public WifiIntentService() {
        super("WifiIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean state = false;
        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);



        int ip = wm.getConnectionInfo().getIpAddress();
        //ToastMSG(Integer.toString(ip));
        //String ipAddress = Formatter.formatIpAddress(ip);
        if (ip !=0) {

            final int netState = ifUSTCNet();

            //ToastMSG(Integer.toString(netState));

            if(netState==USTCNet){
                final int netState2 = CheckInternet();
                if(netState2==InternetAccess){
                    state=true;
                    ToastMSG("Already have access to Internet");

                } else if(netState2 == NoInternetAccess){

                    Intent connect = new Intent(getBaseContext(),MainActivity.class);
                    connect.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplication().startActivity(connect);

                }
            }

        }
    }

    protected void ToastMSG(final String msg){
        handler.post(new Runnable() {
            public void run() {
                //your operation...
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

            }
        });
    }
    public  int CheckInternet(){
        try {
            HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.baidu.com").openConnection()); // If you use http, baidu will response with 302 with the user-agents that is thought to supports https by baidu,
            //Weid thing: On Firefox@Desktop, WLT@Android , sometimes you'll not be able to be redirected to wlt.ustc.edu.cn
            urlc.setRequestProperty("User-Agent", "WLT");// you can either use https or set user-agent to other values
            urlc.setRequestMethod("GET");
            urlc.setDoOutput(true);// set connection output to true
            urlc.setConnectTimeout(1500);
            urlc.setReadTimeout(1000);

            urlc.connect();
            /*int responsCode = urlc.getResponseCode() ;
            ToastMSG(Integer.toString(responsCode));*/
            if( urlc.getResponseCode() == HttpURLConnection.HTTP_OK ){
                if(urlc.getHeaderFields().get("Server").toString().contains("redir_url")){//convert to String, otherwise this will be false
                    return NoInternetAccess;
                }else{
                    return InternetAccess;
                }


            }else {
                return NoInternetAccess;
            }

            //return (urlc.getResponseCode() == HttpURLConnection.HTTP_OK); // If there's response code && ==200, the Internet is available
        } catch (java.net.SocketTimeoutException e) {
            return NoInternetAccess;
        } catch (IOException e) {
            Log.e("WLT", "Error checking internet connection", e);
            return NoInternetAccess;
        }

    }

    public int ifUSTCNet(){
        try {
            HttpURLConnection urlc = (HttpURLConnection) (new URL("http://wlt.ustc.edu.cn/cgi-bin/ip").openConnection()); // If you use http, baidu will response with 302 with the user-agents that is thought to supports https by baidu,
            //Wired thing: On Firefox@Desktop, WLT@Android , sometimes you'll not be able to be redirected to wlt.ustc.edu.cn
            urlc.setRequestProperty("User-Agent", "WLT");// you can either use https or set user-agent to other values
            urlc.setRequestMethod("GET");
            urlc.setDoOutput(true);// set connection output to true
            urlc.setConnectTimeout(1500);
            urlc.setReadTimeout(1500);

            urlc.connect();

            //int rcode=urlc.getResponseCode();

            //ToastMSG(Integer.toString(rcode));
            if( urlc.getResponseCode() == HttpURLConnection.HTTP_OK ){
                String line;
                BufferedReader rd = new BufferedReader(new InputStreamReader(urlc.getInputStream(),"GB2312"));
                //ToastMSG(rd.readLine());
                while ((line = rd.readLine()) != null) {
                    if(line.contains("非科大IP地址")){//If 非科大IP地址 contained, it's not USTC IP address
                        return NOT_USTCNet;

                    }
                    if(line.contains("记住密码")){
                        return USTCNet;

                    }
                }

            }else {
                return NOT_USTCNet;
            }

            //return (urlc.getResponseCode() == HttpURLConnection.HTTP_OK); // If there's response code && ==200, the Internet is available
        } catch (java.net.SocketTimeoutException e) {

            return TIMEOUT;
        } catch (IOException e) {

            Log.e("WLT", "Error checking internet connection", e);
            return NOT_USTCNet;
        }
        return NOT_USTCNet;
    }
}
