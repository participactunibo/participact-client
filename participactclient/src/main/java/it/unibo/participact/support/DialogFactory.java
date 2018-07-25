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

import android.app.AlertDialog;
import android.content.Context;

public class DialogFactory {

    public static void showCommunicationErrorWithServer(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Attenzione");
        builder.setMessage("Errore di comunicazione con il server. Riprovare più tardi.");
        builder.setPositiveButton(android.R.string.ok, null);
        builder.create().show();
    }

    public static void showTimeError(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Attenzione");
        builder.setMessage("Abbiamo rilevato che la data o l'ora impostata sul dispositivo è stata modificata. " +
                "Il normale funzionamento di PartcipAct sarà ripristinato una volta verificata la validità di tali modifiche. È necessaria una connessione internet.");
        builder.setPositiveButton(android.R.string.ok, null);
        builder.create().show();
    }
}
