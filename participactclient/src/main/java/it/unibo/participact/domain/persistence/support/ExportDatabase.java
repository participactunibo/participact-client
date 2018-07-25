/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.domain.persistence.support;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;


public class ExportDatabase {
    public static void exportDB(Context context) {
        try {
            Log.e("DB_BK", "inizio...");
            String DB_PATH = context.getDatabasePath("domain.db").getAbsolutePath();
            File dbFile = new File(DB_PATH);
            FileInputStream fis = new FileInputStream(dbFile);
            String outFileName = "domain.db";
            File outFile = new File(Environment.getExternalStorageDirectory(), outFileName);
            outFile.createNewFile();
            OutputStream output = new FileOutputStream(outFile, false);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            output.flush();
            output.close();
            fis.close();
            Log.e("DB_BK OK FINE", outFile.getAbsolutePath());
            System.out.println("°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°"+ outFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("DB_BK", e.toString());
        }
    }
}
