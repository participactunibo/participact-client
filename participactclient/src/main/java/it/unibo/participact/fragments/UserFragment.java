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
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.astuetz.PagerSlidingTabStrip;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unibo.participact.R;
import it.unibo.participact.activities.interfaces.ProgressManager;
import it.unibo.participact.domain.persistence.GeoBadgeCollected;
import it.unibo.participact.domain.persistence.InterestPoint;
import it.unibo.participact.domain.persistence.StateUtility;
import it.unibo.participact.domain.persistence.support.DomainDBHelper;
import it.unibo.participact.domain.rest.BadgeRestResult;
import it.unibo.participact.domain.rest.BadgeRestResultList;
import it.unibo.participact.domain.rest.FriendshipRestStatus;
import it.unibo.participact.domain.rest.LevelRank;
import it.unibo.participact.domain.rest.UserRestResult;
import it.unibo.participact.network.request.BadgesForUserRequest;
import it.unibo.participact.network.request.FriendStatusRequest;
import it.unibo.participact.network.request.FriendsPostRequest;
import it.unibo.participact.network.request.ParticipactSpringAndroidService;
import it.unibo.participact.network.request.UserRequest;
import it.unibo.participact.support.ViewUtils;
import it.unibo.participact.support.preferences.ShowTipsPreferences;
import it.unibo.participact.views.adapters.BadgeAdapter;

public class UserFragment extends Fragment {

    static final int NUM_ITEMS = 2;
    private static Context mContext;
    private static Activity mActivity;
    private static ProgressManager mProgressManager;

    public static int SUMMARY_INDEX = 0;
    public static int BADGES_INDEX = 1;
    public static int ME_ID = -1;

    private FloatingActionButton fab;

    private static long id;
    private static int pageIndex;
    private String newFriendshipStatus;
    private SpiceManager _contentManager = new SpiceManager(
            ParticipactSpringAndroidService.class);

    @Override
    public void onAttach(Activity activity) {
        mContext = activity;
        mActivity = activity;
        if (activity instanceof ProgressManager)
            mProgressManager = ((ProgressManager) activity);
        else
            throw new RuntimeException("Parent activity of UserFragment must implement ProgressManager");
        super.onAttach(activity);
    }

    public static UserFragment newInstance(long id) {
        UserFragment f = new UserFragment();

        // Supply id input as an argument.
        Bundle args = new Bundle();
        args.putLong("id", id);
        f.setArguments(args);

        return f;
    }

    public static UserFragment newInstance(long id, int pageIndex) {
        UserFragment f = new UserFragment();

        // Supply id input as an argument.
        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putInt("pageIndex", pageIndex);
        f.setArguments(args);

        return f;
    }

    public void addFriendButton(String status) {
        if (fab != null)
            ViewUtils.toggleFab(fab, true);
        newFriendshipStatus = status;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = getArguments() != null ? getArguments().getLong("id") : -1;
        pageIndex = getArguments() != null ? getArguments().getInt("pageIndex") : -1;
    }

    @Override
    public void onResume() {
        super.onResume();
        performCheckFriendship();
    }

    private void performCheckFriendship() {
        if (id > 0) {

            if (!_contentManager.isStarted()) {
                _contentManager.start(mContext);
            }
            FriendStatusRequest request = new FriendStatusRequest(id,
                    mContext);
            _contentManager.execute(request, new CheckFriendshipListener());
            mProgressManager.showLoading(true);
        }

    }

