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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.GregorianCalendar;

import it.unibo.participact.ParticipActService;
import it.unibo.participact.R;
import it.unibo.participact.activities.interfaces.FragmentSwitcher;
import it.unibo.participact.activities.interfaces.ProgressManager;
import it.unibo.participact.broadcastreceivers.DailyNotificationBroadcastReceiver;
import it.unibo.participact.broadcastreceivers.UploadCollectedGeobadgeReceiver;
import it.unibo.participact.domain.local.ImageDescriptor;
import it.unibo.participact.domain.persistence.StateUtility;
import it.unibo.participact.domain.persistence.support.ExportDatabase;
import it.unibo.participact.fragments.AboutFragment;
import it.unibo.participact.fragments.ConsoleFragment;
import it.unibo.participact.fragments.FAQFragment;
import it.unibo.participact.fragments.FriendsFragment;
import it.unibo.participact.fragments.LeaderBoardFragment;
import it.unibo.participact.fragments.StatsFragment;
import it.unibo.participact.fragments.TaskActiveFragment;
import it.unibo.participact.fragments.TaskAvailableFragment;
import it.unibo.participact.fragments.TaskCreatedFragment;
import it.unibo.participact.fragments.TaskHistoryFragment;
import it.unibo.participact.fragments.UserFragment;
import it.unibo.participact.fragments.WelcomeFragment;
import it.unibo.participact.network.request.GCMRegisterRequest;
import it.unibo.participact.network.request.GCMRegisterRequestListener;
import it.unibo.participact.network.request.ParticipactSpringAndroidService;
import it.unibo.participact.questionnaire.QuestionnaireActivity;
import it.unibo.participact.support.Configuration;
import it.unibo.participact.support.ImageDescriptorUtility;
import it.unibo.participact.support.ViewUtils;
import it.unibo.participact.support.preferences.DataUploaderPhotoPreferences;
import it.unibo.participact.support.preferences.DataUploaderQuestionnairePreferences;
import it.unibo.participact.support.preferences.UserAccountPreferences;
import it.unibo.participact.views.adapters.DrawerMenuAdapter;

