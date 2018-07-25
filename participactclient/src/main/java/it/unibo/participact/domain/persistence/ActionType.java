/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.domain.persistence;

public enum ActionType {
    SENSING_MOST, PHOTO, QUESTIONNAIRE, ACTIVITY_DETECTION, GEOFENCE;

    @Override
    public String toString() {
        switch (this) {
            case SENSING_MOST:
                return "Passive sensing";
            case PHOTO:
                return "Photo";
            case QUESTIONNAIRE:
                return "Questionnaire";
            case ACTIVITY_DETECTION:
                return "Activity detection";
            case GEOFENCE:
                return "Geofence";
            default:
                return "Unknown";
        }
    }

    public static ActionType convertFrom(it.unibo.participact.domain.persistence.ActionType old) {
        if (old.name().equalsIgnoreCase(SENSING_MOST.name())) {
            return SENSING_MOST;
        }

        if (old.name().equalsIgnoreCase(PHOTO.name())) {
            return PHOTO;
        }

        if (old.name().equalsIgnoreCase(QUESTIONNAIRE.name())) {
            return QUESTIONNAIRE;
        }
        if (old.name().equalsIgnoreCase(GEOFENCE.name())) {
            return GEOFENCE;
        }

        return null;
    }

}
