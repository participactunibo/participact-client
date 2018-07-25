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
import android.net.ConnectivityManager;
import android.net.MailTo;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ObservableWebView;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import it.unibo.participact.R;
import it.unibo.participact.activities.interfaces.ProgressManager;
import it.unibo.participact.support.ViewUtils;

/**
 * Created by danielecampogiani on 31/01/15.
 */
public class FAQFragment extends Fragment {

    private static Context mContext;
    private static ActionBarActivity mActivity;
    private static ProgressManager mProgressManager;
    private ObservableWebView mObservableWebView;
    private TextView errorTextView;


    @Override
    public void onAttach(Activity activity) {
        mContext = activity;
        if (activity instanceof ActionBarActivity)
            mActivity = (ActionBarActivity) activity;
        else
            throw new RuntimeException("Parent activity of FAQFragment must extend ActionBarActivity");
        if (activity instanceof ProgressManager)
            mProgressManager = ((ProgressManager) activity);
        else
            throw new RuntimeException("Parent activity of FAQFragment must implement ProgressManager");
        super.onAttach(activity);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_faq, container, false);
        mObservableWebView = (ObservableWebView) root.findViewById(R.id.web_view);
        WebSettings webSettings = mObservableWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mObservableWebView.setWebViewClient(new WebViewClient());
        errorTextView = (TextView) root.findViewById(R.id.textViewError);
        mObservableWebView.setScrollViewCallbacks(new ObservableScrollViewCallbacks() {
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
        mObservableWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                ViewUtils.toggleError(errorTextView, null, false);
                if (progress == 0)
                    mProgressManager.showLoading(true);
                else if (progress == 100)
                    mProgressManager.showLoading(false);
            }

        });

        mObservableWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                ViewUtils.toggleAlpha(mObservableWebView, false);
                ViewUtils.toggleError(errorTextView, getString(R.string.network_error), true);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("mailto:")) {
                    MailTo mt = MailTo.parse(url);
                    Intent i = newEmailIntent(mContext, mt.getTo(), "ParticAct Support from mobile app", "", "");
                    startActivity(i);
                    view.reload();
                    return true;
                } else {
                    view.loadUrl(url);
                }
                return true;
            }

        });

        if (isNetworkAvailable())
            mObservableWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        else
            mObservableWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        mObservableWebView.loadUrl(getString(R.string.faq_url));

        return root;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static Intent newEmailIntent(Context context, String address, String subject, String body, String cc) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_CC, cc);
        intent.setType("message/rfc822");
        return intent;
    }

}
