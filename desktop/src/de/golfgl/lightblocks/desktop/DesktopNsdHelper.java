package de.golfgl.lightblocks.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import de.golfgl.lightblocks.multiplayer.INsdHelper;
import de.golfgl.lightblocks.multiplayer.IRoomLocation;
import de.golfgl.lightblocks.multiplayer.KryonetRoomLocation;

public class DesktopNsdHelper implements INsdHelper {
    public static final String HTTP = "_http._tcp.local.";
    final Map<String, InetAddress> currentServices = new ConcurrentHashMap<>();
    MyListener listener = new MyListener();
    private JmDNS jmdns;

    @Override
    public void registerService() {
        try {
            // Create a JmDNS instance
            if (jmdns == null)
                jmdns = JmDNS.create(InetAddress.getLocalHost());

            // Register a service
            ServiceInfo serviceInfo = ServiceInfo.create(HTTP, SERVICE_NAME + "-" + "Desktop-" + MathUtils.random(100, 999), 8080, "");
            jmdns.registerService(serviceInfo);
            Gdx.app.log("Server", "Registered for service discovery " + jmdns.getInetAddress());
        } catch (IOException e) {
            Gdx.app.error("Server", "Could not register for service discovery.", e);
        }
    }

    @Override
    public void unregisterService() {
        if (jmdns != null) {
            // Unregister all services
            jmdns.unregisterAllServices();
        }
    }

    @Override
    public void startDiscovery() {
        try {
            if (jmdns == null) {
                jmdns = JmDNS.create(InetAddress.getLocalHost());
            }
            // Add a service listener
            jmdns.addServiceListener(HTTP, listener);
        } catch (IOException e) {
            Gdx.app.error("Client", "Could not start service discovery.", e);
        }
    }

    @Override
    public void stopDiscovery() {
        if (jmdns != null)
            jmdns.removeServiceListener(HTTP, listener);
    }

    @Override
    public List<IRoomLocation> getDiscoveredServices() {
        List<IRoomLocation> retVal = new LinkedList<>();

        synchronized (currentServices) {
            for (Map.Entry<String, InetAddress> entry : currentServices.entrySet()) {
                retVal.add(new KryonetRoomLocation(entry.getKey(), entry.getValue()));
            }
        }

        return retVal;
    }

    private class MyListener implements ServiceListener {
        @Override
        public void serviceAdded(ServiceEvent event) {
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            ServiceInfo info = event.getInfo();
            if (info != null && info.getName().startsWith(SERVICE_NAME)) {
                synchronized (currentServices) {
                    currentServices.remove(info.getName().substring(SERVICE_NAME.length() + 1));
                }
            }
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            ServiceInfo info = event.getInfo();
            if (info != null && info.getName().startsWith(SERVICE_NAME)) {
                try {
                    synchronized (currentServices) {
                        currentServices.put(info.getName().substring(SERVICE_NAME.length() + 1), InetAddress.getByName
                                (info.getInetAddress().getHostName()));
                    }
                } catch (UnknownHostException e) {
                    // eat
                }
            }
        }
    }
}
