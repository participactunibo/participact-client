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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nispok.snackbar.Snackbar;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import it.unibo.participact.R;
import it.unibo.participact.activities.CreateTaskActivity;
import it.unibo.participact.activities.interfaces.FragmentSwitcher;
import it.unibo.participact.activities.interfaces.ProgressManager;
import it.unibo.participact.domain.rest.TaskFlat;
import it.unibo.participact.domain.rest.TaskFlatRequest;
import it.unibo.participact.domain.rest.UserRestResult;
import it.unibo.participact.domain.rest.UserRestResultList;
import it.unibo.participact.network.request.FriendsGetRequest;
import it.unibo.participact.network.request.ParticipactSpringAndroidService;
import it.unibo.participact.network.request.TaskCreatePostRequest;
import it.unibo.participact.support.ViewUtils;
import it.unibo.participact.views.adapters.FriendsAcceptedAdapter;
import it.unibo.participact.views.adapters.SelectFriendsAdapter;

/**
 * Created by alessandro on 11/02/15.
 */
public class SelectFriendsFragment extends Fragment implements SelectFriendsAdapter.FriendSelectedListener{

    private static final String KEY_LAST_REQUEST_CACHE_KEY = "FriendsAcceptedResquest";
    private static Context mContext;
    private static ActionBarActivity mActivity;

    private TaskFlatRequest taskFlatRequest;
    private TaskCreatePostRequest request;
    private FriendsGetRequest friendRequest;
    private boolean taskRequesting;
    private boolean friendRequesting;

    private boolean inBackground;
    private boolean pendingShowDialog;
    private boolean success;
    private String errorMessage;
    private int lastScrollPosition;

    private static FragmentSwitcher mFragmentSwitcher;
    private static ProgressManager mProgressManager;
    private AnimationAdapter animationAdapter;
    private SelectFriendsAdapter adapter;
    private FloatingActionButton fab;
    private ObservableListView listView;

    private SpiceManager _contentManager = new SpiceManager(
            ParticipactSpringAndroidService.class);
    private String _lastRequestCacheKey;
    private TextView errorTextView;


    List<UserRestResult> friends;



