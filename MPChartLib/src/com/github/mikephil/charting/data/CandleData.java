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

public class CandleData extends BarLineScatterCandleData<CandleDataSet> {

    public CandleData(ArrayList<String> xVals) {
        super(xVals);
    }
    
    public CandleData(String[] xVals) {
        super(xVals);
    }
    
    public CandleData(ArrayList<String> xVals, ArrayList<CandleDataSet> dataSets) {
        super(xVals, dataSets);
    }

    public CandleData(String[] xVals, ArrayList<CandleDataSet> dataSets) {
        super(xVals, dataSets);
    }
    
    public CandleData(ArrayList<String> xVals, CandleDataSet dataSet) {
        super(xVals, toArrayList(dataSet));        
    }
    
    public CandleData(String[] xVals, CandleDataSet dataSet) {
        super(xVals, toArrayList(dataSet));
    }
    
    private static ArrayList<CandleDataSet> toArrayList(CandleDataSet dataSet) {
        ArrayList<CandleDataSet> sets = new ArrayList<CandleDataSet>();
        sets.add(dataSet);
        return sets;
    }
}
