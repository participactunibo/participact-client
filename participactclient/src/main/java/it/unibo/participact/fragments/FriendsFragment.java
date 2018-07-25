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
import android.support.annotation.Nullable;
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
import android.widget.Button;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nispok.snackbar.Snackbar;
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
import it.unibo.participact.domain.rest.UserRestResult;
import it.unibo.participact.domain.rest.UserRestResultList;
import it.unibo.participact.network.request.FriendsGetRequest;
import it.unibo.participact.network.request.FriendsPostRequest;
import it.unibo.participact.network.request.ParticipactSpringAndroidService;
import it.unibo.participact.support.ViewUtils;
import it.unibo.participact.views.adapters.FriendsAcceptedAdapter;
import it.unibo.participact.views.adapters.FriendsAcceptedAdapter.FriendRemoveListener;
import it.unibo.participact.views.adapters.FriendsPendingAdapter;
import it.unibo.participact.views.adapters.FriendsPendingAdapter.FriendInteractionListener;

public class FriendsFragment extends Fragment {

    static final int NUM_ITEMS = 3;

    public static int ACCEPTED_INDEX = 0;
    public static int PENDING_INDEX = 1;
    public static int FIND_INDEX = 2;

    private static Context mContext;
    private static ActionBarActivity mActivity;
    private static FragmentSwitcher mFragmentSwitcher;
    private static ProgressManager mProgressManager;
    private static int pageIndex;

