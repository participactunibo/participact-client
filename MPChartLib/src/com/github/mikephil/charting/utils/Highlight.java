
/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.mikephil.charting.utils;

/**
 * Contains information needed to determine the highlighted value.
 */
public class Highlight {

    /** the x-index of the highlighted value */
    private int mXIndex;
    
    /** the index of the dataset the highlighted value is in */
    private int mDataSetIndex;

    /**
     * constructor
     * 
     * @param x the index of the highlighted value on the x-axis
     * @param val the value at the position the user touched
     * @param dataSet the index of the DataSet the highlighted value belongs to
     */
    public Highlight(int x, int dataSet) {
        this.mXIndex = x;
        this.mDataSetIndex = dataSet;
    }

    /**
     * returns the index of the DataSet the highlighted value is in
     * 
     * @return
     */
    public int getDataSetIndex() {
        return mDataSetIndex;
    }

    /**
     * returns the index of the highlighted value on the x-axis
     * 
     * @return
     */
    public int getXIndex() {
        return mXIndex;
    }

    /**
     * returns true if this highlight object is equal to the other (compares
     * xIndex and dataSetIndex)
     * 
     * @param h
     * @return
     */
    public boolean equalTo(Highlight h) {

        if (h == null)
            return false;
        else {
            if (this.mDataSetIndex == h.mDataSetIndex && this.mXIndex == h.mXIndex)
                return true;
            else
                return false;
        }
    }
}
