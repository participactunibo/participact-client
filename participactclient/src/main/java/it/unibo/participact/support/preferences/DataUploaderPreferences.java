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

public class DataUploaderPreferences {

    private static DataUploaderPreferences instance;
    private static SharedPreferences sharedPreferences;

    private static final long DEFAULT_RES_VALUE = 0L;
    private static final long TIME_THRESHOLD = 1000 * 60 * 15; //15 minutes

    private static final int MODE = Context.MODE_PRIVATE;
    private static final String NAME = "DATA_UPLOADER_PREFERENCES";

    public static synchronized DataUploaderPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new DataUploaderPreferences();
            sharedPreferences = context.getSharedPreferences(NAME, MODE);
        }
        return instance;
    }

    public boolean checkLastUpload(String dataUploadType) {
        long lastUpload = sharedPreferences.getLong(dataUploadType, DEFAULT_RES_VALUE);
        if (lastUpload == DEFAULT_RES_VALUE || (lastUpload + TIME_THRESHOLD <= System.currentTimeMillis())) {
            return true;
        } else {
            return false;
        }
    }

    public long getLastUpload(String dataUploadType) {
        return sharedPreferences.getLong(dataUploadType, DEFAULT_RES_VALUE);
    }

    public void setLastUpload(String dataUploadType, long value) {
        Editor editor = sharedPreferences.edit();
        editor.putLong(dataUploadType, value);
        editor.apply();
    }

}
