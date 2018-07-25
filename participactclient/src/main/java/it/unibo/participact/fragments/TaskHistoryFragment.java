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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.fima.cardsui.objects.CardStack;
import com.fima.cardsui.views.CardUI;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.List;

import it.unibo.participact.R;
import it.unibo.participact.activities.CreateTaskActivity;
import it.unibo.participact.activities.interfaces.ProgressManager;
import it.unibo.participact.domain.enums.TaskState;
import it.unibo.participact.domain.rest.TaskFlat;
import it.unibo.participact.domain.rest.TaskFlatList;
import it.unibo.participact.domain.rest.TaskFlatMap;
import it.unibo.participact.network.request.AvailableTaskRequest;
import it.unibo.participact.network.request.CreatedTaskByStateRequest;
import it.unibo.participact.network.request.ParticipactSpringAndroidService;
import it.unibo.participact.support.DialogFactory;
import it.unibo.participact.support.LoginUtility;
import it.unibo.participact.support.preferences.ShowTipsPreferences;
import it.unibo.participact.views.cards.NoTaskAvailableCard;
import it.unibo.participact.views.cards.TaskCompletedCard;
import it.unibo.participact.views.cards.TaskCompletedFirstCard;
import it.unibo.participact.views.cards.TaskCreatedCompletedCard;
import it.unibo.participact.views.cards.TaskCreatedCompletedFirstCard;

public class TaskHistoryFragment extends Fragment {


