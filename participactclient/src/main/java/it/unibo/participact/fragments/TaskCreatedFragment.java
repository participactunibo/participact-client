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

import it.unibo.participact.R;
import it.unibo.participact.activities.CreateTaskActivity;
import it.unibo.participact.activities.interfaces.ProgressManager;
import it.unibo.participact.domain.enums.TaskValutation;
import it.unibo.participact.domain.rest.TaskFlat;
import it.unibo.participact.domain.rest.TaskFlatList;
import it.unibo.participact.network.request.ParticipactSpringAndroidService;
import it.unibo.participact.network.request.TaskCreatedRequest;
import it.unibo.participact.support.preferences.ShowTipsPreferences;
import it.unibo.participact.views.cards.NoTaskAvailableCard;
import it.unibo.participact.views.cards.TaskCreatedApprovedFirstCard;
import it.unibo.participact.views.cards.TaskCreatedCard;
import it.unibo.participact.views.cards.TaskCreatedPendingFirstCard;
import it.unibo.participact.views.cards.TaskCreatedRefusedFirstCard;

/**
 * Created by alessandro on 11/11/14.
 */
public class TaskCreatedFragment extends Fragment {

    static final int NUM_ITEMS = 3;
    private static int tabIndex;

    public static int ACCEPTED_TASK_INDEX = 0;
    public static int PENDING_INDEX = 1;
    public static int REFUSED_TASK_INDEX = 2;


    private static FragmentActivity myContext;
    private FloatingActionButton fab;


    public static TaskCreatedFragment newInstance(int pageIndex) {
        TaskCreatedFragment f = new TaskCreatedFragment();

        // Supply id input as an argument.
        Bundle args = new Bundle();
        args.putInt("tabIndex", pageIndex);
        f.setArguments(args);

        return f;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        tabIndex = getArguments() != null ? getArguments().getInt("tabIndex") : -1;

    }


