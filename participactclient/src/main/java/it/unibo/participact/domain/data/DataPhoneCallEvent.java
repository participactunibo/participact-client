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


public class DataPhoneCallEvent extends Data {

    private static final long serialVersionUID = -837842733892409270L;

    private boolean is_start;

    private boolean is_incoming;

    private String phone_number;

    public boolean isIs_start() {
        return is_start;
    }

    public void setIs_start(boolean is_start) {
        this.is_start = is_start;
    }

    public boolean isIs_incoming() {
        return is_incoming;
    }

    public void setIs_incoming(boolean is_incoming) {
        this.is_incoming = is_incoming;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

}
