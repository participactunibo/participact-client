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

import it.unibo.participact.domain.rest.TwitterStatusList;
import it.unibo.participact.support.BasicAuthenticationUtility;
import it.unibo.participact.support.Configuration;

public class TwitterRequest extends SpringAndroidSpiceRequest<TwitterStatusList> {

//	private static final String OAUTH_CONSUMER_KEY = "6RXduk1K0qVp1KySa2jwgA";
//	private static final String OAUTH_CONSUMER_SECRET = "DG7cH3h2YsCHFj6A23qqozZwbiDNN0VAkUd9cCsaq0";
//	private static final String OAUTH_ACCESS_TOKEN = "1433054970-1uzccYY2iEZ9t5RfjgjP5wwcat9RQUafnvhC16U";
//	private static final String OAUTH_ACCESS_TOKEN_SECRET = "X2fWvhTqWnkN0ajNrHvZ2mQLk2ASyaBy6kISYtC6cs";

    private Context context;

    public TwitterRequest(Context context) {
        super(TwitterStatusList.class);
        this.context = context;
    }

    @Override
    public TwitterStatusList loadDataFromNetwork() throws Exception {

        ResponseEntity<TwitterStatusList> response = getRestTemplate().exchange(Configuration.TWITTER_URL,
                HttpMethod.GET, BasicAuthenticationUtility.getHttpEntityForAuthentication(context),
                TwitterStatusList.class);
        return response.getBody();
    }


    public String createCacheKey() {
        return "tweets.participact";
    }
}