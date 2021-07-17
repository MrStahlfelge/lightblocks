package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.pay.Information;
import com.badlogic.gdx.pay.Offer;
import com.badlogic.gdx.pay.OfferType;
import com.badlogic.gdx.pay.PurchaseManagerConfig;
import com.badlogic.gdx.pay.PurchaseObserver;
import com.badlogic.gdx.pay.Transaction;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.gdxgameanalytics.GameAnalytics;
import de.golfgl.lightblocks.DonationPurchaseManager;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.InfoButton;
import de.golfgl.lightblocks.scene2d.ProgressDialog;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.ScoreLabel;
import de.golfgl.lightblocks.scene2d.VetoDialog;

/**
 * Created by Benjamin Schulte on 24.06.2018.
 */

public class DonationDialog extends ControllerMenuDialog {
    public static final String LIGHTBLOCKS_SUPPORTER = "lightblocks.supporter";
    public static final String LIGHTBLOCKS_SPONSOR = "lightblocks.sponsor";
    public static final String LIGHTBLOCKS_PATRON = "lightblocks.patron";
    public static final int TETROCOUNT_FIRST_REMINDER = 5000;
    public static final int TETROCOUNT_NEXT_REMINDER = 2500;
    private static final float MAX_WAIT_TIME_FORCEDMODE = 5f;
    private static final float MIN_WAIT_TIME_FORCEDMODE = 3f;
    private final LightBlocksGame app;
    private final RoundedTextButton restoreButton;
    private final RoundedTextButton closeButton;
    private Cell mainDonationButtonsCell;
    private DonationButton donateSupporter;
    private Table donationButtonTable;
    private DonationButton donateSponsor;
    private DonationButton donatePatron;
    private ScaledLabel doDonateLabel;
    private ScaledLabel hintLabel;
    private boolean reclaimPressed;
    private boolean forcedMode = false;
    private float waitTimeseconds;
    private boolean canShowTable;

