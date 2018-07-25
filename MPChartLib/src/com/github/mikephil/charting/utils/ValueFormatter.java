
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
 * Interface that allows custom formatting of all values and value-labels
 * displayed inside the chart. Simply create your own formatting class and let
 * it implement ValueFormatter. Then override the getFormattedLabel(...) method
 * and return whatever you want.
 */
public interface ValueFormatter {

    /**
     * Called when a value (from labels, or inside the chart) is formatted
     * before being drawn. For performance reasons, avoid excessive calculations
     * and memory allocations inside this method.
     * 
     * @param value the value to be formatted
     * @return the formatted label ready for being drawn
     */
    public String getFormattedValue(float value);
}
