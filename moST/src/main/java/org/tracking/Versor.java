/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @author marcomoschettini
 */

package org.tracking;

public class Versor {
    private String activity;
    private String activity_pole;
    private double amp_average;
    private double amp_std;
    private double shape_std;
    private double[] fourier_shape;

    public Versor(double amp_average, double amp_std, double shape_std, double[] fourier_shape, String activity, String activity_pole) {
        this.setAmp_average(amp_average);
        this.setAmp_std(amp_std);
        this.setShape_std(shape_std);
        this.setFourier_shape(fourier_shape);
        this.setActivity(activity);
        this.setActivity_pole(activity_pole);
    }

    /**
     * @return the amp_average
     */
    public double getAmp_average() {
        return amp_average;
    }

    /**
     * @param amp_average the amp_average to set
     */
    public void setAmp_average(double amp_average) {
        this.amp_average = amp_average;
    }

    /**
     * @return the amp_std
     */
    public double getAmp_std() {
        return amp_std;
    }

    /**
     * @param amp_std the amp_std to set
     */
    public void setAmp_std(double amp_std) {
        this.amp_std = amp_std;
    }

    /**
     * @return the shape_std
     */
    public double getShape_std() {
        return shape_std;
    }

    /**
     * @param shape_std the shape_std to set
     */
    public void setShape_std(double shape_std) {
        this.shape_std = shape_std;
    }

    /**
     * @return the fourier_shape
     */
    public double[] getFourier_shape() {
        return fourier_shape;
    }

    /**
     * @param fourier_shape the fourier_shape to set
     */
    public void setFourier_shape(double[] fourier_shape) {
        this.fourier_shape = fourier_shape;
    }

    /**
     * @return the activity
     */
    public String getActivity() {
        return activity;
    }

    /**
     * @param activity the activity to set
     */
    public void setActivity(String activity) {
        this.activity = activity;
    }

    /**
     * @return the activity_pole
     */
    public String getActivity_pole() {
        return activity_pole;
    }

    /**
     * @param activity_pole the activity_pole to set
     */
    public void setActivity_pole(String activity_pole) {
        this.activity_pole = activity_pole;
    }
}