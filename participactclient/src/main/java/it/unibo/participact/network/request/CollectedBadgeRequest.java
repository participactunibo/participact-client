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

import it.unibo.participact.domain.persistence.GeoBadgeCollected;
import it.unibo.participact.domain.rest.ResponseMessage;
import it.unibo.participact.support.BasicAuthenticationUtility;
import it.unibo.participact.support.Configuration;

public class CollectedBadgeRequest extends SpringAndroidSpiceRequest<ResponseMessage> {

    private Context context;
    private GeoBadgeCollected geoBadgeCollected;


    public CollectedBadgeRequest(Context context, GeoBadgeCollected geoBadgeCollected) {
        super(ResponseMessage.class);
        this.context = context;
        this.geoBadgeCollected = geoBadgeCollected;
    }


    @Override
    public ResponseMessage loadDataFromNetwork() throws
            Exception {
        java.util.Date now = new java.util.Date();
        java.sql.Timestamp timestamp = new java.sql.Timestamp(now.getTime());
        ResponseEntity<ResponseMessage> response = getRestTemplate().exchange(Configuration.COLLECTED_GEOBADGE_URL, HttpMethod.GET, BasicAuthenticationUtility.getHttpEntityForAuthentication(context), ResponseMessage.class,
                geoBadgeCollected.getTaskId(), geoBadgeCollected.getActionFlatId(), timestamp.getTime(), geoBadgeCollected.getDesctioprionGeofence());
        return response.getBody();
    }

    public String createCacheKey() {
        return String.format("collectedBadgeRequest.%s", geoBadgeCollected.getId());
    }
}
