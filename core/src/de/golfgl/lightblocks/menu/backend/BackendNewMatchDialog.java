package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import java.util.List;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.MatchEntity;
import de.golfgl.lightblocks.backend.PlayerDetails;
import de.golfgl.lightblocks.menu.BeginningLevelChooser;
import de.golfgl.lightblocks.menu.ShareButton;
import de.golfgl.lightblocks.scene2d.EditableLabel;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 18.01.2019.
 */

public class BackendNewMatchDialog extends ControllerMenuDialog {
    private final LightBlocksGame app;
    private final Button randomPlayerButton;
    private final BeginningLevelChooser beginningLevelSlider;
    private final Button leaveButton;
    private final EditableLabel nicknameEditable;
    private final Button againstFriendButton;
    private String opponentId;

    public BackendNewMatchDialog(LightBlocksGame app) {
        super("", app.skin);
        this.app = app;

        leaveButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);

        getButtonTable().defaults().pad(20, 40, 20, 40);
        button(leaveButton);
        ShareButton shareButton = new ShareButton(app);
        getButtonTable().add(shareButton);
        addFocusableActor(shareButton);

        beginningLevelSlider = new BeginningLevelChooser(app, app.localPrefs.getBattleBeginningLevel(), 9) {
            @Override
            protected void onControllerDefaultKeyDown() {
                ((MyStage) getStage()).setFocusedActor(randomPlayerButton);
            }
        };
        addFocusableActor(beginningLevelSlider.getSlider());
        Table levelSliderTable = new Table();
        levelSliderTable.add(new ScaledLabel(app.TEXTS.get("labelBeginningMaxLevel"), app.skin,
                LightBlocksGame.SKIN_FONT_REG)).left();
        levelSliderTable.row();
        levelSliderTable.add(beginningLevelSlider);


        randomPlayerButton = new RoundedTextButton(app.TEXTS.get("buttonRandomOpponent"), app.skin);
        randomPlayerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                openNewMatch(null);
            }
        });
        addFocusableActor(randomPlayerButton);

        againstFriendButton = new RoundedTextButton(app.TEXTS.get("buttonChallengeFriend"), app.skin);
        againstFriendButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                openNewMatch(opponentId);
            }
        });
        againstFriendButton.setDisabled(true);
        addFocusableActor(againstFriendButton);

        Table contentTable = getContentTable();
        contentTable.add(new ScaledLabel(app.TEXTS.get("buttonNewBattle"), app.skin, LightBlocksGame.SKIN_FONT_TITLE)
        ).pad(10);

        contentTable.row().pad(15, 20, 15, 20);
        contentTable.add(levelSliderTable);

        contentTable.row();
        contentTable.add(randomPlayerButton);

        contentTable.row().padTop(15);
        contentTable.add(new ScaledLabel(app.TEXTS.get("labelChallengeAFriend"), app.skin,
                LightBlocksGame.SKIN_FONT_TITLE, 0.5f));
        contentTable.row().padTop(10);
        nicknameEditable = new EditableLabel(new ScaledLabel("", app.skin, LightBlocksGame
                .SKIN_EDIT_BIG),
                new FaButton(FontAwesome.SETTING_PENCIL, app.skin), app.skin, "Nickname") {
            @Override
            protected void onNewTextSet(String newText) {
                searchForFriendNick(newText);
            }

        };
        contentTable.add(nicknameEditable).pad(0, 20, 0, 20);
        addFocusableActor(nicknameEditable);
        nicknameEditable.setWidth(LightBlocksGame.nativeGameWidth - 100);
        nicknameEditable.getLabel().setEllipsis(true);

        contentTable.row();
        contentTable.add(againstFriendButton).padBottom(10);
    }

    private void searchForFriendNick(final String nick) {
        if (nick == null || nick.isEmpty())
            return;

        app.backendManager.getBackendClient().fetchPlayerByNicknamePrefixList(nick,
                new WaitForResponse<List<PlayerDetails>>(app, getStage()) {
                    @Override
                    public void onRequestSuccess(List<PlayerDetails> retrievedData) {
                        if (!retrievedData.isEmpty()) {
                            super.onRequestSuccess(retrievedData);
                            nicknameEditable.getLabel().setText(retrievedData.get(0).getUserNickName());
                            opponentId = retrievedData.get(0).uuid;
                            againstFriendButton.setDisabled(false);
                        } else
                            showError("No profile with nickname " + nick + " found");
                    }
                });
    }

    private void openNewMatch(String opponentId) {
        app.localPrefs.saveBattleBeginningLevel(beginningLevelSlider.getValue());
        app.backendManager.openNewMultiplayerMatch(opponentId, beginningLevelSlider.getValue(),
                new WaitForResponse<MatchEntity>(app, getStage()) {
                    @Override
                    protected void onSuccess() {
                        hide();
                    }
                });
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return randomPlayerButton;
    }

    @Override
    protected Actor getConfiguredEscapeActor() {
        return leaveButton;
    }
}
