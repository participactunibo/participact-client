/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.support;

import android.content.Context;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.support.Base64;

import java.util.Map;

import it.unibo.participact.domain.local.UserAccount;
import it.unibo.participact.support.preferences.UserAccountPreferences;

public class BasicAuthenticationUtility {

    private static HttpEntity<Map<Object, Object>> instance = null;


    public static HttpEntity<Map<Object, Object>> getHttpEntityForAuthentication(Context context) {
        if (instance == null) {
            HttpHeaders httpHeaders = new HttpHeaders();
            //Basic Authentication
            UserAccount user = UserAccountPreferences.getInstance(context).getUserAccount();
            String authStr = String.format("%s:%s", user.getUsername(), user.getPassword());
            String authEncoded = Base64.encodeBytes(authStr.getBytes());
            httpHeaders.add("Authorization", "Basic " + authEncoded);
            instance = new HttpEntity<Map<Object, Object>>(httpHeaders);
        }
        return instance;
    }
}
