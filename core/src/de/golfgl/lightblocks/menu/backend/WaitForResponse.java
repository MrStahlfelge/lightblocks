package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.scenes.scene2d.Stage;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.BackendManager;
import de.golfgl.lightblocks.scene2d.ProgressDialog;

/**
 * Blendet Progressdialog ein und bei Fehlern die Fehlermeldung
 * <p>
 * Created by Benjamin Schulte on 18.10.2018.
 */

public class WaitForResponse extends BackendManager.AbstractQueuedBackendResponse<Void> {
    private ProgressDialog progressDialog;

    public WaitForResponse(LightBlocksGame app, Stage stage) {
        super(app);
        progressDialog = new ProgressDialog(app.TEXTS.get
                ("pleaseWaitLabel"), app, LightBlocksGame.nativeGameWidth * .8f);
        progressDialog.show(stage);
    }

    @Override
    public void onRequestFailed(int statusCode, final String errorMsg) {
        progressDialog.getLabel().setText(errorMsg);
        progressDialog.showOkButton();
    }

    @Override
    public void onRequestSuccess(Void retrievedData) {
        progressDialog.hide();
        onSuccess();
    }

    protected void onSuccess() {

    }
}
