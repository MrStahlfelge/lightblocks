package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.pay.Offer;
import com.badlogic.gdx.pay.OfferType;
import com.badlogic.gdx.pay.PurchaseManagerConfig;
import com.badlogic.gdx.pay.PurchaseObserver;
import com.badlogic.gdx.pay.Transaction;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 24.06.2018.
 */

public class DonationDialog extends ControllerMenuDialog {
    private final LightBlocksGame app;

    public DonationDialog(LightBlocksGame app) {
        super("", app.skin);
        this.app = app;

        FaButton closeButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);
        button(closeButton);

        // erstmal schicke Animation bis alles soweit ist...

        initPurchaseManager();
    }

    private void initPurchaseManager() {
        if (app.purchaseManager == null)
            return;

        // IAP
        PurchaseManagerConfig pmc = new PurchaseManagerConfig();
        pmc.addOffer(new Offer().setType(OfferType.ENTITLEMENT).setIdentifier("SUPPORT"));

        app.purchaseManager.install(new LbPurchaseObserver(), pmc, true);
    }


    private class LbPurchaseObserver implements PurchaseObserver {

        @Override
        public void handleInstall() {
            Gdx.app.log("LB-IAP", "Installed");

            // und hier dann füllen
        }

        @Override
        public void handleInstallError(Throwable e) {

        }

        @Override
        public void handleRestore(Transaction[] transactions) {

        }

        @Override
        public void handleRestoreError(Throwable e) {

        }

        @Override
        public void handlePurchase(Transaction transaction) {

        }

        @Override
        public void handlePurchaseError(Throwable e) {

        }

        @Override
        public void handlePurchaseCanceled() {

        }
    }
}
