package com.hangpy.hangpy.actionbar;

import android.support.v7.app.ActionBar;

/**
 * Provides methods for customizing the default action bar.
 */
public class ActionBarUtils {
    private static ActionBarUtils instance = new ActionBarUtils();

    public static ActionBarUtils getInstance() {
        return instance;
    }

    private ActionBarUtils() {
    }
}
