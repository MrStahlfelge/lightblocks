package de.golfgl.lightblocks.multiplayer;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * Created by Benjamin Schulte on 12.04.2018.
 */

public class NetUtils {
    //Hardcoded Android AP address
    private static final String WIFI_AP_ADDRESS = "192.168.43.1";

    public boolean shouldShowAdvancedWifiSettings() {
        return false;
    }

    public void showAdvancedWifiSettings() {

    }

    public static InetAddress intToInet(int value) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = byteOfInt(value, i);
        }
        try {
            return InetAddress.getByAddress(bytes);
        } catch (UnknownHostException e) {
            // This only happens if the byte array has a bad length
            return null;
        }
    }

    public static byte byteOfInt(int value, int which) {
        int shift = which * 8;
        return (byte) (value >> shift);
    }

    public boolean isConnectedToLocalNetwork() {
        return true;
    }

    public boolean isEnabledWifiHotspot() {
        return false;
    }

    public String getLocalIpAsString() {
        InetAddress address = getLocalInetAddress();

        if (address != null)
            return address.getHostAddress();
        else
            return "-";
    }

    public InetAddress getLocalInetAddress() {
        InetAddress savedAddress = null;

        try {
            Enumeration<NetworkInterface> netinterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (netinterfaces.hasMoreElements()) {
                NetworkInterface netinterface = netinterfaces.nextElement();
                Enumeration<InetAddress> addresses = netinterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();

                    if (isEnabledWifiHotspot()
                            && WIFI_AP_ADDRESS.equals(address.getHostAddress()))
                        return address;

                    // this is the condition that sometimes gives problems
                    if (!address.isLoopbackAddress()
                            && !address.isLinkLocalAddress()) {
                        if (address instanceof Inet4Address)
                            return address;
                        else
                            savedAddress = address;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return savedAddress;
    }

}
