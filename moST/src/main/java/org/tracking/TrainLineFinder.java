/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package org.tracking;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.most.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrainLineFinder {
    public static final String TAG = TrainLineFinder.class.getSimpleName();
    public static final String OVERPASS_API = "http://www.overpass-api.de/api/interpreter?data=";
    private Utils utils;
    private int version;

    public TrainLineFinder() {
        super();
    }

    /**
     * @param position is the GPS position of the device
     * @param radius   is the range to use to query the map system
     * @return a list of train lines around the actual GPS position
     */
    public List<TrainLine> searchTrainLines(Utils utils, int version, GPSPosition position, double radius) throws JSONException {
        this.utils = utils;
        this.version = version;

        List<TrainLine> train_lines = new ArrayList<TrainLine>();
        String json = loadJson(position, radius);

        JSONObject jsonObject = new JSONObject(json);

        //I get all the train lines around me
        JSONArray arr = jsonObject.getJSONArray("elements");
        for (int i = 0; i < arr.length(); i++) {
            //I get details of a train line
            String type = arr.getJSONObject(i).getString("type");
            if (type.equals("way")) {
                int lineIdOSM = arr.getJSONObject(i).getInt("id");
                String line_name = null;
                if (arr.getJSONObject(i).getJSONObject("tags").has("name"))
                    line_name = arr.getJSONObject(i).getJSONObject("tags").getString("name");
                train_lines.add(new TrainLine(lineIdOSM, line_name));
            }
        }
        return train_lines;
    }

    private String loadJson(GPSPosition position, double radius) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        String encodedQuery = null;
        double queryRadius = position.getAccuracy() + radius;

        String queryLines = "[out:json];(way(around:" + queryRadius + "," + position.getLatitude() + "," + position.getLongitude() + ")[railway~" + "\"^(rail)$" + "\"];>;);out;";

        try {
            encodedQuery = URLEncoder.encode(queryLines, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String request_url = OVERPASS_API + encodedQuery;
        HttpGet httpGet = new HttpGet(request_url);
        //Setting timeout 60 sec
//	    HttpParams httpParams = new BasicHttpParams();
//		HttpConnectionParams.setConnectionTimeout(httpParams, 60000);
//		client = new DefaultHttpClient(httpParams);
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();

            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null)
                    builder.append(line);
            } else {
                Log.e(TAG, "Failed to download file. StatusCode=" + statusCode);
                utils.appendLog("TrainLineFinder.loadJson() " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n\tFailed to download file. StatusCode=" + statusCode + "\n", "errors", version);
                utils.appendLog("TrainLineFinder.loadJson() " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n\tFailed to download file. StatusCode=" + statusCode + "\n", "train_infos", version);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            utils.appendLog("TrainLineFinder.loadJson() " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n\tClientProtocolException: " + e.toString() + "\n", "errors", version);
            utils.appendLog("TrainLineFinder.loadJson() " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n\tClientProtocolException: " + e.toString() + "\n", "train_infos", version);
        } catch (IOException e) {
            e.printStackTrace();
            utils.appendLog("TrainLineFinder.loadJson() " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n\tIOException: " + e.toString() + "\n", "errors", version);
            utils.appendLog("TrainLineFinder.loadJson() " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n\tIOException: " + e.toString() + "\n", "train_infos", version);
        }
        return builder.toString();
    }
}
