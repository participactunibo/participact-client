/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.domain.persistence;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.vividsolutions.jts.awt.PointShapeFactory;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import org.most.MoSTService;
import org.most.pipeline.Pipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import it.unibo.participact.domain.enums.SensingActionEnum;
import it.unibo.participact.domain.persistence.support.State;
import it.unibo.participact.views.cards.TaskActiveCard;

public class TaskService {

    private final static Logger logger = LoggerFactory.getLogger(TaskService.class);

    protected GoogleApiClient mGoogleApiClient;


    public static void activateTask(Context context, TaskFlat task) {
        State state = StateUtility.loadState(context);
        if (state.getTaskById(task.getId()) != null) {

            logger.info("Activating Task with id {} to MoST.", task.getId());

            for (ActionFlat action : task.getActions()) {
                switch (action.getType()) {
                    case SENSING_MOST:
                        if (Pipeline.Type.fromInt(action.getInput_type()) != Pipeline.Type.DUMMY) {
                            Intent i = new Intent(context, MoSTService.class);
                            i.setAction(MoSTService.START);
                            i.putExtra(MoSTService.KEY_PIPELINE_TYPE, action.getInput_type());
                            context.startService(i);
                            logger.info("Sending start intent of pipeline {} to MoST.",
                                    SensingActionEnum.Type.fromIntToHumanReadable(action.getInput_type().intValue()));}
                        break;
                    case PHOTO:
                        break;
                    case QUESTIONNAIRE:
                        break;
                    case GEOFENCE:
                        break;

                    default:
                        break;
                }
            }
        }
    }




    public static void suspendTask(Context context, TaskFlat task) {
        // At the moment only sensing tasks can be suspended
        State state = StateUtility.loadState(context);
        if (state.getTaskById(task.getId()) != null) {
            logger.info("Suspending Task with id {} to MoST.", task.getId());
            for (ActionFlat action : task.getActions()) {
                switch (action.getType()) {
                    case SENSING_MOST:
                        if (Pipeline.Type.fromInt(action.getInput_type()) != Pipeline.Type.DUMMY) {
                            Intent i = new Intent(context, MoSTService.class);
                            i.setAction(MoSTService.STOP);
                            i.putExtra(MoSTService.KEY_PIPELINE_TYPE, action.getInput_type());
                            context.startService(i);
                            logger.info("Sending stop intent of pipeline {} to MoST.",
                                    SensingActionEnum.Type.fromIntToHumanReadable(action.getInput_type().intValue()));
                        }
                        break;

                    case ACTIVITY_DETECTION:
                        Intent i = new Intent(context, MoSTService.class);
                        i.setAction(MoSTService.STOP);
                        i.putExtra(MoSTService.KEY_PIPELINE_TYPE, Pipeline.Type.ACTIVITY_RECOGNITION_COMPARE.toInt());//TODO change
                        context.startService(i);

                        // reset btn state
                        Editor editor = context.getSharedPreferences(TaskActiveCard.ACTIVITY_DETECTION_PREFS, Context.MODE_PRIVATE).edit();
                        editor.putInt(TaskActiveCard.KEY_AD_SELECTED, -1);
                        editor.apply();

                        break;

                    default:
                        break;
                }
            }
        }
    }

    public static boolean isTaskCompatibleWithThisAppVersion(it.unibo.participact.domain.persistence.TaskFlat task) {

        for (it.unibo.participact.domain.persistence.ActionFlat action : task.getActions()) {
            switch (action.getType()) {
                case SENSING_MOST:
                    Pipeline.Type inputType = Pipeline.Type.fromInt(action.getInput_type());
                    switch (inputType) {
                        case ACCELEROMETER:
                        case APP_ON_SCREEN:
                        case ACCELEROMETER_CLASSIFIER:
                        case BATTERY:
                        case CELL:
                        case BLUETOOTH:
                        case GYROSCOPE:
                        case INSTALLED_APPS:
                        case LIGHT:
                        case LOCATION:
                        case MAGNETIC_FIELD:
                        case PHONE_CALL_DURATION:
                        case PHONE_CALL_EVENT:
                        case SYSTEM_STATS:
                        case WIFI_SCAN:
                        case APPS_NET_TRAFFIC:
                        case DEVICE_NET_TRAFFIC:
                        case CONNECTION_TYPE:
                        case GOOGLE_ACTIVITY_RECOGNITION:
                        case ACTIVITY_RECOGNITION_COMPARE:
                        case DR:
                            break;

                        case AVERAGE_ACCELEROMETER:
                        case AUDIO_CLASSIFIER:
                        case RAW_AUDIO:
                            return false;

                        default:
                            return false;
                    }
                case PHOTO:
                    break;
                case QUESTIONNAIRE:
                    break;
                case ACTIVITY_DETECTION:
                    break;
                case GEOFENCE:

                    break;
                default:
                    return false;
            }
        }

        return true;

    }

    public static boolean isTaskCompatibleWithThisAppVersion(it.unibo.participact.domain.rest.TaskFlat task) {

        for (it.unibo.participact.domain.rest.ActionFlat action : task.getActions()) {
            switch (action.getType()) {
                case SENSING_MOST:
                    Pipeline.Type inputType = Pipeline.Type.fromInt(action.getInput_type());
                    switch (inputType) {
                        case ACCELEROMETER:
                        case APP_ON_SCREEN:
                        case ACCELEROMETER_CLASSIFIER:
                        case BATTERY:
                        case CELL:
                        case BLUETOOTH:
                        case GYROSCOPE:
                        case INSTALLED_APPS:
                        case LIGHT:
                        case LOCATION:
                        case MAGNETIC_FIELD:
                        case PHONE_CALL_DURATION:
                        case PHONE_CALL_EVENT:
                        case SYSTEM_STATS:
                        case WIFI_SCAN:
                        case APPS_NET_TRAFFIC:
                        case DEVICE_NET_TRAFFIC:
                        case CONNECTION_TYPE:
                        case GOOGLE_ACTIVITY_RECOGNITION:
                        case ACTIVITY_RECOGNITION_COMPARE:
                        case DR:
                            break;

                        case AVERAGE_ACCELEROMETER:
                        case AUDIO_CLASSIFIER:
                        case RAW_AUDIO:
                            return false;

                        default:
                            return false;
                    }
                case PHOTO:
                    break;
                case QUESTIONNAIRE:
                    break;
                case ACTIVITY_DETECTION:
                    break;
                case GEOFENCE:
                    break;
                default:
                    return false;
            }
        }

        return true;

    }

}