public class DashboardActivity extends ActionBarActivity implements FragmentSwitcher, ProgressManager,
        ListView.OnItemClickListener {

    public static final String GO_TO_TASK_ACTIVE_FRAGMENT = "GO_TO_TASK_ACTIVE_FRAGMENT";
    public static final String GO_TO_TASK_AVAILABLE_FRAGMENT = "GO_TO_TASK_AVAILABLE_FRAGMENT";
    public static final String GO_TO_PROFILE_FRAGMENT = "GO_TO_PROFILE_FRAGMENT";
    public static final String GO_TO_FRIENDS_FRAGMENT = "GO_TO_FRIENDS_FRAGMENT";
    public static final String GO_TO_APRROVED_TASK_CREATED_FRAGMENT = "GO_TO_APRROVED_TASK_CREATED_FRAGMENT";
    public static final String GO_TO_REFUSED_TASK_CREATED_FRAGMENT = "GO_TO_REFUSED_TASK_CREATED_FRAGMENT";

    public static final String SOCIAL_NETWORK_TAG = "SocialIntegrationMain.SOCIAL_NETWORK_TAG";
    private static final String KEY_CURRENT_FRAGMENT = "currentFragment";
    private static final Logger logger = LoggerFactory
            .getLogger(DashboardActivity.class);
    private SpiceManager _contentManager = new SpiceManager(
            ParticipactSpringAndroidService.class);

    private Fragment _currentFragment;
    private String[] mMenuTitles;
    private DrawerMenuAdapter mMenuAdapter;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private ProgressBar progressBar;
    private BroadcastReceiver mBroadcastReceiverGoToActiveTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        setTitle("ParticipAct");

        ExportDatabase.exportDB(this);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMenuTitles = getResources().getStringArray(R.array.menu_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mMenuAdapter = new DrawerMenuAdapter(this,
                R.layout.drawer_nav_item_layout, mMenuTitles);

        // Set the adapter for the list view
        mDrawerList.setAdapter(mMenuAdapter);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(this);


        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                toolbar, /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open, /* "open drawer" description */
                R.string.drawer_close /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                // getSupportActionBar().setTitle("ParticipAct");
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // getSupportActionBar().setTitle("ParticipAct");
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // set the Above View
        boolean goToSpecificFragment = false;
        Intent comingIntent = getIntent();
        if (comingIntent != null) {
            String comingAction = getIntent().getAction();
            if (comingAction != null) {
                if (comingAction.equals(GO_TO_TASK_ACTIVE_FRAGMENT)) {
                    setTitle(mMenuTitles[3]);
                    switchContent(new TaskActiveFragment(), false);
                    goToSpecificFragment = true;
                } else if (comingAction.equals(GO_TO_TASK_AVAILABLE_FRAGMENT)) {
                    setTitle(mMenuTitles[4]);
                    switchContent(new TaskAvailableFragment(), false);
                    goToSpecificFragment = true;
                } else if (comingAction.equals(GO_TO_PROFILE_FRAGMENT)) {
                    setTitle(mMenuTitles[7]);
                    switchContent(UserFragment.newInstance(UserFragment.ME_ID, UserFragment.BADGES_INDEX), false);
                    goToSpecificFragment = true;
                } else if (comingAction.equals(GO_TO_FRIENDS_FRAGMENT)) {
                    setTitle(mMenuTitles[9]);
                    switchContent(FriendsFragment.newInstance(FriendsFragment.PENDING_INDEX), false);
                    goToSpecificFragment = true;
                } else if (comingAction.equals(GO_TO_APRROVED_TASK_CREATED_FRAGMENT)) {
                    setTitle(mMenuTitles[2]);
                    switchContent(TaskCreatedFragment.newInstance(TaskCreatedFragment.ACCEPTED_TASK_INDEX), false);
                    goToSpecificFragment = true;
                } else if (comingAction.equals(GO_TO_REFUSED_TASK_CREATED_FRAGMENT)) {
                    setTitle(mMenuTitles[2]);
                    switchContent(TaskCreatedFragment.newInstance(TaskCreatedFragment.REFUSED_TASK_INDEX), false);
                    goToSpecificFragment = true;

                }

            }
        }

        if (!goToSpecificFragment) {

            if (savedInstanceState != null) {
                _currentFragment = getSupportFragmentManager().getFragment(
                        savedInstanceState, KEY_CURRENT_FRAGMENT);
            }
            if (_currentFragment == null) {
                switchContent(new WelcomeFragment(), false);
            }
        }
        Intent i = new Intent(this, ParticipActService.class);
        i.setAction(ParticipActService.START);
        startService(i);

        // check if gcm is sync to server or expired
        boolean isSetOnServer = UserAccountPreferences.getInstance(
                getApplicationContext()).isGCMSetOnServer();
        long expirationTime = UserAccountPreferences.getInstance(
                getApplicationContext()).getgcmOnServerExpirationTime();

        if (!isSetOnServer || System.currentTimeMillis() > expirationTime) {
            logger.info(
                    "Registering gcmid, isSetOnServer={}  expirationTime={}.",
                    isSetOnServer, expirationTime);
            registerGCMBackground();
        }

        // Schedule alarm at 12 or 18
        Calendar current = new GregorianCalendar();
        current.setTimeInMillis(System.currentTimeMillis());

        if (current.get(Calendar.HOUR_OF_DAY) <= 18) {

            Calendar cal = new GregorianCalendar();
            cal.set(Calendar.MINUTE, 10);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            //noinspection ResourceType
            cal.set(current.get(Calendar.YEAR), current.get(Calendar.MONTH),
                    current.get(Calendar.DAY_OF_MONTH));

            if (current.get(Calendar.HOUR_OF_DAY) <= 12) {
                cal.set(Calendar.HOUR_OF_DAY, 12);
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 18);
            }



            Intent intent = new Intent();
            intent.setAction(DailyNotificationBroadcastReceiver.DAILY_NOTIFICATION_INTENT);
            PendingIntent pendingIntent = PendingIntent
                    .getBroadcast(
                            getApplicationContext(),
                            DailyNotificationBroadcastReceiver.DAILY_NOTIFICATION_REQUEST_CODE,
                            intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarm = (AlarmManager) getApplicationContext()
                    .getSystemService(Context.ALARM_SERVICE);
            alarm.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                    pendingIntent);
            logger.info("Daily notification alarm setted at {}:{}",
                    cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE));



            //            /**************DEBUG************/
//            Calendar time = new GregorianCalendar();
//            time.setTimeInMillis(System.currentTimeMillis());
//            cal.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
//            cal.set(Calendar.MINUTE, time.get(Calendar.MINUTE) + 2);
//            /**************DEBUG************/
            Intent intentCollected = new Intent();
            intentCollected.setAction(UploadCollectedGeobadgeReceiver.DAILY_UPLOAD_COLLECTEDGEOBADGE_INTENT);
            PendingIntent pendingCollectedGeobadgeIntent= PendingIntent
                    .getBroadcast(
                            getApplicationContext(),
                            UploadCollectedGeobadgeReceiver.DAILY_UPLOAD_COLLECTEDGEOBADGE_REQUEST_CODE,
                            intentCollected, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmcollected = (AlarmManager) getApplicationContext()
                    .getSystemService(Context.ALARM_SERVICE);

            alarmcollected.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                    pendingCollectedGeobadgeIntent);

            logger.info("Daily update alarm setted at {}:{}", cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE));


            //Create Receiver used to switch to ActiveTaskFragment

            if (mBroadcastReceiverGoToActiveTask == null) {
                mBroadcastReceiverGoToActiveTask = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        switchContent(new TaskActiveFragment(), true);
                    }
                };
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        //getSupportFragmentManager().putFragment(state, KEY_CURRENT_FRAGMENT,_currentFragment);
    }

    @Override
    public void onResume() {
        showLoading(false);
        super.onResume();
        if (mBroadcastReceiverGoToActiveTask == null) {
            mBroadcastReceiverGoToActiveTask = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switchContent(new TaskActiveFragment(), true);
                }
            };
        }
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(mBroadcastReceiverGoToActiveTask, new IntentFilter(GO_TO_TASK_ACTIVE_FRAGMENT));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(mBroadcastReceiverGoToActiveTask);
        super.onPause();
    }

    @Override
    public void switchContent(Fragment fragment, boolean addToBack) {
        showLoading(false);
        if (!getSupportActionBar().isShowing())
            getSupportActionBar().show();
        _currentFragment = fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right).replace(R.id.content_frame, fragment);
        if (addToBack)
            transaction.addToBackStack(null);
        transaction.commit();
    }

    private void registerGCMBackground() {
        GCMRegisterRequest request = new GCMRegisterRequest(this);
        String lastRequestCacheKey = request.createCacheKey();
        if (!_contentManager.isStarted()) {
            _contentManager.start(this);
        }
        _contentManager
                .execute(request, lastRequestCacheKey,
                        DurationInMillis.ONE_HOUR,
                        new GCMRegisterRequestListener(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Configuration.PHOTO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                ImageDescriptor imgD = ImageDescriptorUtility
                        .loadImageDescriptor(this, "temp.ids");
                if (imgD != null) {
                    ImageDescriptorUtility.renameFile(this, "temp.ids",
                            imgD.getImageName() + ".ids");
                    StateUtility.incrementPhotoProgress(this, imgD.getTaskId(),
                            imgD.getActionId());
                    Fragment fragment = getSupportFragmentManager()
                            .findFragmentById(R.id.content_frame);
                    if (fragment instanceof TaskActiveFragment) {
                        ((TaskActiveFragment) fragment).refresh();
                    }
                    logger.info(
                            "Successfully stored new photo {} of taskId {} and actionId {}.",
                            imgD.getImageName(), imgD.getTaskId(),
                            imgD.getActionId());
                }
            } else {
                ImageDescriptor imgD = ImageDescriptorUtility
                        .loadImageDescriptor(this, "temp.ids");
                ImageDescriptorUtility.deleteImageDescriptor(this, "temp.ids");
                logger.info(
                        "Failed to store photo {} of taskId {} and actionId {}.",
                        imgD.getImageName(), imgD.getTaskId(),
                        imgD.getActionId());
            }

            DataUploaderPhotoPreferences.getInstance(this).setPhotoUpload(true);
        }

        if (requestCode == Configuration.QUESTIONNAIRE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Long taskId = data.getLongExtra(
                        QuestionnaireActivity.EXTRA_TASK_ID, -1);
                Long actionId = data.getLongExtra(
                        QuestionnaireActivity.EXTRA_ACTION_ID, -1);
                if (taskId != -1 && actionId != -1) {
                    StateUtility.incrementQuestionnaireProgress(this, taskId,
                            actionId);
                    Fragment fragment = getSupportFragmentManager()
                            .findFragmentById(R.id.content_frame);
                    if (fragment instanceof TaskActiveFragment) {
                        ((TaskActiveFragment) fragment).refresh();
                    }
                    logger.info(
                            "Successfully completed questionnaire of taskId {} and actionId {}.",
                            taskId, actionId);
                    DataUploaderQuestionnairePreferences.getInstance(this)
                            .setQuestionnaireUpload(true);
                }
            }
            logger.info("Questionnaire failed or cancelled.");
        } else {
            Fragment fragmentSocial = getSupportFragmentManager().findFragmentByTag(SOCIAL_NETWORK_TAG);
            if (fragmentSocial != null) {
                fragmentSocial.onActivityResult(requestCode, resultCode, data);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

        Fragment newFragment = null;
        switch (position) {
            case 0:
                newFragment = new WelcomeFragment();
                break;
            case 1:
                mMenuAdapter.toggleTaskSubMenu();
                mMenuAdapter.setSelected(view, position);
                setTitle(mMenuTitles[position]);
                break;
            case 2:
                newFragment = new TaskCreatedFragment();
                break;
            case 3:
                newFragment = new TaskActiveFragment();
                break;
            case 4:
                newFragment = new TaskAvailableFragment();
                break;
            case 5:
                newFragment = new TaskHistoryFragment();
                break;
            case 6:
                newFragment = new ConsoleFragment();
                break;
            case 7:
                newFragment = UserFragment.newInstance(UserFragment.ME_ID);
                break;
            case 8:
                newFragment = new LeaderBoardFragment();
                break;
            case 9:
                newFragment = new FriendsFragment();
                break;
            case 10:
                newFragment = new StatsFragment();
                break;
            case 11:
                newFragment = new FAQFragment();
                break;
            case 12:
                newFragment = new AboutFragment();
                break;
            default:
                break;
        }
        if (newFragment != null) {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            switchContent(newFragment, false);
            mDrawerList.setItemChecked(position, true);
            setTitle(mMenuTitles[position]);
            mMenuAdapter.setSelected(view, position);
            mDrawerLayout.closeDrawer(mDrawerList);
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showLoading(boolean value) {
        ViewUtils.toggleAlpha(progressBar, value);

    }
}
