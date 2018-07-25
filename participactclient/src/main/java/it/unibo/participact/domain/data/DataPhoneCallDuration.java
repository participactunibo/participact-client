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


public class DataPhoneCallDuration extends Data {

    private static final long serialVersionUID = 4562522041518010186L;

    private long call_start;

    private long call_end;

    private boolean is_incoming;

    private String phone_number;

    public long getCall_start() {
        return call_start;
    }

    public void setCall_start(long call_start) {
        this.call_start = call_start;
    }

    public long getCall_end() {
        return call_end;
    }

    public void setCall_end(long call_end) {
        this.call_end = call_end;
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
