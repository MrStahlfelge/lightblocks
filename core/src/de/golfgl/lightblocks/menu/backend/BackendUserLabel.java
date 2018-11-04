package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.IPlayerInfo;
import de.golfgl.lightblocks.scene2d.FaTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;

/**
 * Created by Benjamin Schulte on 06.10.2018.
 */

public class BackendUserLabel extends FaTextButton {
    private final String nickName;
    private final String gameService;
    private final String decoration;
    private final Cell<ScaledLabel> decorationCell;

    public BackendUserLabel(String nickName, final String userId, String decoration, final LightBlocksGame app,
                            String styleName) {
        super("", app.skin, styleName);

        int atSignIdx = nickName.indexOf('@');
        this.nickName = atSignIdx > 0 ? nickName.substring(0, atSignIdx) : nickName;
        this.gameService = atSignIdx > 0 ? nickName.substring(atSignIdx + 1) : null;
        this.decoration = decoration;

        setText(this.nickName);
        getLabel().setAlignment(Align.left);

        ScaledLabel actor = new ScaledLabel("@", app.skin, LightBlocksGame.SKIN_FONT_REG);
        decorationCell = add(gameService != null ? actor : null).left().expandX();

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                new BackendUserDetailsScreen(app, userId).show(getStage());
            }
        };
        addListener(listener);
    }

    public BackendUserLabel(IPlayerInfo playerInfo, LightBlocksGame app, String styleName) {
        this(playerInfo.getUserNickName(), playerInfo.getUserId(), playerInfo.getUserDecoration(), app, styleName);
    }

    /**
     * schaltet das Buttonverhalten ab
     */
    public BackendUserLabel setToLabelMode() {
        setTouchable(Touchable.childrenOnly);
        getListeners().clear();
        return this;
    }

    public void setMaxLabelWidth(float maxWidth) {
        if (getLabel().getPrefWidth() + decorationCell.getPrefWidth() >= maxWidth) {
            getLabel().setEllipsis(true);
            getLabelCell().width(maxWidth - decorationCell.getPrefWidth());
        }
    }

    public String getNickName() {
        return nickName;
    }
}
