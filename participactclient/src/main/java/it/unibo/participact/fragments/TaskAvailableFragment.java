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
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.astuetz.PagerSlidingTabStrip;
import com.fima.cardsui.views.CardUI;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.springframework.http.converter.HttpMessageNotReadableException;

import it.unibo.participact.ParticipActService;
import it.unibo.participact.R;
import it.unibo.participact.activities.CreateTaskActivity;
import it.unibo.participact.activities.interfaces.ProgressManager;
import it.unibo.participact.domain.enums.TaskState;
import it.unibo.participact.domain.persistence.StateUtility;
import it.unibo.participact.domain.rest.TaskFlat;
import it.unibo.participact.domain.rest.TaskFlatList;
import it.unibo.participact.network.request.AcceptTaskRequest;
import it.unibo.participact.network.request.AvailableTaskRequest;
import it.unibo.participact.network.request.NotificationAcceptMandatoryTaskListener;
import it.unibo.participact.network.request.ParticipactSpringAndroidService;
import it.unibo.participact.support.DialogFactory;
import it.unibo.participact.support.GeolocalizationTaskUtils;
import it.unibo.participact.support.LoginUtility;
import it.unibo.participact.support.preferences.ChangeTimePreferences;
import it.unibo.participact.support.preferences.ShowTipsPreferences;
import it.unibo.participact.views.cards.NoTaskAvailableCard;
import it.unibo.participact.views.cards.TaskAdminAvailableFirstCard;
import it.unibo.participact.views.cards.TaskAvailableCard;
import it.unibo.participact.views.cards.TaskHiddenAvailableCard;
import it.unibo.participact.views.cards.TaskNotSupportedCard;
import it.unibo.participact.views.cards.TaskUserFirstAvailableCard;

public class TaskAvailableFragment extends Fragment {

