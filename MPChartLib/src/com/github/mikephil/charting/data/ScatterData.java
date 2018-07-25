
/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.mikephil.charting.data;

import java.util.ArrayList;

public class ScatterData extends BarLineScatterCandleData<ScatterDataSet> {

    public ScatterData(ArrayList<String> xVals) {
        super(xVals);
    }

    public ScatterData(String[] xVals) {
        super(xVals);
    }

    public ScatterData(ArrayList<String> xVals, ArrayList<ScatterDataSet> dataSets) {
        super(xVals, dataSets);
    }

    public ScatterData(String[] xVals, ArrayList<ScatterDataSet> dataSets) {
        super(xVals, dataSets);
    }

    public ScatterData(ArrayList<String> xVals, ScatterDataSet dataSet) {
        super(xVals, toArrayList(dataSet));
    }

    public ScatterData(String[] xVals, ScatterDataSet dataSet) {
        super(xVals, toArrayList(dataSet));
    }

    private static ArrayList<ScatterDataSet> toArrayList(ScatterDataSet dataSet) {
        ArrayList<ScatterDataSet> sets = new ArrayList<ScatterDataSet>();
        sets.add(dataSet);
        return sets;
    }

    /**
     * Returns the maximum shape-size across all DataSets.
     * 
     * @return
     */
    public float getGreatestShapeSize() {

        float max = 0f;

        for (ScatterDataSet set : mDataSets) {
            float size = set.getScatterShapeSize();

            if (size > max)
                max = size;
        }

        return max;
    }
}
