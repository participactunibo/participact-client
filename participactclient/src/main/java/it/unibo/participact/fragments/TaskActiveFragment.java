/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.fima.cardsui.views.CardUI;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import it.unibo.participact.ParticipActService;
import it.unibo.participact.R;
import it.unibo.participact.activities.CreateTaskActivity;
import it.unibo.participact.domain.enums.TaskState;
import it.unibo.participact.domain.persistence.StateUtility;
import it.unibo.participact.domain.persistence.TaskStatus;
import it.unibo.participact.domain.persistence.support.State;
import it.unibo.participact.support.preferences.ShowTipsPreferences;
import it.unibo.participact.views.cards.TaskActiveCard;
import it.unibo.participact.views.cards.TaskActiveFirstCard;
import it.unibo.participact.views.cards.TaskActiveRunningNotExecCard;

public class TaskActiveFragment extends Fragment {

    private CardUI _cardUI;
    private FloatingActionButton fab;
    private GeoBroadcastReceiver receiver;
    private IntentFilter filter;
    private static FragmentActivity myContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_taskactive_pager, container, false);

        _cardUI = (CardUI) root.findViewById(R.id.cardUIView);

        //add head card
        _cardUI.addCard(new TaskActiveFirstCard(getActivity()));

        //add one card for each running task
        State state = StateUtility.loadState(this.getActivity());
        if (state != null) {
            for (TaskStatus task : state.getTaskStatusByState(TaskState.RUNNING)) {
                TaskActiveCard taskCard = new TaskActiveCard(getActivity(), task);
                _cardUI.addCard(taskCard);
            }

            for (TaskStatus task : state.getTaskStatusByState(TaskState.RUNNING_BUT_NOT_EXEC)) {
                TaskActiveRunningNotExecCard taskCard = new TaskActiveRunningNotExecCard(getActivity(), task);
                _cardUI.addCard(taskCard);
            }

        }
        _cardUI.refresh();

        fab = (FloatingActionButton) root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getActivity(), CreateTaskActivity.class);
                getActivity().startActivity(myIntent);
            }
        });

        if (ShowTipsPreferences.getInstance(myContext).shouldShowTips(myContext.getString(R.string.add_task_btn_id))) {

            RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            int margin = ((Number) (getResources().getDisplayMetrics().density * 50)).intValue();
            lps.setMargins(margin, margin, margin, margin);

            ShowcaseView sv = new ShowcaseView.Builder(myContext, true).setTarget(new ViewTarget(fab)).setContentTitle(myContext.getString(R.string.add_task_btn_desc)).setStyle(R.style.CustomShowcaseTheme).hideOnTouchOutside().build();
            sv.setButtonPosition(lps);
            ShowTipsPreferences.getInstance(myContext).setShouldShowTips(myContext.getString(R.string.add_task_btn_id), false);


        }

        //register broadcast to receive geolocation task update
        receiver = new GeoBroadcastReceiver();
        filter = new IntentFilter(ParticipActService.GEO_TASK_UPDATE_INTENT);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getApplicationContext().registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        getActivity().getApplicationContext().unregisterReceiver(receiver);
        super.onPause();
    }


    public void refresh() {
        _cardUI.clearCards();
        //add head card
        _cardUI.addCard(new TaskActiveFirstCard(getActivity()));

        //add one card for each running task
        State state = StateUtility.loadState(this.getActivity());
        if (state != null) {
            for (TaskStatus task : state.getTaskStatusByState(TaskState.RUNNING)) {
                TaskActiveCard taskCard = new TaskActiveCard(getActivity(), task);
                _cardUI.addCard(taskCard);
            }

            for (TaskStatus task : state.getTaskStatusByState(TaskState.RUNNING_BUT_NOT_EXEC)) {
                TaskActiveRunningNotExecCard taskCard = new TaskActiveRunningNotExecCard(getActivity(), task);
                _cardUI.addCard(taskCard);
            }
        }
        _cardUI.refresh();
    }


    private class GeoBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }

    }

}