    public DonationDialog(final LightBlocksGame app) {
        super("", app.skin, LightBlocksGame.SKIN_WINDOW_ALLBLACK);
        this.app = app;

        closeButton = new RoundedTextButton(app.TEXTS.get("donationNoThanks"), app.skin);
        button(closeButton);

        restoreButton = new RoundedTextButton(app.TEXTS.get("donationReclaim"), app.skin);
        restoreButton.setDisabled(true);
        restoreButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                reclaimPressed = true;
                restoreButton.setDisabled(true);
                app.purchaseManager.purchaseRestore();
            }
        });
        addFocusableActor(restoreButton);
        getButtonTable().add(restoreButton);

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
            contentTable.add(new ScaledLabel(app.TEXTS.get("donationYouStacked1"), app.skin,
                    LightBlocksGame.SKIN_FONT_TITLE));
            ScoreLabel scoreLabel = new ScoreLabel(0, 0, app.skin, LightBlocksGame.SKIN_FONT_TITLE);
            scoreLabel.setMaxCountingTime(2);
            scoreLabel.setCountingSpeed(2000);
            scoreLabel.setScore(drawnTetrominos);
            contentTable.row();
            contentTable.add(scoreLabel).height(scoreLabel.getPrefHeight() * .8f);
            contentTable.row();
            contentTable.add(new ScaledLabel(app.TEXTS.get("donationYouStacked2"), app.skin,
                    LightBlocksGame.SKIN_FONT_TITLE));
            contentTable.row().padTop(10);
        }

        ScaledLabel textLabel = new ScaledLabel(app.TEXTS.get("donationText"), app.skin);
        textLabel.setWrap(true);
        textLabel.setAlignment(Align.center);
        contentTable.add(textLabel).fillX().minWidth(LightBlocksGame.nativeGameWidth * .9f);
        contentTable.row().padTop(20);
        doDonateLabel = new ScaledLabel(app.TEXTS.get("donationIntro"), app.skin, LightBlocksGame.SKIN_FONT_TITLE);
        doDonateLabel.setVisible(false);
        contentTable.add(doDonateLabel);
        contentTable.row();
        // erstmal schicke Animation bis alles soweit ist...
        mainDonationButtonsCell = contentTable.add(new ProgressDialog.WaitRotationImage(app));

        donationButtonTable = new Table();
        donationButtonTable.defaults().fillX().uniform().expandX();
        donateSupporter = new DonationButton(LIGHTBLOCKS_SUPPORTER, 179);
        donationButtonTable.add(donateSupporter);
        donateSponsor = new DonationButton(LIGHTBLOCKS_SPONSOR, 349);
        donationButtonTable.add(donateSponsor);
        donationButtonTable.row();
        donatePatron = new DonationButton(LIGHTBLOCKS_PATRON, 529);
        donationButtonTable.add(donatePatron);
        hintLabel = new ScaledLabel(app.TEXTS.get("donationHelp"), app.skin);
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

        //TODO Es muss bei entfernen des Dialogs dispose aufgerufen werden, damit observer und damit dieser Dialog
        // nicht geleakt wird. Da es sich aber um einen einmaligen Leak handelt, nicht ganz so schlimm...
    }

    public DonationDialog setForcedMode() {
        if (app.localPrefs.getSupportLevel() == 0) {
            forcedMode = true;
            closeButton.setDisabled(true);
            waitTimeseconds = 0;
            
            closeButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    app.localPrefs.setNextDonationReminder(app.savegame.getTotalScore().getDrawnTetrominos() +
                            TETROCOUNT_NEXT_REMINDER);
                }
            });

            if (app.gameAnalytics != null)
                app.gameAnalytics.submitDesignEvent("donationscreen:forced");
        }
        return this;
    }

    @Override
    public void act(float delta) {
        if (forcedMode) {
            waitTimeseconds += delta;

            if (waitTimeseconds > MIN_WAIT_TIME_FORCEDMODE)
                showTableIfPossible();

            if (waitTimeseconds > MAX_WAIT_TIME_FORCEDMODE)
                disarmForcedMode();
        } else
            showTableIfPossible();

        super.act(delta);
    }

    private void showTableIfPossible() {
        if (canShowTable && !donationButtonTable.hasParent()) {
            mainDonationButtonsCell.setActor(donationButtonTable);
            doDonateLabel.setVisible(true);
            mainDonationButtonsCell.fillX();
        }
    }

    protected void disarmForcedMode() {
        if (forcedMode) {
            closeButton.setDisabled(false);
            forcedMode = false;
        }
    }

    private void updateGuiWhenPurchaseManInstalled(String errorMessage) {
        // einfüllen der Infos
        donateSupporter.updateFromManager();
        donateSponsor.updateFromManager();
        donatePatron.updateFromManager();
        canShowTable = true;

        if (app.purchaseManager.installed() && errorMessage == null) {
            restoreButton.setDisabled(false);
        } else {
            errorMessage = (errorMessage == null ? "Error instantiating the donation system" : errorMessage);
            ScaledLabel errorLabel = new ScaledLabel(errorMessage, app.skin,
                    LightBlocksGame.SKIN_FONT_BIG);
            errorLabel.setWrap(true);
            errorLabel.setAlignment(Align.center);
            Cell donationLabelCell = getContentTable().getCell(doDonateLabel);
            if (donationLabelCell != null)
                donationLabelCell.setActor(errorLabel).fillX();
        }

        if (app.purchaseManager.installed() && app.purchaseManager.storeName().equals(DonationPurchaseManager.STORE_DONATIONS)) {
            // some extra code for the DonationPurchaseManager
            restoreButton.setVisible(false);
            donateSupporter.setVisible(!donateSupporter.isDisabled());
            donateSponsor.setVisible(!donateSponsor.isDisabled());
            donatePatron.setVisible(!donatePatron.isDisabled());
            doDonateLabel.remove();
            hintLabel.remove();
            waitTimeseconds = Math.max(waitTimeseconds, MIN_WAIT_TIME_FORCEDMODE);
        } else {
            Array<String> levels = app.localPrefs.getSupportLevels();

            if (levels.contains(DonationDialog.LIGHTBLOCKS_SUPPORTER, false))
                donateSupporter.setBought(true);

            if (levels.contains(DonationDialog.LIGHTBLOCKS_SPONSOR, false))
                donateSponsor.setBought(true);

            if (levels.contains(DonationDialog.LIGHTBLOCKS_PATRON, false))
                donatePatron.setBought(true);
        }
    }

    @Override
    protected Actor getConfiguredEscapeActor() {
        return closeButton;
    }

    private class DonationButton extends InfoButton {
        private final String sku;
        private final int usdCents;

        public DonationButton(String sku, int usdCents) {
            // feste Werte damit die Breite und Höhe schonmal passt
            super(app.TEXTS.get("donationType_" + sku), "x,xxx", app.skin);
            this.sku = sku;
            this.usdCents = usdCents;
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

        public void setBought(boolean fromRestore) {
            setDisabled(true);
            getDescLabel().setText(app.TEXTS.get("donationThankYou"));
            closeButton.setText(app.TEXTS.get("donationButtonClose"));
            app.localPrefs.addSupportLevel(sku);
            // Die Meldung an GA wird etwas verzögert, damit vorher eine neue Verbindung aufgebaut werden kann.
            // Ansonsten wurde das Event oft nicht gemeldet. getStage(), damit auch beim schnellen Ende des Dialogs
            // gesendet wird
            if (!fromRestore && app.gameAnalytics != null)
                getStage().getRoot().addAction(Actions.delay(1f,
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                app.gameAnalytics.submitBusinessEvent("donation", sku, usdCents, "USD");
                            }
                        })));
            disarmForcedMode();
        }

        public void updateFromManager() {
            Information skuInfo = app.purchaseManager.getInformation(sku);

            if (skuInfo == null || skuInfo.equals(Information.UNAVAILABLE)) {
                setDisabled(true);
                getDescLabel().setText("Not available");
            } else {
                getDescLabel().setText(skuInfo.getLocalPricing());
                if (app.purchaseManager.storeName().equals(DonationPurchaseManager.STORE_DONATIONS)) {
                    getLabel().setText(skuInfo.getLocalName());
                }
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
                    updateGuiWhenPurchaseManInstalled(null);
                }
            });
        }

        @Override
        public void handleInstallError(final Throwable e) {
            Gdx.app.error("LB-IAP", "Error when trying to install PurchaseManager", e);
            if (app.gameAnalytics != null)
                app.gameAnalytics.submitErrorEvent(GameAnalytics.ErrorType.warning, e.getMessage());

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    updateGuiWhenPurchaseManInstalled(e.getMessage());
                }
            });
        }

        @Override
        public void handleRestore(final Transaction[] transactions) {
            if (transactions != null && transactions.length > 0)
                for (Transaction t : transactions) {
                    handlePurchase(t, true);
                }
            else if (reclaimPressed)
                showErrorOnMainThread("donationNoReclaim", true);
        }

        @Override
        public void handleRestoreError(Throwable e) {
            if (reclaimPressed)
                showErrorOnMainThread("Error restoring donations: " + e.getMessage(), false);
        }

        @Override
        public void handlePurchase(final Transaction transaction) {
            handlePurchase(transaction, false);
        }

        protected void handlePurchase(final Transaction transaction, final boolean fromRestore) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    if (transaction.isPurchased()) {
                        if (transaction.getIdentifier().equals(LIGHTBLOCKS_SUPPORTER))
                            donateSupporter.setBought(fromRestore);
                        else if (transaction.getIdentifier().equals(LIGHTBLOCKS_SPONSOR))
                            donateSponsor.setBought(fromRestore);
                        else if (transaction.getIdentifier().equals(LIGHTBLOCKS_PATRON))
                            donatePatron.setBought(fromRestore);

                    }
                }
            });
        }

        @Override
        public void handlePurchaseError(Throwable e) {
            if (app.gameAnalytics != null)
                app.gameAnalytics.submitErrorEvent(GameAnalytics.ErrorType.warning, e.getMessage());
            showErrorOnMainThread("Error making donation:\n" + e.getMessage(), false);
        }

        @Override
        public void handlePurchaseCanceled() {

        }

        private void showErrorOnMainThread(final String message, final boolean loadFromTexts) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    new VetoDialog(loadFromTexts ? app.TEXTS.get(message) : message, getSkin(),
                            getWidth() * .8f).show(getStage());
                }
            });
        }
    }
}
