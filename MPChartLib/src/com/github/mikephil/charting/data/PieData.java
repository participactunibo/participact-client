
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
 * A PieData object can only represent one DataSet. Unlike all other charts, the
 * legend labels of the PieChart are created from the x-values array, and not
 * from the DataSet labels.
 */
public class PieData extends ChartData<PieDataSet> {
    
    public PieData(ArrayList<String> xVals) {
        super(xVals);
    }
    
    public PieData(String[] xVals) {
        super(xVals);
    }

    public PieData(ArrayList<String> xVals, PieDataSet dataSet) {
        super(xVals, toArrayList(dataSet));
    }

    public PieData(String[] xVals, PieDataSet dataSet) {
        super(xVals, toArrayList(dataSet));
    }
    
    private static ArrayList<PieDataSet> toArrayList(PieDataSet dataSet) {
        ArrayList<PieDataSet> sets = new ArrayList<PieDataSet>();
        sets.add(dataSet);
        return sets;
    }

    /**
     * Returns the DataSet this PieData object represents.
     * 
     * @return
     */
    public PieDataSet getDataSet() {
        return (PieDataSet) mDataSets.get(0);
    }
}
