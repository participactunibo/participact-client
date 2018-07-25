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


public class DataGyroscope extends Data {

    private static final long serialVersionUID = 9078245648320258665L;

    private float rotation_x;

    private float rotation_y;

    private float rotation_z;

    public float getRotation_x() {
        return rotation_x;
    }

    public void setRotation_x(float rotation_x) {
        this.rotation_x = rotation_x;
    }

    public float getRotation_y() {
        return rotation_y;
    }

    public void setRotation_y(float rotation_y) {
        this.rotation_y = rotation_y;
    }

    public float getRotation_z() {
        return rotation_z;
    }

    public void setRotation_z(float rotation_z) {
        this.rotation_z = rotation_z;
    }

}
