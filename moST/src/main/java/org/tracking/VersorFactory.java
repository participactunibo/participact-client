/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package org.tracking;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class VersorFactory {

    private Context context;

    public VersorFactory(Context context) {
        this.context = context;

    }

    @SuppressWarnings("resource")
    public List<Versor> getVersors() throws IOException {
        List<Versor> versors = new ArrayList<Versor>();
        AssetManager am = context.getAssets();
        String[] versors_names = am.list("versors");
        String activity_pole = "";
        BufferedReader buf;

        for (String versor_name : versors_names) {

            String activity = versor_name.split("\\.")[0];
            try {
                activity_pole = versor_name.split("\\.")[1];
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            try {
                InputStream is = context.getAssets().open("versors/" + versor_name);
                buf = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line = buf.readLine();
                StringTokenizer st = new StringTokenizer(line, ";");
                Double amp_average = Double.parseDouble((String) st.nextElement());
                Double amp_std = Double.parseDouble((String) st.nextElement());
                Double shape_std = Double.parseDouble((String) st.nextElement());

                line = buf.readLine();
                st = new StringTokenizer(line, ",");
                List<Double> fourier_shape = new ArrayList<Double>();
                while (st.hasMoreElements()) {
                    fourier_shape.add(Double.parseDouble((String) st.nextElement()));
                }
                double[] fourier_array = new double[fourier_shape.size()];
                for (int i = 0; i < fourier_shape.size(); i++)
                    fourier_array[i] = fourier_shape.get(i);
                versors.add(new Versor(amp_average, amp_std, shape_std, fourier_array, activity, activity_pole));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return versors;
    }

}
