/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.support.preferences;


import android.content.Context;
import android.content.SharedPreferences;

public class ShowTipsPreferences {

    private static ShowTipsPreferences instance;
    private static SharedPreferences sharedPreferences;

    private static final boolean DEFAULT_SHOW_VALUE = true;

    private static final int MODE = Context.MODE_PRIVATE;
    private static final String NAME = "SHOW_TIPS_PREFERENCES";

    public static synchronized ShowTipsPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new ShowTipsPreferences();
            sharedPreferences = context.getSharedPreferences(NAME, MODE);
        }
        return instance;
    }

    public boolean shouldShowTips(String scene) {
        return sharedPreferences.getBoolean(scene, DEFAULT_SHOW_VALUE);
    }

    public void setShouldShowTips(String scene, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(scene, value);
        editor.apply();
    }
}
