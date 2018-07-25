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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import it.unibo.participact.R;
import it.unibo.participact.activities.interfaces.FragmentSwitcher;
import it.unibo.participact.activities.interfaces.ProgressManager;
import it.unibo.participact.fragments.CreateTaskFragment;


public class CreateTaskActivity extends ActionBarActivity implements FragmentSwitcher, ProgressManager {

    public static final String KEY_NEW_QUESTION_PARCELABLE = "currentNewQuestionRequest";
    private ProgressBar progressBar;
    FrameLayout frameLayout;
    private Fragment _currentFragment;
    private static final String KEY_CURRENT_FRAGMENT = "currentFragment";
    public static final String KEY_NEW_TASKFLAT_PARCELABLE = "currentNewTaskflatRequest";
    public static final String KEY_NEW_ACTION_PARCELABLE = "currentNewActionRequest";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        frameLayout = (FrameLayout) findViewById(R.id.content_frame);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (savedInstanceState != null) {
            _currentFragment = getSupportFragmentManager().getFragment(
                    savedInstanceState, KEY_CURRENT_FRAGMENT);
            //     newTaskFlatRequest = Parcels.unwrap(savedInstanceState.getParcelable(KEY_NEW_TASKFLAT_PARCELABLE));
        }
        if (_currentFragment == null) {
            switchContent(CreateTaskFragment.newInstance(this), false);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    @Override
    public void switchContent(Fragment fragment, boolean addToBack) {
        showLoading(false);
        _currentFragment = fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right).replace(R.id.content_frame, fragment);
        if (addToBack)
            transaction.addToBackStack(null);
        transaction.commit();
    }


    @Override
    public void showLoading(boolean value) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime) / 2;

        final boolean show = value;
        progressBar.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                        frameLayout.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });
    }


    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
