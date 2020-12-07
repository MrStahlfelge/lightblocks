package de.golfgl.lightblocks.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controllers;

import javax.annotation.Nullable;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.input.InputIdentifier;
import de.golfgl.lightblocks.screen.AbstractScreen;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * On device multiplayer mode
 */
public class DeviceMultiplayerModel extends AbstractMultiplayerModel<DeviceMultiplayerModel> {
    public static final String MODEL_ID = "deviceMultiplayer";
    private InputIdentifier myInputId;

    @Override
    public InitGameParameters getInitParameters() {
        InitGameParameters retVal = super.getInitParameters();
        retVal.setGameMode(InitGameParameters.GameMode.DeviceMultiplayer);

        if (isFirstPlayer()) {
            retVal.setPlayerInputIds(myInputId, getSecondGameModel().myInputId);
        }

        return retVal;
    }

    @Override
    public String getIdentifier() {
        return MODEL_ID;
    }

    @Override
    public void checkPrerequisites(LightBlocksGame app) throws VetoException {
        int inputDevices = Controllers.getControllers().size +
                ((Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard)) ? 1 : 0);

        if (!((AbstractScreen) app.getScreen()).isLandscape() || inputDevices < 2) {
            throw new VetoException(app.TEXTS.get("messageDevMultiPrerequisites"));
        }
    }

    @Override
    protected DeviceMultiplayerModel createSecondGameModel(InitGameParameters newGameParams) {
        DeviceMultiplayerModel deviceMultiplayerModel = new DeviceMultiplayerModel();
        if (newGameParams.getFirstPlayerInputId() != null && newGameParams.getSecondPlayerInputId() != null) {
            myInputId = newGameParams.getFirstPlayerInputId();
            deviceMultiplayerModel.myInputId = newGameParams.getSecondPlayerInputId();
        }
        return deviceMultiplayerModel;
    }

    @Override
    public void setUserInterface(LightBlocksGame app, PlayScreen userInterface, IGameModelListener uiGameboard) {
        super.setUserInterface(app, userInterface, uiGameboard);
        if (isFirstPlayer() && myInputId == null) {
            playScreen.showOverlayMessage("labelDevMultiChooseDevice", String.valueOf(1));
        }
    }

    private boolean isOtherPlayerInput(InputIdentifier inputId) {
        if (!isFirstPlayer()) {
            return false;
        }

        if (getSecondGameModel().myInputId == null) {
            if (!inputId.isSameInput(myInputId) && !playScreen.isPaused()) {
                getSecondGameModel().myInputId = inputId;
                playScreen.showOverlayMessage(null);
            }
            return false;
        }

        return getSecondGameModel().myInputId.isSameInput(inputId);
    }

    private boolean isMyInput(InputIdentifier inputId) {
        if (myInputId == null) {
            if (!playScreen.isPaused()) {
                // input not set yet, set it now
                myInputId = inputId;
                if (isFirstPlayer()) {
                    playScreen.showOverlayMessage("labelDevMultiChooseDevice", String.valueOf(2));
                }
            }
            return false;
        }

        return myInputId.isSameInput(inputId);
    }

    @Nullable
    @Override
    public InputIdentifier getFixedInputId() {
        return myInputId;
    }

    @Override
    public String getGoalDescription() {
        return "goalModelMultiplayer";
    }

    @Override
    public boolean beginPaused() {
        return false;
    }

    @Override
    public void update(float delta) {
        if (myInputId == null || isFirstPlayer() && getSecondGameModel().myInputId == null) {
            return;
        }

        super.update(delta);
    }

    @Override
    public boolean isSecondGameboardOptional() {
        return false;
    }

    @Override
    public boolean inputHoldActiveTetromino(InputIdentifier inputId) {
        if (isMyInput(inputId)) {
            return super.inputHoldActiveTetromino(inputId);
        } else if (isOtherPlayerInput(inputId)) {
            return secondGameModel.inputHoldActiveTetromino(inputId);
        } else {
            return false;
        }
    }

    @Override
    public void inputSetSoftDropFactor(InputIdentifier inputId, float newVal) {
        if (isMyInput(inputId)) {
            super.inputSetSoftDropFactor(inputId, newVal);
        } else if (isOtherPlayerInput(inputId)) {
            secondGameModel.inputSetSoftDropFactor(inputId, newVal);
        }
    }

    @Override
    public void inputRotate(InputIdentifier inputId, boolean clockwise) {
        if (isMyInput(inputId)) {
            super.inputRotate(inputId, clockwise);
        } else if (isOtherPlayerInput(inputId)) {
            secondGameModel.inputRotate(inputId, clockwise);
        }
    }

    @Override
    public boolean inputTimelabelTouched(InputIdentifier inputId) {
        if (isMyInput(inputId)) {
            return super.inputTimelabelTouched(inputId);
        } else if (isOtherPlayerInput(inputId)) {
            return secondGameModel.inputTimelabelTouched(inputId);
        } else {
            return false;
        }
    }

    @Override
    public void inputStartMoveHorizontal(InputIdentifier inputId, boolean isLeft) {
        if (isMyInput(inputId)) {
            super.inputStartMoveHorizontal(inputId, isLeft);
        } else if (isOtherPlayerInput(inputId)) {
            secondGameModel.inputStartMoveHorizontal(inputId, isLeft);
        }
    }

    @Override
    public void inputDoOneHorizontalMove(InputIdentifier inputId, boolean isLeft) {
        if (isMyInput(inputId)) {
            super.inputDoOneHorizontalMove(inputId, isLeft);
        } else if (isOtherPlayerInput(inputId)) {
            secondGameModel.inputDoOneHorizontalMove(inputId, isLeft);
        }
    }

    @Override
    public void inputEndMoveHorizontal(InputIdentifier inputId, boolean isLeft) {
        if (isMyInput(inputId)) {
            super.inputEndMoveHorizontal(inputId, isLeft);
        } else if (isOtherPlayerInput(inputId)) {
            secondGameModel.inputEndMoveHorizontal(inputId, isLeft);
        }
    }

}
