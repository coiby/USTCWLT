package io.github.coiby.ustcwlt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;

import android.net.wifi.WifiManager;

import android.preference.PreferenceManager;

public class WifiReceiver extends BroadcastReceiver {

    //private static boolean firstConnect = true;

    //private static int ctime = 0;

    private static final Long SYNCTIME = 800L;
    private static final String LASTTIMESYNC = "DATE";
    SharedPreferences sharedPreferences;

    @Override
    public void onReceive(final Context context, Intent intent) {

        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

        if(networkInfo.isConnected()){
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            if(System.currentTimeMillis()-sharedPreferences.getLong(LASTTIMESYNC, 0)>=SYNCTIME)
            {
                sharedPreferences.edit().putLong(LASTTIMESYNC, System.currentTimeMillis()).commit();
                // Your code Here.
                Intent wifiIntent = new Intent(context, WifiIntentService.class);
                context.startService(wifiIntent);
            }


        }

    }


}
