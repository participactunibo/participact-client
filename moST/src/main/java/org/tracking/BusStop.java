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

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a Bus Station.
 *
 * @author marcomoschettini
 */

public class BusStop {
    private GPSPosition position;
    private String name;
    private String location;
    private List<BusLine> lines;
    private int zone_code;
    private double error;
    private String code;

    public BusStop(GPSPosition position, String name, String code, String location, List<BusLine> lines, int zone_code, double error) {
        super();
        this.position = position;
        this.name = name;
        this.setCode(code);
        this.location = location;
        this.setLines(lines);
        this.zone_code = zone_code;
        this.error = error;
    }

    public GPSPosition getPosition() {
        return position;
    }

    public void setPosition(GPSPosition position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getError() {
        return error;
    }

    public void setError(double error) {
        this.error = error;
    }


    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the zone_code
     */
    public int getZone_code() {
        return zone_code;
    }

    /**
     * @param zone_code the zone_code to set
     */
    public void setZone_code(int zone_code) {
        this.zone_code = zone_code;
    }

    /**
     * @return the lines
     */
    public List<BusLine> getLines() {
        return lines;
    }

    /**
     * @param lines the lines to set
     */
    public void setLines(List<BusLine> lines) {
        this.lines = lines;
    }


    @Override
    public String toString() {
        return "BusStop [position=" + position + ", name=" + name
                + ", location=" + location + ", lines=" + lines
                + ", zone_code=" + zone_code + ", error=" + error + ", code="
                + code + "]";
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public List<BusLine> linesInCommon(BusStop other) {
        List<BusLine> lines_in_common = new ArrayList<BusLine>();
        for (BusLine line : this.lines) {
            for (BusLine other_line : other.getLines()) {
                if (line.equals(other_line))
                    lines_in_common.add(line);
            }
        }


        return lines_in_common;

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BusStop other = (BusStop) obj;
        if (code == null) {
            if (other.code != null)
                return false;
        } else if (!code.equals(other.code))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (position == null) {
            if (other.position != null)
                return false;
        } else if (!position.equals(other.position))
            return false;
        if (zone_code != other.zone_code)
            return false;
        return true;
    }

}
