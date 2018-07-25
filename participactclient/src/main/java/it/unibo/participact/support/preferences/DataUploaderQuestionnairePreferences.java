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

public class DataUploaderQuestionnairePreferences {

    private static DataUploaderQuestionnairePreferences instance;
    private static SharedPreferences sharedPreferences;

    private static final String KEY_QUESTIONNAIRE_UPLOAD = "DataUploaderQuestionnairePreferences.keyQuestionnaire";
    private static final boolean QUESTIONNAIRE_UPLOAD_DEFAULT_VALUE = false;

    private static final int MODE = Context.MODE_PRIVATE;
    private static final String NAME = "DATA_UPLOADER_QUESTIONNAIRE_PREFERENCES";

    public static synchronized DataUploaderQuestionnairePreferences getInstance(Context context) {
        if (instance == null) {
            instance = new DataUploaderQuestionnairePreferences();
            sharedPreferences = context.getApplicationContext().getSharedPreferences(NAME, MODE);
        }
        return instance;
    }

    public boolean getQuestionnaireUpload() {
        return sharedPreferences.getBoolean(KEY_QUESTIONNAIRE_UPLOAD, QUESTIONNAIRE_UPLOAD_DEFAULT_VALUE);
    }

    public void setQuestionnaireUpload(boolean value) {
        Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_QUESTIONNAIRE_UPLOAD, value);
        editor.apply();
    }

}
