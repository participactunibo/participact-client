/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */
package org.most.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.most.DataBundle;

/**
 * Bus that dispatches data from {@link Pipeline} to users.
 *
 */
public class PipelineBus {

    protected Map<Pipeline.Type, SinglePipelineBus> _buses;

    public PipelineBus() {
        _buses = new HashMap<Pipeline.Type, SinglePipelineBus>();
        for (Pipeline.Type type : Pipeline.Type.values()) {
            _buses.put(type, new SinglePipelineBus());
        }
    }

    public void addListener(Pipeline.Type inputType, Listener listener) {
        _buses.get(inputType).addListener(listener);
    }

    public void removeListener(Pipeline.Type inputType, Listener listener) {
        _buses.get(inputType).removeListener(listener);
    }

    public SinglePipelineBus getBus(Pipeline.Type inputType) {
        return _buses.get(inputType);
    }

    public interface Listener {

        public void onData(DataBundle b);
    }

    public static class SinglePipelineBus {

        private Collection<Listener> _listeners;
        private int _listenerCount;

        public SinglePipelineBus() {
            _listeners = new ArrayList<PipelineBus.Listener>();
        }

        public synchronized void addListener(Listener listener) {
            if (!_listeners.contains(listener)) {
                _listeners.add(listener);
                _listenerCount++;
            }
        }

        public synchronized void removeListener(Listener listener) {
            if (_listeners.contains(listener)) {
                _listeners.add(listener);
                _listenerCount--;
            }
        }

        public synchronized void post(DataBundle b) {
            b.setRefCount(_listenerCount);
            for (Listener listener : _listeners) {
                listener.onData(b);
            }
        }
    }
}
