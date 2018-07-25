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
import org.springframework.http.ResponseEntity;
import org.springframework.util.support.Base64;

import java.util.Map;

import it.unibo.participact.support.Configuration;

public class LoginRequest extends SpringAndroidSpiceRequest<Boolean> {

    String email;
    String password;

    public LoginRequest(String email, String password) {
        super(Boolean.class);
        this.email = email;
        this.password = password;
    }

    @Override
    public Boolean loadDataFromNetwork() throws Exception {
        HttpHeaders httpHeaders = new HttpHeaders();
        //Basic Authentication
        String authStr = String.format("%s:%s", email, password);
        String authEncoded = Base64.encodeBytes(authStr.getBytes());
        httpHeaders.add("Authorization", "Basic " + authEncoded);
        ResponseEntity<Boolean> response = getRestTemplate().exchange(Configuration.LOGIN_URL, HttpMethod.GET, new HttpEntity<Map<Object, Object>>(httpHeaders), Boolean.class);
        return response.getBody();
//        return true;
    }

    /**
     * This method generates a unique cache key for this request. In this case our cache key depends just on the
     * keyword.
     *
     * @return
     */
    public String createCacheKey() {
        return String.format("login.%s", email);
    }
}