    private static FragmentActivity myContext;
    private FloatingActionButton fab;
    static final int NUM_ITEMS = 2;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_taskavailable_pager, container, false);
        TaskAvailableAdapter adapter = new TaskAvailableAdapter(getChildFragmentManager());

        ViewPager pager = (ViewPager) root.findViewById(R.id.taskavailable_pager);
        pager.setAdapter(adapter);
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) root.findViewById(R.id.taskavailable_tabs);
        tabs.setAllCaps(true);
        tabs.setShouldExpand(true);
        tabs.setIndicatorColorResource(R.color.icons);
        tabs.setViewPager(pager);
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
        return root;
    }


    public static class TaskAvailableAdapter extends FragmentPagerAdapter {

        public TaskAvailableAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int position) {
            return InnerTaskAvailableFragment.newInstance(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE; // hack to recreate fragments
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return myContext.getResources().getString(R.string.page_admin_title);
            else if (position == 1)
                return myContext.getResources().getString(R.string.page_user_title);
            else
                return "";
        }
    }

    public static class InnerTaskAvailableFragment extends Fragment {

        private static final String KEY_LAST_TASK_ADMIN_REQUEST_CACHE_KEY = "taskadminlastRequestCacheKey";
        private static final String KEY_LAST_TASK_USER_REQUEST_CACHE_KEY = "taskuserlastRequestCacheKey";
        private SpiceManager _contentManager = new SpiceManager(ParticipactSpringAndroidService.class);
        private String _lastRequestCacheKey;
        private static final TaskState TASK_STATE = TaskState.AVAILABLE;

        private int mNum;
        private String type;


        private CardUI _cardUI;


        static InnerTaskAvailableFragment newInstance(int type) {
            InnerTaskAvailableFragment innerTaskAvailableFragment = new InnerTaskAvailableFragment();

            Bundle args = new Bundle();
            args.putInt("num", type);
            innerTaskAvailableFragment.setArguments(args);
            return innerTaskAvailableFragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments() != null ? getArguments().getInt("num") : 0;
            if (mNum == 0)
                type = AvailableTaskRequest.ADMIN;
            else if (mNum == 1)
                type = AvailableTaskRequest.USER;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            if (!_contentManager.isStarted()) {
                _contentManager.start(getActivity());
            }

            View root = inflater.inflate(R.layout.inner_fragment_task_available, container, false);
            _cardUI = (CardUI) root.findViewById(R.id.cardUIView);
            if (mNum == 0)
                _cardUI.addCard(new TaskAdminAvailableFirstCard(getActivity()));
            else
                _cardUI.addCard(new TaskUserFirstAvailableCard(getActivity()));
            _cardUI.refresh();


            return root;
        }

        @Override
        public void onResume() {
            super.onResume();
            performRequest();
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            if (!TextUtils.isEmpty(_lastRequestCacheKey)) {
                if (mNum == 0) {
                    outState.putString(KEY_LAST_TASK_ADMIN_REQUEST_CACHE_KEY + TASK_STATE.toString(),
                            _lastRequestCacheKey);
                } else
                    outState.putString(KEY_LAST_TASK_USER_REQUEST_CACHE_KEY + TASK_STATE.toString(), _lastRequestCacheKey);
            }
            super.onSaveInstanceState(outState);
        }

        @Override
        public void onStop() {
            if (_contentManager.isStarted()) {
                _contentManager.shouldStop();
            }
            super.onStop();
        }

        private void performRequest() {
            if (!ChangeTimePreferences.getInstance(getActivity()).getChangeTimeRequest()) {
                AvailableTaskRequest request = new AvailableTaskRequest(getActivity(), TASK_STATE, type);
                _lastRequestCacheKey = request.createCacheKey();
                _contentManager.execute(request, _lastRequestCacheKey, DurationInMillis.ALWAYS_EXPIRED,
                        new AvailableTaskRequestListener(getActivity().getApplicationContext()));
                _cardUI.clearCards();
                if (mNum == 0)
                    _cardUI.addCard(new TaskAdminAvailableFirstCard(getActivity()));
                else
                    _cardUI.addCard(new TaskUserFirstAvailableCard(getActivity()));
                showLoading(true);
            } else {
                DialogFactory.showTimeError(getActivity());
            }
        }

        private class AvailableTaskRequestListener implements RequestListener<TaskFlatList> {

            private SpiceManager _contentManager = new SpiceManager(
                    ParticipactSpringAndroidService.class);
            Context context;

            public AvailableTaskRequestListener(Context context) {
                this.context = context;
            }

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                if (getActivity() != null) {
                    showLoading(false);
                    if (spiceException.getCause() instanceof HttpMessageNotReadableException) {
                        _cardUI.addCard(new TaskNotSupportedCard());
                        _cardUI.refresh();
                    } else if (LoginUtility.checkIfLoginException(context, spiceException)) {

                    } else {
                        DialogFactory.showCommunicationErrorWithServer(getActivity());
                    }
                }
            }

            @Override
            public void onRequestSuccess(TaskFlatList result) {
                if (getActivity() != null && result != null) {
                    showLoading(false);

                    boolean added = false;

                    if (result.getList().size() > 0) {
                        for (TaskFlat task : result.getList()) {
                            if (!task.getCanBeRefused() && !GeolocalizationTaskUtils.isNotifiedByArea(task)) {
                                AcceptTaskRequest request = new AcceptTaskRequest(context, task.getId());
                                if (!_contentManager.isStarted()) {
                                    _contentManager.start(context);
                                }
                                _contentManager.execute(request,
                                        new NotificationAcceptMandatoryTaskListener(context, task));
                            } else if (GeolocalizationTaskUtils.isNotifiedByArea(task)) {

                                it.unibo.participact.domain.persistence.TaskFlat taskDB = StateUtility.getTaskById(context, task.getId());
                                if (taskDB == null) {
                                    taskDB = StateUtility.addTask(context, task);
                                    if (taskDB != null) {
                                        //state in hidden
                                        StateUtility.changeTaskState(context, taskDB, TaskState.HIDDEN);
                                    }
                                }
                            } else {
                                TaskAvailableCard taskCard = new TaskAvailableCard(task);
                                _cardUI.addCard(taskCard);
                                added = true;
                            }
                        }
                    }

                    if (StateUtility.getTaskByState(context, TaskState.HIDDEN).size() > 0) {
                        Location last = ParticipActService.getLastLocation();
                        if (last != null) {
                            for (it.unibo.participact.domain.persistence.TaskFlat task : StateUtility.getTaskByState(context, TaskState.HIDDEN)) {
                                if (GeolocalizationTaskUtils.isInside(context, last.getLongitude(), last.getLatitude(), task.getNotificationArea())) {

                                    if (task.getCanBeRefused()) {
                                        StateUtility.changeTaskState(context, task, TaskState.GEO_NOTIFIED_AVAILABLE);
                                    } else {
                                        AcceptTaskRequest request = new AcceptTaskRequest(context, task.getId());

                                        if (!_contentManager.isStarted()) {
                                            _contentManager.start(context);
                                        }
                                        _contentManager.execute(request, new it.unibo.participact.network.NotificationAcceptMandatoryTaskListener(context, task));
                                    }
                                }
                            }
                        }
                    }

                    if (StateUtility.getTaskByState(context, TaskState.GEO_NOTIFIED_AVAILABLE).size() > 0) {
                        for (it.unibo.participact.domain.persistence.TaskFlat task : StateUtility.getTaskByState(context, TaskState.GEO_NOTIFIED_AVAILABLE)) {
                            TaskHiddenAvailableCard taskCard = new TaskHiddenAvailableCard(task);
                            _cardUI.addCard(taskCard);
                            added = true;
                        }
                    }

                    if (!added) {

                        NoTaskAvailableCard card = new NoTaskAvailableCard();
                        _cardUI.addCard(card);
                    }
                    _cardUI.refresh();
                }
            }

        }

        private void showLoading(boolean value) {
            Activity activity = getActivity();
            if (activity != null && activity instanceof ProgressManager)
                ((ProgressManager) activity).showLoading(value);

        }
    }
}
