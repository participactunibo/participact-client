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

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import it.unibo.participact.domain.rest.UserRestResultList;
import it.unibo.participact.support.BasicAuthenticationUtility;
import it.unibo.participact.support.Configuration;

public class FriendsGetRequest extends SpringAndroidSpiceRequest<UserRestResultList> {

    public static final String PENDING_SENT = "pending_sent";
    public static final String PENDING_RECEIVED = "pending_received";
    public static final String ACCEPTED = "accepted";
    private final Context context;
    private String status;

    public FriendsGetRequest(String status, Context context) {
        super(UserRestResultList.class);
        if (status == null)
            throw new NullPointerException();
        if (context == null)
            throw new NullPointerException();
        this.status = status;
        if (this.status == null)
            this.status = ACCEPTED;
        this.context = context;
    }

    @Override
    public UserRestResultList loadDataFromNetwork() throws Exception {
        ResponseEntity<UserRestResultList> response = getRestTemplate().exchange(Configuration.FRIENDS_GET_URL, HttpMethod.GET, BasicAuthenticationUtility.getHttpEntityForAuthentication(context), UserRestResultList.class, status);
        return response.getBody();
    }


    public String createCacheKey() {
        return "friends." + status;
    }

}
