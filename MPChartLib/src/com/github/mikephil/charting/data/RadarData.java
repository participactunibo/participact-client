
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

/**
 * Data container for the RadarChart.
 */
public class RadarData extends BarLineScatterCandleRadarData<RadarDataSet> {

    public RadarData(ArrayList<String> xVals) {
        super(xVals);
    }
    
    public RadarData(String[] xVals) {
        super(xVals);
    }
    
    public RadarData(ArrayList<String> xVals, ArrayList<RadarDataSet> dataSets) {
        super(xVals, dataSets);
    }

    public RadarData(String[] xVals, ArrayList<RadarDataSet> dataSets) {
        super(xVals, dataSets);
    }

    public RadarData(ArrayList<String> xVals, RadarDataSet dataSet) {
        super(xVals, toArrayList(dataSet));
    }

    public RadarData(String[] xVals, RadarDataSet dataSet) {
        super(xVals, toArrayList(dataSet));
    }
    
    private static ArrayList<RadarDataSet> toArrayList(RadarDataSet dataSet) {
        ArrayList<RadarDataSet> sets = new ArrayList<RadarDataSet>();
        sets.add(dataSet);
        return sets;
    }
}
