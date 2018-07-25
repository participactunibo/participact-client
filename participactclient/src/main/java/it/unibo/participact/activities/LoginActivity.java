/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.networkstate.DefaultNetworkStateChecker;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.apache.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import it.unibo.participact.R;
import it.unibo.participact.domain.local.UserAccount;
import it.unibo.participact.network.request.GCMRegisterRequest;
import it.unibo.participact.network.request.GCMRegisterRequestListener;
import it.unibo.participact.network.request.LoginRequest;
import it.unibo.participact.network.request.ParticipactSpringAndroidService;
import it.unibo.participact.support.ViewUtils;
import it.unibo.participact.support.preferences.UserAccountPreferences;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {


    private static final String KEY_LAST_REQUEST_CACHE_KEY = "loginlastRequestCacheKey";


    private SpiceManager _contentManager = new SpiceManager(ParticipactSpringAndroidService.class);
    NetworkStateChecker networkChecker;

    private String _lastRequestCacheKey;

    // Values for email and password at the time of the login attempt.
    private String _email;
    private String _password;

    // UI references.
    private EditText _emailView;
    private EditText _passwordView;
    private View _loginFormView;
    private View _loginStatusView;
    private TextView _loginStatusMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );
        setContentView(R.layout.activity_login);

        networkChecker = new DefaultNetworkStateChecker();

        // Set up the login form.
        _email = "";
        _emailView = (EditText) findViewById(R.id.email);
        _emailView.setText(_email);

        _passwordView = (EditText) findViewById(R.id.password);
        _passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        _loginFormView = findViewById(R.id.login_form);
        _loginStatusView = findViewById(R.id.login_status);
        _loginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		super.onCreateOptionsMenu(menu);
//		getMenuInflater().inflate(R.menu.activity_login, menu);
//		return true;
//	}

    @Override
    protected void onStart() {
        super.onStart();
        if (!_contentManager.isStarted()) {
            _contentManager.start(this);
        }
    }

    @Override
    protected void onStop() {
        if (_contentManager.isStarted()) {
            _contentManager.shouldStop();
        }
        super.onStop();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {

        // Reset errors.
        _emailView.setError(null);
        _passwordView.setError(null);

        // Store values at the time of the login attempt.
        _email = _emailView.getText().toString();
        _password = _passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(_password)) {
            _passwordView.setError(getString(R.string.error_field_required));
            focusView = _passwordView;
            cancel = true;
        } else if (_password.length() < 4) {
            _passwordView.setError(getString(R.string.error_invalid_password));
            focusView = _passwordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(_email)) {
            _emailView.setError(getString(R.string.error_field_required));
            focusView = _emailView;
            cancel = true;
        } else if (!_email.contains("@")) {
            _emailView.setError(getString(R.string.error_invalid_email));
            focusView = _emailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            _loginStatusMessageView.setText(R.string.login_progress_signing_in);
            ViewUtils.toggleAlpha(_loginStatusView, true);
            ViewUtils.toggleAlpha(_loginFormView, false);

            // login request
            performRequest(_email, _password);

        }
    }

    private void performRequest(String user, String password) {
        // MainActivity.this.setSupportProgressBarIndeterminateVisibility( true );

        LoginRequest request = new LoginRequest(user, password);
        _lastRequestCacheKey = request.createCacheKey();
        _contentManager.execute(request, _lastRequestCacheKey, DurationInMillis.ALWAYS_EXPIRED,
                new LoginRequestListener());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (!TextUtils.isEmpty(_lastRequestCacheKey)) {
            outState.putString(KEY_LAST_REQUEST_CACHE_KEY, _lastRequestCacheKey);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(KEY_LAST_REQUEST_CACHE_KEY)) {
            _lastRequestCacheKey = savedInstanceState.getString(KEY_LAST_REQUEST_CACHE_KEY);
            _contentManager.addListenerIfPending(Boolean.class, _lastRequestCacheKey, new LoginRequestListener());
            _contentManager.getFromCache(Boolean.class, _lastRequestCacheKey, DurationInMillis.ONE_MINUTE,
                    new LoginRequestListener());
        }
    }

    private void registerGCMBackground() {

        GCMRegisterRequest request = new GCMRegisterRequest(this);
        _lastRequestCacheKey = request.createCacheKey();
        if (!_contentManager.isStarted()) {
            _contentManager.start(this);
        }
        _contentManager.execute(request, _lastRequestCacheKey, DurationInMillis.ONE_HOUR, new GCMRegisterRequestListener(getApplicationContext()));
    }


    private class LoginRequestListener implements RequestListener<Boolean> {
        @Override
        public void onRequestFailure(SpiceException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setTitle(R.string.error_login_dialog_title);


            if (e.getCause() instanceof HttpClientErrorException) {
                if (((HttpClientErrorException) e.getCause()).getStatusCode().value() == HttpStatus.SC_UNAUTHORIZED) {
                    builder.setMessage(R.string.error_login_dialog_bad_credential);
                }
            } else {
                builder.setMessage(R.string.error_login_dialog_generic_error);
            }

            builder.setPositiveButton(R.string.error_login_dialog_ok_button, new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.create().show();
            ViewUtils.toggleAlpha(_loginStatusView, false);
            ViewUtils.toggleAlpha(_loginFormView, true);
        }

        @Override
        public void onRequestSuccess(Boolean result) {
            if (result == null) {
                return;
            }
            Toast.makeText(LoginActivity.this, getString(R.string.success_login), Toast.LENGTH_LONG).show();
            if (result) {
                UserAccount account = new UserAccount();
                account.setUsername(_email);
                account.setPassword(_password);
                UserAccountPreferences.getInstance(getApplicationContext()).saveUserAccount(account);
                // register gcm
                registerGCMBackground();
                Intent i = new Intent(getApplicationContext(), DashboardActivity.class);
                startActivity(i);
            }
            ViewUtils.toggleAlpha(_loginStatusView, false);
            ViewUtils.toggleAlpha(_loginFormView, false);
        }
    }


}
