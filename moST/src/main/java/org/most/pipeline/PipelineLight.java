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
import org.most.input.Input;
import org.most.input.LightInput;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

public class PipelineLight extends Pipeline {

    public static final String PREF_KEY_DUMP_TO_DB = "PipelineLight.DumpToDB";
    public static final boolean PREF_DEFAULT_DUMP_TO_DB = true;
    public static final String PREF_KEY_SEND_INTENT = "PipelineLight.SendIntent";
    public static final boolean PREF_DEFAULT_SEND_INTENT = false;

    public static final String KEY_ACTION = "PipelineLight";

    public static final String KEY_VALUE = "PipelineLight.value";

    public static final String TBL_LIGHT = "LIGHT";
    public static final String FLD_TIMESTAMP = "timestamp";
    public static final String FLD_VALUE = "value";

    public static final String CREATE_LIGHT_TABLE = String.format(
            "_ID INTEGER PRIMARY KEY, %s INT NOT NULL, %s REAL NOT NULL", FLD_TIMESTAMP, FLD_VALUE);

    protected boolean _isDump;
    protected boolean _isSend;

    public PipelineLight(MoSTApplication context) {
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
                cv.put(FLD_VALUE, b.getFloat(LightInput.KEY_VALUE));
                getContext().getDbAdapter().storeData(TBL_LIGHT, cv, true);
            }
            if (_isSend) {
                Intent i = new Intent(KEY_ACTION);
                i.putExtra(KEY_TIMESTAMP, b.getLong(Input.KEY_TIMESTAMP));
                i.putExtra(KEY_VALUE, b.getFloat(LightInput.KEY_VALUE));
                getContext().sendBroadcast(i);
            }
        } finally {
            b.release();
        }
    }

    @Override
    public Type getType() {
        return Type.LIGHT;
    }

    @Override
    public Set<org.most.input.Input.Type> getInputs() {
        Set<Input.Type> result = new TreeSet<Input.Type>();
        result.add(Input.Type.LIGHT);
        return result;
    }

}
