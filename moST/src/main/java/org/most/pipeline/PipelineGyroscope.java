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
import org.most.input.GyroscopeInput;
import org.most.input.Input;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

/**
 * This pipeline publishes
 *
 */
public class PipelineGyroscope extends Pipeline {

    public static final String PREF_KEY_DUMP_TO_DB = "PipelineGyroscope.DumpToDB";
    public static final boolean PREF_DEFAULT_DUMP_TO_DB = true;
    public static final String PREF_KEY_SEND_INTENT = "PipelineGyroscope.SendIntent";
    public static final boolean PREF_DEFAULT_SEND_INTENT = false;

    public static final String KEY_ACTION = "PipelineGyroscope";

    public static final String KEY_ROTATION_X = "PipelineGyroscope.rotation_x";
    public static final String KEY_ROTATION_Y = "PipelineGyroscope.rotation_y";
    public static final String KEY_ROTATION_Z = "PipelineGyroscope.rotation_z";

    public static final String TBL_GYROSCOPE = "GYROSCOPE";
    public static final String FLD_TIMESTAMP = "timestamp";
    public static final String FLD_ROTATION_X = "rotation_x";
    public static final String FLD_ROTATION_Y = "rotation_y";
    public static final String FLD_ROTATION_Z = "rotation_z";
    public static final String CREATE_GYROSCOPE_TABLE = String.format(
            "_ID INTEGER PRIMARY KEY, %s INT NOT NULL, %s REAL NOT NULL, %s REAL NOT NULL, %s INT NOT NULL",
            FLD_TIMESTAMP, FLD_ROTATION_X, FLD_ROTATION_Y, FLD_ROTATION_Z);

    protected boolean _isDump;
    protected boolean _isSend;

    public PipelineGyroscope(MoSTApplication context) {
        super(context);
    }

    @Override
    public boolean onActivate() {
        checkNewState(State.ACTIVATED);
        _isDump = getContext().getSharedPreferences(MoSTApplication.PREF_PIPELINES, Context.MODE_PRIVATE).getBoolean(
                PREF_KEY_DUMP_TO_DB, PREF_DEFAULT_DUMP_TO_DB);
        _isSend = getContext().getSharedPreferences(MoSTApplication.PREF_PIPELINES, Context.MODE_PRIVATE).getBoolean(
                PREF_KEY_SEND_INTENT, PREF_DEFAULT_SEND_INTENT);
        return super.onActivate();
    }

    public void onData(DataBundle b) {
        try {
            if (_isDump) {
                ContentValues cv = new ContentValues();
                cv.put(FLD_TIMESTAMP, b.getLong(Input.KEY_TIMESTAMP));
                cv.put(FLD_ROTATION_X, b.getFloat(GyroscopeInput.KEY_ROTATION_X));
                cv.put(FLD_ROTATION_Y, b.getFloat(GyroscopeInput.KEY_ROTATION_Y));
                cv.put(FLD_ROTATION_Z, b.getFloat(GyroscopeInput.KEY_ROTATION_Z));
                getContext().getDbAdapter().storeData(TBL_GYROSCOPE, cv);
            }
            if (_isSend) {
                Intent i = new Intent(KEY_ACTION);
                i.putExtra(KEY_TIMESTAMP, b.getLong(Input.KEY_TIMESTAMP));
                i.putExtra(KEY_ROTATION_X, b.getFloat(GyroscopeInput.KEY_ROTATION_X));
                i.putExtra(KEY_ROTATION_Y, b.getFloat(GyroscopeInput.KEY_ROTATION_Y));
                i.putExtra(KEY_ROTATION_Z, b.getFloat(GyroscopeInput.KEY_ROTATION_Z));
                getContext().sendBroadcast(i);
            }
        } finally {
            b.release();
        }
    }

    @Override
    public Type getType() {
        return Type.GYROSCOPE;
    }

    @Override
    public Set<org.most.input.Input.Type> getInputs() {
        Set<Input.Type> result = new TreeSet<Input.Type>();
        result.add(Input.Type.GYROSCOPE);
        return result;
    }

}
