/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.domain.data;


public class DataMagneticField extends Data {

    private static final long serialVersionUID = -7734250913687773940L;

    private float magnetic_field_x;

    private float magnetic_field_y;

    private float magnetic_field_z;

    public float getMagnetic_field_x() {
        return magnetic_field_x;
    }

    public void setMagnetic_field_x(float magnetic_field_x) {
        this.magnetic_field_x = magnetic_field_x;
    }

    public float getMagnetic_field_y() {
        return magnetic_field_y;
    }

    public void setMagnetic_field_y(float magnetic_field_y) {
        this.magnetic_field_y = magnetic_field_y;
    }

    public float getMagnetic_field_z() {
        return magnetic_field_z;
    }

    public void setMagnetic_field_z(float magnetic_field_z) {
        this.magnetic_field_z = magnetic_field_z;
    }

}
