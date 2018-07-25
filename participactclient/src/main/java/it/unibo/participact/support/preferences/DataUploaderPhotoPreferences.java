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

public class DataUploaderPhotoPreferences {

    private static DataUploaderPhotoPreferences instance;
    private static SharedPreferences sharedPreferences;

    private static final String KEY_PHOTO_UPLOAD = "DataUploaderPhotoPreferences.keyPhoto";
    private static final boolean PHOTO_UPLOAD_DEFAULT_VALUE = true;

    private static final int MODE = Context.MODE_PRIVATE;
    private static final String NAME = "DATA_UPLOADER_PHOTO_PREFERENCES";

    public static synchronized DataUploaderPhotoPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new DataUploaderPhotoPreferences();
            sharedPreferences = context.getApplicationContext().getSharedPreferences(NAME, MODE);
        }
        return instance;
    }

    public boolean getPhotoUpload() {
        return sharedPreferences.getBoolean(KEY_PHOTO_UPLOAD, PHOTO_UPLOAD_DEFAULT_VALUE);
    }

    public void setPhotoUpload(boolean value) {
        Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_PHOTO_UPLOAD, value);
        editor.apply();
    }

}
