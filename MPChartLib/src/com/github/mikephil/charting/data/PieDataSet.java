
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

import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;

public class PieDataSet extends DataSet<Entry> {

    /** the space in degrees between the chart-slices, default 0f */
    private float mSliceSpace = 0f;

    /** indicates the selection distance of a pie slice */
    private float mShift = 18f;

    public PieDataSet(ArrayList<Entry> yVals, String label) {
        super(yVals, label);

//        mShift = Utils.convertDpToPixel(12f);
    }

    @Override
    public DataSet<Entry> copy() {

        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for (int i = 0; i < mYVals.size(); i++) {
            yVals.add(mYVals.get(i).copy());
        }

        PieDataSet copied = new PieDataSet(yVals, getLabel());
        copied.mColors = mColors;
        copied.mSliceSpace = mSliceSpace;
        copied.mShift = mShift;
        return copied;
    }

    /**
     * sets the space that is left out between the piechart-slices, default: 0°
     * --> no space, maximum 45, minimum 0 (no space)
     * 
     * @param degrees
     */
    public void setSliceSpace(float degrees) {

        if (degrees > 45)
            degrees = 45f;
        if (degrees < 0)
            degrees = 0f;

        mSliceSpace = degrees;
    }

    /**
     * returns the space that is set to be between the piechart-slices of this
     * DataSet, in degrees
     * 
     * @return
     */
    public float getSliceSpace() {
        return mSliceSpace;
    }

    /**
     * sets the distance the highlighted piechart-slice of this DataSet is
     * "shifted" away from the center of the chart, default 12f
     * 
     * @param shift
     */
    public void setSelectionShift(float shift) {
        mShift = Utils.convertDpToPixel(shift);
    }

    /**
     * returns the distance a highlighted piechart slice is "shifted" away from
     * the chart-center
     * 
     * @return
     */
    public float getSelectionShift() {
        return mShift;
    }
}