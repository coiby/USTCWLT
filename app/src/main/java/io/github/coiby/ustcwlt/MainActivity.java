package io.github.coiby.ustcwlt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;

public class MainActivity extends Activity implements View.OnClickListener {
    private TextView text;

    private  String username;
    private  String password;
    private  String wport;
    private  SocketThread socketThread;
    private  Thread thread;

    private  SharedPreferences prefs;

    Button buttonOne;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //text = (TextView) findViewById(R.id.text2);

        //SharedPreferences prefs = this.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //addPreferencesFromResource(R.xml.preferences);
        //getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        username=prefs.getString("username",null);

        password=prefs.getString("password",null);
        wport=prefs.getString("port","0");

        socketThread=new SocketThread();
        //checkInter ci = new checkInter();

        buttonOne = (Button) findViewById(R.id.button);
        buttonOne.setOnClickListener(this);

        thread=new Thread(socketThread);
        thread.start();



    }

    @Override
    public void onClick(View v) {

        username=prefs.getString("username",null);
        password=prefs.getString("password",null);
        wport=prefs.getString("port","0");
        if (v.getId() == R.id.button) {

            thread=new Thread(socketThread);
            thread.start();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
       /* Intent i = new Intent(this, MyPreferencesActivity.class);
        startActivity(i);*/
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, MyPreferencesActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
        //return true;
    }

    class checkInter implements Runnable{

        public  int USTCNet_NoInternet=1;
        public  int YESInternet=2;
        public  int NOT_USTCNet=3;

        @Override
        public void run() {
            WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);

            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            int ip = wm.getConnectionInfo().getIpAddress();

            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.baidu.com").openConnection()); // If you use http, baidu will response with 302 with the user-agents that is thought to supports https by baidu,
                urlc.setRequestProperty("User-Agent", "WLT");// you can either use https or set user-agent to other values
                urlc.setRequestMethod("GET");

                urlc.setDoOutput(true);// set connection output to true
                urlc.setConnectTimeout(2000);
                urlc.setReadTimeout(2000);

                urlc.connect();
                int responsCode = urlc.getResponseCode();
                if (urlc.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    if (urlc.getHeaderFields().get("Server").contains("redir_url")) {
                        //return USTCNet_NoInternet;
                    } else {
                        //return YESInternet;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    class SocketThread implements Runnable {

        public SocketThread(){

        }
        @Override
        public void run() {


            String params = null;
            if (username != null && password != null) {
                try {

                    params = URLEncoder.encode("name", "UTF-8")
                            + "=" + URLEncoder.encode(username, "UTF-8");

                    params += "&" + URLEncoder.encode("password", "UTF-8")
                            + "=" + URLEncoder.encode(password, "UTF-8");

                    params += "&" + URLEncoder.encode("cmd", "UTF-8")
                            + "=" + URLEncoder.encode("login", "UTF-8");

                    params += "&" + URLEncoder.encode("url", "UTF-8")
                            + "=" + URLEncoder.encode("URL", "UTF-8");

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                String hostname = "wlt.ustc.edu.cn";
                int port = 80;


                InetAddress addr = null;
                try {
                    InetAddress inetAddress = InetAddress.getByName(hostname);
                    ;
                    addr = InetAddress.getByName(hostname);

                    //System.out.println(inetAddress.getHostAddress());

                    params += "&" + URLEncoder.encode("ip", "UTF-8")
                            + "=" + URLEncoder.encode(inetAddress.getHostAddress(), "UTF-8");


                    Socket socket = new Socket(hostname, port);

                    // Send headers
                    BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
                    String path = "/cgi-bin/ip";

                    wr.write("POST " + path + " HTTP/1.1" + "\r\n");
                    wr.write("HOST: wlt.ustc.edu.cn" + "\r\n");
                    //System.out.println(params.length());
                    wr.write("Content-Type: application/x-www-form-urlencoded" + "\r\n");
                    wr.write("Connection: Keep-alive\r\n");
                    wr.write("Content-Length: " + params.length() + "\r\n");
                    wr.write("\r\n");

                    // Send parameters
                    wr.write(params);
                    wr.flush();

                    // Get response
                    BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream(),"GB2312"));
                    String line;
                    String cookie = "";
                    while ((line = rd.readLine()) != null) {
                        if (line.contains("Set-Cookie")) {
                            //text.setText(text.getText().toString()+ "Logged in" + "\n");
                            cookie = line.replace("Set-Cookie", "Cookie");
                            try {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        Toast.makeText(MainActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //System.out.println(i+" "+line);
                        }
                        //System.out.println(line);
                    }
                    //wr.close();
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                        Socket socket2 = new Socket(hostname, port);
                        BufferedWriter wr2 = new BufferedWriter(new OutputStreamWriter(socket2.getOutputStream(), "UTF8"));

                        path = "/cgi-bin/ip?cmd=set&type=" + wport + "&exp=0";

                        wr2.write("GET " + path + " HTTP/1.1" + "\r\n");
                        System.out.println("GET " + path + " HTTP/1.1" + "\r\n");
                        wr2.write("Host: wlt.ustc.edu.cn" + "\r\n");
                        wr2.write(cookie + "\r\n");
                        wr2.write("\r\n");
                        wr2.write("\r\n");
                        wr2.flush();
                        //System.out.println(cookie);
                        BufferedReader rd2 = new BufferedReader(new InputStreamReader(
                                socket2.getInputStream(), "GB2312"));
                        String output = "";
                        while ((line = rd2.readLine()) != null) {

                            if(line.contains("网络设置成功")){
                                try {
                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            //text.setText(text.getText().toString()+"Successfully set network type to "+wport+1+"\n");
                                            Toast.makeText(MainActivity.this, "Successfully set network type to "+wport+1, Toast.LENGTH_SHORT).show();
                                            buttonOne.setText("Re-connect");
                                        }
                                    });
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }
                        }

                        wr.close();
                        rd.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }

    }
}
