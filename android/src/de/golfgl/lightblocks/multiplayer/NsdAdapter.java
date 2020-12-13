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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter between my game and android nsd helper
 * <p>
 * Created by Benjamin Schulte on 25.02.2017.
 */

public class NsdAdapter implements INsdHelper, NsdListener {

    final Map<String, InetAddress> currentServices;
    final Map<String, ServerAddress> multiplayerServers;
    private NsdHelper nsdHelper;

    public NsdAdapter(Context context) {
        nsdHelper = new NsdHelper(context, this);

        nsdHelper.setLogEnabled(true);
        //nsdHelper.setAutoResolveEnabled(true);
        nsdHelper.setDiscoveryTimeout(300);

        currentServices = new ConcurrentHashMap<String, InetAddress>();
        multiplayerServers = new ConcurrentHashMap<>();
    }

    @Override
    public void onNsdRegistered(NsdService nsdService) {

    }

    @Override
    public void onNsdDiscoveryFinished() {

    }

    @Override
    public void onNsdServiceFound(NsdService nsdService) {
        if (nsdService.getName().startsWith(SERVICE_NAME) || nsdService.getType().contains(LIGHTBLOCKS_TYPE_NAME))
            nsdHelper.resolveService(nsdService);

    }

    @Override
    public void onNsdServiceResolved(NsdService nsdService) {
        if (nsdService.getName().startsWith(SERVICE_NAME)) {
            try {
                synchronized (currentServices) {
                    currentServices.put(nsdService.getName().substring(SERVICE_NAME.length() + 1), InetAddress.getByName
                            (nsdService.getHost().getHostName()));
                }
            } catch (UnknownHostException e) {
                // eat
            }
        }
        if (nsdService.getType().contains(LIGHTBLOCKS_TYPE_NAME)) {
            synchronized (multiplayerServers) {
                multiplayerServers.put(nsdService.getName(), new ServerAddress(nsdService.getName(), nsdService.getHostName(), nsdService.getPort(), false));
            }
        }

    }

    @Override
    public void onNsdServiceLost(NsdService nsdService) {
        if (nsdService.getName().startsWith(SERVICE_NAME))
            synchronized (currentServices) {
                currentServices.remove(nsdService.getName().substring(SERVICE_NAME.length() + 1));
            }
        if (nsdService.getType().contains(LIGHTBLOCKS_TYPE_NAME)) {
            synchronized (multiplayerServers) {
                multiplayerServers.remove(nsdService.getName());
            }
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
    public void startDiscovery(boolean legacy) {
        currentServices.clear();
        if (legacy)
            nsdHelper.startDiscovery(NsdType.HTTP);
        else
            nsdHelper.startDiscovery(LIGHTBLOCKS_TYPE_NAME + ".");
        // strangely discovered services will be pf type "." + LIGHTBLOCKS_TYPE_NAME
    }

    @Override
    public void stopDiscovery() {
        nsdHelper.stopDiscovery();
    }

    @Override
    public List<IRoomLocation> getDiscoveredLegacyServices() {

        List<IRoomLocation> retVal = new LinkedList<>();

        synchronized (currentServices) {
            for (Map.Entry<String, InetAddress> entry : currentServices.entrySet()) {
                retVal.add(new KryonetRoomLocation(entry.getKey(), entry.getValue()));
            }
        }

        return retVal;
    }

    @Override
    public List<ServerAddress> getDiscoveredMultiplayerServers() {
        synchronized (multiplayerServers) {
            return new ArrayList<>(multiplayerServers.values());
        }
    }
}
