/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.domain.rest;

import java.io.Serializable;
import java.util.Properties;

public class ResponseMessage implements Serializable {

    private static final long serialVersionUID = -4920503490628523872L;

    public static final int RESULT_OK = 200;
    public static final int DATA_ALREADY_ON_SERVER = 555;
    public static final int DATA_NOT_REQUIRED = 556;

    private static final String KEY = "key";
    private static final String RESULT_CODE = "resultCode";

    Properties map;

    public ResponseMessage() {
        map = new Properties();
        map.setProperty(KEY, "");
        map.setProperty(RESULT_CODE, -1 + "");
    }

    public ResponseMessage(String key, int resultCode) {
        map = new Properties();
        map.put(KEY, key);
        map.put(RESULT_CODE, resultCode);
    }

    public String getProperty(String key) {
        return map.getProperty(key);
    }

    public void setProperty(String key, String value) {
        map.put(key, value);
    }

    public String getKey() {
        return map.getProperty(KEY);
    }

    public void setKey(String key) {
        map.put(KEY, key);
    }

    public int getResultCode() {
        return Integer.parseInt(map.getProperty(RESULT_CODE));
    }

    public void setResultCode(int resultCode) {
        map.setProperty(RESULT_CODE, resultCode + "");
    }


}
