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

import com.octo.android.robospice.request.SpiceRequest;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.springframework.util.support.Base64;

import java.util.UUID;

import it.unibo.participact.domain.local.UserAccount;
import it.unibo.participact.support.preferences.UserAccountPreferences;

public abstract class ApacheHttpSpiceRequest<RESULT> extends SpiceRequest<RESULT> {

    private static final String HEADER_KEY = "Request_key";

    HttpClient httpClient;
    Context context;
    String key;

    public ApacheHttpSpiceRequest(Context context, Class<RESULT> clazz) {
        super(clazz);
        this.setContext(context);
        key = UUID.randomUUID().toString();

    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public HttpPost getHttpPost(String URL) {
        HttpPost httppost = new HttpPost(URL);
        // for basic Autentication
        UserAccount user = UserAccountPreferences.getInstance(context).getUserAccount();
        String authStr = String.format("%s:%s", user.getUsername(), user.getPassword());
        String authEncoded = Base64.encodeBytes(authStr.getBytes());
        httppost.setHeader("Authorization", "Basic " + authEncoded);
        httppost.setHeader(HEADER_KEY, key);
        return httppost;
    }

}
