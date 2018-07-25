/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.support.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import it.unibo.participact.domain.local.UserAccount;

public class UserAccountPreferences {


    private static UserAccountPreferences instance;
    private static SharedPreferences sharedPreferences;

    private static int MODE = Context.MODE_PRIVATE;
    private static final String NAME = "USER_ACCOUNT";
    private static final String USER_NAME = "userName";
    private static final String PASSWORD = "password";
    private static final String REGISTRATION_ID = "registrationId";
    private static final String IS_SET = "isSet";
    private static final String IS_REGISTRATION_ID_SET_ON_SERVER = "isSetOnServer";
    private static final String ON_SERVER_EXPIRATION_TIME = "gcmOnServerExpirationTime";


    public static synchronized UserAccountPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new UserAccountPreferences();
            sharedPreferences = context.getSharedPreferences(NAME, MODE);
        }
        return instance;
    }

    public void saveUserAccount(UserAccount u) {
        Editor e = sharedPreferences.edit();
        e.putString(USER_NAME, u.getUsername());
        e.putString(PASSWORD, u.getPassword());
        e.putString(REGISTRATION_ID, u.getRegistrationId());
        e.putBoolean(IS_SET, true);
        e.apply();
    }

    public void updateUserAccount(UserAccount u) {
        Editor e = sharedPreferences.edit();
        e.putString(USER_NAME, u.getUsername());
        e.putString(PASSWORD, u.getPassword());
        e.putString(REGISTRATION_ID, u.getRegistrationId());
        e.putBoolean(IS_SET, true);
        e.apply();
    }

    public void deleteUserAccount() {
        Editor e = sharedPreferences.edit();
        e.remove(USER_NAME);
        e.remove(PASSWORD);
        e.remove(REGISTRATION_ID);
        e.remove(IS_SET);
        e.remove(IS_REGISTRATION_ID_SET_ON_SERVER);
        e.apply();
    }

    public boolean isUserAccountValid() {
        return sharedPreferences.getBoolean(IS_SET, false);
    }

    public boolean isGCMSetOnServer() {
        return sharedPreferences.getBoolean(IS_REGISTRATION_ID_SET_ON_SERVER, false);
    }

    public void setGCMSetOnServer(boolean isSet) {
        Editor e = sharedPreferences.edit();
        e.putBoolean(IS_REGISTRATION_ID_SET_ON_SERVER, isSet);
        e.apply();
    }

    public void setGcmOnServerExpirationTime(long expirationTime) {
        Editor e = sharedPreferences.edit();
        e.putLong(ON_SERVER_EXPIRATION_TIME, expirationTime);
        e.apply();
    }

    public long getgcmOnServerExpirationTime() {
        return sharedPreferences.getLong(ON_SERVER_EXPIRATION_TIME, -1);
    }

    public UserAccount getUserAccount() {
        UserAccount u = new UserAccount();
        u.setUsername(sharedPreferences.getString(USER_NAME, "notRegistered"));
        u.setPassword(sharedPreferences.getString(PASSWORD, "noPassword"));
        u.setRegistrationId(sharedPreferences.getString(REGISTRATION_ID, ""));
        return u;
    }
}
