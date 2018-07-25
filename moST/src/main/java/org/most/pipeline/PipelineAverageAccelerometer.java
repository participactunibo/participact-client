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

import java.util.HashSet;
import java.util.Set;

import org.most.DataBundle;
import org.most.MoSTApplication;
import org.most.input.Input;
import org.most.input.InputAccelerometer;
import org.most.utils.LimitedLinkedList;

import android.util.Log;

public class PipelineAverageAccelerometer extends Pipeline {

    private LimitedLinkedList<Float> _x;
    private LimitedLinkedList<Float> _y;
    private LimitedLinkedList<Float> _z;
    private static final int WINDOW_SIZE = 200;
    private static final int WINDOW_SLIDE = 100;
    private int counter;

    public PipelineAverageAccelerometer(MoSTApplication context) {
        super(context);
    }

    @Override
    public boolean onActivate() {
        _x = new LimitedLinkedList<Float>(WINDOW_SIZE);
        _y = new LimitedLinkedList<Float>(WINDOW_SIZE);
        _z = new LimitedLinkedList<Float>(WINDOW_SIZE);
        counter = WINDOW_SLIDE;
        return super.onActivate();
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        _x.clear();
        _y.clear();
        _z.clear();
    }

    public void onData(DataBundle b) {
        float[] data = b.getFloatArray(InputAccelerometer.KEY_ACCELERATIONS);
        _x.add(data[0]);
        _y.add(data[1]);
        _z.add(data[2]);
        counter--;
        if (counter == 0) {
            counter = WINDOW_SLIDE;
            int listSize = _x.size();
            float avgx = 0;
            float avgy = 0;
            float avgz = 0;
            for (int i = 0; i < listSize; i++) {
                avgx += _x.get(i);
                avgy += _y.get(i);
                avgz += _z.get(i);
            }
            avgx = avgx / listSize;
            avgy = avgy / listSize;
            avgz = avgz / listSize;
            Log.d("AvgAccel", String.format("x = %f ; y = %f; z = %f", avgx, avgy, avgz));
        }
        b.release();
    }

    @Override
    public Pipeline.Type getType() {
        return Type.AVERAGE_ACCELEROMETER;
    }

    @Override
    public Set<org.most.input.Input.Type> getInputs() {
        Set<Input.Type> usedInputs = new HashSet<Input.Type>();
        usedInputs.add(Input.Type.ACCELEROMETER);
        return usedInputs;
    }

}
