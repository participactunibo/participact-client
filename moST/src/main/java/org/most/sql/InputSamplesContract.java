/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package org.most.sql;

import android.provider.BaseColumns;

public class InputSamplesContract {
    public InputSamplesContract() {
    }

    public static abstract class InputSamplesEntry implements BaseColumns {
        public static final String TABLE_NAME = "input_samples";
        public static final String TIMESTAMP_COLUMN = "timestamp";
        public static final String NORM_COLUMN = "norm";
        public static final String SAMPLES_COLUMN = "samples";
        public static final String GPS_DISTANCE_COLUMN = "gps_distance";
        public static final String ACTIVITY_TYPE_COLUMN = "activity";
        public static final String ACTIVITY_POLE_COLUMN = "activity_pole";
    }

}
