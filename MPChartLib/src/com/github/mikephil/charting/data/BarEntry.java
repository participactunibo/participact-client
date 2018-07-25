
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

/**
 * Entry class for the BarChart. (especially stacked bars)
 */
public class BarEntry extends Entry {

    /** the values the stacked barchart holds */
    private float[] mVals;

    /**
     * Constructor for stacked bar entries.
     * 
     * @param vals
     * @param xIndex
     */
    public BarEntry(float[] vals, int xIndex) {
        super(calcSum(vals), xIndex);

        this.mVals = vals;
    }

    /**
     * Constructor for normal bars (not stacked).
     * 
     * @param val
     * @param xIndex
     */
    public BarEntry(float val, int xIndex) {
        super(val, xIndex);
    }

    /**
     * Constructor for stacked bar entries.
     * 
     * @param vals
     * @param xIndex
     * @param label Additional description label.
     */
    public BarEntry(float[] vals, int xIndex, String label) {
        super(calcSum(vals), xIndex, label);

        this.mVals = vals;
    }

    /**
     * Constructor for normal bars (not stacked).
     * 
     * @param val
     * @param xIndex
     * @param data Spot for additional data this Entry represents.
     */
    public BarEntry(float val, int xIndex, Object data) {
        super(val, xIndex, data);
    }

    /**
     * Returns an exact copy of the BarEntry.
     */
    public BarEntry copy() {

        BarEntry copied = new BarEntry(getVal(), getXIndex(), getData());
        copied.mVals = mVals;
        return copied;
    }

    /**
     * Returns the stacked values this BarEntry represents, or null, if only a
     * single value is represented (then, use getVal()).
     * 
     * @return
     */
    public float[] getVals() {
        return mVals;
    }

    /**
     * Set the array of values this BarEntry should represent.
     * 
     * @param vals
     */
    public void setVals(float[] vals) {
        mVals = vals;
    }

    /**
     * Returns the closest value inside the values array (for stacked barchart)
     * to the value given as a parameter. The closest value must be higher
     * (above) the provided value.
     * 
     * @param val
     * @return
     */
    public int getClosestIndexAbove(float val) {

        if (mVals == null)
            return 0;

        float dist = 0f;
        int closestIndex = 0;

        for (int i = 0; i < mVals.length; i++) {

            float newDist = Math.abs((getVal() - mVals[i]) - val);

            if (newDist < dist && mVals[i] > val) {
                dist = newDist;
                closestIndex = i;
            }
        }

        return closestIndex;
    }

    /**
     * Calculates the sum across all values.
     * 
     * @param vals
     * @return
     */
    private static float calcSum(float[] vals) {

        float sum = 0f;

        for (float f : vals)
            sum += f;

        return sum;
    }
}
