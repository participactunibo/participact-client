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

public class BusLine {
    private String line_code;

    public BusLine(String name) {
        super();
        this.line_code = name;
    }

    public String getName() {
        return line_code;
    }

    public void setName(String name) {
        this.line_code = name;
    }


    @Override
    public String toString() {
        return "BusLine [line_code=" + line_code + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BusLine other = (BusLine) obj;
        if (line_code == null) {
            if (other.line_code != null)
                return false;
        } else if (!line_code.equals(other.line_code))
            return false;
        return true;
    }

}
