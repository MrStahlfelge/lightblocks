package de.golfgl.lightblocks;

import com.badlogic.gdx.backends.gwt.preloader.DefaultAssetFilter;

/**
 * Created by Benjamin Schulte on 28.03.2018.
 */

public class AssetFilter extends DefaultAssetFilter {
    @Override
    public boolean accept(String file, boolean isDirectory) {
        String normFile = file.replace('\\', '/');
        if (isDirectory && normFile.endsWith("/data")) return false;
        return super.accept(file, isDirectory);
    }
}