    private void performAddFriend(String newFriendshipStatus) {
        if (!_contentManager.isStarted()) {
            _contentManager.start(mContext);
        }

        FriendsPostRequest request = new FriendsPostRequest(id,
                newFriendshipStatus);
        _contentManager.execute(request, new FriendAddListener());
        mProgressManager.showLoading(true);

    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user_pager, container, false);
        MyAdapter mAdapter = new MyAdapter(getChildFragmentManager());
        ViewPager mPager = (ViewPager) root.findViewById(R.id.user_pager);
        mPager.setAdapter(mAdapter);
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) root.findViewById(R.id.user_tabs);
        tabs.setAllCaps(true);
        tabs.setShouldExpand(true);
        tabs.setIndicatorColorResource(R.color.icons);
        tabs.setViewPager(mPager);
        fab = (FloatingActionButton) root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAddFriend(newFriendshipStatus);
            }
        });

        if (ShowTipsPreferences.getInstance(mContext).shouldShowTips(UserFragment.class.getSimpleName())) {
            View badgesButton = ((ViewGroup) tabs.getChildAt(0)).getChildAt(1);
            new ShowcaseView.Builder(mActivity, true).setTarget(new ViewTarget(badgesButton)).setContentTitle(mContext.getString(R.string.tip_badges)).setStyle(R.style.CustomShowcaseTheme).hideOnTouchOutside().build();
            ShowTipsPreferences.getInstance(mContext).setShouldShowTips(UserFragment.class.getSimpleName(), false);
        }

        if (pageIndex >= 0)
            mPager.setCurrentItem(pageIndex);
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
                return mContext.getString(R.string.user_fragment_summary);
            else if (position == 1)
                return mContext.getString(R.string.user_fragment_badges);
            else
                throw new IllegalArgumentException("" + position);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return SummaryUserFragment.newInstance(id);
            else if (position == 1)
                return BadgesUserFragment.newInstance(id);
            else
                throw new IllegalArgumentException("" + position);

        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE; // hack to recreate fragments
        }
    }

    public static class SummaryUserFragment extends Fragment {

        private static final String KEY_LAST_REQUEST_USER_CACHE_KEY = "UserResquest";
        private SpiceManager _contentManager = new SpiceManager(
                ParticipactSpringAndroidService.class);
        private String _lastRequestCacheKeyUser;

        private long id;
        private ImageView letterImageView;
        private TextView userNameTextView;
        private PieChart pieActivity;
        private PieChart pieQuestionnaire;
        private PieChart piePhoto;
        private PieChart pieSensing;
        private TextView textViewError;
        private LinearLayout linearLayout;

        static SummaryUserFragment newInstance(long id) {
            SummaryUserFragment f = new SummaryUserFragment();

            // Supply id input as an argument.
            Bundle args = new Bundle();
            args.putLong("id", id);
            f.setArguments(args);

            return f;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            id = getArguments() != null ? getArguments().getLong("id") : -1;
        }

        @SuppressLint("InflateParams")
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View root = inflater.inflate(R.layout.fragment_user_summary, container, false);

            letterImageView = (ImageView) root
                    .findViewById(R.id.user_imageView);

            letterImageView.setVisibility(View.INVISIBLE);

            userNameTextView = (TextView) root.findViewById(R.id.user_name);

            textViewError = (TextView) root.findViewById(R.id.textViewError);

            linearLayout = (LinearLayout) root.findViewById(R.id.linearLayout);

            pieActivity = (PieChart) root.findViewById(R.id.pieChartActivityDetection);
            pieQuestionnaire = (PieChart) root.findViewById(R.id.pieChartQuestionnaire);
            piePhoto = (PieChart) root.findViewById(R.id.pieChartPhoto);
            pieSensing = (PieChart) root.findViewById(R.id.pieChartSensing);

            pieActivity.setDrawYValues(false);
            pieQuestionnaire.setDrawYValues(false);
            piePhoto.setDrawYValues(false);
            pieSensing.setDrawYValues(false);

            pieActivity.setDrawXValues(false);
            pieQuestionnaire.setDrawXValues(false);
            piePhoto.setDrawXValues(false);
            pieSensing.setDrawXValues(false);


            pieActivity.setCenterText(mContext.getString(R.string.activity_detecion).replace(' ', '\n'));
            pieQuestionnaire.setCenterText(mContext.getString(R.string.questionnaire));
            piePhoto.setCenterText(mContext.getString(R.string.photo));
            pieSensing.setCenterText(mContext.getString(R.string.sensing));

            Typeface tf = Typeface.createFromAsset(mContext.getAssets(), "Roboto-Light.ttf");

            pieActivity.setCenterTextTypeface(tf);
            pieQuestionnaire.setCenterTextTypeface(tf);
            piePhoto.setCenterTextTypeface(tf);
            pieSensing.setCenterTextTypeface(tf);

            pieActivity.setHighlightEnabled(false);
            pieActivity.setRotationEnabled(false);
            pieQuestionnaire.setHighlightEnabled(false);
            pieQuestionnaire.setRotationEnabled(false);
            piePhoto.setHighlightEnabled(false);
            piePhoto.setRotationEnabled(false);
            pieSensing.setHighlightEnabled(false);
            pieSensing.setRotationEnabled(false);

            pieActivity.setDrawLegend(false);
            pieQuestionnaire.setDrawLegend(false);
            piePhoto.setDrawLegend(false);
            pieSensing.setDrawLegend(false);

            pieActivity.setNoDataText("");
            pieQuestionnaire.setNoDataText("");
            piePhoto.setNoDataText("");
            pieSensing.setNoDataText("");

            pieActivity.setDescription("");
            pieQuestionnaire.setDescription("");
            piePhoto.setDescription("");
            pieSensing.setDescription("");

            return root;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            // restore state
            if (savedInstanceState != null) {
                if (savedInstanceState
                        .containsKey(KEY_LAST_REQUEST_USER_CACHE_KEY + id)) {
                    _lastRequestCacheKeyUser = savedInstanceState
                            .getString(KEY_LAST_REQUEST_USER_CACHE_KEY + id);
                    _contentManager
                            .getFromCache(UserRestResult.class,
                                    _lastRequestCacheKeyUser,
                                    DurationInMillis.ONE_MINUTE * 5,
                                    new UserListener());
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
            if (!TextUtils.isEmpty(_lastRequestCacheKeyUser)) {
                outState.putString(KEY_LAST_REQUEST_USER_CACHE_KEY + id,
                        _lastRequestCacheKeyUser);
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
            if (!_contentManager.isStarted()) {
                _contentManager.start(mContext);
            }

            UserRequest request = new UserRequest(id, mContext);
            _lastRequestCacheKeyUser = request.createCacheKey();
            _contentManager.execute(request, _lastRequestCacheKeyUser,
                    DurationInMillis.ONE_MINUTE * 5, new UserListener());

            mProgressManager.showLoading(true);
        }

        private class UserListener implements RequestListener<UserRestResult> {

            @Override
            public void onRequestFailure(SpiceException error) {
                mProgressManager.showLoading(false);
                ViewUtils.toggleAlpha(linearLayout, false);
                ViewUtils.toggleError(textViewError, getString(R.string.network_error), true);

            }

            @Override
            public void onRequestSuccess(UserRestResult result) {
                ViewUtils.toggleAlpha(linearLayout, true);
                ViewUtils.toggleError(textViewError, null, false);
                mProgressManager.showLoading(false);

                StringBuilder stringBuilder = new StringBuilder(result.getName());
                stringBuilder.append(" ");
                stringBuilder.append(result.getSurname());

                StringBuilder initialsBuilder = new StringBuilder();
                initialsBuilder.append(result.getName().charAt(0));
                initialsBuilder.append(result.getSurname().charAt(0));
                //TextDrawable drawable = TextDrawable.builder().buildRound(initialsBuilder.toString().toUpperCase(), ColorGenerator.DEFAULT.getColor(result.getId()));
                TextDrawable drawable = TextDrawable.builder().buildRound(initialsBuilder.toString().toUpperCase(), mContext.getResources().getColor(R.color.primary_dark));
                letterImageView.setImageDrawable(drawable);

                userNameTextView.setText(stringBuilder.toString());
                letterImageView.setVisibility(View.VISIBLE);

                ArrayList<String> xValsActivity = new ArrayList<String>();
                xValsActivity.add("");
                xValsActivity.add("");

                ArrayList<String> xValsQuestionnaire = new ArrayList<String>();
                xValsQuestionnaire.add("");
                xValsQuestionnaire.add("");

                ArrayList<String> xValsPhoto = new ArrayList<String>();
                xValsPhoto.add("");
                xValsPhoto.add("");

                ArrayList<String> xValsSensing = new ArrayList<String>();
                xValsSensing.add("");
                xValsSensing.add("");

                ArrayList<Entry> yValsActivity = new ArrayList<Entry>();
                ArrayList<Entry> yValsQuestionnaire = new ArrayList<Entry>();
                ArrayList<Entry> yValsPhoto = new ArrayList<Entry>();
                ArrayList<Entry> yValsSensing = new ArrayList<Entry>();


                LevelRank questionnaireLevelRank = result
                        .getQuestionnaireLevel();

                int valueActivity = 0;
                int valueQuestionnaire = 0;
                int valuePhoto = 0;
                int valueSensing = 0;

                if (questionnaireLevelRank == LevelRank.LOW)
                    valueQuestionnaire = 25;
                else if (questionnaireLevelRank == LevelRank.MEDIUM_LOW)
                    valueQuestionnaire = 50;
                else if (questionnaireLevelRank == LevelRank.MEDIUM_HIGH)
                    valueQuestionnaire = 75;
                else if (questionnaireLevelRank == LevelRank.HIGH)
                    valueQuestionnaire = 100;

                LevelRank sensingLevelRank = result.getSensingMostLevel();

                if (sensingLevelRank == LevelRank.LOW)
                    valueSensing = 25;
                else if (sensingLevelRank == LevelRank.MEDIUM_LOW)
                    valueSensing = 50;
                else if (sensingLevelRank == LevelRank.MEDIUM_HIGH)
                    valueSensing = 75;
                else if (sensingLevelRank == LevelRank.HIGH)
                    valueSensing = 100;

                LevelRank photoLevelRank = result.getPhotoLevel();

                if (photoLevelRank == LevelRank.LOW)
                    valuePhoto = 25;
                else if (photoLevelRank == LevelRank.MEDIUM_LOW)
                    valuePhoto = 50;
                else if (photoLevelRank == LevelRank.MEDIUM_HIGH)
                    valuePhoto = 75;
                else if (photoLevelRank == LevelRank.HIGH)
                    valuePhoto = 100;

                LevelRank activityLevelRank = result
                        .getActivityDetectionLevel();

                if (activityLevelRank == LevelRank.LOW)
                    valueActivity = 25;
                else if (activityLevelRank == LevelRank.MEDIUM_LOW)
                    valueActivity = 50;
                else if (activityLevelRank == LevelRank.MEDIUM_HIGH)
                    valueActivity = 75;
                else if (activityLevelRank == LevelRank.HIGH)
                    valueActivity = 100;

                yValsActivity.add(new Entry(valueActivity, 0));
                yValsActivity.add(new Entry(100 - valueActivity, 1));

                yValsQuestionnaire.add(new Entry(valueQuestionnaire, 0));
                yValsQuestionnaire.add(new Entry(100 - valueQuestionnaire, 1));

                yValsPhoto.add(new Entry(valuePhoto, 0));
                yValsPhoto.add(new Entry(100 - valuePhoto, 1));

                yValsSensing.add(new Entry(valueSensing, 0));
                yValsSensing.add(new Entry(100 - valueSensing, 1));

                PieDataSet setActivity = new PieDataSet(yValsActivity, "Activity");
                PieDataSet setQuestionnaire = new PieDataSet(yValsQuestionnaire, "Questionnaire");
                PieDataSet setPhoto = new PieDataSet(yValsPhoto, "Photo");
                PieDataSet setSensing = new PieDataSet(yValsSensing, "Sensing");

                ArrayList<Integer> colors = new ArrayList<Integer>(2);
                //colors.add(ColorGenerator.DEFAULT.getColor(result.getId()));
                colors.add(mContext.getResources().getColor(R.color.primary));
                colors.add(Color.TRANSPARENT);

                setActivity.setColors(colors);
                setQuestionnaire.setColors(colors);
                setPhoto.setColors(colors);
                setSensing.setColors(colors);

                PieData dataActivity = new PieData(xValsActivity, setActivity);
                PieData dataQuestionnaire = new PieData(xValsQuestionnaire, setQuestionnaire);
                PieData dataPhoto = new PieData(xValsPhoto, setPhoto);
                PieData dataSensing = new PieData(xValsSensing, setSensing);

                pieActivity.setData(dataActivity);
                pieActivity.invalidate();

                pieQuestionnaire.setData(dataQuestionnaire);
                pieQuestionnaire.invalidate();

                piePhoto.setData(dataPhoto);
                piePhoto.invalidate();

                pieSensing.setData(dataSensing);
                pieSensing.invalidate();

                pieActivity.animateY(1800);
                pieQuestionnaire.animateY(1800);
                piePhoto.animateY(1800);
                pieSensing.animateY(1800);


            }

        }

    }

    public static class BadgesUserFragment extends Fragment {

        private static final String KEY_LAST_REQUEST_BAGDES_CACHE_KEY = "UserBadgesResquest";
        private SpiceManager _contentManager = new SpiceManager(
                ParticipactSpringAndroidService.class);
        private String _lastRequestCacheKeyBadges;
        private TextView textViewError;

        private long id;
        private GridView badgesGrid;
        private List<BadgeRestResult> badges;
        private BadgeAdapter badgeAdapter;


        static BadgesUserFragment newInstance(long id) {
            BadgesUserFragment f = new BadgesUserFragment();


            // Supply id input as an argument.
            Bundle args = new Bundle();
            args.putLong("id", id);
            f.setArguments(args);

            return f;
        }



        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            id = getArguments() != null ? getArguments().getLong("id") : -1;

            badges = new ArrayList<BadgeRestResult>();

        }






        @SuppressLint("InflateParams")
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View root = inflater.inflate(R.layout.fragment_user_badges, container, false);

            badgesGrid = (GridView) root.findViewById(R.id.user_badges_grid);


            badgeAdapter = new BadgeAdapter(mContext, R.layout.badge,
                    badges, id <= 0);
            badgesGrid.setAdapter(badgeAdapter);
            textViewError = (TextView) root.findViewById(R.id.textViewError);
            return root;
        }



        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            // restore state
            if (savedInstanceState != null) {
                if (savedInstanceState
                        .containsKey(KEY_LAST_REQUEST_BAGDES_CACHE_KEY + id)) {
                    _lastRequestCacheKeyBadges = savedInstanceState
                            .getString(KEY_LAST_REQUEST_BAGDES_CACHE_KEY + id);
                    _contentManager.getFromCache(BadgeRestResultList.class,
                            _lastRequestCacheKeyBadges,
                            DurationInMillis.ONE_MINUTE * 5,
                            new BadgesListener());
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
            if (!TextUtils.isEmpty(_lastRequestCacheKeyBadges)) {
                outState.putString(KEY_LAST_REQUEST_BAGDES_CACHE_KEY + id,
                        _lastRequestCacheKeyBadges);
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
            if (!_contentManager.isStarted()) {
                _contentManager.start(mContext);
            }
            BadgesForUserRequest requestBadges = new BadgesForUserRequest(id,
                    mContext);
            _lastRequestCacheKeyBadges = requestBadges.createCacheKey();
            _contentManager.execute(requestBadges, _lastRequestCacheKeyBadges,
                    DurationInMillis.ONE_MINUTE * 5, new BadgesListener());

            mProgressManager.showLoading(true);
        }


        private class BadgesListener implements
                RequestListener<BadgeRestResultList> {

            @Override
            public void onRequestFailure(SpiceException arg0) {
                mProgressManager.showLoading(false);
                ViewUtils.toggleError(textViewError, getString(R.string.network_error), true);
                ViewUtils.toggleAlpha(badgesGrid, false);

            }

            @Override
            public void onRequestSuccess(BadgeRestResultList result) {
                mProgressManager.showLoading(false);
                ViewUtils.toggleAlpha(badgesGrid, true);
                ViewUtils.toggleError(textViewError, null, false);

                /*inserisco i geobadge presenti in locale, raccolti e non ancora sincronizzati con il server */
                List<GeoBadgeCollected> geoBadgeNotSync = StateUtility.getBadgeCollected(mContext);
                BadgeRestResult currentBudgetLocal;

                for(int i=0;i<geoBadgeNotSync.size();i++){
                    currentBudgetLocal = new BadgeRestResult();
                    currentBudgetLocal.setId(geoBadgeNotSync.get(i).getId());
                    currentBudgetLocal.setDescription(geoBadgeNotSync.get(i).getDesctioprionGeofence());
                    currentBudgetLocal.setTitle("GEO Badge");
                    result.add(currentBudgetLocal);
                }



                if (result.size() == 0) {
                    textViewError.setText(getString(R.string.no_data_badges));
                    textViewError.setVisibility(View.VISIBLE);
                    return;

                }


                badges.clear();
                badges.addAll(result);

                Collections.reverse(badges);
                badgeAdapter.notifyDataSetChanged();

            }

        }
    }

    private class CheckFriendshipListener implements
            RequestListener<FriendshipRestStatus> {

        @Override
        public void onRequestFailure(SpiceException arg0) {
            if (!_contentManager.isStarted()) {
                _contentManager.start(mContext);
            }

        }

        @Override
        public void onRequestSuccess(FriendshipRestStatus result) {
            if (!_contentManager.isStarted()) {
                _contentManager.start(mContext);
            }


            if ("not_setted".equalsIgnoreCase(result.getStatus()))
                addFriendButton(FriendsPostRequest.PENDING);
            else if ("pending_received".equalsIgnoreCase(result.getStatus()))
                addFriendButton(FriendsPostRequest.ACCEPTED);
            else if ("rejected_received".equalsIgnoreCase(result.getStatus()))
                addFriendButton(FriendsPostRequest.ACCEPTED);

        }

    }

    private class FriendAddListener implements RequestListener<Boolean> {

        @Override
        public void onRequestFailure(SpiceException arg0) {
            mProgressManager.showLoading(false);
        }

        @Override
        public void onRequestSuccess(Boolean result) {
            mProgressManager.showLoading(false);

            if (result) {
                ViewUtils.toggleFab(fab, false);
                _contentManager.removeAllDataFromCache();
            }

        }

    }

}
