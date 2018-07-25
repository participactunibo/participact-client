/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */
package org.most;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;

public class StateUtility {

    private static final String FILENAME = "MoST.state";

    public static synchronized boolean persistState(Context context, MoSTState state) {

        try {
            FileOutputStream fileOutputStream = context.openFileOutput(
                    FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                    fileOutputStream);
            objectOutputStream.writeObject(state);
            objectOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static synchronized MoSTState loadState(Context context) {

        MoSTState result = null;
        try {
            File file = new File(context.getFilesDir(), FILENAME);
            if (file.exists()) {
                FileInputStream fileInputStream = context.openFileInput(FILENAME);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                Object obj = objectInputStream.readObject();
                objectInputStream.close();
                if (obj instanceof MoSTState) {
                    result = (MoSTState) obj;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }

    public static synchronized boolean deleteState(Context context) {

        boolean result = false;
        try {
            result = context.deleteFile(FILENAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


}
