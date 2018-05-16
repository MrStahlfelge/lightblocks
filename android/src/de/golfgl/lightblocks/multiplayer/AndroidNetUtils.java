package de.golfgl.lightblocks.multiplayer;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.badlogic.gdx.Gdx;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 12.04.2018.
 */

public class AndroidNetUtils extends NetUtils {
    private final Context context;

    public AndroidNetUtils(Context context) {
        this.context = context;
    }

    @Override
    public boolean shouldShowAdvancedWifiSettings() {
        return (!isEnabledWifiHotspot() && !isConnectedToWifi() && !LightBlocksGame.isOnAndroidTV());
    }

    @Override
    public void showAdvancedWifiSettings() {
        context.startActivity(new Intent(android.provider.Settings.ACTION_WIFI_IP_SETTINGS));
    }

    @Override
    public boolean isConnectedToLocalNetwork() {
        boolean connected = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        connected = ni != null
                && ni.isConnected()
                && (ni.getType() & (ConnectivityManager.TYPE_WIFI | ConnectivityManager.TYPE_ETHERNET)) != 0;
        if (!connected) {
            Gdx.app.log("NET", "isConnectedToLocalNetwork: see if it is an USB AP");
            try {
                for (NetworkInterface netInterface : Collections.list(NetworkInterface
                        .getNetworkInterfaces())) {
                    if (netInterface.getDisplayName().startsWith("rndis")) {
                        connected = true;
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        return connected;
    }

    private boolean isConnectedToWifi() {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected()
                && ni.getType() == ConnectivityManager.TYPE_WIFI;
    }

    @Override
    public boolean isEnabledWifiHotspot() {
        boolean enabled = false;
        Gdx.app.log("NET", "isEnabledWifiHotspot: see if it is an WIFI AP");
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = wm.getClass().getDeclaredMethod("isWifiApEnabled");
            enabled = (Boolean) method.invoke(wm);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return enabled;
    }

    @Override
    public InetAddress getLocalInetAddress() {
        if (!isConnectedToLocalNetwork() && !isEnabledWifiHotspot()) {
            Gdx.app.log("NET", "getLocalInetAddress called and no connection");
            return null;
        }

        if (isConnectedToWifi()) {
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            int ipAddress = wm.getConnectionInfo().getIpAddress();
            if (ipAddress == 0)
                return null;
            return intToInet(ipAddress);
        }

        return super.getLocalInetAddress();
    }
}