    private static final int NUM_ITEMS = 2;
    private static FragmentActivity myContext;
    private FloatingActionButton fab;

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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_taskhistory_pager, container, false);
        TaskHistoryPagerAdapter taskHistoryPagerAdapter = new TaskHistoryPagerAdapter(getChildFragmentManager());
        ViewPager pager = (ViewPager) root.findViewById(R.id.taskhistory_pager);
        pager.setAdapter(taskHistoryPagerAdapter);
        PagerSlidingTabStrip pagerSlidingTabStrip = (PagerSlidingTabStrip) root.findViewById(R.id.taskhistory_tabs);
        pagerSlidingTabStrip.setAllCaps(true);
        pagerSlidingTabStrip.setShouldExpand(true);
        pagerSlidingTabStrip.setIndicatorColorResource(R.color.icons);
        pagerSlidingTabStrip.setViewPager(pager);
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

    public static class TaskHistoryPagerAdapter extends FragmentPagerAdapter {


        public TaskHistoryPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return myContext.getResources().getString(R.string.page_task_history_completed);
            else if (position == 1)
                return myContext.getResources().getString(R.string.page_task_history_created_completed);
            else
                return "";
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new InnerTaskCompletedFragment();
            } else if (position == 1) {
                return new InnerTaskCreatedFragment();
            }
            return new InnerTaskCompletedFragment();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE; // hack to recreate fragments
        }
    }


    public static class InnerTaskCreatedFragment extends Fragment {

        private static final String KEY_LAST_REQUEST_CACHE_KEY = "taskcreatedlastRequestCacheKey";
        private SpiceManager _contentManager = new SpiceManager(ParticipactSpringAndroidService.class);
        private String _lastRequestCacheKey;
        private static final TaskState TASK_STATE = TaskState.COMPLETED_WITH_SUCCESS;

        private CardUI _cardUI;

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            if (!_contentManager.isStarted()) {
                _contentManager.start(getActivity());


            }

            View root = inflater.inflate(R.layout.inner_fragment_task_history, container, false);
            _cardUI = (CardUI) root.findViewById(R.id.cardUIView);
            _cardUI.addCard(new TaskCreatedCompletedFirstCard(getActivity()));
            _cardUI.refresh();


            return root;


        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            if (!TextUtils.isEmpty(_lastRequestCacheKey)) {

                outState.putString(KEY_LAST_REQUEST_CACHE_KEY + TASK_STATE.toString(),
                        _lastRequestCacheKey);
            }
            super.onSaveInstanceState(outState);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            if (savedInstanceState != null) {
                if (savedInstanceState.containsKey(KEY_LAST_REQUEST_CACHE_KEY + TASK_STATE.toString())) {
                    _lastRequestCacheKey = savedInstanceState.getString(KEY_LAST_REQUEST_CACHE_KEY + TASK_STATE.toString());
                    _contentManager.addListenerIfPending(TaskFlatMap.class, _lastRequestCacheKey, new CreatedTaskRequestListener());
                    _contentManager.getFromCache(TaskFlatMap.class, _lastRequestCacheKey, DurationInMillis.ONE_MINUTE, new CreatedTaskRequestListener());
                }
            }
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public void onStart() {
            super.onStart();
            performRequest();
        }

        @Override
        public void onStop() {
            if (_contentManager.isStarted()) {
                _contentManager.shouldStop();
            }
            super.onStop();
        }

        private void performRequest() {
            CreatedTaskByStateRequest request = new CreatedTaskByStateRequest(getActivity(), TASK_STATE);
            _lastRequestCacheKey = request.createCacheKey();
            _contentManager.execute(request, _lastRequestCacheKey, DurationInMillis.ALWAYS_EXPIRED, new CreatedTaskRequestListener());
            showLoading(true);

        }

        private void showLoading(boolean value) {
            Activity activity = getActivity();
            if (activity != null && activity instanceof ProgressManager)
                ((ProgressManager) activity).showLoading(value);

        }


        private class CreatedTaskRequestListener implements PendingRequestListener<TaskFlatMap> {

            @Override
            public void onRequestNotFound() {
                showLoading(false);
            }

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                showLoading(false);


            }

            @Override
            public void onRequestSuccess(TaskFlatMap taskFlatMap) {
                showLoading(false);

                boolean added = false;
                _cardUI.clearCards();
                _cardUI.addCard(new TaskCreatedCompletedFirstCard(getActivity()));
                CardStack stack = new CardStack();

                if (taskFlatMap != null) {

                    for (String size : taskFlatMap.keySet()) {
                        List<TaskFlat> tasksFlat = taskFlatMap.get(size);
                        for (TaskFlat t : tasksFlat) {
                            TaskCreatedCompletedCard taskCard = new TaskCreatedCompletedCard(t, size);
                            stack.add(taskCard);
                            added = true;
                        }
                    }

                    if (!added) {
                        NoTaskAvailableCard card = new NoTaskAvailableCard();
                        _cardUI.addCard(card);
                    } else
                        _cardUI.addStack(stack);
                    _cardUI.refresh();
                    //	    	Toast.makeText(getActivity(), "new result", Toast.LENGTH_LONG).show();
                }


            }
        }

    }

    public static class InnerTaskCompletedFragment extends Fragment {

        private static final String KEY_LAST_REQUEST_CACHE_KEY = "taskcompletedlastRequestCacheKey";
        private SpiceManager _contentManager = new SpiceManager(ParticipactSpringAndroidService.class);
        private String _lastRequestCacheKey;
        private static final TaskState TASK_STATE = TaskState.COMPLETED_WITH_SUCCESS;

        private CardUI _cardUI;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            if (!_contentManager.isStarted()) {
                _contentManager.start(getActivity());
            }

            View root = inflater.inflate(R.layout.inner_fragment_task_history, container, false);
            _cardUI = (CardUI) root.findViewById(R.id.cardUIView);
            _cardUI.addCard(new TaskCompletedFirstCard(getActivity()));
            _cardUI.refresh();

            return root;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            //restore state
            if (savedInstanceState != null) {
                if (savedInstanceState.containsKey(KEY_LAST_REQUEST_CACHE_KEY + TASK_STATE.toString())) {
                    _lastRequestCacheKey = savedInstanceState.getString(KEY_LAST_REQUEST_CACHE_KEY + TASK_STATE.toString());
                    _contentManager.addListenerIfPending(TaskFlatList.class, _lastRequestCacheKey, new CompletedTaskRequestListener());
                    _contentManager.getFromCache(TaskFlatList.class, _lastRequestCacheKey, DurationInMillis.ONE_MINUTE, new CompletedTaskRequestListener());
                }
            }
            super.onActivityCreated(savedInstanceState);

        }

        @Override
        public void onStart() {
            super.onStart();
            performRequest();
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            if (!TextUtils.isEmpty(_lastRequestCacheKey)) {
                outState.putString(KEY_LAST_REQUEST_CACHE_KEY + TASK_STATE.toString(), _lastRequestCacheKey);
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
            AvailableTaskRequest request = new AvailableTaskRequest(getActivity(), TASK_STATE, AvailableTaskRequest.ALL);
            _lastRequestCacheKey = request.createCacheKey();
            _contentManager.execute(request, _lastRequestCacheKey, DurationInMillis.ALWAYS_EXPIRED, new CompletedTaskRequestListener());
            showLoading(true);
        }


        private class CompletedTaskRequestListener implements RequestListener<TaskFlatList> {

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                showLoading(false);

                if (LoginUtility.checkIfLoginException(getActivity().getApplicationContext(), spiceException)) {

                } else {
                    DialogFactory.showCommunicationErrorWithServer(getActivity());
                }
            }

            @Override
            public void onRequestSuccess(TaskFlatList result) {
                showLoading(false);

                _cardUI.clearCards();
                _cardUI.addCard(new TaskCompletedFirstCard(getActivity()));
                CardStack stack = new CardStack();
                boolean added = false;


                if (result != null) {
                    for (TaskFlat task : result.getList()) {
                        TaskCompletedCard taskCard = new TaskCompletedCard(task);
                        stack.add(taskCard);
                        added = true;
                    }
                    if (!added) {
                        NoTaskAvailableCard card = new NoTaskAvailableCard();
                        _cardUI.addCard(card);
                    } else
                        _cardUI.addStack(stack);
                    _cardUI.refresh();
                    //	    	Toast.makeText(getActivity(), "new result", Toast.LENGTH_LONG).show();
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
