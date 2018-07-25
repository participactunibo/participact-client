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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.github.gorbin.asne.core.SocialNetwork;
import com.github.gorbin.asne.core.SocialNetworkManager;
import com.github.gorbin.asne.core.listener.OnLoginCompleteListener;
import com.github.gorbin.asne.core.listener.OnRequestGetFriendsCompleteListener;
import com.github.gorbin.asne.core.listener.OnRequestSocialPersonCompleteListener;
import com.github.gorbin.asne.core.persons.SocialPerson;
import com.github.gorbin.asne.facebook.FacebookSocialNetwork;
import com.github.gorbin.asne.twitter.TwitterSocialNetwork;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unibo.participact.R;
import it.unibo.participact.activities.DashboardActivity;
import it.unibo.participact.activities.interfaces.FragmentSwitcher;
import it.unibo.participact.activities.interfaces.ProgressManager;
import it.unibo.participact.domain.rest.UserRestResult;
import it.unibo.participact.domain.rest.UserRestResultList;
import it.unibo.participact.network.request.FriendsPostRequest;
import it.unibo.participact.network.request.ParticipactSpringAndroidService;
import it.unibo.participact.network.request.SocialPresenceAddRequest;
import it.unibo.participact.network.request.SocialPresenceGetFriendsRequest;
import it.unibo.participact.views.adapters.FriendsSocialAdapter;


public class SocialFragment extends Fragment implements SocialNetworkManager.OnInitializationCompleteListener, OnLoginCompleteListener {

    public static final int TWITTER = 1;
    public static final int FACEBOOK = 4;

    private boolean isTwitterGoing = false;
    private boolean isFacebookGoing = false;

    private View.OnClickListener loginClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (isTwitterGoing || isFacebookGoing)
                return; //user has already tap the button

