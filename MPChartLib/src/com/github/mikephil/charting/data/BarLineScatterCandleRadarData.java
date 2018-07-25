
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

import com.github.mikephil.charting.utils.LimitLine;

import java.util.ArrayList;

/**
 * Baseclass for all Line, Bar, Radar and ScatterData. Supports LimitLines.
 */
public abstract class BarLineScatterCandleRadarData<T extends BarLineScatterCandleRadarDataSet<? extends Entry>>
        extends ChartData<T> {

    /** array of limit-lines that are set for this data object */
    private ArrayList<LimitLine> mLimitLines;
    
    public BarLineScatterCandleRadarData(ArrayList<String> xVals) {
        super(xVals);
    }
    
    public BarLineScatterCandleRadarData(String[] xVals) {
        super(xVals);
    }

    public BarLineScatterCandleRadarData(ArrayList<String> xVals, ArrayList<T> sets) {
        super(xVals, sets);
    }

    public BarLineScatterCandleRadarData(String[] xVals, ArrayList<T> sets) {
        super(xVals, sets);
    }

    /**
     * Adds a new LimitLine to the data.
     * 
     * @param limitLine
     */
    public void addLimitLine(LimitLine limitLine) {
        if (mLimitLines == null)
            mLimitLines = new ArrayList<LimitLine>();
        mLimitLines.add(limitLine);
        updateMinMax();
    }

    /**
     * Adds a new array of LimitLines.
     * 
     * @param lines
     */
    public void addLimitLines(ArrayList<LimitLine> lines) {
        mLimitLines = lines;
        updateMinMax();
    }

    /**
     * Resets the limit lines array to null. Causes no more limit lines to be
     * set for this data object.
     */
    public void resetLimitLines() {
        mLimitLines = null;
        calcMinMax(mDataSets);
    }

    /**
     * Returns the LimitLine array of this data object.
     * 
     * @return
     */
    public ArrayList<LimitLine> getLimitLines() {
        return mLimitLines;
    }

    /**
     * Returns the LimitLine from the limitlines array at the specified index.
     * 
     * @param index
     * @return
     */
    public LimitLine getLimitLine(int index) {
        if (mLimitLines == null || mLimitLines.size() <= index)
            return null;
        else
            return mLimitLines.get(index);
    }

    /**
     * Updates the min and max y-value according to the set limits.
     */
    private void updateMinMax() {

        if (mLimitLines == null)
            return;

        for (int i = 0; i < mLimitLines.size(); i++) {

            LimitLine l = mLimitLines.get(i);

            if (l.getLimit() > mYMax)
                mYMax = l.getLimit();

            if (l.getLimit() < mYMin)
                mYMin = l.getLimit();
        }
    }
}
