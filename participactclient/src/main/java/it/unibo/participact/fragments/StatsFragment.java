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

import com.fima.cardsui.views.CardUI;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.Properties;

import it.unibo.participact.R;
import it.unibo.participact.activities.interfaces.ProgressManager;
import it.unibo.participact.domain.rest.StatisticsMessage;
import it.unibo.participact.network.request.ParticipactSpringAndroidService;
import it.unibo.participact.network.request.StatisticsRequest;
import it.unibo.participact.support.DialogFactory;
import it.unibo.participact.support.LoginUtility;
import it.unibo.participact.views.cards.StatisticCard;
import it.unibo.participact.views.cards.StatisticsFirstCard;

public class StatsFragment extends Fragment {

    private static final String KEY_LAST_REQUEST_CACHE_KEY = "statisticsLastRequestCacheKey";
    private SpiceManager _contentManager = new SpiceManager(ParticipactSpringAndroidService.class);
    private String _lastRequestCacheKey;

    private CardUI _cardUI;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (!_contentManager.isStarted()) {
            _contentManager.start(getActivity());
        }

        View root = inflater.inflate(R.layout.fragment_card, container, false);
        _cardUI = (CardUI) root.findViewById(R.id.cardUIView);

        StatisticsFirstCard first = new StatisticsFirstCard(getActivity());
        _cardUI.addCard(first);

        // MyCard user = new MyCard("Attenzione",
        // "L'applicazione è ancora in fase beta. Supporterà questa funzione al più presto.");
        // _cardUI.addCard(user);

        _cardUI.refresh();

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // restore state
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_LAST_REQUEST_CACHE_KEY)) {
                _lastRequestCacheKey = savedInstanceState.getString(KEY_LAST_REQUEST_CACHE_KEY);
                _contentManager.addListenerIfPending(StatisticsMessage.class, _lastRequestCacheKey,
                        new StatisticsRequestListener());
                _contentManager.getFromCache(StatisticsMessage.class, _lastRequestCacheKey,
                        DurationInMillis.ALWAYS_EXPIRED, new StatisticsRequestListener());
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
        StatisticsRequest request = new StatisticsRequest(getActivity());
        _lastRequestCacheKey = request.createCacheKey();
        _contentManager.execute(request, _lastRequestCacheKey, DurationInMillis.ALWAYS_EXPIRED,
                new StatisticsRequestListener());
        _cardUI.clearCards();
        _cardUI.addCard(new StatisticsFirstCard(getActivity()));
        showLoading(true);
    }

    private class StatisticsRequestListener implements RequestListener<StatisticsMessage> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            if (getActivity() != null) {
                showLoading(false);
                if (LoginUtility.checkIfLoginException(getActivity().getApplicationContext(),
                        spiceException)) {

                } else {
                    DialogFactory.showCommunicationErrorWithServer(getActivity());
                }
            }
        }

        @Override
        public void onRequestSuccess(StatisticsMessage result) {
            if (getActivity() != null) {
                showLoading(false);
                if (result != null) {
                    for (Properties stat : result.getList()) {
                        StatisticCard card = new StatisticCard(stat);
                        _cardUI.addCard(card);
                    }
                    _cardUI.refresh();
                }
            }
        }

    }

    private void showLoading(boolean value) {
        Activity activity = getActivity();
        if (activity != null && activity instanceof ProgressManager)
            ((ProgressManager) activity).showLoading(value);

    }

}
