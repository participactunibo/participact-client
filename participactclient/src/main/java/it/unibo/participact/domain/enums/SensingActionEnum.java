/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.domain.enums;

public class SensingActionEnum {

    public static enum Type {
        DUMMY, AUDIO_CLASSIFIER, ACCELEROMETER, ACCELEROMETER_CLASSIFIER, RAW_AUDIO, AVERAGE_ACCELEROMETER, APP_ON_SCREEN, APPS_NET_TRAFFIC, BATTERY, BLUETOOTH, CELL, CONNECTION_TYPE, DEVICE_NET_TRAFFIC, GYROSCOPE, INSTALLED_APPS, LIGHT, LOCATION, MAGNETIC_FIELD, PHONE_CALL_DURATION, PHONE_CALL_EVENT, SYSTEM_STATS, WIFI_SCAN, TEST;

        /**
         * Converts an integer to a valid Pipeline type.
         *
         * @param value The integer to convert
         * @return The Type represented by <code>value</code>. If the conversion
         * fails, returns DUMMY.
         */
        public static Type fromInt(int value) {
            switch (value) {
                case 1:
                    return ACCELEROMETER;
                case 2:
                    return RAW_AUDIO;
                case 3:
                    return AVERAGE_ACCELEROMETER;
                case 4:
                    return APP_ON_SCREEN;
                case 5:
                    return BATTERY;
                case 6:
                    return BLUETOOTH;
                case 7:
                    return CELL;
                case 8:
                    return GYROSCOPE;
                case 9:
                    return INSTALLED_APPS;
                case 10:
                    return LIGHT;
                case 11:
                    return LOCATION;
                case 12:
                    return MAGNETIC_FIELD;
                case 13:
                    return PHONE_CALL_DURATION;
                case 14:
                    return PHONE_CALL_EVENT;
                case 15:
                    return ACCELEROMETER_CLASSIFIER;
                case 16:
                    return SYSTEM_STATS;
                case 17:
                    return WIFI_SCAN;
                case 18:
                    return AUDIO_CLASSIFIER;
                case 19:
                    return DEVICE_NET_TRAFFIC;
                case 20:
                    return APPS_NET_TRAFFIC;
                case 21:
                    return CONNECTION_TYPE;
                case 99:
                    return TEST;
                default:
                    return DUMMY;
            }
        }


        public static String fromIntToHumanReadable(int value) {
            switch (value) {
                case 1:
                    return "Accelerometro";
                case 2:
                    return "Audio";
                case 3:
                    return "Accelerometro";
                case 4:
                    return "Applicazione in uso";
                case 5:
                    return "Livello batteria";
                case 6:
                    return "Bluetooth";
                case 7:
                    return "Stazione cellulare";
                case 8:
                    return "Giroscopio";
                case 9:
                    return "Applicazioni installate";
                case 10:
                    return "Sensore di luminosità";
                case 11:
                    return "Posizione geografica";
                case 12:
                    return "Magnetometro";
                case 13:
                    return "Durata telefonate";
                case 14:
                    return "Numero di telefonate";
                case 15:
                    return "Attività fisica";
                case 16:
                    return "Statistiche di sistema";
                case 17:
                    return "Wifi";
                case 18:
                    return "Tipo di suono (Microfono)";
                case 19:
                    return "Misura volume traffico dati dispositivo";
                case 20:
                    return "Misura volume traffico dati applicazioni";
                case 21:
                    return "Tipo di connessione";
                case 22:
                    return "Attività fisica V.2";
                case 23:
                    return "Riconoscimento Attività";
                case 24:
                    return "Riconoscimenti Dinamico";
                case 99:
                    return "Test";
                default:
                    return "Dummy";
            }
        }

        /**
         * Converts the Pipeline type to an integer.
         *
         * @return
         */
        public int toInt() {
            switch (this) {
                case ACCELEROMETER:
                    return 1;
                case RAW_AUDIO:
                    return 2;
                case AVERAGE_ACCELEROMETER:
                    return 3;
                case APP_ON_SCREEN:
                    return 4;
                case BATTERY:
                    return 5;
                case BLUETOOTH:
                    return 6;
                case CELL:
                    return 7;
                case GYROSCOPE:
                    return 8;
                case INSTALLED_APPS:
                    return 9;
                case LIGHT:
                    return 10;
                case LOCATION:
                    return 11;
                case MAGNETIC_FIELD:
                    return 12;
                case PHONE_CALL_DURATION:
                    return 13;
                case PHONE_CALL_EVENT:
                    return 14;
                case ACCELEROMETER_CLASSIFIER:
                    return 15;
                case SYSTEM_STATS:
                    return 16;
                case WIFI_SCAN:
                    return 17;
                case AUDIO_CLASSIFIER:
                    return 18;
                case DEVICE_NET_TRAFFIC:
                    return 19;
                case APPS_NET_TRAFFIC:
                    return 20;
                case CONNECTION_TYPE:
                    return 21;
                case TEST:
                    return 99;
                default:
                    return 0;
            }
        }

    }
}
