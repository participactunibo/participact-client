/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.domain.persistence.support;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import it.unibo.participact.R;
import it.unibo.participact.domain.persistence.ActionFlat;
import it.unibo.participact.domain.persistence.ClosedAnswer;
import it.unibo.participact.domain.persistence.DataQuestionnaireFlat;
import it.unibo.participact.domain.persistence.GeoBadgeCollected;
import it.unibo.participact.domain.persistence.InterestPoint;
import it.unibo.participact.domain.persistence.Question;
import it.unibo.participact.domain.persistence.QuestionnaireProgressPerAction;
import it.unibo.participact.domain.persistence.RemainingPhotoPerAction;
import it.unibo.participact.domain.persistence.TaskFlat;
import it.unibo.participact.domain.persistence.TaskStatus;

public class DomainDBHelper extends OrmLiteSqliteOpenHelper {

    private static final String DB_NAME = "domain.db";
    private static final int DB_VERSION = 2;


    public DomainDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION, R.raw.ormlite_domain_config);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {


            TableUtils.createTableIfNotExists(connectionSource, InterestPoint.class);
            TableUtils.createTableIfNotExists(connectionSource, GeoBadgeCollected.class);
            TableUtils.createTableIfNotExists(connectionSource, ActionFlat.class);
            TableUtils.createTableIfNotExists(connectionSource, TaskFlat.class);
            TableUtils.createTableIfNotExists(connectionSource, TaskStatus.class);
            TableUtils.createTableIfNotExists(connectionSource, QuestionnaireProgressPerAction.class);
            TableUtils.createTableIfNotExists(connectionSource, RemainingPhotoPerAction.class);
            TableUtils.createTableIfNotExists(connectionSource, ClosedAnswer.class);
            TableUtils.createTableIfNotExists(connectionSource, Question.class);
            TableUtils.createTableIfNotExists(connectionSource, DataQuestionnaireFlat.class);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i2) {
//        onCreate(sqLiteDatabase, connectionSource);
        try {
            TableUtils.createTableIfNotExists(connectionSource, InterestPoint.class);
            TableUtils.createTableIfNotExists(connectionSource, GeoBadgeCollected.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
