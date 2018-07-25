
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
 * Baseclass for all Line, Bar and ScatterData.
 */
public abstract class BarLineScatterCandleData<T extends BarLineScatterCandleRadarDataSet<? extends Entry>>
        extends BarLineScatterCandleRadarData<T> {
    
    public BarLineScatterCandleData(ArrayList<String> xVals) {
        super(xVals);
    }
    
    public BarLineScatterCandleData(String[] xVals) {
        super(xVals);
    }

    public BarLineScatterCandleData(ArrayList<String> xVals, ArrayList<T> sets) {
        super(xVals, sets);
    }

    public BarLineScatterCandleData(String[] xVals, ArrayList<T> sets) {
        super(xVals, sets);
    }
}
