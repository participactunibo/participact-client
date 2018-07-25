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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import it.unibo.participact.R;

public class NotificationUtility {

    public static void addNotification(Context context, int smallIconId, String title, String content, int notificationId) {
        addNotification(context, smallIconId, title, content, notificationId, null);
    }

    public static void addNotification(Context context, int smallIconId, String title, String content, int notificationId, PendingIntent pendingIntent) {

        if (context == null)
            throw new NullPointerException();
        if (title == null)
            throw new NullPointerException();
        if (content == null)
            throw new NullPointerException();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(smallIconId)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setColor(context.getResources().getColor(R.color.primary_dark));

        if (pendingIntent != null) {
            mBuilder.setContentIntent(pendingIntent);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, mBuilder.build());
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);
    }
}
