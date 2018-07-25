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

import org.apache.commons.pool.BasePoolableObjectFactory;

/**
 * A factory for creating DataBundle objects.
 * 
 */
public class DataBundleFactory extends BasePoolableObjectFactory<DataBundle> {

	/** The _data bundle pool. */
	private DataBundlePool _dataBundlePool = null;

	/**
	 * Instantiates a new data bundle factory.
	 * 
	 * @param dataBundlePool
	 *            the data bundle pool
	 */
	public DataBundleFactory(DataBundlePool dataBundlePool) {
		_dataBundlePool = dataBundlePool;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.pool.BasePoolableObjectFactory#makeObject()
	 */
	@Override
	public DataBundle makeObject() throws Exception {
		return new DataBundle(_dataBundlePool);
	}

}
