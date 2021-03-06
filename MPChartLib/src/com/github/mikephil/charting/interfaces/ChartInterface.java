
/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.mikephil.charting.interfaces;

import android.graphics.RectF;
import android.view.View;

/**
 * Interface that provides everything there is to know about the dimensions,
 * bounds, and range of the chart.
 */
public interface ChartInterface {

    public float getOffsetBottom();

    public float getOffsetTop();

    public float getOffsetLeft();

    public float getOffsetRight();

    public float getDeltaX();

    public float getDeltaY();

    public float getYChartMin();

    public float getYChartMax();

    public int getWidth();

    public int getHeight();
    
    public RectF getContentRect();
    
    public View getChartView();
}
