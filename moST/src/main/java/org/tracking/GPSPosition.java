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
 * @author marcomoschettini
 */

package org.tracking;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;


public class GPSPosition {
    private double latitude;
    private double longitude;
    private double accuracy;
    private long timestamp;

    public GPSPosition(double latitude, double longitude, double accuracy, long timestamp) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.setTimestamp(timestamp);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long time) {
        this.timestamp = time;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public String toString() {
        return "GPSPosition [latitude=" + latitude + ", longitude=" + longitude
                + ", accuracy=" + accuracy + ", time=" + new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").format(new Date(this.timestamp * 1000)) + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GPSPosition other = (GPSPosition) obj;
        if (Double.doubleToLongBits(latitude) != Double
                .doubleToLongBits(other.latitude))
            return false;
        if (Double.doubleToLongBits(longitude) != Double
                .doubleToLongBits(other.longitude))
            return false;
        return true;
    }

}
