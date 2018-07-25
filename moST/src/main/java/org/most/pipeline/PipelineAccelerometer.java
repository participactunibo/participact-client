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

import java.util.HashSet;
import java.util.Set;

import org.most.DataBundle;
import org.most.MoSTApplication;
import org.most.input.Input;
import org.most.input.InputAccelerometer;
import org.most.persistence.DBAdapter;

import android.content.ContentValues;

public class PipelineAccelerometer extends Pipeline {

    public final static String TBL_ACCELEROMETER = "ACCELEROMETER";
    public final static String FLD_X = "X";
    public final static String FLD_Y = "Y";
    public final static String FLD_Z = "Z";
    public final static String FLD_TIMESTAMP = "TIMESTAMP";

    // Any Pipeline that wants to dump data must define 2 String:
    // 1)a String that includes the table structure. The String must use this
    // pattern: "FieldName Type [opt] [,]". For example
    // "ID INTEGER PRIMARY KEY, XVALUES REAL NOT NULL, DAYOFWEEK TEXT" ecc...
    // 2)a String that define the table name.
    public static final String CREATE_ACCELEROMETER_TABLE = String.format(
            "_ID INTEGER PRIMARY KEY, %s REAL NOT NULL, %s REAL NOT NULL, %s REAL NOT NULL, %s INT NOT NULL", FLD_X,
            FLD_Y, FLD_Z, FLD_TIMESTAMP);

    private boolean _dump;
    private DBAdapter _dbAdapter;

    public PipelineAccelerometer(MoSTApplication context) {
        this(context, false);
        _dbAdapter = context.getDbAdapter();
    }

    public PipelineAccelerometer(MoSTApplication context, boolean dump) {
        super(context, 50);
        // setDump(dump);
        _dump = dump;
        _dbAdapter = context.getDbAdapter();
    }

    public void onData(DataBundle b) {
        if (_dump) {
            float[] data = b.getFloatArray(InputAccelerometer.KEY_ACCELERATIONS);
            long timestamp = b.getLong(InputAccelerometer.KEY_TIMESTAMP);
            ContentValues map = new ContentValues();
            map.put(FLD_X, data[0]);
            map.put(FLD_Y, data[1]);
            map.put(FLD_Z, data[2]);
            map.put(FLD_TIMESTAMP, timestamp);

            _dbAdapter.storeData(TBL_ACCELEROMETER, map);
        }
        b.release();
    }

    @Override
    public Set<Input.Type> getInputs() {
        Set<Input.Type> result = new HashSet<Input.Type>();
        result.add(Input.Type.ACCELEROMETER);
        return result;
    }

    @Override
    public Type getType() {
        return Type.ACCELEROMETER;
    }

    public boolean isDumpActive() {
        return _dump;
    }

}
