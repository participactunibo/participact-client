/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */
package org.most.pipeline;

import java.util.Set;
import java.util.TreeSet;

import org.most.DataBundle;
import org.most.MoSTApplication;
import org.most.input.GoogleActivityRecognitionInput;
import org.most.input.Input;
import org.most.persistence.DBAdapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

public class PipelineGoogleActivityRecognition extends Pipeline {

    public static final String PREF_KEY_DUMP_TO_DB = "PipelineGoogleActivityRecognition.DumpToDB";
    public static final boolean PREF_DEFAULT_DUMP_TO_DB = true;
    public static final String PREF_KEY_SEND_INTENT = "PipelineGoogleActivityRecognition.SendIntent";
    public static final boolean PREF_DEFAULT_SEND_INTENT = false;

    // intent
    public static final String KEY_ACTION = "PipelineGoogleActivityRecognition";
    public final static String KEY_CONFIDENCE = "PipelineGoogleActivityRecognition.confidence";
    public final static String KEY_RECOGNIZED_ACTIVITY = "PipelineGoogleActivityRecognition.PipelineGoogleActivityRecognition";

    public final static String FLD_TIMESTAMP = "TIMESTAMP";
    public final static String FLD_CONFIDENCE = "CONFIDENCE";
    public final static String FLD_RECOGNIZED_ACTIVITY = "RECOGNIZED_ACTIVITY";

    public final static String TBL_GOOGLE_ACTIVITY_RECOGNITION = "GOOGLE_ACTIVITY_RECOGNITION";

    public static final String CREATE_GOOGLE_ACTIVITY_RECOGNITION_TABLE = String.format(
            "_ID INTEGER PRIMARY KEY, %s INT NOT NULL, %s TEXT NOT NULL, %s INT NOT NULL",
            FLD_TIMESTAMP, FLD_RECOGNIZED_ACTIVITY, FLD_CONFIDENCE);

    private boolean _isDump;
    private boolean _isSend;
    private DBAdapter _dbAdapter;

    public PipelineGoogleActivityRecognition(MoSTApplication context) {
        super(context);
    }

    @Override
    public boolean onActivate() {
        _isDump = getContext().getSharedPreferences(MoSTApplication.PREF_PIPELINES,
                Context.MODE_PRIVATE).getBoolean(PREF_KEY_DUMP_TO_DB, PREF_DEFAULT_DUMP_TO_DB);
        _isSend = getContext().getSharedPreferences(MoSTApplication.PREF_PIPELINES,
                Context.MODE_PRIVATE).getBoolean(PREF_KEY_SEND_INTENT, PREF_DEFAULT_SEND_INTENT);
        _dbAdapter = getContext().getDbAdapter();
        return super.onActivate();
    }

    public void onData(DataBundle b) {
        try {
            if (_isDump || _isSend) {

                if (_isDump) {
                    ContentValues cv = new ContentValues();
                    cv.put(KEY_TIMESTAMP, b.getLong(Input.KEY_TIMESTAMP));
                    cv.put(FLD_RECOGNIZED_ACTIVITY,
                            b.getString(GoogleActivityRecognitionInput.KEY_RECOGNIZED_ACTIVITY));
                    cv.put(FLD_CONFIDENCE, b.getInt(GoogleActivityRecognitionInput.KEY_CONFIDENCE));
                    _dbAdapter.storeData(TBL_GOOGLE_ACTIVITY_RECOGNITION, cv, true);
                }

                if (_isSend) {
                    Intent i = new Intent(KEY_ACTION);
                    i.putExtra(FLD_TIMESTAMP, b.getLong(Input.KEY_TIMESTAMP));
                    i.putExtra(KEY_RECOGNIZED_ACTIVITY,
                            b.getString(GoogleActivityRecognitionInput.KEY_RECOGNIZED_ACTIVITY));
                    i.putExtra(KEY_CONFIDENCE,
                            b.getInt(GoogleActivityRecognitionInput.KEY_CONFIDENCE));
                    getContext().sendBroadcast(i);
                }
            }
        } finally {
            b.release();
        }
    }

    @Override
    public Type getType() {
        return Type.GOOGLE_ACTIVITY_RECOGNITION;
    }

    @Override
    public Set<org.most.input.Input.Type> getInputs() {
        Set<Input.Type> result = new TreeSet<Input.Type>();
        result.add(Input.Type.GOOGLE_ACTIVITY_RECOGNITION);
        return result;
    }

}
