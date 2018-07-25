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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fima.cardsui.objects.CardStack;
import com.fima.cardsui.views.CardUI;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import it.unibo.participact.R;
import it.unibo.participact.activities.interfaces.ProgressManager;
import it.unibo.participact.domain.rest.TwitterStatus;
import it.unibo.participact.domain.rest.TwitterStatusList;
import it.unibo.participact.network.request.ParticipactSpringAndroidService;
import it.unibo.participact.network.request.TwitterRequest;
import it.unibo.participact.views.cards.NewsCard;
import it.unibo.participact.views.cards.WelcomeCard;

public class WelcomeFragment extends Fragment {

    private static final String KEY_LAST_REQUEST_CACHE_KEY = "WelcomeResquest";

    private SpiceManager _contentManager = new SpiceManager(ParticipactSpringAndroidService.class);
    private CardUI _cardUI;
    private String _lastRequestCacheKey;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_card, container, false);
        _cardUI = (CardUI) root.findViewById(R.id.cardUIView);

        WelcomeCard welcome = new WelcomeCard(getActivity());
        _cardUI.addCard(welcome);
        _cardUI.refresh();

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //restore state
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_LAST_REQUEST_CACHE_KEY)) {
                _lastRequestCacheKey = savedInstanceState.getString(KEY_LAST_REQUEST_CACHE_KEY);
//	            _contentManager.addListenerIfPending( TwitterStatusList.class, _lastRequestCacheKey, new TwitterListener() );
                _contentManager.getFromCache(TwitterStatusList.class, _lastRequestCacheKey, DurationInMillis.ONE_MINUTE * 5, new TwitterListener());
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
            outState.putString(KEY_LAST_REQUEST_CACHE_KEY, _lastRequestCacheKey);
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
            _contentManager.start(getActivity());
        }

        TwitterRequest request = new TwitterRequest(getActivity());
        _lastRequestCacheKey = request.createCacheKey();
        _contentManager.execute(request, _lastRequestCacheKey, DurationInMillis.ONE_MINUTE * 5, new TwitterListener());
        _cardUI.clearCards();
        _cardUI.addCard(new WelcomeCard(getActivity()));
        showLoading(true);
    }


    private class TwitterListener implements RequestListener<TwitterStatusList> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            showLoading(false);

        }

        @Override
        public void onRequestSuccess(TwitterStatusList result) {
            showLoading(false);


            if (result != null) {
                DateTimeFormatter formatter = DateTimeFormat.forPattern("d/MM");
                CardStack stack = new CardStack();
                if (result.size() >= 3) {
                    for (int i = 2; i >= 0; i--) {
                        TwitterStatus status = result.get(i);
                        stack.add(new NewsCard(getActivity(), getString(R.string.news_of) + " " + formatter.print(status.getCreatedAt()), status.getText()));
                    }
                }
                _cardUI.addStack(stack);
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
