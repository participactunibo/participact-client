
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

import android.graphics.Path;

import com.github.mikephil.charting.charts.ScatterChart.ScatterShape;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;

public class ScatterDataSet extends BarLineScatterCandleRadarDataSet<Entry> {

    /** the size the scattershape will have, in screen pixels */
    private float mShapeSize = 12f;

    /**
     * the type of shape that is set to be drawn where the values are at,
     * default ScatterShape.SQUARE
     */
    private ScatterShape mScatterShape = ScatterShape.SQUARE;

    /**
     * Custom path object the user can provide that is drawn where the values
     * are at. This is used when ScatterShape.CUSTOM is set for a DataSet.
     */
    private Path mCustomScatterPath = null;

    public ScatterDataSet(ArrayList<Entry> yVals, String label) {
        super(yVals, label);

        // mShapeSize = Utils.convertDpToPixel(8f);
    }

    @Override
    public DataSet<Entry> copy() {

        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for (int i = 0; i < mYVals.size(); i++) {
            yVals.add(mYVals.get(i).copy());
        }

        ScatterDataSet copied = new ScatterDataSet(yVals, getLabel());
        copied.mColors = mColors;
        copied.mShapeSize = mShapeSize;
        copied.mScatterShape = mScatterShape;
        copied.mCustomScatterPath = mCustomScatterPath;
        copied.mHighLightColor = mHighLightColor;

        return copied;
    }

    /**
     * Sets the size in density pixels the drawn scattershape will have. This
     * only applies for non custom shapes.
     * 
     * @param size
     */
    public void setScatterShapeSize(float size) {
        mShapeSize = Utils.convertDpToPixel(size);
    }

    /**
     * returns the currently set scatter shape size
     * 
     * @return
     */
    public float getScatterShapeSize() {
        return mShapeSize;
    }

    /**
     * Sets the shape that is drawn on the position where the values are at. If
     * "CUSTOM" is chosen, you need to call setCustomScatterShape(...) and
     * provide a path object that is drawn as the custom scattershape.
     * 
     * @param shape
     */
    public void setScatterShape(ScatterShape shape) {
        mScatterShape = shape;
    }

    /**
     * returns all the different scattershapes the chart uses
     * 
     * @return
     */
    public ScatterShape getScatterShape() {
        return mScatterShape;
    }

    /**
     * Sets a path object as the shape to be drawn where the values are at. Do
     * not forget to call setScatterShape(...) and set the shape to
     * ScatterShape.CUSTOM.
     * 
     * @param shape
     */
    public void setCustomScatterShape(Path shape) {
        mCustomScatterPath = shape;
    }

    /**
     * returns the custom path / shape that is specified to be drawn where the
     * values are at
     * 
     * @return
     */
    public Path getCustomScatterShape() {
        return mCustomScatterPath;
    }
}
