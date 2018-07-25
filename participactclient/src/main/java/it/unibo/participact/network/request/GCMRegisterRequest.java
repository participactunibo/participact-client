/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.network.request;


import android.content.Context;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import it.unibo.participact.domain.local.UserAccount;
import it.unibo.participact.support.BasicAuthenticationUtility;
import it.unibo.participact.support.Configuration;
import it.unibo.participact.support.preferences.UserAccountPreferences;

public class GCMRegisterRequest extends SpringAndroidSpiceRequest<Boolean> {

    private Context context;
    private String gcmId;

    public GCMRegisterRequest(Context context) {
        super(Boolean.class);
        this.context = context;
    }

    @Override
    public Boolean loadDataFromNetwork() throws Exception {

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        gcmId = gcm.register(Configuration.GCM_SENDER_ID);
        UserAccountPreferences accountDao = UserAccountPreferences.getInstance(context);
        UserAccount account = accountDao.getUserAccount();
        account.setRegistrationId(gcmId);
        accountDao.saveUserAccount(account);
        accountDao.setGCMSetOnServer(false);
        accountDao.setGcmOnServerExpirationTime(System.currentTimeMillis() + Configuration.REGISTRATION_EXPIRY_TIME_MS);
//      return getRestTemplate().getForObject(Configuration.GCM_REGISTER_URL, Boolean.class, user, password, gcmId);
        ResponseEntity<Boolean> response = getRestTemplate().exchange(Configuration.GCM_REGISTER_URL, HttpMethod.GET, BasicAuthenticationUtility.getHttpEntityForAuthentication(context), Boolean.class, gcmId);
        return response.getBody();
    }

    /**
     * This method generates a unique cache key for this request. In this case our cache key depends just on the
     * keyword.
     *
     * @return
     */
    public String createCacheKey() {
        return String.format("registergcm.%s", UserAccountPreferences.getInstance(context).getUserAccount().getUsername());
    }
}

