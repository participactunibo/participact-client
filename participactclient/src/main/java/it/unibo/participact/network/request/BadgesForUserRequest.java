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

import it.unibo.participact.domain.rest.BadgeRestResultList;
import it.unibo.participact.support.BasicAuthenticationUtility;
import it.unibo.participact.support.Configuration;

public class BadgesForUserRequest extends
        SpringAndroidSpiceRequest<BadgeRestResultList> {

    private final long id; //if not set, the request is for the logged user
    private final Context context;

    public BadgesForUserRequest(long id, Context context) {
        super(BadgeRestResultList.class);

        if (context == null)
            throw new NullPointerException();

        this.id = id;
        this.context = context;
    }

    public BadgesForUserRequest(Context context) {
        super(BadgeRestResultList.class);
        if (context == null)
            throw new NullPointerException();
        this.id = -1; //logged user
        this.context = context;
    }

    @Override
    public BadgeRestResultList loadDataFromNetwork() throws Exception {
        String idS;
        if (id > 0)
            idS = String.valueOf(id);
        else
            idS = "me";
        ResponseEntity<BadgeRestResultList> response = getRestTemplate().exchange(Configuration.BADGES_FOR_USER_URL, HttpMethod.GET, BasicAuthenticationUtility.getHttpEntityForAuthentication(context), BadgeRestResultList.class, idS);
        return response.getBody();
    }

    public String createCacheKey() {
        return "badges." + id;
    }
}
