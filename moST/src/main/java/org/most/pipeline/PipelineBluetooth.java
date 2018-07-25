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
import org.most.input.BluetoothScanInput;
import org.most.input.Input;
import org.most.persistence.DBAdapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

/**
 * Configuration:
 * <ul>
 * <li>{@link #PREF_KEY_DUMP_TO_DB} (boolean): if <code>true</code>, scanned
 * devices data are written in MoST's DB. The DB stores the timestamp of the
 * scan, MAC address, friendly name, major device class, full device class.
 * Default setting: {@value #PREF_DEFAULT_DUMP_TO_DB}.</li>
 * <li>{@link #PREF_KEY_SEND_INTENT} (boolean): if <code>true</code>, the
 * pipeline periodically broadcasts an {@link Intent}.</li>
 * </ul>
 *
 */
public class PipelineBluetooth extends Pipeline {

    public static final String PREF_KEY_DUMP_TO_DB = "PipelineBluetooth.DumpToDB";
    public static final boolean PREF_DEFAULT_DUMP_TO_DB = true;
    public static final String PREF_KEY_SEND_INTENT = "PipelineBluetooth.SendIntent";
    public static final boolean PREF_DEFAULT_SEND_INTENT = false;

    public static final String KEY_ACTION = "PipelineBluetooth";

    public final static String KEY_MAC = "PipelineBluetooth.MAC";
    public final static String KEY_NAME = "PipelineBluetooth.FriendlyName";
    public final static String KEY_DEVICECLASS = "PipelineBluetooth.DeviceClass";
    public final static String KEY_DEVICEMAJORCLASS = "PipelineBluetooth.DeviceMajorClass";

    public final static String FLD_MAC = "mac";
    public final static String FLD_TIMESTAMP = "timestamp";
    public final static String FLD_NAME = "friendly_name";
    public final static String FLD_DEVICECLASS = "class";
    public final static String FLD_DEVICEMAJORCLASS = "major_class";
    public final static String TBL_BLUETOOH = "BLUETOOTH";
    public final static String CREATE_BLUETOOTH_TABLE = String
            .format("_ID INTEGER PRIMARY KEY, %s INT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL, %s INT NOT NULL, %s INT NOT NULL",
                    FLD_TIMESTAMP, FLD_MAC, FLD_NAME, FLD_DEVICECLASS, FLD_DEVICEMAJORCLASS);

    protected boolean _isDump;
    protected boolean _isSend;

    public PipelineBluetooth(MoSTApplication context) {
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
                cv.put(FLD_MAC, b.getString(BluetoothScanInput.KEY_MAC));
                cv.put(FLD_NAME, b.getString(BluetoothScanInput.KEY_NAME));
                cv.put(FLD_DEVICEMAJORCLASS, b.getInt(BluetoothScanInput.KEY_DEVICEMAJORCLASS));
                cv.put(FLD_DEVICECLASS, b.getInt(BluetoothScanInput.KEY_DEVICECLASS));
                DBAdapter dba = getContext().getDbAdapter();
                dba.storeData(TBL_BLUETOOH, cv, true);
            }

            if (_isSend) {
                Intent i = new Intent(KEY_ACTION);
                i.putExtra(KEY_TIMESTAMP, b.getLong(Input.KEY_TIMESTAMP));
                i.putExtra(KEY_MAC, b.getString(BluetoothScanInput.KEY_MAC));
                i.putExtra(KEY_NAME, b.getString(BluetoothScanInput.KEY_NAME));
                i.putExtra(KEY_DEVICECLASS, b.getInt(BluetoothScanInput.KEY_DEVICECLASS));
                i.putExtra(KEY_DEVICEMAJORCLASS, b.getInt(BluetoothScanInput.KEY_DEVICEMAJORCLASS));
                getContext().sendBroadcast(i);
            }
        } finally {
            b.release();
        }
    }

    @Override
    public Type getType() {
        return Type.BLUETOOTH;
    }

    @Override
    public Set<org.most.input.Input.Type> getInputs() {
        Set<Input.Type> result = new TreeSet<Input.Type>();
        result.add(Input.Type.BLUETOOTHSCAN);
        return result;
    }

}