    public static FriendsFragment newInstance(int pageIndex) {
        FriendsFragment f = new FriendsFragment();

        // Supply id input as an argument.
        Bundle args = new Bundle();
        args.putInt("pageIndex", pageIndex);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        mContext = activity;
        if (activity instanceof ActionBarActivity)
            mActivity = (ActionBarActivity) activity;
        else
            throw new RuntimeException("Parent activity of FriendsFragment must extend ActionBarActivity");
        if (activity instanceof FragmentSwitcher)
            mFragmentSwitcher = (FragmentSwitcher) activity;
        else
            throw new RuntimeException("Parent activity of FriendsFragment must implement FragmentSwitcher");
        if (activity instanceof ProgressManager)
            mProgressManager = ((ProgressManager) activity);
        else
            throw new RuntimeException("Parent activity of FriendsFragment must implement ProgressManager");
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageIndex = getArguments() != null ? getArguments().getInt("pageIndex") : -1;
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_friends_pager, container, false);
        FriendsPagerAdapter mAdapter = new FriendsPagerAdapter(getChildFragmentManager());
        ViewPager mPager = (ViewPager) root.findViewById(R.id.friends_pager);
        mPager.setAdapter(mAdapter);
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) root.findViewById(R.id.friends_tabs);
        tabs.setAllCaps(true);
        tabs.setShouldExpand(true);
        tabs.setIndicatorColorResource(R.color.icons);
        tabs.setViewPager(mPager);

        if (pageIndex >= 0)
            mPager.setCurrentItem(pageIndex);

        return root;
    }

    public interface ViewPagerRefresher {
        void forceReload();
    }

    public static class FriendsPagerAdapter extends FragmentPagerAdapter
            implements ViewPagerRefresher {

        public FriendsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return mContext.getString(R.string.friends_fragment_friends);
            else if (position == 1)
                return mContext.getString(R.string.friends_fragment_pending);
            else if (position == 2)
                return mContext.getString(R.string.friends_fragment_find);
            else
                throw new IllegalArgumentException("" + position);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                FriendsAcceptedInnerFragment f = new FriendsAcceptedInnerFragment();
                f.setViewPagerRefresher(this);
                return f;
            } else if (position == 1) {
                FriendsPendingInnerFragment f = new FriendsPendingInnerFragment();
                f.setViewPagerRefresher(this);
                return f;
            } else if (position == 2) {
                return new FriendsFindInnerFragment();
            }
            throw new IllegalArgumentException("" + position);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE; // hack to recreate fragments
        }

        @Override
        public void forceReload() {
            notifyDataSetChanged();
        }

    }

    public static class FriendsAcceptedInnerFragment extends Fragment implements
            FriendRemoveListener {

        private static final String KEY_LAST_REQUEST_CACHE_KEY = "FriendsAcceptedResquest";
        private List<UserRestResult> friends;
        private ObservableListView listView;
        private AnimationAdapter animationAdapter;
        private FriendsAcceptedAdapter adapter;
        private ViewPagerRefresher viewPagerRefresher;
        private SpiceManager _contentManager = new SpiceManager(
                ParticipactSpringAndroidService.class);
        private String _lastRequestCacheKey;
        private TextView errorTextView;
        private SwipeRefreshLayout swipeRefreshLayout;

        public ViewPagerRefresher getViewPagerRefresher() {
            return viewPagerRefresher;
        }

        public void setViewPagerRefresher(ViewPagerRefresher viewPagerRefresher) {
            if (viewPagerRefresher == null)
                throw new NullPointerException();
            this.viewPagerRefresher = viewPagerRefresher;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            friends = new ArrayList<UserRestResult>();

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View v = inflater.inflate(R.layout.fragment_friends_accepted_list,
                    container, false);
            listView = (ObservableListView) v.findViewById(R.id.list_accepted_friends);
            errorTextView = (TextView) v.findViewById(R.id.textViewError);

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

            adapter = new FriendsAcceptedAdapter(mContext,
                    R.layout.friends_accepted_item, friends, this);
            animationAdapter = new SwingBottomInAnimationAdapter(adapter);
            animationAdapter.setAbsListView(listView);
            listView.setAdapter(animationAdapter);

            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    if (mFragmentSwitcher != null) {
                        Fragment userFragment = UserFragment
                                .newInstance(friends.get(position).getId());
                        mFragmentSwitcher.switchContent(userFragment, true);
                    }

                }
            });

            return v;
        }

        @Override
        public void onFriendRemove(int position) {

            if (!_contentManager.isStarted()) {
                _contentManager.start(mContext);
            }
            FriendsPostRequest request = new FriendsPostRequest(friends.get(
                    position).getId(), FriendsPostRequest.REJECTED);
            _contentManager.execute(request, new FriendsRemoveListener());
            mProgressManager.showLoading(true);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            if (!TextUtils.isEmpty(_lastRequestCacheKey)) {

                outState.putString(KEY_LAST_REQUEST_CACHE_KEY,
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
            FriendsGetRequest request = new FriendsGetRequest(
                    FriendsGetRequest.ACCEPTED, mContext);
            _lastRequestCacheKey = request.createCacheKey();
            _contentManager.execute(request, _lastRequestCacheKey,
                    DurationInMillis.ONE_MINUTE * 5,
                    new FriendsAcceptedListener());
            if (!swipeRefreshLayout.isRefreshing())
                mProgressManager.showLoading(true);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            // restore state
            if (savedInstanceState != null) {
                if (savedInstanceState.containsKey(KEY_LAST_REQUEST_CACHE_KEY)) {
                    _lastRequestCacheKey = savedInstanceState
                            .getString(KEY_LAST_REQUEST_CACHE_KEY);
                    _contentManager.getFromCache(UserRestResultList.class,
                            _lastRequestCacheKey,
                            DurationInMillis.ONE_MINUTE * 5,
                            new FriendsAcceptedListener());
                }
            }
            super.onActivityCreated(savedInstanceState);
        }

        private class FriendsAcceptedListener implements
                RequestListener<UserRestResultList> {

            @Override
            public void onRequestFailure(SpiceException arg0) {
                if (!swipeRefreshLayout.isRefreshing())
                    mProgressManager.showLoading(false);
                else swipeRefreshLayout.setRefreshing(false);
                ViewUtils.toggleError(errorTextView, getString(R.string.network_error), true);
                ViewUtils.toggleAlpha(listView, false);

            }

            @Override
            public void onRequestSuccess(UserRestResultList result) {
                if (!swipeRefreshLayout.isRefreshing())
                    mProgressManager.showLoading(false);
                else swipeRefreshLayout.setRefreshing(false);

                if (result.size() == 0) {
                    ViewUtils.toggleAlpha(listView, false);
                    ViewUtils.toggleError(errorTextView, getString(R.string.no_friends_yet), true);
                } else {
                    ViewUtils.toggleAlpha(listView, true);
                    ViewUtils.toggleError(errorTextView, null, false);
                    friends.clear();
                    friends.addAll(result);
                    Collections.reverse(friends);
                    adapter.notifyDataSetChanged();

                }

            }

        }

        private class FriendsRemoveListener implements RequestListener<Boolean> {

            @Override
            public void onRequestFailure(SpiceException arg0) {
                mProgressManager.showLoading(false);
                Snackbar.with(mContext).text(getString(R.string.network_error)).show(mActivity);
            }

            @Override
            public void onRequestSuccess(Boolean result) {
                mProgressManager.showLoading(false);
                String toastMessage;
                if (result)
                    toastMessage = mContext.getString(R.string.friends_fragment_success_friend_remove);
                else
                    toastMessage = mContext.getString(R.string.friends_fragment_fail_friend_remove);
                Snackbar.with(mContext).text(toastMessage).show(mActivity);
                //Toast.makeText(myContext, toastMessage, Toast.LENGTH_SHORT).show();
                if (result) {
                    _contentManager.removeAllDataFromCache();
                    if (viewPagerRefresher != null)
                        viewPagerRefresher.forceReload();
                }

                _contentManager.removeAllDataFromCache();

            }

        }
    }

    public static class FriendsPendingInnerFragment extends Fragment implements
            FriendInteractionListener {

        private List<UserRestResult> pendingFriends;
        private ObservableListView listView;
        private AnimationAdapter animationAdapter;
        private FriendsPendingAdapter adapter;
        private ViewPagerRefresher viewPagerRefresher;
        private SpiceManager _contentManager = new SpiceManager(
                ParticipactSpringAndroidService.class);
        private TextView errorTextView;
        private SwipeRefreshLayout swipeRefreshLayout;

        public ViewPagerRefresher getViewPagerRefresher() {
            return viewPagerRefresher;
        }

        public void setViewPagerRefresher(ViewPagerRefresher viewPagerRefresher) {
            if (viewPagerRefresher == null)
                throw new NullPointerException();
            this.viewPagerRefresher = viewPagerRefresher;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            pendingFriends = new ArrayList<UserRestResult>();

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View v = inflater.inflate(R.layout.fragment_friends_pending_list,
                    container, false);
            listView = (ObservableListView) v.findViewById(R.id.list_pending_friends);
            errorTextView = (TextView) v.findViewById(R.id.textViewError);

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

            adapter = new FriendsPendingAdapter(mContext,
                    R.layout.friends_pending_item, pendingFriends, this);
            animationAdapter = new SwingBottomInAnimationAdapter(adapter);
            animationAdapter.setAbsListView(listView);
            listView.setAdapter(animationAdapter);

            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    if (mFragmentSwitcher != null) {
                        Fragment userFragment = UserFragment
                                .newInstance(pendingFriends.get(position)
                                        .getId());
                        mFragmentSwitcher.switchContent(userFragment, true);
                    }

                }
            });

            return v;
        }

        @Override
        public void onFriendInteraction(int position, boolean accepted) {

            String status;
            if (accepted)
                status = FriendsPostRequest.ACCEPTED;
            else
                status = FriendsPostRequest.REJECTED;

            FriendsPostRequest request = new FriendsPostRequest(pendingFriends
                    .get(position).getId(), status);

            _contentManager.execute(request, new FriendsManageListener());
            mProgressManager.showLoading(true);

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
            FriendsGetRequest request = new FriendsGetRequest(
                    FriendsGetRequest.PENDING_RECEIVED, mContext);
            _contentManager.execute(request, new FriendsPendingListener());
            if (!swipeRefreshLayout.isRefreshing())
                mProgressManager.showLoading(true);
        }

        private class FriendsPendingListener implements
                RequestListener<UserRestResultList> {

            @Override
            public void onRequestFailure(SpiceException arg0) {
                if (!swipeRefreshLayout.isRefreshing())
                    mProgressManager.showLoading(false);
                else swipeRefreshLayout.setRefreshing(false);
                ViewUtils.toggleError(errorTextView, getString(R.string.network_error), true);
                ViewUtils.toggleAlpha(listView, false);

            }

            @Override
            public void onRequestSuccess(UserRestResultList result) {
                if (!swipeRefreshLayout.isRefreshing())
                    mProgressManager.showLoading(false);
                else swipeRefreshLayout.setRefreshing(false);

                if (result.size() == 0) {
                    ViewUtils.toggleAlpha(listView, false);
                    ViewUtils.toggleError(errorTextView, getString(R.string.no_friends_pending), true);
                } else {
                    ViewUtils.toggleAlpha(listView, true);
                    ViewUtils.toggleError(errorTextView, null, false);
                    pendingFriends.clear();
                    pendingFriends.addAll(result);
                    Collections.reverse(pendingFriends);
                    adapter.notifyDataSetChanged();
                }

            }

        }

        private class FriendsManageListener implements RequestListener<Boolean> {

            @Override
            public void onRequestFailure(SpiceException arg0) {
                mProgressManager.showLoading(false);
                Snackbar.with(mContext).text(getString(R.string.network_error)).show(mActivity);

            }

            @Override
            public void onRequestSuccess(Boolean result) {
                mProgressManager.showLoading(false);
                String toastMessage;
                if (result)
                    toastMessage = mContext.getString(R.string.friends_fragment_success_request);
                else
                    toastMessage = mContext.getString(R.string.friends_fragment_fail_request);
                Snackbar.with(mContext).text(toastMessage).show(mActivity);
                //Toast.makeText(myContext, toastMessage, Toast.LENGTH_SHORT).show();
                if (result) {
                    _contentManager.removeAllDataFromCache();
                    if (viewPagerRefresher != null)
                        viewPagerRefresher.forceReload();
                }

            }

        }
    }


    public static class FriendsFindInnerFragment extends Fragment {

        private Button button;

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_social_intro,
                    container, false);
            button = (Button) v.findViewById(R.id.button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment goTo = new SocialFragment();
                    mFragmentSwitcher.switchContent(goTo, true);

                }
            });

            return v;
        }
    }

}