    public static Fragment newInstance(FragmentSwitcher fragmentSwitcher,TaskFlatRequest taskFlatRequest)
    {
        SelectFriendsFragment selectFriendsFragment = new SelectFriendsFragment();
        selectFriendsFragment.setFragmentSwitcher(fragmentSwitcher);
        Bundle args = new Bundle();
        args.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(taskFlatRequest));
        selectFriendsFragment.setArguments(args);
        return selectFriendsFragment;
    }

    private void setFragmentSwitcher(FragmentSwitcher fragmentSwitcher) {
        this.mFragmentSwitcher = fragmentSwitcher;
    }

    @Override
    public void onAttach(Activity activity)
    {
        mContext = activity;
        if (activity instanceof ActionBarActivity)
            mActivity = (ActionBarActivity) activity;
        else
            throw new RuntimeException("Parent activity of FriendsFragment must extend ActionBarActivity");
        if (activity instanceof ProgressManager)
            mProgressManager = ((ProgressManager) activity);
        super.onAttach(activity);


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friends = new ArrayList<UserRestResult>();
        setHasOptionsMenu(true);
        mActivity.getSupportActionBar().setTitle(mContext.getResources().getString(R.string.action_select_friends_title));
        Bundle args = getArguments();
        taskFlatRequest = Parcels.unwrap(args.getParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE));
        taskRequesting = false;
        friendRequesting = false;
        pendingShowDialog = false;
        inBackground = false;
        success = false;
        lastScrollPosition = -1;

        taskFlatRequest.setIdFriends(new HashSet<Long>());
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment f = null;

        switch (item.getItemId()) {
            case android.R.id.home:
                if (taskRequesting)
                    request.cancel();
                if(friendRequesting)
                    friendRequest.cancel();
                f = CreateTaskFragment.newInstance(taskFlatRequest, mFragmentSwitcher);
                mFragmentSwitcher.switchContent(f, false);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_select_friends_list,container,false);

        listView = (ObservableListView) v.findViewById(R.id.list_accepted_friends);
        errorTextView = (TextView) v.findViewById(R.id.textViewError);
        fab = (FloatingActionButton) v.findViewById(R.id.fab);

        adapter = new SelectFriendsAdapter(mActivity,R.layout.fragment_select_friends_item,friends,this);

        animationAdapter = new SwingBottomInAnimationAdapter(adapter);
        animationAdapter.setAbsListView(listView);
        listView.setAdapter(animationAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performTaskRequest();
            }
        });

        listView.setScrollViewCallbacks(new ObservableScrollViewCallbacks() {
            @Override
            public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
                if (lastScrollPosition < scrollY && lastScrollPosition >= 0)
                  ViewUtils.toggleFab(fab,false);

                else if (lastScrollPosition > scrollY) {
                    ViewUtils.toggleFab(fab,true);
                }
                lastScrollPosition = scrollY;

            }

            @Override
            public void onDownMotionEvent() {

            }

            @Override
            public void onUpOrCancelMotionEvent(ScrollState scrollState) {

            }
        });

        return v;



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
        inBackground = true;
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (taskRequesting)
            mProgressManager.showLoading(true);
        else {
            mProgressManager.showLoading(false);
            if (inBackground && pendingShowDialog) {
                inBackground = false;
                pendingShowDialog = false;
                if (success)
                    showSuccessAlertDialog(errorMessage);
                else
                    showFailedAlertDialog(errorMessage);
            }
            else
                performFriendRequest();
        }
    }


    private void performFriendRequest() {
        if (!_contentManager.isStarted()) {
            _contentManager.start(mActivity);
        }
         friendRequest = new FriendsGetRequest(
                FriendsGetRequest.ACCEPTED, mActivity);
        _lastRequestCacheKey = friendRequest.createCacheKey();
        _contentManager.execute(friendRequest, _lastRequestCacheKey,
                DurationInMillis.ONE_MINUTE * 5,
                new FriendsAcceptedListener());
        friendRequesting = true;
        mProgressManager.showLoading(true);
    }

    private void performTaskRequest() {
        if (!_contentManager.isStarted()) {
            _contentManager.start(getActivity());
        }


        request = new TaskCreatePostRequest(taskFlatRequest);
        _contentManager.execute(request, new TaskCreatedListener());
        taskRequesting = true;
       mProgressManager.showLoading(true);
    }

    private void showSuccessAlertDialog(String mesage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mContext.getResources().getString(R.string.builder_confirmation_message)).setMessage(mesage).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });

        builder.create().show();

    }

    private void showFailedAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mContext.getResources().getString(R.string.builder_error_message)).setMessage(message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }



    @Override
    public void onFriendSelected(int position, boolean checked) {
        UserRestResult result = friends.get(position);
        if(checked)
        {
            if(!taskFlatRequest.getIdFriends().contains(result.getId()))
                taskFlatRequest.getIdFriends().add(result.getId());
        }
        else
        {
            if(taskFlatRequest.getIdFriends().contains(result.getId()))
                taskFlatRequest.getIdFriends().remove(result.getId());
        }

    }

    private class FriendsAcceptedListener implements
            RequestListener<UserRestResultList> {

        @Override
        public void onRequestFailure(SpiceException arg0) {

                mProgressManager.showLoading(false);
                friendRequesting = false;
                ViewUtils.toggleError(errorTextView, getString(R.string.network_error), true);
                ViewUtils.toggleAlpha(listView, false);
                fab.setVisibility(View.INVISIBLE);


        }

        @Override
        public void onRequestSuccess(UserRestResultList result) {

                mProgressManager.showLoading(false);
                friendRequesting = false;
                fab.setVisibility(View.VISIBLE);

                if (result.size() == 0) {
                    ViewUtils.toggleAlpha(listView, false);
                    ViewUtils.toggleError(errorTextView, mContext.getResources().getString(R.string.no_friends_send_task), true);
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


    private class TaskCreatedListener implements RequestListener<it.unibo.participact.domain.rest.TaskFlat> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            mProgressManager.showLoading(false);
            taskRequesting = false;
            success = false;

            String message;
            if (spiceException instanceof NoNetworkException) {
                errorMessage = mContext.getResources().getString(R.string.builder_error_messagge) ;
                if (!inBackground)
                    showFailedAlertDialog(errorMessage);
                else
                    pendingShowDialog = true;
            }
            else {
                errorMessage = mContext.getResources().getString(R.string.builder_error_messagge_network) ;
                if (!inBackground)
                    showFailedAlertDialog(errorMessage);
                else
                    pendingShowDialog = true;
            }

        }

        @Override
        public void onRequestSuccess(TaskFlat taskFlat) {
            mProgressManager.showLoading(false);
            taskRequesting = false;
            success = true;
            errorMessage = mContext.getResources().getString(R.string.builder_success_message) ;
            if (!inBackground) {
                showSuccessAlertDialog(errorMessage);
            } else
                pendingShowDialog = true;
        }


    }






}
