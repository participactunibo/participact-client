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
import android.widget.Toast;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.participact.domain.persistence.TaskFlat;

public class GeolocalizationTaskUtils {

    private static final Logger logger = LoggerFactory.getLogger(GeolocalizationTaskUtils.class);

    public static boolean isGeolocalized(TaskFlat task) {
        return isActivatedByArea(task) || isNotifiedByArea(task);
    }

    public static boolean isActivatedByArea(TaskFlat task) {
        return StringUtils.isNotBlank(task.getActivationArea());
    }

    public static boolean isNotifiedByArea(TaskFlat task) {
        return StringUtils.isNotBlank(task.getNotificationArea());
    }

    public static boolean isGeolocalized(it.unibo.participact.domain.rest.TaskFlat task) {
        return isActivatedByArea(task) || isNotifiedByArea(task);
    }

    public static boolean isActivatedByArea(it.unibo.participact.domain.rest.TaskFlat task) {
        return StringUtils.isNotBlank(task.getActivationArea());
    }

    public static boolean isNotifiedByArea(it.unibo.participact.domain.rest.TaskFlat task) {
        return StringUtils.isNotBlank(task.getNotificationArea());
    }

    public static boolean isInside(Context context, double longitude, double latitude, String wkt) {
        try {

            Coordinate coordinate = new Coordinate(longitude, latitude);
            GeometryFactory factory = new GeometryFactory();
            Geometry current = factory.createPoint(coordinate);
            String[] polygons = StringUtils.split(wkt, ";");
            for (String string : polygons) {
                Geometry polygon = new WKTReader().read(string);
                if (polygon.contains(current)) {
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            logger.warn("Exception checking if wkt {} contains current location", wkt, e);
        }

        return false;
    }


    public static boolean isInsideCircle(Context context, double longitude, double latitude, String wkt) {
        try {
            Coordinate coordinate = new Coordinate(longitude, latitude);
            GeometryFactory factory = new GeometryFactory();
            Geometry current = factory.createPoint(coordinate);
            String[] polygons = StringUtils.split(wkt, ";");
            for (String string : polygons) {

                Geometry polygon = new WKTReader().read(string);
                if (polygon.contains(current)) {
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            logger.warn("Exception checking if wkt {} contains current location", "", e);
        }

        return false;
    }

}