            int networkId = 0;
            switch (view.getId()) {
                case R.id.twitter_button:
                    networkId = TWITTER;
                    break;
                case R.id.facebook_button:
                    networkId = FACEBOOK;
                    break;
            }
            SocialNetwork socialNetwork = mSocialNetworkManager.getSocialNetwork(networkId);
            if (!socialNetwork.isConnected()) {
                if (networkId != 0) {
                    if (networkId == TWITTER)
                        Snackbar.with(mContext).text(getString(R.string.please_wait)).duration(7000).show(mActivity);
                    else
                        Snackbar.with(mContext).text(getString(R.string.please_wait)).show(mActivity);
                    if (networkId == TWITTER && !isTwitterGoing)
                        isTwitterGoing = true;
                    if (networkId == FACEBOOK && !isFacebookGoing)
                        isFacebookGoing = true;
                    try {
                        socialNetwork.requestLogin();
                    } catch (Exception e) {
                        Snackbar.with(mContext).type(SnackbarType.MULTI_LINE).text(getString(R.string.social_network_error)).show(mActivity);
                        if (socialNetwork.getID() == TWITTER)
                            isTwitterGoing = false;
                        else if (socialNetwork.getID() == FACEBOOK)
                            isFacebookGoing = false;
                    }
                }
            } else {
                showList(socialNetwork.getID());
            }
        }
    };
    public static SocialNetworkManager mSocialNetworkManager;
    private static FragmentSwitcher mFragmentSwitcher;
    private Button twitterButton;
    private Button facebookButton;
    private static Context mContext;
    private static Activity mActivity;
    private static ProgressManager mProgressManager;

    @Override
    public void onAttach(Activity activity) {
        mContext = activity;
        mActivity = activity;
        if (activity instanceof FragmentSwitcher)
            mFragmentSwitcher = (FragmentSwitcher) activity;
        else
            throw new RuntimeException("Parent activity of SocialFragment must implement FragmentSwitcher");
        if (activity instanceof ProgressManager)
            mProgressManager = ((ProgressManager) activity);
        else
            throw new RuntimeException("Parent activity of SocialFragment must implement ProgressManager");
        super.onAttach(activity);
    }

    static SocialFragment newInstance() {
        return new SocialFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_social,
                container, false);
        twitterButton = (Button) v.findViewById(R.id.twitter_button);
        twitterButton.setOnClickListener(loginClick);
        facebookButton = (Button) v.findViewById(R.id.facebook_button);
        facebookButton.setOnClickListener(loginClick);

        String TWITTER_CONSUMER_KEY = mContext.getString(R.string.twitter_consumer_key);
        String TWITTER_CONSUMER_SECRET = mContext.getString(R.string.twitter_consumer_secret);
        String TWITTER_CALLBACK_URL = "oauth://ASNE";


        ArrayList<String> fbScope = new ArrayList<String>();
        fbScope.addAll(Arrays.asList("public_profile, user_friends"));

        mSocialNetworkManager = (SocialNetworkManager) getFragmentManager().findFragmentByTag(DashboardActivity.SOCIAL_NETWORK_TAG);

        if (mSocialNetworkManager == null) {
            mSocialNetworkManager = new SocialNetworkManager();

            //Init and add to manager FacebookSocialNetwork
            FacebookSocialNetwork fbNetwork = new FacebookSocialNetwork(this, fbScope);
            mSocialNetworkManager.addSocialNetwork(fbNetwork);

            //Init and add to manager TwitterSocialNetwork
            TwitterSocialNetwork twNetwork = new TwitterSocialNetwork(this, TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET, TWITTER_CALLBACK_URL);
            mSocialNetworkManager.addSocialNetwork(twNetwork);

            //GooglePlusSocialNetwork gpNetwork = new GooglePlusSocialNetwork(this);
            //mSocialNetworkManager.addSocialNetwork(gpNetwork);

            //Initiate every network from mSocialNetworkManager
            getFragmentManager().beginTransaction().add(mSocialNetworkManager, DashboardActivity.SOCIAL_NETWORK_TAG).commit();
            mSocialNetworkManager.setOnInitializationCompleteListener(this);
        } else {
            //if manager exist - get and setup login only for initialized SocialNetworks
            if (!mSocialNetworkManager.getInitializedSocialNetworks().isEmpty()) {
                List<SocialNetwork> socialNetworks = mSocialNetworkManager.getInitializedSocialNetworks();
                for (SocialNetwork socialNetwork : socialNetworks) {
                    socialNetwork.setOnLoginCompleteListener(this);
                    try {
                        initSocialNetwork(socialNetwork);
                    } catch (Exception e) {
                        Snackbar.with(mContext).type(SnackbarType.MULTI_LINE).text(getString(R.string.social_network_error)).show(mActivity);
                        if (socialNetwork.getID() == TWITTER)
                            isTwitterGoing = false;
                        else if (socialNetwork.getID() == FACEBOOK)
                            isFacebookGoing = false;
                    }

                }
            }
        }

        return v;
    }

    private void initSocialNetwork(SocialNetwork socialNetwork) {
        if (socialNetwork.isConnected()) {
            switch (socialNetwork.getID()) {
                case FACEBOOK:
                    facebookButton.setText(mContext.getString(R.string.social_find_friends_button_uppercase));
                    break;
                case TWITTER:
                    twitterButton.setText(mContext.getString(R.string.social_find_friends_button_uppercase));
                    break;
            }
        }
    }

    @Override
    public void onSocialNetworkManagerInitialized() {
        //when init SocialNetworks - get and setup login only for initialized SocialNetworks
        for (SocialNetwork socialNetwork : mSocialNetworkManager.getInitializedSocialNetworks()) {
            socialNetwork.setOnLoginCompleteListener(this);
            initSocialNetwork(socialNetwork);
        }

    }

    @Override
    public void onLoginSuccess(int networkId) {
        if (networkId == FACEBOOK)
            isFacebookGoing = false;
        else if (networkId == TWITTER)
            isTwitterGoing = false;

        try {
            showList(networkId);
        } catch (Exception e) {
            Snackbar.with(mContext).type(SnackbarType.MULTI_LINE).text(getString(R.string.social_network_error)).show(mActivity);
            if (networkId == TWITTER)
                isTwitterGoing = false;
            else if (networkId == FACEBOOK)
                isFacebookGoing = false;
        }


    }

    @Override
    public void onError(int networkId, String requestID, String errorMessage, Object data) {
        if (networkId == FACEBOOK)
            isFacebookGoing = false;
        else if (networkId == TWITTER)
            isTwitterGoing = false;
        mProgressManager.showLoading(false);
    }

    private void showList(int networkId) {
        mProgressManager.showLoading(true);
        FriendsAddFromSocial friendsAddFromSocial = FriendsAddFromSocial.newInstance(networkId);
        mFragmentSwitcher.switchContent(friendsAddFromSocial, true);

    }

    public static class FriendsAddFromSocial extends Fragment implements OnRequestSocialPersonCompleteListener, OnRequestGetFriendsCompleteListener, FriendsSocialAdapter.FriendAddListener {

        private static final String NETWORK_ID = "NETWORK_ID";
        private int networkId;
        private ListView listView;
        private SocialNetwork socialNetwork;
        private List<UserRestResult> people;
        private FriendsSocialAdapter adapter;
        private SpiceManager _contentManager = new SpiceManager(
                ParticipactSpringAndroidService.class);
        private TextView errorTextView;
        private UserRestResult pendingFriend;

        public static FriendsAddFromSocial newInstance(int id) {
            FriendsAddFromSocial fragment = new FriendsAddFromSocial();
            Bundle args = new Bundle();
            args.putInt(NETWORK_ID, id);
            fragment.setArguments(args);
            return fragment;
        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            networkId = getArguments().containsKey(NETWORK_ID) ? getArguments().getInt(NETWORK_ID) : 0;
            people = new ArrayList<UserRestResult>();

        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_friends_add_from_social,
                    container, false);
            listView = (ListView) v.findViewById(R.id.list_add_from_social_friends);
            errorTextView = (TextView) v.findViewById(R.id.textViewError);

            socialNetwork = SocialFragment.mSocialNetworkManager.getSocialNetwork(networkId);
            socialNetwork.setOnRequestCurrentPersonCompleteListener(this);

            try {
                socialNetwork.requestCurrentPerson();

                socialNetwork.setOnRequestGetFriendsCompleteListener(this);
                socialNetwork.requestGetFriends();

            } catch (Exception e) {
                mProgressManager.showLoading(false);
                Snackbar.with(mContext).type(SnackbarType.MULTI_LINE).text(getString(R.string.social_network_error)).show(mActivity);

            }


            adapter = new FriendsSocialAdapter(mContext,
                    R.layout.friends_social_item, people, this);
            listView.setAdapter(adapter);

            return v;
        }

        @Override
        public void onRequestSocialPersonSuccess(int socialNetworkId, SocialPerson socialPerson) {
            StringBuilder stringBuilder = new StringBuilder(mContext.getString(R.string.hi));
            stringBuilder.append(" ");
            stringBuilder.append(socialPerson.name);
            Snackbar.with(mContext).text(stringBuilder.toString()).show(mActivity);
            performAddSocialPresenceRequest(socialNetworkId, socialPerson);
        }

        @Override
        public void onError(int networkId, String requestID, String errorMessage, Object data) {
            StringBuilder stringBuilder = new StringBuilder(mContext.getString(R.string.error));
            stringBuilder.append(" ");
            stringBuilder.append(errorMessage);
            Snackbar.with(mContext).text(stringBuilder.toString()).show(mActivity);
            mProgressManager.showLoading(false);
            errorTextView.setText(mContext.getString(R.string.social_network_error));
            errorTextView.setVisibility(View.VISIBLE);
        }

        @Override
        public void OnGetFriendsIdComplete(int socialNetworkId, String[] friendsID) {
            HashSet<String> friendsIds = new HashSet<String>(Arrays.asList(friendsID));
            try {
                performGetFriendsFromIds(socialNetworkId, friendsIds);
            } catch (Exception e) {
                mProgressManager.showLoading(false);
                Snackbar.with(mContext).type(SnackbarType.MULTI_LINE).text(getString(R.string.social_network_error)).show(mActivity);
            }

        }

        @Override
        public void OnGetFriendsComplete(int networkID, ArrayList<SocialPerson> socialPersons) {
            //showLoading(false);
        }

        @Override
        public void onStop() {
            if (_contentManager.isStarted()) {
                _contentManager.shouldStop();
            }
            super.onStop();
        }

        private void performGetFriendsFromIds(int socialNetworkId, Set<String> ids) {

            if (!_contentManager.isStarted()) {
                _contentManager.start(mContext);
            }

            String socialNetwork;

            if (socialNetworkId == TWITTER)
                socialNetwork = SocialPresenceGetFriendsRequest.TWITTER;
            else if (socialNetworkId == FACEBOOK)
                socialNetwork = SocialPresenceGetFriendsRequest.FACEBOOK;
            else return;

            SocialPresenceGetFriendsRequest request = new SocialPresenceGetFriendsRequest(socialNetwork, ids);
            _contentManager.execute(request, new FriendsFromSocialListener());
            mProgressManager.showLoading(true);

        }

        private void performAddSocialPresenceRequest(int socialNetworkId, SocialPerson socialPerson) {

            if (!_contentManager.isStarted()) {
                _contentManager.start(mContext);
            }

            String socialNetwork;

            if (socialNetworkId == TWITTER)
                socialNetwork = SocialPresenceAddRequest.TWITTER;
            else if (socialNetworkId == FACEBOOK)
                socialNetwork = SocialPresenceAddRequest.FACEBOOK;
            else return;

            SocialPresenceAddRequest request = new SocialPresenceAddRequest(socialPerson.id, socialNetwork);

            _contentManager.execute(request, new AddSocialPresenceListener());
            mProgressManager.showLoading(true);

        }

        @Override
        public void onFriendAdd(int position) {

            pendingFriend = people.get(position);

            FriendsPostRequest request = new FriendsPostRequest(people
                    .get(position).getId(), FriendsPostRequest.PENDING);

            _contentManager.execute(request, new FriendsAddListener());
            mProgressManager.showLoading(true);

        }

        private class FriendsFromSocialListener implements RequestListener<UserRestResultList> {

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                mProgressManager.showLoading(false);
                errorTextView.setText(getString(R.string.network_error));
                errorTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onRequestSuccess(UserRestResultList userRestResults) {
                mProgressManager.showLoading(false);

                if (userRestResults.size() == 0) {
                    errorTextView.setText(R.string.no_data_friends);
                    errorTextView.setVisibility(View.VISIBLE);
                    return;
                }

                people.clear();
                people.addAll(userRestResults);
                Collections.sort(people);
                adapter.notifyDataSetChanged();

            }
        }

        private class FriendsAddListener implements RequestListener<Boolean> {

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                mProgressManager.showLoading(false);

            }

            @Override
            public void onRequestSuccess(Boolean result) {
                mProgressManager.showLoading(false);
                if (result) {
                    Snackbar.with(mContext).text(mContext.getString(R.string.social_fragment_success_request)).show(mActivity);
                    if (pendingFriend != null) {
                        people.remove(pendingFriend);
                        if (people.size() == 0) {
                            errorTextView.setText(R.string.no_data_friends);
                            errorTextView.setVisibility(View.VISIBLE);
                        }
                        adapter.notifyDataSetChanged();
                        pendingFriend = null;
                    }
                }

            }
        }

        private class AddSocialPresenceListener implements RequestListener<Boolean> {

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                //showLoading(false);
            }

            @Override
            public void onRequestSuccess(Boolean result) {
                //showLoading(false);

                if (result) {
                    Snackbar.with(mContext).text(mContext.getString(R.string.social_fragment_success_link_account)).show(mActivity);
                } else {
                    Snackbar.with(mContext).text(mContext.getString(R.string.social_fragment_fail_link_account)).show(mActivity);
                }

            }
        }

    }


}
