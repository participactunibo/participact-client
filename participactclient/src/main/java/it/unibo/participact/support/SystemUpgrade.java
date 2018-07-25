/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.support;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import it.unibo.participact.State;
import it.unibo.participact.domain.enums.TaskState;
import it.unibo.participact.domain.local.TaskStatus;

public class SystemUpgrade {

    private static final String UPGRADE_PREFERENCES = "UPGRADE_PREFERENCES";
    private static final String VERSION = "VERSION";


    public static void upgrade(Context context) {

        context = context.getApplicationContext();

        SharedPreferences pref = context.getSharedPreferences(UPGRADE_PREFERENCES, Context.MODE_PRIVATE);
        int old = pref.getInt(VERSION, -1);

        if (old == -1) {
            StateUtility.deleteState(context);
            org.most.StateUtility.deleteState(context);
        }

        switch (old) {
            case 1:
            case 2:
                StateUtility.deleteState(context);
                org.most.StateUtility.deleteState(context);
            case 17:
                int i = 0;
                File file = new File(context.getFilesDir(), "state.raw");
                if (file.exists()) {
                    try {
                        FileInputStream fileInputStream = context.openFileInput("state.raw");
                        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                        Object obj = objectInputStream.readObject();
                        objectInputStream.close();
                        State oldState = null;
                        if (obj instanceof State) {
                            oldState = (State) obj;
                        }

                        if (oldState != null) {
                            for (TaskStatus task : oldState.getTaskStatusByState(TaskState.RUNNING)) {
                                it.unibo.participact.domain.persistence.StateUtility.convertTaskStatus(context, task);
                                Log.i(SystemUpgrade.class.getSimpleName(), "Updating task");
                                i++;
                            }
                            for (TaskStatus task : oldState.getTaskStatusByState(TaskState.SUSPENDED)) {
                                it.unibo.participact.domain.persistence.StateUtility.convertTaskStatus(context, task);
                                Log.i(SystemUpgrade.class.getSimpleName(), "Updating task");
                                i++;
                            }
                            for (TaskStatus task : oldState.getTaskStatusByState(TaskState.ERROR)) {
                                it.unibo.participact.domain.persistence.StateUtility.convertTaskStatus(context, task);
                                Log.i(SystemUpgrade.class.getSimpleName(), "Updating task");
                                i++;
                            }
                            Log.i(SystemUpgrade.class.getSimpleName(), "Successfully updated " + i + " task.");
                            file.delete();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        }

        Editor editor = pref.edit();
        editor.putInt(VERSION, Configuration.VERSION);
        editor.apply();
    }
}
