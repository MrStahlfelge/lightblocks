package de.golfgl.lightblocks.multiplayer;

import android.content.Context;

import com.badlogic.gdx.math.MathUtils;
import com.rafakob.nsdhelper.NsdHelper;
import com.rafakob.nsdhelper.NsdListener;
import com.rafakob.nsdhelper.NsdService;
import com.rafakob.nsdhelper.NsdType;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter between my game and android nsd helper
 * <p>
 * Created by Benjamin Schulte on 25.02.2017.
 */

public class NsdAdapter implements INsdHelper, NsdListener {

    public static final String SERVICE_NAME = "Lightblocks";
    final Map<String, InetAddress> currentServices;
    private NsdHelper nsdHelper;

    public NsdAdapter(Context context) {
        nsdHelper = new NsdHelper(context, this);

        nsdHelper.setLogEnabled(true);
        //nsdHelper.setAutoResolveEnabled(true);
        nsdHelper.setDiscoveryTimeout(60);

        currentServices = new ConcurrentHashMap<String, InetAddress>();
    }

    @Override
    public void onNsdRegistered(NsdService nsdService) {

    }

    @Override
    public void onNsdDiscoveryFinished() {

    }

    @Override
    public void onNsdServiceFound(NsdService nsdService) {
        if (nsdService.getName().startsWith(SERVICE_NAME))
            nsdHelper.resolveService(nsdService);

    }

    @Override
    public void onNsdServiceResolved(NsdService nsdService) {
        if (nsdService.getName().startsWith(SERVICE_NAME)) {
            try {
                currentServices.put(nsdService.getName().substring(SERVICE_NAME.length() + 1), InetAddress.getByName
                        (nsdService.getHost().getHostName()));
            } catch (UnknownHostException e) {
                // eat
            }
        }

    }

    @Override
    public void onNsdServiceLost(NsdService nsdService) {
        if (nsdService.getName().startsWith(SERVICE_NAME)) {
            currentServices.remove(nsdService.getName());
        }
    }

    @Override
    public void onNsdError(String s, int i, String s1) {

    }

    @Override
    public void registerService() {
        nsdHelper.registerService(SERVICE_NAME + "-" + android.os.Build.MODEL + "-" + MathUtils.random(100, 999),
                NsdType.HTTP);

    }

    @Override
    public void unregisterService() {
        nsdHelper.unregisterService();

    }

    @Override
    public void startDiscovery() {
        currentServices.clear();
        nsdHelper.startDiscovery(NsdType.HTTP);

    }

    @Override
    public void stopDiscovery() {
        nsdHelper.stopDiscovery();
    }

    @Override
    public List<IRoomLocation> getDiscoveredServices() {

        List<IRoomLocation> retVal = new ArrayList<>();

        for (Map.Entry<String, InetAddress> entry : currentServices.entrySet()) {
            retVal.add(new KryonetRoomLocation(entry.getKey(), entry.getValue()));
        }

        return retVal;
    }
}
