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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unibo.participact.R;
import it.unibo.participact.activities.interfaces.FragmentSwitcher;
import it.unibo.participact.activities.interfaces.ProgressManager;
import it.unibo.participact.domain.rest.ScoreRestResult;
import it.unibo.participact.domain.rest.ScoreRestResultList;
import it.unibo.participact.domain.rest.UserRestResult;
import it.unibo.participact.network.request.LeaderboardRequest;
import it.unibo.participact.network.request.ParticipactSpringAndroidService;
import it.unibo.participact.network.request.UserRequest;
import it.unibo.participact.support.ViewUtils;
import it.unibo.participact.support.preferences.ShowTipsPreferences;
import it.unibo.participact.views.adapters.ScoreAdapter;

public class LeaderBoardFragment extends Fragment {

    static final int NUM_ITEMS = 2;

    private static Context mContext;
    private static ActionBarActivity mActivity;
    private static FragmentSwitcher mFragmentSwitcher;
    private static ProgressManager mProgressManager;

    @Override
    public void onAttach(Activity activity) {
        mContext = activity;
        if (activity instanceof ActionBarActivity)
            mActivity = (ActionBarActivity) activity;
        else
            throw new RuntimeException("Parent activity of LeaderBoardFragment must extend ActionBarActivity");
        if (activity instanceof FragmentSwitcher)
            mFragmentSwitcher = (FragmentSwitcher) activity;
        else
            throw new RuntimeException("Parent activity of LeaderBoardFragment must implement FragmentSwitcher");
        if (activity instanceof ProgressManager)
            mProgressManager = ((ProgressManager) activity);
        else
            throw new RuntimeException("Parent activity of LeaderBoardFragment must implement ProgressManager");
        super.onAttach(activity);
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_leaderboard_pager, container, false);
        MyAdapter mAdapter = new MyAdapter(getChildFragmentManager());
        ViewPager mPager = (ViewPager) root.findViewById(R.id.leaderboard_pager);
        mPager.setAdapter(mAdapter);
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) root.findViewById(R.id.leaderboard_tabs);
        tabs.setAllCaps(true);
        tabs.setShouldExpand(true);
        tabs.setIndicatorColorResource(R.color.icons);
        tabs.setViewPager(mPager);

        if (ShowTipsPreferences.getInstance(mContext).shouldShowTips(LeaderBoardFragment.class.getSimpleName())) {
            View button = ((ViewGroup) tabs.getChildAt(0)).getChildAt(1);
            new ShowcaseView.Builder(mActivity, true).setTarget(new ViewTarget(button)).setContentTitle(mContext.getString(R.string.tip_leaderboard_friends)).setStyle(R.style.CustomShowcaseTheme).hideOnTouchOutside().build();
            ShowTipsPreferences.getInstance(mContext).setShouldShowTips(LeaderBoardFragment.class.getSimpleName(), false);
        }

        return root;
    }

    public static class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return mContext.getString(R.string.leaderboard_fragment_global);
            else if (position == 1)
                return mContext.getString(R.string.leaderboard_fragment_social);
            else
                throw new IllegalArgumentException("" + position);
        }

        @Override
        public Fragment getItem(int position) {
            return InnerLeaderboardFragment.newInstance(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE; // hack to recreate fragments
        }

    }

    public static class InnerLeaderboardFragment extends Fragment {

        private static final String KEY_LAST_REQUEST_GLOBAL_CACHE_KEY = "LeaderboardGlobalResquest";
        private static final String KEY_LAST_REQUEST_SOCIAL_CACHE_KEY = "LeaderboardGlobalResquest";
        private int mNum;
        private List<ScoreRestResult> scores;
        private String type;
        private ObservableListView listView;
        private AnimationAdapter animationAdapter;
        private ScoreAdapter adapter;
        private SpiceManager _contentManager = new SpiceManager(
                ParticipactSpringAndroidService.class);
        private String _lastRequestCacheKey;
        private TextView textViewError;
        private SwipeRefreshLayout swipeRefreshLayout;
        private String _lastRequestCacheKeyUser;
        private long userId = -1;

        /**
         * Create a new instance of CountingFragment, providing "num" as an
         * argument.
         */
        static InnerLeaderboardFragment newInstance(int num) {
            InnerLeaderboardFragment f = new InnerLeaderboardFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("num", num);
            f.setArguments(args);
            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments() != null ? getArguments().getInt("num") : 0;
            if (mNum == 0)
                type = LeaderboardRequest.GLOBAL;
            else if (mNum == 1)
                type = LeaderboardRequest.SOCIAL;

            scores = new ArrayList<ScoreRestResult>();

        }

        /**
         * The Fragment's UI is just a simple text view showing its instance
         * number.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View v = inflater.inflate(R.layout.fragment_leaderboard_list,
                    container, false);
            listView = (ObservableListView) v.findViewById(R.id.list_leaderboard);
            textViewError = (TextView) v.findViewById(R.id.textViewError);
            swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
            swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.primary_dark);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    performRequest();
                }
            });

            listView.setScrollViewCallbacks(new ObservableScrollViewCallbacks() {
                @Override
                public void onScrollChanged(int i, boolean b, boolean b2) {

                }

                @Override
                public void onDownMotionEvent() {

                }

                @Override
                public void onUpOrCancelMotionEvent(ScrollState scrollState) {

                    ActionBar ab = mActivity.getSupportActionBar();
                    if (scrollState == ScrollState.UP) {
                        if (ab.isShowing()) {
                            ab.hide();
                        }
                    } else if (scrollState == ScrollState.DOWN) {
                        if (!ab.isShowing()) {
                            ab.show();
                        }
                    }
                }
            });

            adapter = new ScoreAdapter(mContext,
                    R.layout.leaderboard_item_layout, scores);
            animationAdapter = new SwingBottomInAnimationAdapter(adapter);
            animationAdapter.setAbsListView(listView);
            listView.setAdapter(animationAdapter);

            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    if (mFragmentSwitcher != null) {
                        Fragment userFragment = UserFragment.newInstance(scores
                                .get(position).getUserId());
                        mFragmentSwitcher.switchContent(userFragment, true);
                    }

                }
            });

            return v;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            if (!TextUtils.isEmpty(_lastRequestCacheKey)) {

                if (mNum == 0)
                    outState.putString(KEY_LAST_REQUEST_GLOBAL_CACHE_KEY,
                            _lastRequestCacheKey);
                else if (mNum == 1)
                    outState.putString(KEY_LAST_REQUEST_SOCIAL_CACHE_KEY,
                            _lastRequestCacheKey);
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

        @Override
        public void onResume() {
            super.onResume();
            performRequest();
        }

        private void performRequest() {
            if (!_contentManager.isStarted()) {
                _contentManager.start(mContext);
            }
            LeaderboardRequest request = new LeaderboardRequest(type, mContext);
            _lastRequestCacheKey = request.createCacheKey();
            _contentManager.execute(request, _lastRequestCacheKey,
                    DurationInMillis.ONE_MINUTE * 5, new ScoreListener());
            if (!swipeRefreshLayout.isRefreshing())
                mProgressManager.showLoading(true);

            UserRequest userRequest = new UserRequest(-1, mContext);
            _lastRequestCacheKeyUser = userRequest.createCacheKey();
            _contentManager.execute(userRequest, _lastRequestCacheKeyUser,
                    DurationInMillis.ONE_MINUTE * 20, new RequestListener<UserRestResult>() {
                        @Override
                        public void onRequestFailure(SpiceException spiceException) {
                            userId = -1;
                        }

                        @Override
                        public void onRequestSuccess(UserRestResult userRestResult) {
                            userId = userRestResult.getId();
                            if (adapter != null)
                                adapter.setHighlightId(userId);
                        }
                    });
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            // restore state
            if (savedInstanceState != null) {

                if (mNum == 0) {// global
                    if (savedInstanceState
                            .containsKey(KEY_LAST_REQUEST_GLOBAL_CACHE_KEY)) {
                        _lastRequestCacheKey = savedInstanceState
                                .getString(KEY_LAST_REQUEST_GLOBAL_CACHE_KEY);
                        _contentManager.getFromCache(ScoreRestResultList.class,
                                _lastRequestCacheKey,
                                DurationInMillis.ONE_MINUTE * 5,
                                new ScoreListener());
                    }

                } else if (mNum == 1) {// social

                    if (savedInstanceState
                            .containsKey(KEY_LAST_REQUEST_SOCIAL_CACHE_KEY)) {
                        _lastRequestCacheKey = savedInstanceState
                                .getString(KEY_LAST_REQUEST_SOCIAL_CACHE_KEY);
                        _contentManager.getFromCache(ScoreRestResultList.class,
                                _lastRequestCacheKey,
                                DurationInMillis.ONE_MINUTE * 5,
                                new ScoreListener());
                    }

                }

            }
            super.onActivityCreated(savedInstanceState);
        }

        private class ScoreListener implements
                RequestListener<ScoreRestResultList> {

            @Override
            public void onRequestFailure(SpiceException error) {
                if (!swipeRefreshLayout.isRefreshing())
                    mProgressManager.showLoading(false);
                else swipeRefreshLayout.setRefreshing(false);
                ViewUtils.toggleAlpha(listView, false);
                ViewUtils.toggleError(textViewError, getString(R.string.network_error), true);
            }

            @Override
            public void onRequestSuccess(final ScoreRestResultList scoresResult) {
                if (!swipeRefreshLayout.isRefreshing())
                    mProgressManager.showLoading(false);
                else swipeRefreshLayout.setRefreshing(false);

                ViewUtils.toggleError(textViewError, null, false);

                if (userId > 0)
                    adapter.setHighlightId(userId);

                scores.clear();
                Collections.sort(scoresResult);
                scores.addAll(scoresResult);
                Collections.reverse(scores);
                adapter.notifyDataSetChanged();


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        ViewUtils.toggleAlpha(listView, true);

                        if (userId > 0) {

                            for (ScoreRestResult current : scores) {
                                if (current.getUserId() == userId) {
                                    int index = scores.indexOf(current);
                                    if (index > 30) {
                                        listView.setSelection(index - 10);
                                    }
                                    listView.smoothScrollToPosition(scores.indexOf(current));
                                    break;
                                }
                            }

                        }

                        if (type == LeaderboardRequest.SOCIAL && scores.size() == 1) {//forever alone
                            Snackbar.with(mContext).type(SnackbarType.MULTI_LINE).text(getString(R.string.no_friends_yet)).actionLabel(getString(R.string.go)).actionListener(new ActionClickListener() {
                                @Override
                                public void onActionClicked() {
                                    mFragmentSwitcher.switchContent(new FriendsFragment(), true);
                                }
                            }).show(mActivity);


                        }

                    }
                }, 500);

            }

        }

    }


}
