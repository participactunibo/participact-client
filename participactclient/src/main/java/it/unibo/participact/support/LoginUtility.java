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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;

import com.octo.android.robospice.persistence.exception.SpiceException;

import org.apache.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import it.unibo.participact.R;
import it.unibo.participact.activities.LoginActivity;

public class LoginUtility {

    private static final int NOTIFICATION_ID = 102;

    public static boolean checkIfLoginException(Context context, SpiceException e) {
        if (e.getCause() instanceof HttpClientErrorException) {
            if (((HttpClientErrorException) e.getCause()).getStatusCode().value() == HttpStatus.SC_UNAUTHORIZED) {


                Intent intent = new Intent(context, LoginActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addParentStack(LoginActivity.class);
                stackBuilder.addNextIntent(intent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                NotificationUtility.addNotification(context, R.drawable.ic_login_err, context.getString(R.string.participact_notification), context.getString(R.string.login_again_error), NOTIFICATION_ID, resultPendingIntent);

                return true;
            }
        }
        return false;
    }

}
