
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
 * Class representing one entry in the chart. Might contain multiple values.
 * Might only contain a single value depending on the used constructor.
 */
public class Entry {

    /** the actual value */
    private float mVal = 0f;

    /** the index on the x-axis */
    private int mXIndex = 0;

    /** optional spot for additional data this Entry represents */
    private Object mData = null;

    /**
     * A Entry represents one single entry in the chart.
     * 
     * @param val the y value (the actual value of the entry)
     * @param xIndex the corresponding index in the x value array (index on the
     *            x-axis of the chart, must NOT be higher than the length of the
     *            x-values String array)
     */
    public Entry(float val, int xIndex) {
        mVal = val;
        mXIndex = xIndex;
    }

    /**
     * A Entry represents one single entry in the chart.
     * 
     * @param val the y value (the actual value of the entry)
     * @param xIndex the corresponding index in the x value array (index on the
     *            x-axis of the chart, must NOT be higher than the length of the
     *            x-values String array)
     * @param data Spot for additional data this Entry represents.
     */
    public Entry(float val, int xIndex, Object data) {
        this(val, xIndex);

        this.mData = data;
    }

    /**
     * returns the x-index the value of this object is mapped to
     * 
     * @return
     */
    public int getXIndex() {
        return mXIndex;
    }

    /**
     * sets the x-index for the entry
     * 
     * @param x
     */
    public void setXIndex(int x) {
        this.mXIndex = x;
    }

    /**
     * Returns the total value the entry represents.
     * 
     * @return
     */
    public float getVal() {
        return mVal;
    }

    /**
     * Sets the value for the entry.
     * 
     * @param val
     */
    public void setVal(float val) {
        this.mVal = val;
    }

    /**
     * Returns the data, additional information that this Entry represents, or
     * null, if no data has been specified.
     * 
     * @return
     */
    public Object getData() {
        return mData;
    }

    /**
     * Sets additional data this Entry should represents.
     * 
     * @param data
     */
    public void setData(Object data) {
        this.mData = data;
    }

    // /**
    // * If this Enry represents mulitple values (e.g. Stacked BarChart), it
    // will
    // * return the sum of them, otherwise just the one value it represents.
    // *
    // * @return
    // */
    // public float getSum() {
    // if (mVals == null)
    // return mVal;
    // else {
    //
    // float sum = 0f;
    //
    // for (int i = 0; i < mVals.length; i++)
    // sum += mVals[i];
    //
    // return sum;
    // }
    // }

    /**
     * returns an exact copy of the entry
     * 
     * @return
     */
    public Entry copy() {
        Entry e = new Entry(mVal, mXIndex, mData);
        return e;
    }

    /**
     * Compares value, xIndex and data of the entries. Returns true if entries
     * are equal, false if not.
     * 
     * @param e
     * @return
     */
    public boolean equalTo(Entry e) {

        if (e == null)
            return false;

        if (e.mData != this.mData)
            return false;
        if (e.mXIndex != this.mXIndex)
            return false;

        if (Math.abs(e.mVal - this.mVal) > 0.00001f)
            return false;

        return true;
    }

    /**
     * returns a string representation of the entry containing x-index and value
     */
    @Override
    public String toString() {
        return "Entry, xIndex: " + mXIndex + " val (sum): " + getVal();
    }
}
