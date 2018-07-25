/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */
package org.most;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.most.pipeline.Pipeline;

public class MoSTState implements Serializable {

    private static final long serialVersionUID = 3179401417398931073L;

    List<Pipeline.Type> activePipeline;
    AtomicInteger runningPipeline = new AtomicInteger(0);
    Map<Pipeline.Type, Integer> listeners;

    public List<Pipeline.Type> getActivePipeline() {
        return activePipeline;
    }
    public void setActivePipeline(List<Pipeline.Type> activePipeline) {
        this.activePipeline = activePipeline;
    }
    public AtomicInteger getRunningPipeline() {
        return runningPipeline;
    }
    public void setRunningPipeline(AtomicInteger runningPipeline) {
        this.runningPipeline = runningPipeline;
    }
    public Map<Pipeline.Type, Integer> getListeners() {
        return listeners;
    }
    public void setListeners(Map<Pipeline.Type, Integer> listeners) {
        this.listeners = listeners;
    }

}
