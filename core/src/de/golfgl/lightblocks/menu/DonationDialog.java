package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.pay.Information;
import com.badlogic.gdx.pay.Offer;
import com.badlogic.gdx.pay.OfferType;
import com.badlogic.gdx.pay.PurchaseManagerConfig;
import com.badlogic.gdx.pay.PurchaseObserver;
import com.badlogic.gdx.pay.Transaction;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.InfoButton;
import de.golfgl.lightblocks.scene2d.ProgressDialog;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.ScoreLabel;

/**
 * Created by Benjamin Schulte on 24.06.2018.
 */

public class DonationDialog extends ControllerMenuDialog {
    private static final String LIGHTBLOCKS_SUPPORTER = "lightblocks.supporter";
    private static final String LIGHTBLOCKS_SPONSOR = "lightblocks.sponsor";
    private static final String LIGHTBLOCKS_PATRON = "lightblocks.patron";
    private final LightBlocksGame app;
    private final RoundedTextButton reclaimButton;
    private Cell mainDonationButtonsCell;
    private DonationButton donateSupporter;
    private Table donationButtonTable;
    private DonationButton donateSponsor;
    private DonationButton donatePatron;
    private ScaledLabel doDonateLabel;

    public DonationDialog(final LightBlocksGame app) {
        super("", app.skin);
        this.app = app;

        RoundedTextButton closeButton = new RoundedTextButton("No, thanks", app.skin);
        button(closeButton);

        reclaimButton = new RoundedTextButton("Reclaim", app.skin);
        reclaimButton.setDisabled(true);
        reclaimButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                reclaimButton.setDisabled(true);
                app.purchaseManager.purchaseRestore();
            }
        });
        addFocusableActor(reclaimButton);
        getButtonTable().add(reclaimButton);

        // GUI erstmal so aufbauen
        fillContent(app);

        // den Init lostreten so früh es geht, aber nicht bevor die GUI-Referenzen existieren :-)
        initPurchaseManager();
    }

    private void fillContent(LightBlocksGame app) {
        Table contentTable = getContentTable();
        contentTable.pad(10);

        long drawnTetrominos = app.savegame.getTotalScore().getDrawnTetrominos();
        if (drawnTetrominos > 5000) {
            contentTable.add(new ScaledLabel("You stacked", app.skin, LightBlocksGame.SKIN_FONT_TITLE));
            ScoreLabel scoreLabel = new ScoreLabel(0, 0, app.skin, LightBlocksGame.SKIN_FONT_TITLE);
            scoreLabel.setMaxCountingTime(2);
            scoreLabel.setCountingSpeed(2000);
            scoreLabel.setScore(drawnTetrominos);
            contentTable.row();
            contentTable.add(scoreLabel).height(scoreLabel.getPrefHeight() * .8f);
            contentTable.row();
            contentTable.add(new ScaledLabel("blocks so far.", app.skin, LightBlocksGame.SKIN_FONT_TITLE));
            contentTable.row().padTop(10);
        }

        ScaledLabel textLabel = new ScaledLabel("It is great to see people having fun with this game. That's why " +
                "Lightblocks is free and will stay free.\n\n" +
                "However, if you want, you can support me in my efforts with a voluntary donation.", app.skin);
        textLabel.setWrap(true);
        textLabel.setAlignment(Align.center);
        contentTable.add(textLabel).fillX().minWidth(LightBlocksGame.nativeGameWidth * .8f);
        contentTable.row().padTop(20);
        doDonateLabel = new ScaledLabel("Donate and become a", app.skin, LightBlocksGame.SKIN_FONT_TITLE);
        doDonateLabel.setVisible(false);
        contentTable.add(doDonateLabel);
        contentTable.row();
        // erstmal schicke Animation bis alles soweit ist...
        mainDonationButtonsCell = contentTable.add(new ProgressDialog.WaitRotationImage(app));

        donationButtonTable = new Table();
        donationButtonTable.defaults().fillX().uniform().expandX();
        donateSupporter = new DonationButton(LIGHTBLOCKS_SUPPORTER);
        donationButtonTable.add(donateSupporter);
        donateSponsor = new DonationButton(LIGHTBLOCKS_SPONSOR);
        donationButtonTable.add(donateSponsor);
        donationButtonTable.row();
        donatePatron = new DonationButton(LIGHTBLOCKS_PATRON);
        donationButtonTable.add(donatePatron);
        ScaledLabel hintLabel = new ScaledLabel("Pick the amount you are willing to give or combine them.", app.skin);
        hintLabel.setWrap(true);
        hintLabel.setAlignment(Align.center);
        donationButtonTable.add(hintLabel).fill();

        mainDonationButtonsCell.minSize(donationButtonTable.getPrefWidth(), donationButtonTable.getPrefHeight());
    }

    private void initPurchaseManager() {
        // IAP
        PurchaseManagerConfig pmc = new PurchaseManagerConfig();
        pmc.addOffer(new Offer().setType(OfferType.ENTITLEMENT).setIdentifier(LIGHTBLOCKS_SUPPORTER));
        pmc.addOffer(new Offer().setType(OfferType.ENTITLEMENT).setIdentifier(LIGHTBLOCKS_SPONSOR));
        pmc.addOffer(new Offer().setType(OfferType.ENTITLEMENT).setIdentifier(LIGHTBLOCKS_PATRON));

        app.purchaseManager.install(new LbPurchaseObserver(), pmc, true);
    }

    private void updateGuiWhenPurchaseManInstalled() {
        // einfüllen der Infos
        donateSupporter.updateFromManager();
        donateSponsor.updateFromManager();
        donatePatron.updateFromManager();

        // TODO: im aggressiven Modus frühestens 5 Sekunden nach Start...
        reclaimButton.setDisabled(false);
        doDonateLabel.setVisible(true);
        mainDonationButtonsCell.setActor(donationButtonTable);
        mainDonationButtonsCell.fillX();
    }

    private class DonationButton extends InfoButton {
        private final String sku;

        public DonationButton(String sku) {
            // feste Werte damit die Breite und Höhe schonmal passt
            super("Supporter", "x,xxx", app.skin);
            this.sku = sku;
            addFocusableActor(this);
            getDescLabel().setAlignment(Align.center);

            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    buyItem();
                }
            });
        }

        private void buyItem() {
            app.purchaseManager.purchase(sku);
        }

        public void setBought() {
            setDisabled(true);
            getDescLabel().setText("Thank you!");
        }

        public void updateFromManager() {
            Information skuInfo = app.purchaseManager.getInformation(sku);

            if (skuInfo == null || skuInfo.equals(Information.UNAVAILABLE)) {
                setDisabled(true);
                setText("Not available");
                getDescLabel().setText("n/a");
            } else {
                setText(skuInfo.getLocalName());
                getDescLabel().setText(skuInfo.getLocalPricing());
            }
        }
    }

    private class LbPurchaseObserver implements PurchaseObserver {

        @Override
        public void handleInstall() {
            Gdx.app.log("LB-IAP", "Installed");

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    updateGuiWhenPurchaseManInstalled();
                }
            });
        }

        @Override
        public void handleInstallError(Throwable e) {
            Gdx.app.error("LB-IAP", "Error when trying to install PurchaseManager", e);
            handleInstall();
        }

        @Override
        public void handleRestore(final Transaction[] transactions) {
            for (Transaction t : transactions) {
                handlePurchase(t);
            }
        }

        @Override
        public void handleRestoreError(Throwable e) {

        }

        @Override
        public void handlePurchase(final Transaction transaction) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    if (transaction.isPurchased()) {
                        // TODO richtig persistieren
                        if (transaction.getIdentifier().equals(LIGHTBLOCKS_SUPPORTER))
                            donateSupporter.setBought();
                        else if (transaction.getIdentifier().equals(LIGHTBLOCKS_SPONSOR))
                            donateSponsor.setBought();
                        else if (transaction.getIdentifier().equals(LIGHTBLOCKS_PATRON))
                            donatePatron.setBought();

                    }
                }
            });
        }

        @Override
        public void handlePurchaseError(Throwable e) {

        }

        @Override
        public void handlePurchaseCanceled() {

        }
    }
}
