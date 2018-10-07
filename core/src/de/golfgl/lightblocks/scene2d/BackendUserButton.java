package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 06.10.2018.
 */

public class BackendUserButton extends FaTextButton {
    private final String nickName;
    private final String gameService;
    private final String decoration;
    private final Cell<ScaledLabel> decorationCell;

    public BackendUserButton(String nickName, String userId, String decoration, LightBlocksGame app, String styleName) {
        super("", app.skin, styleName);

        int atSignIdx = nickName.indexOf('@');
        this.nickName = atSignIdx > 0 ? nickName.substring(0, atSignIdx) : nickName;
        this.gameService = atSignIdx > 0 ? nickName.substring(atSignIdx + 1) : null;
        this.decoration = decoration;

        setText(this.nickName);
        getLabel().setAlignment(Align.left);

        ScaledLabel actor = new ScaledLabel("@", app.skin, LightBlocksGame.SKIN_FONT_REG);
        decorationCell = add(gameService != null ? actor : null).left().expandX();
    }

    public void setMaxLabelWidth(float maxWidth) {
        if (getLabel().getPrefWidth() + decorationCell.getPrefWidth() >= maxWidth) {
            getLabel().setEllipsis(true);
            getLabelCell().width(maxWidth - decorationCell.getPrefWidth());
        }
    }
}
