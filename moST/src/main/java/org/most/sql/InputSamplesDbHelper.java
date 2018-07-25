/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package org.most.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.most.sql.InputSamplesContract.InputSamplesEntry;

public class InputSamplesDbHelper extends SQLiteOpenHelper {
    public static final String TAG = InputSamplesDbHelper.class.getSimpleName();
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "InputSamples.db";

    //DB TYPES
    private static final String TEXT_TYPE = " TEXT";
    private static final String DOUBLE_TYPE = " FLOAT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String BIGINT_TYPE = "BIGINT";
    private static final String COMMA_SEP = ",";

    //CRUD METHODS
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + InputSamplesEntry.TABLE_NAME + " (" +
                    InputSamplesEntry._ID + " INTEGER PRIMARY KEY," +
                    InputSamplesEntry.TIMESTAMP_COLUMN + BIGINT_TYPE + COMMA_SEP +
                    InputSamplesEntry.NORM_COLUMN + DOUBLE_TYPE + COMMA_SEP +
                    InputSamplesEntry.SAMPLES_COLUMN + TEXT_TYPE + COMMA_SEP +
                    InputSamplesEntry.GPS_DISTANCE_COLUMN + DOUBLE_TYPE + COMMA_SEP +
                    InputSamplesEntry.ACTIVITY_TYPE_COLUMN + TEXT_TYPE + COMMA_SEP +
                    InputSamplesEntry.ACTIVITY_POLE_COLUMN + INTEGER_TYPE + " )";

    private static final String SQL_DELETE_SAMPLES =
            "DROP TABLE IF EXISTS " + InputSamplesEntry.TABLE_NAME;
    ;

    public InputSamplesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_SAMPLES);
        onCreate(db);
    }

}
