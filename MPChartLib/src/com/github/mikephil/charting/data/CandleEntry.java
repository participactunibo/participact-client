
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
 * Subclass of Entry that holds all values for one entry in a CandleStickChart.
 */
public class CandleEntry extends Entry {

    /** shadow-high value */
    private float mShadowHigh = 0f;

    /** shadow-low value */
    private float mShadowLow = 0f;

    /** close value */
    private float mClose = 0f;

    /** open value */
    private float mOpen = 0f;

    /**
     * Constructor.
     * 
     * @param xIndex The index on the x-axis.
     * @param shadowH The (shadow) high value.
     * @param shadowL The (shadow) low value.
     * @param open
     * @param close
     */
    public CandleEntry(int xIndex, float shadowH, float shadowL, float open, float close) {
        super((shadowH + shadowL) / 2f, xIndex);

        this.mShadowHigh = shadowH;
        this.mShadowLow = shadowL;
        this.mOpen = open;
        this.mClose = close;
    }

    /**
     * Constructor.
     * 
     * @param xIndex The index on the x-axis.
     * @param shadowH The (shadow) high value.
     * @param shadowL The (shadow) low value.
     * @param open
     * @param close
     * @param data Spot for additional data this Entry represents.
     */
    public CandleEntry(int xIndex, float shadowH, float shadowL, float open, float close,
            Object data) {
        super((shadowH + shadowL) / 2f, xIndex, data);

        this.mShadowHigh = shadowH;
        this.mShadowLow = shadowL;
        this.mOpen = open;
        this.mClose = close;
    }

    /**
     * Returns the overall range (difference) between shadow-high and
     * shadow-low.
     * 
     * @return
     */
    public float getShadowRange() {
        return Math.abs(mShadowHigh - mShadowLow);
    }

    /**
     * Returns the body size (difference between open and close).
     * 
     * @return
     */
    public float getBodyRange() {
        return Math.abs(mOpen - mClose);
    }

    /**
     * Returns the center value of the candle. (Middle value between high and
     * low)
     */
    @Override
    public float getVal() {
        return super.getVal();
    }

    public CandleEntry copy() {

        CandleEntry c = new CandleEntry(getXIndex(), mShadowHigh, mShadowLow, mOpen,
                mClose, getData());

        return c;
    }

    /**
     * Returns the upper shadows highest value.
     * 
     * @return
     */
    public float getHigh() {
        return mShadowHigh;
    }

    public void setHigh(float mShadowHigh) {
        this.mShadowHigh = mShadowHigh;
    }

    /**
     * Returns the lower shadows lowest value.
     * 
     * @return
     */
    public float getLow() {
        return mShadowLow;
    }

    public void setLow(float mShadowLow) {
        this.mShadowLow = mShadowLow;
    }

    /**
     * Returns the bodys close value.
     * 
     * @return
     */
    public float getClose() {
        return mClose;
    }

    public void setClose(float mClose) {
        this.mClose = mClose;
    }

    /**
     * Returns the bodys open value.
     * 
     * @return
     */
    public float getOpen() {
        return mOpen;
    }

    public void setOpen(float mOpen) {
        this.mOpen = mOpen;
    }
}
