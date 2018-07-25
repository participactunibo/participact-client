/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @author giuseppe giammarino
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

public class TrainStationFinder {
    public static final String TAG = TrainStationFinder.class.getSimpleName();
    public static final String OVERPASS_API = "http://www.overpass-api.de/api/interpreter?data=";
    private Utils utils;
    private int version;

    public TrainStationFinder() {
        super();
    }

    /**
     * @param position is the GPS position of the device
     * @param radius   is the range to use to query the map system
     * @return a list of train stations around the actual GPS position
     */
    public List<TrainStation> searchTrainStation(Utils utils, int version, GPSPosition position, double radius) {
        this.utils = utils;
        this.version = version;

        List<TrainStation> train_stations = new ArrayList<TrainStation>();
        String json = loadJson(position, radius);

        try {
            JSONObject jsonObject = new JSONObject(json);

            //I get all the stations around me
            JSONArray arr = jsonObject.getJSONArray("elements");
            for (int i = 0; i < arr.length(); i++) {
                //I get details of a train station
                Double latitude = arr.getJSONObject(i).getDouble("lat");
                Double longitude = arr.getJSONObject(i).getDouble("lon");
                String station_name = arr.getJSONObject(i).getJSONObject("tags").getString("name");
                GPSPosition new_position = new GPSPosition(latitude, longitude, 1, System.currentTimeMillis() / 1000);
                train_stations.add(new TrainStation(new_position, station_name));
            }
        } catch (Exception e) {
            e.printStackTrace();
            utils.appendLog("TrainStationFinder.searchTrainStations() " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n\tException: " + e.toString() + "\n", "errors", version);
            utils.appendLog("TrainStationFinder.searchTrainStations() " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n\tException: " + e.toString() + "\n", "train_infos", version);
        }
        return train_stations;
    }


    private String loadJson(GPSPosition position, double radius) {

        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        String encodedQuery = null;
        double queryRadius = position.getAccuracy() + radius;

        String queryStations = "[out:json];(node(around:" + queryRadius + "," + position.getLatitude() + "," + position.getLongitude() + ")[railway~" + "\"^(station)$" + "\"][name];>;);out;";
        try {
            encodedQuery = URLEncoder.encode(queryStations, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String request_url = OVERPASS_API + encodedQuery;
        HttpGet httpGet = new HttpGet(request_url);
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
                utils.appendLog("TrainStationFinder.loadJson() " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n\tFailed to download file. StatusCode=" + statusCode + "\n", "errors", version);
                utils.appendLog("TrainStationFinder.loadJson() " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n\tFailed to download file. StatusCode=" + statusCode + "\n", "train_infos", version);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            utils.appendLog("TrainStationFinder.loadJson() " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n\tClientProtocolException: " + e.toString() + "\n", "errors", version);
            utils.appendLog("TrainStationFinder.loadJson() " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n\tClientProtocolException: " + e.toString() + "\n", "train_infos", version);
        } catch (IOException e) {
            e.printStackTrace();
            utils.appendLog("TrainStationFinder.loadJson() " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n\tIOException: " + e.toString() + "\n", "errors", version);
            utils.appendLog("TrainStationFinder.loadJson() " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALY).format(new Date().getTime()) + "\n\tIOException: " + e.toString() + "\n", "train_infos", version);
        }
        return builder.toString();
    }
}
