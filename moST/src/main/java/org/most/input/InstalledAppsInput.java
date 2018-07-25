/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */
package org.most.input;

import java.util.ArrayList;
import java.util.List;

import org.most.DataBundle;
import org.most.MoSTApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class InstalledAppsInput extends Input {

    private final static String TAG = InstalledAppsInput.class.getSimpleName();

    /**
     * Key to access the list of installed apps.
     */
    public final static String KEY_INSTALLED_APPS_LIST = "InstalledAppsInput.AppList";

    private PackageManager _packageManager;
    private InstalledAppReceiver _installedAppReceiver;

    /**
     * @param context
     */
    public InstalledAppsInput(MoSTApplication context) {
        super(context);
    }

    @Override
    public void onInit() {
        checkNewState(Input.State.INITED);
        _packageManager = getContext().getApplicationContext().getPackageManager();
        super.onInit();
    }

    @Override
    public boolean onActivate() {
        checkNewState(Input.State.ACTIVATED);
        super.onActivate();
        postInstalledApplications();
        _installedAppReceiver = new InstalledAppReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        getContext().registerReceiver(_installedAppReceiver, intentFilter);
        return true;
    }

    @Override
    public void onDeactivate() {
        checkNewState(Input.State.DEACTIVATED);
        getContext().unregisterReceiver(_installedAppReceiver);
        _installedAppReceiver = null;
        super.onDeactivate();
    }

    @Override
    public void onFinalize() {
        checkNewState(Input.State.FINALIZED);
        _packageManager = null;
        super.onFinalize();
    }

    protected void postInstalledApplications() {
        Log.d(TAG, "Retrieving app state");
        List<PackageInfo> installedPkgs = new ArrayList<PackageInfo>(
                _packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS));
        DataBundle b = _bundlePool.borrowBundle();
        b.putObject(KEY_INSTALLED_APPS_LIST, installedPkgs);
        b.putLong(Input.KEY_TIMESTAMP, System.currentTimeMillis());
        b.putInt(Input.KEY_TYPE, Input.Type.INSTALLED_APPS.toInt());
        post(b);
    }

    @Override
    public Type getType() {
        return Input.Type.INSTALLED_APPS;
    }

    @Override
    public boolean isWakeLockNeeded() {
        return false;
    }

    private class InstalledAppReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            postInstalledApplications();
        }

    }
}