    @Override
    public void onAttach(Activity activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_taskcreated_pager, container, false);
        TaskCreatedPagerAdapter mAdapter = new TaskCreatedPagerAdapter(getChildFragmentManager());
        ViewPager mPager = (ViewPager) root.findViewById(R.id.taskcreated_pager);
        mPager.setAdapter(mAdapter);
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) root.findViewById(R.id.taskcreated_tabs);
        tabs.setAllCaps(true);
        tabs.setShouldExpand(true);
        tabs.setIndicatorColorResource(R.color.icons);
        tabs.setViewPager(mPager);
        mPager.setCurrentItem(tabIndex);
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


    public static class TaskCreatedPagerAdapter extends FragmentPagerAdapter {


        public TaskCreatedPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return myContext.getResources().getString(R.string.page_task_created_approved);
            else if (position == 1)
                return myContext.getResources().getString(R.string.page_task_created_under_valutation);
            else if (position == 2)
                return myContext.getResources().getString(R.string.page_task_created_refused);
            else
                return "";
        }

        @Override
        public Fragment getItem(int position) {
            return TaskCreatedInnerFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }


        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE; // hack to recreate fragments
        }
    }


    public static class TaskCreatedInnerFragment extends Fragment {
        private static final String KEY_LAST_REQUEST_APPROVED_CACHE_KEY = "TaskCreatedApprovedRequest";
        private static final String KEY_LAST_REQUEST_REFUSED_CACHE_KEY = "TaskCreatedRefusedRequest";
        private static final String KEY_LAST_REQUEST_PENDING_CACHE_KEY = "TaskCreatedPendingRequest";
        int num;
        private TaskValutation type;
        private SpiceManager _contentManager = new SpiceManager(
                ParticipactSpringAndroidService.class);
        private String _lastRequestCacheKey;
        private CardUI _cardUI;


        static TaskCreatedInnerFragment newInstance(int num) {
            TaskCreatedInnerFragment t = new TaskCreatedInnerFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("num", num);
            t.setArguments(args);
            return t;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            num = getArguments() != null ? getArguments().getInt("num") : 0;
            if (num == 0)
                type = TaskValutation.APPROVED;
            else if (num == 1)
                type = TaskValutation.PENDING;
            else
                type = TaskValutation.REFUSED;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.inner_fragment_task_created, container, false);
            _cardUI = (CardUI) root.findViewById(R.id.cardUIView);
            if (num == 0)
                _cardUI.addCard(new TaskCreatedApprovedFirstCard(getActivity()));
            else if (num == 1)
                _cardUI.addCard(new TaskCreatedPendingFirstCard(getActivity()));
            else if (num == 2)
                _cardUI.addCard(new TaskCreatedRefusedFirstCard(getActivity()));


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
                if (num == 0)
                    outState.putString(KEY_LAST_REQUEST_APPROVED_CACHE_KEY, _lastRequestCacheKey);
                else if (num == 1)
                    outState.putString(KEY_LAST_REQUEST_PENDING_CACHE_KEY, _lastRequestCacheKey);
                else if (num == 2)
                    outState.putString(KEY_LAST_REQUEST_REFUSED_CACHE_KEY, _lastRequestCacheKey);
            }
            super.onSaveInstanceState(outState);
        }


        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            //restore state
            if (savedInstanceState != null) {
                if (savedInstanceState.containsKey(KEY_LAST_REQUEST_APPROVED_CACHE_KEY)) {
                    _lastRequestCacheKey = savedInstanceState.getString(KEY_LAST_REQUEST_APPROVED_CACHE_KEY);
                    _contentManager.addListenerIfPending(TaskFlatList.class, _lastRequestCacheKey, new TaskCreatedRequestListener());
                    _contentManager.getFromCache(TaskFlatList.class, _lastRequestCacheKey, DurationInMillis.ONE_MINUTE, new TaskCreatedRequestListener());
                } else if (savedInstanceState.containsKey(KEY_LAST_REQUEST_REFUSED_CACHE_KEY)) {
                    _lastRequestCacheKey = savedInstanceState.getString(KEY_LAST_REQUEST_REFUSED_CACHE_KEY);
                    _contentManager.addListenerIfPending(TaskFlatList.class, _lastRequestCacheKey, new TaskCreatedRequestListener());
                    _contentManager.getFromCache(TaskFlatList.class, _lastRequestCacheKey, DurationInMillis.ONE_MINUTE, new TaskCreatedRequestListener());
                } else if (savedInstanceState.containsKey(KEY_LAST_REQUEST_PENDING_CACHE_KEY)) {
                    _lastRequestCacheKey = savedInstanceState.getString(KEY_LAST_REQUEST_REFUSED_CACHE_KEY);
                    _contentManager.addListenerIfPending(TaskFlatList.class, _lastRequestCacheKey, new TaskCreatedRequestListener());
                    _contentManager.getFromCache(TaskFlatList.class, _lastRequestCacheKey, DurationInMillis.ONE_MINUTE, new TaskCreatedRequestListener());
                }

            }
            super.onActivityCreated(savedInstanceState);

        }


        @Override
        public void onStop() {
            if (_contentManager.isStarted()) {
                _contentManager.shouldStop();
            }
            super.onStop();
        }


        private void performRequest() {
            if (!_contentManager.isStarted()) {
                _contentManager.start(getActivity());
            }
            TaskCreatedRequest request = new TaskCreatedRequest(getActivity(), type);
            _lastRequestCacheKey = request.createCacheKey();
            _contentManager.execute(request, _lastRequestCacheKey, DurationInMillis.ALWAYS_EXPIRED, new TaskCreatedRequestListener());
            showLoading(true);
        }


        private class TaskCreatedRequestListener implements PendingRequestListener<TaskFlatList> {

            @Override
            public void onRequestNotFound() {
                showLoading(false);
            }

            @Override
            public void onRequestFailure(SpiceException spiceException) {

                showLoading(false);


            }

            @Override
            public void onRequestSuccess(TaskFlatList taskFlatList) {

                boolean added = false;
                showLoading(false);

                _cardUI.clearCards();
                if (num == 0)
                    _cardUI.addCard(new TaskCreatedApprovedFirstCard(getActivity()));
                else if (num == 1)
                    _cardUI.addCard(new TaskCreatedPendingFirstCard(getActivity()));
                else if (num == 2)
                    _cardUI.addCard(new TaskCreatedRefusedFirstCard(getActivity()));

                _cardUI.refresh();
                CardStack stack = new CardStack();

                if (taskFlatList != null) {
                    for (TaskFlat task : taskFlatList.getList()) {
                        TaskCreatedCard taskCard = new TaskCreatedCard(task);
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
