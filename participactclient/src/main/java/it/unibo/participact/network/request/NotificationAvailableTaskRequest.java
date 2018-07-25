/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.network.request;

import android.content.Context;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import it.unibo.participact.domain.enums.TaskState;
import it.unibo.participact.domain.rest.TaskFlat;
import it.unibo.participact.domain.rest.TaskFlatList;
import it.unibo.participact.support.BasicAuthenticationUtility;
import it.unibo.participact.support.Configuration;

public class NotificationAvailableTaskRequest extends SpringAndroidSpiceRequest<Integer> {

    public final static int NO_TASK = 0;
    public final static int OPT_TASK_ONLY = 1;
    public final static int MANDATORY_TASK_ONLY = 2;
    public final static int OPT_AND_MANDATORY = 3;

    private SpiceManager contentManager = new SpiceManager(ParticipactSpringAndroidService.class);

    private TaskState state;
    private Context context;


    public NotificationAvailableTaskRequest(Context context) {
        super(Integer.class);
        this.context = context;
        this.state = TaskState.AVAILABLE;
    }

    @Override
    public Integer loadDataFromNetwork() throws Exception {
        boolean mandatory = false;
        boolean opt = false;
        ResponseEntity<TaskFlatList> taskListEntity = getRestTemplate().exchange(Configuration.TASK_URL, HttpMethod.GET, BasicAuthenticationUtility.getHttpEntityForAuthentication(context), TaskFlatList.class, state.toString());
        TaskFlatList taskList = taskListEntity.getBody();

        if (taskList.getList().size() == 0) {
            return NO_TASK;
        }

        for (TaskFlat task : taskList.getList()) {
            if (!task.getCanBeRefused()) {
                mandatory = true;
                AcceptTaskRequest request = new AcceptTaskRequest(context, task.getId());
                if (!contentManager.isStarted()) {
                    contentManager.start(context);
                }
                contentManager.execute(request, new NotificationAcceptMandatoryTaskListener(context, task));
            } else {
                opt = true;
            }
        }

        if (mandatory && opt) {
            return OPT_AND_MANDATORY;
        }

        if (mandatory) {
            return MANDATORY_TASK_ONLY;
        } else {
            return OPT_TASK_ONLY;
        }

    }

    public String createCacheKey() {
        return String.format("getTaskFromNotification%s", state.toString());
    }
}

