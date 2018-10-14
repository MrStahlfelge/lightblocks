package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.BackendClient;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.ProgressDialog;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.VetoDialog;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 14.10.2018.
 */

public class CreateNewAccountDialog extends ControllerMenuDialog {

    private final LightBlocksGame app;
    private final FaButton closeButton;
    private final RoundedTextButton createProfileButton;
    private final ScaledLabel nickNameLabel;

    public CreateNewAccountDialog(final LightBlocksGame app) {
        super("", app.skin);

        this.app = app;

        getButtonTable().defaults().pad(0, 40, 20, 40);
        closeButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);
        button(closeButton);

        Table contentTable = getContentTable();
        contentTable.pad(10);
        contentTable.row();
        contentTable.add(new ScaledLabel(app.TEXTS.get("createProfileNickNameLabel"), app.skin, LightBlocksGame
                .SKIN_FONT_TITLE)).fill().width(LightBlocksGame.nativeGameWidth - 80);

        nickNameLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_TITLE);
        FaButton editNicknameButton = new FaButton(FontAwesome.SETTING_PENCIL, app.skin);
        editNicknameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.input.getTextInput(new Input.TextInputListener() {
                    @Override
                    public void input(String text) {
                        setNewNickname(text);
                        ((MyStage) getStage()).setFocusedActor(getConfiguredDefaultActor());
                    }

                    @Override
                    public void canceled() {

                    }
                }, app.TEXTS.get("createProfileNickNameLabel"), nickNameLabel.getText().toString(), "");
            }
        });
        addFocusableActor(editNicknameButton);
        Table nickNameEditTable = new Table();
        nickNameEditTable.add(nickNameLabel).fill().expandX();
        nickNameEditTable.add(editNicknameButton);

        contentTable.row();
        contentTable.add(nickNameEditTable).fill();

        ScaledLabel legalText = new ScaledLabel(app.TEXTS.get("createProfileLegalInfo"), app.skin, LightBlocksGame
                .SKIN_FONT_REG);
        legalText.setWrap(true);
        contentTable.row().padTop(10);
        contentTable.add(legalText).fill();

        contentTable.row().padTop(10);
        createProfileButton = new RoundedTextButton(app.TEXTS.get("createProfileLabel"), app.skin);
        createProfileButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                createProfile(nickNameLabel.getText().toString());
            }
        });
        contentTable.add(createProfileButton);
        addFocusableActor(createProfileButton);

        setNewNickname(app.player.getGamerId());
    }

    @Override
    public Dialog show(Stage stage) {
        return super.show(stage);
    }

    private void createProfile(String nickname) {
        createProfileButton.setDisabled(true);
        final ProgressDialog progressDialog = new ProgressDialog("Please wait", app, getWidth() * .8f);
        progressDialog.show(getStage());

        app.backendManager.getBackendClient().createPlayer(nickname, new BackendClient.IBackendResponse<BackendClient
                .PlayerCreatedInfo>() {
            @Override
            public void onFail(int statusCode, final String errorMsg) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.getLabel().setText(errorMsg);
                        progressDialog.showOkButton();
                        createProfileButton.setDisabled(false);
                    }
                });
            }

            @Override
            public void onSuccess(final BackendClient.PlayerCreatedInfo retrievedData) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.hide(null);
                        Stage stage = getStage();
                        hide();
                        // Funktioniert leider nicht von selbst wegen des doppelten Dialogs
                        if (stage instanceof ControllerMenuStage) {
                            ((ControllerMenuStage) stage).setFocusedActor(previousFocusedActor);
                            ((ControllerMenuStage) stage).setEscapeActor(previousEscapeActor);
                        }
                        // muss nach dem Hide kommen, damit focus im TotalScoreScreen sauber übergeht
                        app.backendManager.setCredentials(retrievedData.userId, retrievedData.userKey);
                        app.localPrefs.setBackendNickname(retrievedData.nickName);
                    }
                });
            }
        });
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
        return createProfileButton;
    }
}
