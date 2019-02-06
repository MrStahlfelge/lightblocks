package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.BackendClient;
import de.golfgl.lightblocks.backend.BackendManager;
import de.golfgl.lightblocks.scene2d.EditableLabel;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.FaTextButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.ProgressDialog;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.AbstractScreen;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 14.10.2018.
 */

public class CreateNewAccountDialog extends ControllerMenuDialog {

    private static final int EDITABLE_WIDTH = LightBlocksGame.nativeGameWidth - 100;
    private final LightBlocksGame app;
    private final FaButton closeButton;
    private final Table createProfileTable;
    private final LinkProfileTable linkProfileTable;
    private final Cell<? extends Table> mainCell;
    private RoundedTextButton createProfileButton;
    private ScaledLabel nickNameLabel;

    public CreateNewAccountDialog(final LightBlocksGame app) {
        super("", app.skin);

        this.app = app;

        getButtonTable().defaults().pad(0, 40, 0, 40);
        closeButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);
        getButtonTable().add(closeButton);
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (createProfileTable.hasParent())
                    hide();
                else {
                    mainCell.setActor(createProfileTable);
                    ((MyStage) getStage()).setFocusedActor(getConfiguredDefaultActor());
                }
            }
        });
        addFocusableActor(closeButton);

        createProfileTable = new Table();
        fillCreateProfileTable();

        linkProfileTable = new LinkProfileTable();

        mainCell = getContentTable().add(linkProfileTable).fill().width(LightBlocksGame.nativeGameWidth - 80);
        validate();

        mainCell.minHeight(linkProfileTable.getPrefHeight());

        mainCell.setActor(createProfileTable);
    }

    protected void fillCreateProfileTable() {
        createProfileTable.pad(10);
        createProfileTable.row();
        final String createProfileNickNameLabel = app.TEXTS.get("createProfileNickNameLabel");
        ScaledLabel createProfileTitleLabel = new ScaledLabel(createProfileNickNameLabel, app.skin, LightBlocksGame
                .SKIN_FONT_TITLE);
        createProfileTitleLabel.setAlignment(Align.top);
        createProfileTable.add(createProfileTitleLabel).width(LightBlocksGame.nativeGameWidth - 80).fill().expandY();

        nickNameLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_EDIT_BIG);
        nickNameLabel.setEllipsis(true);
        FaButton editNicknameButton = new FaButton(FontAwesome.SETTING_PENCIL, app.skin);
        Table nickNameEditTable = new EditableLabel(nickNameLabel, editNicknameButton, app.skin,
                createProfileNickNameLabel) {
            @Override
            protected void onNewTextSet(String newText) {
                setNewNickname(newText);
                ((MyStage) getStage()).setFocusedActor(getConfiguredDefaultActor());
            }
        };
        nickNameEditTable.setWidth(EDITABLE_WIDTH);
        addFocusableActor(nickNameEditTable);

        createProfileTable.row();
        createProfileTable.add(nickNameEditTable);

        ScaledLabel legalText = new ScaledLabel(app.TEXTS.get("createProfileLegalInfo"), app.skin, LightBlocksGame
                .SKIN_FONT_REG);
        legalText.setWrap(true);
        legalText.setAlignment(Align.center);
        createProfileTable.row().padTop(10);
        createProfileTable.add(legalText).fill();

        createProfileTable.row().padTop(10);
        createProfileButton = new RoundedTextButton(app.TEXTS.get("createProfileLabel"), app.skin);
        createProfileButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                createProfile(nickNameLabel.getText().toString());
            }
        });
        createProfileTable.add(createProfileButton);
        addFocusableActor(createProfileButton);

        createProfileTable.row().padTop(40);
        ScaledLabel connectProfile = new ScaledLabel(app.TEXTS.get("createProfileConnectHint"), app.skin,
                LightBlocksGame.SKIN_FONT_REG);
        connectProfile.setWrap(true);
        connectProfile.setAlignment(Align.center);
        createProfileTable.add(connectProfile).fill();

        createProfileTable.row().padTop(10);
        Button connectProfileButton = new FaTextButton(app.TEXTS.get("connectProfileLabel").toUpperCase(), app.skin,
                LightBlocksGame.SKIN_FONT_BIG);
        connectProfileButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                mainCell.setActor(linkProfileTable);
                ((MyStage) getStage()).setFocusedActor(getConfiguredDefaultActor());
            }
        });
        addFocusableActor(connectProfileButton);
        createProfileTable.add(connectProfileButton).expandY().top();

        setNewNickname(app.player.getGamerId());
    }

    @Override
    public Dialog show(Stage stage) {
        return super.show(stage);
    }

    private void createProfile(String nickname) {
        createProfileButton.setDisabled(true);
        app.backendManager.getBackendClient().createPlayer(nickname, new PlayerCreatedHandler());
    }

    private void setNewNickname(String nickname) {
        if (nickname == null)
            return;

        if (nickname.length() > 20)
            nickname = nickname.substring(0, 20);

        nickNameLabel.setText(nickname);
    }

    @Override
    protected Actor getConfiguredEscapeActor() {
        return closeButton;
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return createProfileTable.hasParent() ? createProfileButton : linkProfileTable.nicknameEditable;
    }

    private class LinkProfileTable extends Table {
        public final EditableLabel nicknameEditable;
        private final EditableLabel mailEditable;
        private final EditableLabel activationEditable;
        private final RoundedTextButton requestCode;
        private final RoundedTextButton connectProfileButton;

        public LinkProfileTable() {
            pad(10);
            row();
            ScaledLabel titleLabel = new ScaledLabel(app.TEXTS.get("connectProfileLabel"), app.skin, LightBlocksGame
                    .SKIN_FONT_TITLE);
            titleLabel.setAlignment(Align.center);
            titleLabel.setWrap(true);
            add(titleLabel).fill();

            row();
            ScaledLabel createProfileConnectHint = new ScaledLabel(app.TEXTS.get("createProfileConnectHint"), app.skin,
                    LightBlocksGame.SKIN_FONT_REG);
            createProfileConnectHint.setWrap(true);
            createProfileConnectHint.setAlignment(Align.center);
            add(createProfileConnectHint).fill();

            row().padTop(10);
            add(new ScaledLabel("1. " + app.TEXTS.get("labelEnterNickname") + ":", app.skin, LightBlocksGame
                    .SKIN_FONT_REG)).fill();
            row();
            nicknameEditable = new EditableLabel(new ScaledLabel("", app.skin, LightBlocksGame
                    .SKIN_EDIT_BIG),
                    new FaButton(FontAwesome.SETTING_PENCIL, app.skin), app.skin, "Nickname");
            add(nicknameEditable);
            nicknameEditable.setWidth(EDITABLE_WIDTH);
            nicknameEditable.getLabel().setEllipsis(true);
            addFocusableActor(nicknameEditable);

            row().padTop(15);
            add(new ScaledLabel("2. " + app.TEXTS.get("labelEnterProfileRecovery") + ":", app.skin,
                    LightBlocksGame.SKIN_FONT_REG)).fill();
            row();
            mailEditable = new EditableLabel(new ScaledLabel("", app.skin, LightBlocksGame.SKIN_EDIT_BIG),
                    new FaButton(FontAwesome.SETTING_PENCIL, app.skin), app.skin, "E-Mail");
            add(mailEditable);
            mailEditable.setWidth(EDITABLE_WIDTH);
            mailEditable.getLabel().setEllipsis(true);
            addFocusableActor(mailEditable);

            row();
            requestCode = new RoundedTextButton(app.TEXTS.get("labelRequestActivationCode"), app.skin);
            requestCode.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    app.backendManager.getBackendClient().requestActivationCode(nicknameEditable.getText(),
                            mailEditable.getText(), new WaitForResponse<Void>(app, getStage()));
                }
            });
            add(requestCode);
            addFocusableActor(requestCode);

            row().padTop(15);
            add(new ScaledLabel("3. " + app.TEXTS.get("labelEnterCode") + ":", app.skin, LightBlocksGame
                    .SKIN_FONT_REG)).fill();
            row();
            activationEditable = new EditableLabel(new ScaledLabel("", app.skin, LightBlocksGame
                    .SKIN_EDIT_BIG),
                    new FaButton(FontAwesome.SETTING_PENCIL, app.skin), app.skin, "Activation code");
            add(activationEditable);
            activationEditable.setWidth(EDITABLE_WIDTH);
            activationEditable.getLabel().setEllipsis(true);
            addFocusableActor(activationEditable);

            row();
            connectProfileButton = new RoundedTextButton(titleLabel.getText().toString(), app.skin);
            addFocusableActor(connectProfileButton);
            connectProfileButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    try {
                        Integer activationCode = Integer.valueOf(activationEditable.getText());
                        app.backendManager.getBackendClient().linkProfile(nicknameEditable.getText(),
                                activationCode, new PlayerCreatedHandler());
                    } catch (Throwable t) {
                        ((AbstractScreen) app.getScreen()).showDialog("Enter a valid activation code.");
                    }
                }
            });
            add(connectProfileButton);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            requestCode.setDisabled(nicknameEditable.getText().isEmpty() || mailEditable.getText().isEmpty());
            connectProfileButton.setDisabled(nicknameEditable.getText().isEmpty() || activationEditable.getText()
                    .isEmpty());
        }
    }

    private class PlayerCreatedHandler extends BackendManager.AbstractQueuedBackendResponse<BackendClient
            .PlayerCreatedInfo> {

        private final ProgressDialog progressDialog;

        public PlayerCreatedHandler() {
            super(app);
            progressDialog = new ProgressDialog(app.TEXTS.get("pleaseWaitLabel"), app, getWidth() *
                    .8f);
            progressDialog.show(getStage());
        }

        @Override
        public void onRequestFailed(int statusCode, final String errorMsg) {
            progressDialog.getLabel().setText(errorMsg);
            progressDialog.showOkButton();
            createProfileButton.setDisabled(false);
        }

        @Override
        public void onRequestSuccess(final BackendClient.PlayerCreatedInfo retrievedData) {
            progressDialog.hide(null);
            Stage stage = getStage();
            hide();
            // muss nach dem Hide kommen, damit focus im TotalScoreScreen sauber übergeht
            app.backendManager.setCredentials(retrievedData.userId, retrievedData.userKey);
            app.localPrefs.setBackendNickname(retrievedData.nickName);
            if (app.pushMessageProvider != null)
                app.pushMessageProvider.initService(app);

            new BackendUserDetailsScreen(app, retrievedData.userId).show(getStage());
        }
    }
}
