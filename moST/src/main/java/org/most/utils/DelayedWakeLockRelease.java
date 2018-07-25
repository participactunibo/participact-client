/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */
package org.most.utils;

import org.most.MoSTApplication;

public class DelayedWakeLockRelease extends Thread {

    private long _delay;
    private MoSTApplication _context;

    public DelayedWakeLockRelease(MoSTApplication context, long delay) {
        _context = context;
        _delay = delay;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(_delay);
            _context.getWakeLockHolder().releaseWL();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
