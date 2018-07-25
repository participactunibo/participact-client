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

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

import it.unibo.participact.domain.rest.SocialPresenceFriendsRequest;
import it.unibo.participact.domain.rest.UserRestResultList;
import it.unibo.participact.support.Configuration;

public class SocialPresenceGetFriendsRequest extends SpringAndroidSpiceRequest<UserRestResultList> {

    public static final String FACEBOOK = "facebook";
    public static final String TWITTER = "twitter";

    private String socialNetwork;
    private SocialPresenceFriendsRequest socialPresenceFriendsRequest;

    public SocialPresenceGetFriendsRequest(String socialNetwork, Set<String> ids) {
        super(UserRestResultList.class);
        if (socialNetwork == null)
            throw new NullPointerException();
        if (ids == null)
            throw new NullPointerException();
        if (!(socialNetwork.equals(FACEBOOK) || socialNetwork.equals(TWITTER)))
            throw new IllegalArgumentException(socialNetwork);
        this.socialNetwork = socialNetwork;
        this.socialPresenceFriendsRequest = new SocialPresenceFriendsRequest();
        this.socialPresenceFriendsRequest.setIds(ids);
    }

    @Override
    public UserRestResultList loadDataFromNetwork() throws Exception {
        // Set the Content-Type header
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(new MediaType("application", "json"));
        HttpEntity<SocialPresenceFriendsRequest> requestEntity = new HttpEntity<SocialPresenceFriendsRequest>(this.socialPresenceFriendsRequest, requestHeaders);


        RestTemplate restTemplate = getRestTemplate();

        // Add the Jackson and String message converters
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

        // Make the HTTP POST request, marshaling the request to JSON, and the response to a String
        ResponseEntity<UserRestResultList> responseEntity = restTemplate.exchange(Configuration.SOCIAL_PRESENCE_GET_FIENDS_URL, HttpMethod.POST, requestEntity, UserRestResultList.class, socialNetwork);
        return responseEntity.getBody();
    }
}
