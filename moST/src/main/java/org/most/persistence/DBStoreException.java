/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */
package org.most.persistence;


public class DBStoreException extends Exception {

    private static final long serialVersionUID = 8783171034535335408L;

    public DBStoreException() {
        super();
    }

    public DBStoreException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DBStoreException(String detailMessage) {
        super(detailMessage);
    }

    public DBStoreException(Throwable throwable) {
        super(throwable);
    }

}
