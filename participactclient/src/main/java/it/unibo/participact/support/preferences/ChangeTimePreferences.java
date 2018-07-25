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
import android.content.SharedPreferences.Editor;

public class ChangeTimePreferences {

    private static ChangeTimePreferences instance;
    private static SharedPreferences sharedPreferences;

    private static final String KEY_CHANGE_TIME_UPLOAD = "ChangeTimePreferences.keyDate";
    private static final boolean CHANGE_TIME_UPLOAD_DEFAULT_VALUE = false;

    private static final String KEY_LAST_ELAPSED = "ChangeTimePreferences.elapsed";
    private static final long LAST_ELAPSED_DEFAULT_VALUE = 0L;

    private static final String KEY_LAST_CURRENT_MILLIS = "ChangeTimePreferences.currentmillis";
    private static final long LAST_CURRENT_MILLIS_DEFAULT_VALUE = 0L;

    private static final int MODE = Context.MODE_MULTI_PROCESS;
    private static final String NAME = "DATA_UPLOADER_CHANGE_TIME_PREFERENCES";

    public static synchronized ChangeTimePreferences getInstance(Context context) {
        if (instance == null) {
            instance = new ChangeTimePreferences();
            sharedPreferences = context.getApplicationContext().getSharedPreferences(NAME, MODE);
        }
        return instance;
    }

    public boolean getChangeTimeRequest() {
        return sharedPreferences.getBoolean(KEY_CHANGE_TIME_UPLOAD, CHANGE_TIME_UPLOAD_DEFAULT_VALUE);
    }

    public void setChangeTimeRequest(boolean value) {
        Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_CHANGE_TIME_UPLOAD, value);
        editor.apply();
    }

    public long getLastElapsedChecked() {
        return sharedPreferences.getLong(KEY_LAST_ELAPSED, LAST_ELAPSED_DEFAULT_VALUE);
    }

    public void setLastElapsedChecked(long value) {
        Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_LAST_ELAPSED, value);
        editor.apply();
    }

    public long getLastCurrentMillisChecked() {
        return sharedPreferences.getLong(KEY_LAST_CURRENT_MILLIS, LAST_CURRENT_MILLIS_DEFAULT_VALUE);
    }

    public void setLastCurrentMillisChecked(long value) {
        Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_LAST_CURRENT_MILLIS, value);
        editor.apply();
    }

}
