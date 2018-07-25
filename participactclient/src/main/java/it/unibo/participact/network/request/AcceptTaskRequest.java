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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import it.unibo.participact.domain.rest.ResponseMessage;
import it.unibo.participact.support.BasicAuthenticationUtility;
import it.unibo.participact.support.Configuration;
import it.unibo.participact.support.preferences.ChangeTimePreferences;
import it.unibo.participact.support.preferences.UserAccountPreferences;

public class AcceptTaskRequest extends SpringAndroidSpiceRequest<ResponseMessage> {

    private final static Logger logger = LoggerFactory.getLogger(AcceptTaskRequest.class);

    private Context context;
    private Long taskId;

    public AcceptTaskRequest(Context context, Long taskId) {
        super(ResponseMessage.class);
        this.context = context;
        this.taskId = taskId;
    }

    @Override
    public ResponseMessage loadDataFromNetwork() throws Exception {
        if (ChangeTimePreferences.getInstance(context).getChangeTimeRequest()) {
            logger.warn("Blocked AcceptTaskRequest for change date/time reasons.");
            throw new Exception();
        }

        logger.info("Executing accept task request of task with id {}.", taskId);
        ResponseEntity<ResponseMessage> response = getRestTemplate().exchange(
                Configuration.ACCEPT_TASK_URL, HttpMethod.GET,
                BasicAuthenticationUtility.getHttpEntityForAuthentication(context),
                ResponseMessage.class, taskId);
        return response.getBody();

    }

    /**
     * This method generates a unique cache key for this request. In this case
     * our cache key depends just on the keyword.
     *
     * @return
     */
    public String createCacheKey() {
        return String.format("acceptTaskRequest.%s", UserAccountPreferences.getInstance(context)
                .getUserAccount().getUsername());
    }
}
