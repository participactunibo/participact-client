/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */
package org.most.weka;

import java.util.ArrayList;
import java.util.Iterator;

public class Features {
    private float avgX;
    private float avgY;
    private float avgZ;
    private float devX;
    private float devY;
    private float devZ;
    private float maxX;
    private float maxY;
    private float maxZ;
    private float minX;
    private float minY;
    private float minZ;
    private int crossX;
    private int crossY;
    private int crossZ;
    private float squareEnX;
    private float squareEnY;
    private float squareEnZ;

    public Object[] getFeatures()
	{
        Object[] temp = new Object[18];
        temp[0] = Double.parseDouble("" + avgX);
        temp[1] = Double.parseDouble("" + avgY);
        temp[2] = Double.parseDouble("" + avgZ);
        temp[3] = Double.parseDouble("" + maxX);
        temp[4] = Double.parseDouble("" + maxY);
        temp[5] = Double.parseDouble("" + maxZ);
        temp[6] = Double.parseDouble("" + minX);
        temp[7] = Double.parseDouble("" + minY);
        temp[8] = Double.parseDouble("" + minZ);
        temp[9] = Double.parseDouble("" + devX);
        temp[10] = Double.parseDouble("" + devY);
        temp[11] = Double.parseDouble("" + devZ);
        temp[12] = Double.parseDouble("" + crossX);
        temp[13] = Double.parseDouble("" + crossY);
        temp[14] = Double.parseDouble("" + crossZ);
        temp[15] = Double.parseDouble("" + squareEnX);
        temp[16] = Double.parseDouble("" + squareEnY);
        temp[17] = Double.parseDouble("" + squareEnZ);
        return temp;
    }

    public void calcolaX(ArrayList<Float> x)
	{
        maxX = x.get(0);
        Iterator<Float> i = x.iterator();
        while (i.hasNext())
		{
            float f = i.next();
            if (f > maxX)
                maxX = f;
        }
        minX = x.get(0);
        i = x.iterator();
        while (i.hasNext())
		{
            float f = i.next();
            if (f < minX)
                minX = f;
        }
        avgX = 0;
        i = x.iterator();
        while (i.hasNext())
		{
            avgX += i.next();
        }
        avgX = avgX / x.size();
        devX = 0;
        i = x.iterator();
        while (i.hasNext())
		{
            devX += Math.pow(i.next() - avgX, 2.0);
        }
        devX = devX / x.size();
        devX = (float) Math.sqrt(devX);
        crossX = 0;
        Boolean previousPositive;
        if (x.get(0) >= 0)
            previousPositive = true;
        else
            previousPositive = false;
        i = x.iterator();
        while (i.hasNext())
		{
            float temp = i.next();
            if (temp >= 0)
			{
                if (previousPositive == false)
				{
                    crossX++;
                    previousPositive = true;
                }
            }
			else
			{
                if (previousPositive == true)
				{
                    crossX++;
                    previousPositive = false;
                }
            }
        }
        int numX = 0;
        squareEnX = 0;
        i = x.iterator();
        while (i.hasNext())
		{
            squareEnX = (float) (squareEnX + Math.pow(i.next(), 2.0));
            numX++;
        }
        squareEnX = squareEnX / numX;
        squareEnX = (float) Math.sqrt(squareEnX);
    }

    public void calcolaY(ArrayList<Float> y)
	{
        maxY = y.get(0);
        Iterator<Float> i = y.iterator();
        while (i.hasNext())
		{
            float f = i.next();
            if (f > maxY)
                maxY = f;
        }
        minY = y.get(0);
        i = y.iterator();
        while (i.hasNext())
		{
            float f = i.next();
            if (f < minY)
                minY = f;
        }
        avgY = 0;
        i = y.iterator();
        while (i.hasNext())
		{
            avgY += i.next();
        }
        avgY = avgY / y.size();
        devY = 0;
        i = y.iterator();
        while (i.hasNext())
		{
            devY += Math.pow(i.next() - avgY, 2.0);
        }
        devY = devY / y.size();
        devY = (float) Math.sqrt(devY);
        crossY = 0;
        Boolean previousPositive;
        if (y.get(0) >= 0)
            previousPositive = true;
        else
            previousPositive = false;
        i = y.iterator();
        while (i.hasNext())
		{
            float temp = i.next();
            if (temp >= 0)
			{
                if (previousPositive == false)
				{
                    crossY++;
                    previousPositive = true;
                }
            }
			else
			{
                if (previousPositive == true)
				{
                    crossY++;
                    previousPositive = false;
                }
            }
        }
        int numY = 0;
        squareEnY = 0;
        i = y.iterator();
        while (i.hasNext())
		{
            squareEnY = (float) (squareEnY + Math.pow(i.next(), 2.0));
            numY++;
        }
        squareEnY = squareEnY / numY;
        squareEnY = (float) Math.sqrt(squareEnY);
    }

    public void calcolaZ(ArrayList<Float> z)
	{
        maxZ = z.get(0);
        Iterator<Float> i = z.iterator();
        while (i.hasNext())
		{
            float f = i.next();
            if (f > maxZ)
                maxZ = f;
        }
        minZ = z.get(0);
        i = z.iterator();
        while (i.hasNext())
		{
            float f = i.next();
            if (f < minZ)
                minZ = f;
        }
        avgZ = 0;
        i = z.iterator();
        while (i.hasNext())
		{
            avgZ += i.next();
        }
        avgZ = avgZ / z.size();
        devZ = 0;
        i = z.iterator();
        while (i.hasNext())
		{
            devZ += Math.pow(i.next() - avgZ, 2.0);
        }
        devZ = devZ / z.size();
        devZ = (float) Math.sqrt(devZ);
        crossZ = 0;
        Boolean previousPositive;
        if (z.get(0) >= 0)
            previousPositive = true;
        else
            previousPositive = false;
        i = z.iterator();
        while (i.hasNext())
		{
            float temp = i.next();
            if (temp >= 0)
			{
                if (previousPositive == false)
				{
                    crossZ++;
                    previousPositive = true;
                }
            }
			else
			{
                if (previousPositive == true)
				{
                    crossZ++;
                    previousPositive = false;
                }
            }
        }
        int numZ = 0;
        squareEnZ = 0;
        i = z.iterator();
        while (i.hasNext())
		{
            squareEnZ = (float) (squareEnZ + Math.pow(i.next(), 2.0));
            numZ++;
        }
        squareEnZ = squareEnZ / numZ;
        squareEnZ = (float) Math.sqrt(squareEnZ);
    }

    public String toString()
	{
        return "" + this.avgX + "," + this.avgY + "," + this.avgZ + "," + this.maxX + "," + this.maxY + "," + this.maxZ + "," + this.minX + "," + this.minY + "," + this.minZ + "," + this.devX + "," + this.devY + "," + this.devZ + "," + this.crossX + "," + this.crossY + "," + this.crossZ + "," + this.squareEnX + "," + this.squareEnY + "," + this.squareEnZ;
    }

}
