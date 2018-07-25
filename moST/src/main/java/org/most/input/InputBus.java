/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */
package org.most.input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.most.DataBundle;

/**
 * Bus that dispatches data from {@link Input} to Pipelines.
 */
public class InputBus {

    protected Map<Input.Type, SingleInputBus> _buses;

    public InputBus() {
        _buses = new HashMap<Input.Type, SingleInputBus>();
        for (Input.Type type : Input.Type.values()) {
            _buses.put(type, new SingleInputBus(type));
        }
    }

	/**
	 * Adds a new {@link InputBus.Listener} for type {@link Input.Type}
	 * 
	 * @param inputType
	 *            The type to subscribe the listener to.
	 * @param listener
	 *            The listener to subscribe.
	 */
    public void addListener(Input.Type inputType, Listener listener) {
        _buses.get(inputType).addListener(listener);
    }

	/**
	 * Removes a listener from the {@link InputBus}.
	 * 
	 * @param inputType
	 *            The input type the listeners wants to unsubscribe from.
	 * @param listener
	 *            The listener to unsubscribe.
	 */
    public void removeListener(Input.Type inputType, Listener listener) {
        _buses.get(inputType).removeListener(listener);
    }

	/**
	 * Gets the input bus specific for a single {@link Input.Type}
	 * 
	 * @param inputType
	 *            The target {@link Input.Type}.
	 * @return The {@link SingleInputBus} for the specified type.
	 */
    public SingleInputBus getBus(Input.Type inputType) {
        return _buses.get(inputType);
    }

    /**
     * Returns all single input buses.
     *
     * @return All instantiated single input buses.
     */
    public Collection<SingleInputBus> getInputBuses() {
        List<SingleInputBus> result = new ArrayList<InputBus.SingleInputBus>(
                Input.Type.values().length);
        for (Input.Type t : _buses.keySet()) {
            result.add(_buses.get(t));
        }
        return result;
    }

    /**
     * Interface for objects that want to receive data from Inputs.
     */
    public interface Listener {

        public boolean isActive();

        public void onData(DataBundle b);
    }

    public static class SingleInputBus {

        private final Input.Type _inputType;
        private final Collection<Listener> _listeners;
        private int _listenerCount;

        public SingleInputBus(Input.Type inputType) {
            _inputType = inputType;
            _listeners = new ArrayList<InputBus.Listener>();
        }

        public synchronized int getListenerCount() {
            return _listenerCount;
        }

        public synchronized void addListener(Listener listener) {
            if (listener == null) {
                return;
            }
            if (!_listeners.contains(listener)) {
                _listeners.add(listener);
                _listenerCount++;
            }
        }

        public synchronized void removeListener(Listener listener) {
            if (_listeners.contains(listener)) {
                _listeners.remove(listener);
                _listenerCount--;
            }
        }

        public synchronized void post(DataBundle b) {
            b.setRefCount(_listenerCount);
            if (_listenerCount == 0) {
                b.release();
                return;
            }
            for (Listener listener : _listeners) {
                if (listener.isActive()) {
                    listener.onData(b);
                }
            }
        }

        public Input.Type getType() {
            return _inputType;
        }
    }
}
