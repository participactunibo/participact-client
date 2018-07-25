/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */
package org.most.persistence;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.locks.ReentrantLock;

public class SDCardDBHelper {
    private static final String TAG = SDCardDBHelper.class.getSimpleName();

    public static final String DB_DIR_PATH = Environment
            .getExternalStorageDirectory() + "/data/MoST";
    public static final String DB_NAME = "MoST";
    public static final int DB_VERSION = 1;
    private static long _timestamp = System.currentTimeMillis();
    private String _currentDBNaming;

    private final static ReentrantLock _dbLock = new ReentrantLock();

    private ArrayList<String> _createTables;
    private ArrayList<String> _dropTables;

    // size in bytes
    private static final int _dbSizeLimit = 500000;

    private final CursorFactory _factory;
    private final int _newVersion;

    private SQLiteDatabase _db = null;
    private boolean _isInit = false;

    public SDCardDBHelper(Context context, CursorFactory factory, int version,
                          ArrayList<String> createTables, ArrayList<String> dropTables) {
        if (version < 1)
            throw new IllegalArgumentException("Version must be >= 1, was "
                    + version);
        _factory = factory;
        _newVersion = version;
        _createTables = createTables;
        _dropTables = dropTables;
        File f = new File(DB_DIR_PATH);
        f.mkdirs();
        updateDBName(_timestamp);
    }

    public SDCardDBHelper(Context context, ArrayList<String> createTables,
                          ArrayList<String> dropTables) {
        _factory = null;
        _newVersion = DB_VERSION;
        _createTables = createTables;
        _dropTables = dropTables;
        File f = new File(DB_DIR_PATH);
        f.mkdirs();
        updateDBName(_timestamp);
    }

    public synchronized SQLiteDatabase getWritableDatabase() {
        if (_db != null && _db.isOpen() && !_db.isReadOnly()) {
            return _db; // The database is already open for business
        }

        if (_isInit) {
            throw new IllegalStateException(
                    "getWritableDatabase called recursively");
        }

        boolean success = false;
        SQLiteDatabase db = null;
        if (_db != null)
            _dbLock.lock();
        try {
            _isInit = true;
            if (DB_NAME == null) {
                db = SQLiteDatabase.create(null);
            } else {

                String path = String.format("%s/%s", DB_DIR_PATH,
                        _currentDBNaming);
                db = SQLiteDatabase.openDatabase(path, _factory,
                        SQLiteDatabase.CREATE_IF_NECESSARY
                                | SQLiteDatabase.OPEN_READWRITE);
            }

            int version = db.getVersion();
            if (version != _newVersion) {
                db.beginTransaction();
                try {
                    if (version == 0) {
                        onCreate(db);
                    } else {
                        onUpgrade(db, version, _newVersion);
                    }
                    db.setVersion(_newVersion);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            onOpen(db);
            success = true;
            return db;
        } finally {
            _isInit = false;
            if (success) {
                if (_db != null) {
                    try {
                        _db.close();
                    } catch (Exception e) {
                    }
                    _dbLock.unlock();
                }
                _db = db;
            } else {
                if (_db != null)
                    _dbLock.unlock();
                if (db != null)
                    db.close();
            }
        }
    }

    public synchronized SQLiteDatabase getReadableDatabase() {
        if (_db != null && _db.isOpen()) {
            return _db; // The database is already open for business
        }

        if (_isInit) {
            throw new IllegalStateException(
                    "getReadableDatabase called recursively");
        }

        try {
            return getWritableDatabase();
        } catch (SQLiteException e) {
            if (DB_NAME == null)
                throw e; // Can't open a temp database read-only!
            Log.i(TAG, "Couldn't open " + DB_NAME
                    + " for writing (will try read-only):", e);
        }

        SQLiteDatabase db = null;
        try {
            _isInit = true;
            String path = String.format("%s/%s", DB_DIR_PATH, _currentDBNaming);
            db = SQLiteDatabase.openDatabase(path, _factory,
                    SQLiteDatabase.OPEN_READONLY
                            | SQLiteDatabase.CREATE_IF_NECESSARY);
            if (db.getVersion() != _newVersion) {
                throw new SQLiteException(
                        "Can't upgrade read-only database from version "
                                + db.getVersion() + " to " + _newVersion + ": "
                                + path);
            }

            onOpen(db);
            Log.i(TAG, "Opened " + DB_NAME + " in read-only mode");
            _db = db;
            return _db;
        } finally {
            _isInit = false;
            if (db != null && db != _db)
                db.close();
        }
    }

    public synchronized void close() {
        if (_isInit)
            throw new IllegalStateException("Closed during initialization");

        if (_db != null && _db.isOpen()) {
            _db.close();
            _db = null;
        }

        String path = String.format("%s/%s", DB_DIR_PATH, _currentDBNaming);
        File f = new File(path);

        if (f != null) {
            long dbSize = f.length();
            Log.i(TAG, String.format("DB path: %s size: %s", path, dbSize));
            if (_dbSizeLimit != 0 && dbSize >= _dbSizeLimit) {
                _timestamp = System.currentTimeMillis();
                updateDBName(_timestamp);
            }
        }
    }

    public void onCreate(SQLiteDatabase db) {
        if (db == null || _createTables == null) {
            throw new IllegalArgumentException();
        }
        for (String sql : _createTables)
            db.execSQL(sql);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (db == null || _dropTables == null) {
            throw new IllegalArgumentException();
        }

        for (String sql : _createTables) {
            db.execSQL(sql);
        }
        onCreate(db);
    }

    public void onOpen(SQLiteDatabase db) {
    }

    @SuppressLint("SimpleDateFormat")
    public void updateDBName(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        SimpleDateFormat dataFormatter = new SimpleDateFormat("yyyy.MM.dd");
        SimpleDateFormat timeFormatters = new SimpleDateFormat("HH.mm.ss.SSS");
        _currentDBNaming = String.format("%s_%s_%s.db", DB_NAME,
                dataFormatter.format(calendar.getTime()),
                timeFormatters.format(calendar.getTime()));
    }
}
