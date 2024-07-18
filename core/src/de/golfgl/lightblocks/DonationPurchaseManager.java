package de.golfgl.lightblocks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.pay.Information;
import com.badlogic.gdx.pay.PurchaseManager;
import com.badlogic.gdx.pay.PurchaseManagerConfig;
import com.badlogic.gdx.pay.PurchaseObserver;

import java.util.HashMap;

import de.golfgl.lightblocks.menu.DonationDialog;

public class DonationPurchaseManager implements PurchaseManager {
    public static final String STORE_DONATIONS = "DONATIONS";
    protected PurchaseObserver observer;
    private final HashMap<String, Information> information = new HashMap<>();

    @Override
    public String storeName() {
        return STORE_DONATIONS;
    }

    @Override
    public void install(PurchaseObserver observer, PurchaseManagerConfig config, boolean autoFetchInformation) {
        this.observer = observer;
        information.clear();
        information.put(DonationDialog.LIGHTBLOCKS_SUPPORTER, new Information("Coffee", "", "Spend me a coffee"));
        information.put(DonationDialog.LIGHTBLOCKS_SPONSOR, new Information("PayPal", "", "Send me a tip"));
        observer.handleInstall();
    }

    @Override
    public boolean installed() {
        return true;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void purchase(String identifier) {
        if (identifier.equals(DonationDialog.LIGHTBLOCKS_SUPPORTER))
            Gdx.net.openURI("https://ko-fi.com/mrstahlfelge/");
        else
            Gdx.net.openURI("https://www.paypal.com/paypalme/MrStahlfelge");
    }

    @Override
    public void purchaseRestore() {

    }

    @Override
    public Information getInformation(String identifier) {
        return information.get(identifier);
    }
}
