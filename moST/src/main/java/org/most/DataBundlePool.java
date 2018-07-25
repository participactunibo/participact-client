/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */
package org.most;

import org.apache.commons.pool.impl.StackObjectPool;

import android.util.Log;

/**
 * This class represents a pool of {@link DataBundle}, that can be reused to
 * minimize the memory and CPU cost of the application.
 * 
 */
public class DataBundlePool {

	/** The Constant DEBUG. */
	private final static boolean DEBUG = false;

	/** The Constant TAG. */
	private final static String TAG = DataBundlePool.class.getCanonicalName();

	/** The _pool. */
	private StackObjectPool<DataBundle> _pool = null;

	/**
	 * Instantiates a new DataBundlePool.
	 */
	public DataBundlePool() {
		_pool = new StackObjectPool<DataBundle>(new DataBundleFactory(this));
		// this.setWhenExhaustedAction(WHEN_EXHAUSTED_GROW);
	}

	/**
	 * Gets a new {@link DataBundle} from the pool.
	 * 
	 * @return a new {@link DataBundle}
	 */
	public DataBundle borrowBundle() {
		if (DEBUG)
			Log.d(TAG, "Borrowed DataBundle, active: "
					+ (_pool.getNumActive() + 1));
		try {
			return _pool.borrowObject();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns a new {@link DataBundle} <em>to</em> to pool.
	 * 
	 * @param b
	 *            The DataBundle to return to the pool.
	 */
	public void returnBundle(DataBundle b) {

		try {
			b.setRefCount(0);
			_pool.returnObject(b);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (DEBUG)
			Log.d(TAG, "Returned DataBundle, active: " + _pool.getNumActive());
	}

}
