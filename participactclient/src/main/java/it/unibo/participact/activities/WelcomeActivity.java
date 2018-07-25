/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.splunk.mint.Mint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import it.unibo.participact.R;

import it.unibo.participact.support.preferences.UserAccountPreferences;

public class WelcomeActivity extends Activity {

    private static final Logger logger = LoggerFactory.getLogger(WelcomeActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.initAndStartSession(WelcomeActivity.this, "6e05d719");
        setContentView(R.layout.activity_welcome);
        logger.info("Application started");

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (servicesConnected()) {
            Intent i = null;
            if (UserAccountPreferences.getInstance(this).isUserAccountValid()) {
                i = new Intent(this, DashboardActivity.class);
            } else {
                i = new Intent(this, LoginActivity.class);
            }



            startActivity(i);
            finish();
        }
    }

    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode,
                    this, 0);
            if (dialog != null) {
                dialog.show();
            }
            return false;
        }
    }

}
