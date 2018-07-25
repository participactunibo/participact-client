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


public class DataSystemStats extends Data {

    private static final long serialVersionUID = -6840772171803572959L;

    private float CPU_FREQUENCY;

    private long CPU_USER;

    private long CPU_NICED;

    private long CPU_IDLE;

    private long CPU_SYSTEM;

    private long CPU_IOWAIT;

    private long CPU_HARDIRQ;

    private long CPU_SOFTIRQ;

    private long CONTEXT_SWITCHES;

    private long BOOT_TIME;

    private long PROCESSES;

    private long MEM_TOTAL;

    private long MEM_FREE;

    private long MEM_ACTIVE;

    private long MEM_INACTIVE;

    public float getCPU_FREQUENCY() {
        return CPU_FREQUENCY;
    }

    public void setCPU_FREQUENCY(float value) {
        CPU_FREQUENCY = value;
    }

    public long getCPU_USER() {
        return CPU_USER;
    }

    public void setCPU_USER(long value) {
        CPU_USER = value;
    }

    public long getCPU_NICED() {
        return CPU_NICED;
    }

    public void setCPU_NICED(long value) {
        CPU_NICED = value;
    }

    public long getCPU_IDLE() {
        return CPU_IDLE;
    }

    public void setCPU_IDLE(long value) {
        CPU_IDLE = value;
    }

    public long getCPU_SYSTEM() {
        return CPU_SYSTEM;
    }

    public void setCPU_SYSTEM(long value) {
        CPU_SYSTEM = value;
    }

    public long getCPU_IOWAIT() {
        return CPU_IOWAIT;
    }

    public void setCPU_IOWAIT(long value) {
        CPU_IOWAIT = value;
    }

    public long getCPU_HARDIRQ() {
        return CPU_HARDIRQ;
    }

    public void setCPU_HARDIRQ(long value) {
        CPU_HARDIRQ = value;
    }

    public long getCPU_SOFTIRQ() {
        return CPU_SOFTIRQ;
    }

    public void setCPU_SOFTIRQ(long value) {
        CPU_SOFTIRQ = value;
    }

    public long getCONTEXT_SWITCHES() {
        return CONTEXT_SWITCHES;
    }

    public void setCONTEXT_SWITCHES(long value) {
        CONTEXT_SWITCHES = value;
    }

    public long getBOOT_TIME() {
        return BOOT_TIME;
    }

    public void setBOOT_TIME(long value) {
        BOOT_TIME = value;
    }

    public long getPROCESSES() {
        return PROCESSES;
    }

    public void setPROCESSES(long value) {
        PROCESSES = value;
    }

    public long getMEM_TOTAL() {
        return MEM_TOTAL;
    }

    public void setMEM_TOTAL(long value) {
        MEM_TOTAL = value;
    }

    public long getMEM_FREE() {
        return MEM_FREE;
    }

    public void setMEM_FREE(long value) {
        MEM_FREE = value;
    }

    public long getMEM_ACTIVE() {
        return MEM_ACTIVE;
    }

    public void setMEM_ACTIVE(long value) {
        MEM_ACTIVE = value;
    }

    public long getMEM_INACTIVE() {
        return MEM_INACTIVE;
    }

    public void setMEM_INACTIVE(long value) {
        MEM_INACTIVE = value;
    }


}
