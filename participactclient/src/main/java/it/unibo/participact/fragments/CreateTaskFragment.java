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

import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckedTextView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;
import com.marvinlabs.widget.floatinglabel.instantpicker.DatePickerFragment;
import com.marvinlabs.widget.floatinglabel.instantpicker.FloatingLabelDatePicker;
import com.marvinlabs.widget.floatinglabel.instantpicker.FloatingLabelInstantPicker;
import com.marvinlabs.widget.floatinglabel.instantpicker.FloatingLabelTimePicker;
import com.marvinlabs.widget.floatinglabel.instantpicker.Instant;
import com.marvinlabs.widget.floatinglabel.instantpicker.InstantPickerListener;
import com.marvinlabs.widget.floatinglabel.instantpicker.JavaDateInstant;
import com.marvinlabs.widget.floatinglabel.instantpicker.JavaTimeInstant;
import com.marvinlabs.widget.floatinglabel.instantpicker.TimePickerFragment;
import com.nispok.snackbar.Snackbar;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.parceler.Parcels;

import java.util.HashSet;

import it.unibo.participact.R;
import it.unibo.participact.activities.CreateTaskActivity;
import it.unibo.participact.activities.interfaces.FragmentSwitcher;
import it.unibo.participact.activities.interfaces.ProgressManager;
import it.unibo.participact.domain.persistence.Task;
import it.unibo.participact.domain.rest.ActionFlatRequest;
import it.unibo.participact.domain.rest.TaskFlat;
import it.unibo.participact.domain.rest.TaskFlatRequest;
import it.unibo.participact.network.request.ParticipactSpringAndroidService;
import it.unibo.participact.network.request.TaskCreatePostRequest;
import it.unibo.participact.support.preferences.ShowTipsPreferences;
import it.unibo.participact.views.floating_duration.DurationPickerListener;
import it.unibo.participact.views.floating_duration.DurationTimePickerFragment;
import it.unibo.participact.views.floating_duration.FloatingLabelBaseDurationPicker;
import it.unibo.participact.views.floating_duration.FloatingLabelDurationPicker;

/**
 * Created by alessandro on 13/11/14.
 */
public class CreateTaskFragment extends Fragment implements InstantPickerListener, FloatingLabelInstantPicker.OnInstantPickerEventListener {


    private ObservableScrollView scroll;
    private FloatingActionButton fab;
    private FloatingLabelEditText nameEditText;
    private FloatingLabelEditText descriptionEditText;
    private FloatingLabelDurationPicker durationTimePicker;
    private FloatingLabelDatePicker startDatePicker;
    private FloatingLabelTimePicker startTimePicker;
    private FloatingLabelDatePicker endDatePicker;
    private FloatingLabelTimePicker endTimePicker;
    private CheckedTextView refusableCheckTextView;


    private int lastScrollPosition;
    private boolean animating = false;


    TaskFlatRequest newTaskFlatRequest;
    private FragmentSwitcher fragmentSwitcher;
    TaskCreatePostRequest request;
    private ActionBarActivity myContext;


    @Override
    public void onAttach(Activity activity) {
        myContext = (ActionBarActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        myContext.getSupportActionBar().setTitle(getResources().getString(R.string.create_task_activity_title));


        Bundle args = getArguments();
        if (args != null)
            newTaskFlatRequest = Parcels.unwrap(args.getParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE));
        else if (savedInstanceState != null) {
            newTaskFlatRequest = Parcels.unwrap(savedInstanceState.getParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE));
        } else {
            newTaskFlatRequest = new TaskFlatRequest();
            newTaskFlatRequest.setActions(new HashSet<ActionFlatRequest>());
            newTaskFlatRequest.setType(Task.class.getSimpleName());

        }
    }





    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment f = null;

        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            case R.id.action_actions:
                updateTask();
                f = ActionsFragment.newInstance(newTaskFlatRequest, fragmentSwitcher);
                fragmentSwitcher.switchContent(f, true);
                break;


        }

        return super.onOptionsItemSelected(item);
    }

    private void updateTask() {
        if (nameEditText.getInputWidgetText().toString() != null)
            newTaskFlatRequest.setName(nameEditText.getInputWidgetText().toString());
        if (descriptionEditText.getInputWidgetText().toString() != null)
            newTaskFlatRequest.setDescription(descriptionEditText.getInputWidgetText().toString());
        if (startDatePicker.getSelectedInstant() != null && startTimePicker.getSelectedInstant() != null) {
            JavaDateInstant date = (JavaDateInstant) startDatePicker.getSelectedInstant();
            JavaTimeInstant time = (JavaTimeInstant) startTimePicker.getSelectedInstant();
            DateTime jodaDate = new DateTime(date.getYear(), date.getMonthOfYear() + 2, date.getDayOfMonth(), time.getHourOfDay(), time.getMinuteOfHour(), time.getSecondOfMinute());
            newTaskFlatRequest.setStart(jodaDate);
        }
        if (endDatePicker.getSelectedInstant() != null) {
            JavaDateInstant date = (JavaDateInstant) endDatePicker.getSelectedInstant();
            JavaTimeInstant time = (JavaTimeInstant) endTimePicker.getSelectedInstant();

            DateTime jodaDate = new DateTime(date.getYear(), date.getMonthOfYear() + 2, date.getDayOfMonth(), time.getHourOfDay(), time.getMinuteOfHour(), time.getSecondOfMinute());
            newTaskFlatRequest.setDeadline(jodaDate);
        }
        if (durationTimePicker.getSelectedInstant() != null)
            newTaskFlatRequest.setDuration(Long.parseLong(durationTimePicker.getSelectedInstant()));
        newTaskFlatRequest.setCanBeRefused(refusableCheckTextView.isChecked());
    }




    private boolean validateTaskRequest() {

        if (nameEditText.getInputWidgetText().toString() == null || StringUtils.isBlank(nameEditText.getInputWidgetText().toString())) {
            Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validation_create_task_name_not_empty)).show(myContext);
            return false;
        }
        if (descriptionEditText.getInputWidgetText().toString() == null || StringUtils.isBlank(descriptionEditText.getInputWidgetText().toString())) {
            Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validation_create_task_description_not_empty)).show(myContext);
            return false;
        }
        if (startDatePicker.getSelectedInstant() == null || startTimePicker.getSelectedInstant() == null) {
            Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validation_create_task_starttime_not_empty)).show(myContext);
            return false;
        }
        if (endDatePicker.getSelectedInstant() == null || endTimePicker.getSelectedInstant() == null) {
            Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validation_create_task_endtime_not_empty)).show(myContext);
            return false;
        }
        if (startDatePicker.getSelectedInstant() != null && startTimePicker.getSelectedInstant() != null && endDatePicker.getSelectedInstant() != null && endTimePicker != null) {

            JavaDateInstant date = (JavaDateInstant) startDatePicker.getSelectedInstant();
            JavaTimeInstant time = (JavaTimeInstant) startTimePicker.getSelectedInstant();
            DateTime jodaDateStart = new DateTime(date.getYear(), date.getMonthOfYear() + 2, date.getDayOfMonth(), time.getHourOfDay(), time.getMinuteOfHour(), time.getSecondOfMinute());

            date = (JavaDateInstant) endDatePicker.getSelectedInstant();
            time = (JavaTimeInstant) endTimePicker.getSelectedInstant();

            DateTime jodaDateEnd = new DateTime(date.getYear(), date.getMonthOfYear() + 2, date.getDayOfMonth(), time.getHourOfDay(), time.getMinuteOfHour(), time.getSecondOfMinute());
            if (jodaDateStart.isAfter(jodaDateEnd)) {
                Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validation_create_task_endtime_lower_starttime)).show(myContext);
                return false;
            }
            if (jodaDateEnd.isBeforeNow()) {
                Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validation_create_task_endtime_lower_now)).show(myContext);
                return false;
            }


        }

        if (durationTimePicker.getSelectedInstant() == null) {
            Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validation_create_task_duration_not_empty)).show(myContext);
            return false;
        }
        if (Long.parseLong(durationTimePicker.getSelectedInstant()) <= 0) {
            Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validation_create_task_duration_not_null)).show(myContext);
            return false;
        }
        if (newTaskFlatRequest.getSensingDuration() != null) {
            if (newTaskFlatRequest.getSensingDuration() > newTaskFlatRequest.getDuration()) {
                Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validation_create_task_duration_not_smaller_than_sensing)).show(myContext);
                return false;

            }
        }

        if(newTaskFlatRequest.getActions() == null || newTaskFlatRequest.getActions().size() <= 0)
        {
            Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validation_create_task_action)).show(myContext);
            return false;
        }



        return true;


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_create_task, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void translateFabUp() {
        ViewPropertyAnimator v = fab.animate();
        v.setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                fab.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                animating = false;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        v.setDuration(200);
        v.setInterpolator(new AccelerateDecelerateInterpolator());
        v.translationYBy(150);
        v.translationY(0);
        v.start();
        animating = true;

    }

    private void translateFabDown() {

        ViewPropertyAnimator v = fab.animate();
        v.setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                fab.setVisibility(View.INVISIBLE);
                animating = false;

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        v.setDuration(200);
        v.setInterpolator(new AccelerateDecelerateInterpolator());
        v.translationYBy(150);
        v.translationY(150);
        v.start();
        animating = true;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_create_task, container, false);


        scroll = (ObservableScrollView) root.findViewById(R.id.scroll);
        lastScrollPosition = 0;
        scroll.setScrollViewCallbacks(new ObservableScrollViewCallbacks() {
            @Override
            public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {


                    if (lastScrollPosition < scrollY && lastScrollPosition >= 0) {

                        if (fab.getVisibility() != View.INVISIBLE && !animating) {
                            translateFabDown();

                        }
                    } else if (lastScrollPosition > scrollY) {
                        if (fab.getVisibility() != View.VISIBLE && !animating) {
                            translateFabUp();
                        }
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

        fab = (FloatingActionButton) root.findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    updateTask();
                    if (validateTaskRequest())
                        fragmentSwitcher.switchContent(SelectFriendsFragment.newInstance(fragmentSwitcher,newTaskFlatRequest),false);


            }
        });
        nameEditText = (FloatingLabelEditText) root.findViewById(R.id.task_name);

        descriptionEditText = (FloatingLabelEditText) root.findViewById(R.id.task_description);
        startDatePicker = (FloatingLabelDatePicker) root.findViewById(R.id.task_start_date);
        startTimePicker = (FloatingLabelTimePicker) root.findViewById(R.id.task_start_time);
        endDatePicker = (FloatingLabelDatePicker) root.findViewById(R.id.task_end_date);
        endTimePicker = (FloatingLabelTimePicker) root.findViewById(R.id.task_end_time);
        durationTimePicker = (FloatingLabelDurationPicker) root.findViewById(R.id.task_duration);
        refusableCheckTextView = (CheckedTextView) root.findViewById(R.id.task_refusable);


        startDatePicker.setSelectedInstant(new JavaDateInstant());
        startDatePicker.setInstantPickerListener(this);
        startDatePicker.setWidgetListener(new FloatingLabelInstantPicker.OnWidgetEventListener<JavaDateInstant>() {
            @Override
            public void onShowInstantPickerDialog(FloatingLabelInstantPicker<JavaDateInstant> javaDateInstantFloatingLabelInstantPicker) {
                DatePickerFragment<JavaDateInstant> pickerFragment = DatePickerFragment.<JavaDateInstant>newInstance(javaDateInstantFloatingLabelInstantPicker.getId(), javaDateInstantFloatingLabelInstantPicker.getSelectedInstant());
                pickerFragment.show(getChildFragmentManager(), myContext.getResources().getString(R.string.start_date_picker_name));
            }
        });

        startTimePicker.setSelectedInstant(new JavaTimeInstant());
        startTimePicker.setInstantPickerListener(this);
        startTimePicker.setWidgetListener(new FloatingLabelInstantPicker.OnWidgetEventListener<JavaTimeInstant>() {
            @Override
            public void onShowInstantPickerDialog(FloatingLabelInstantPicker<JavaTimeInstant> floatingLabelInstantPicker) {
                TimePickerFragment<JavaTimeInstant> pickerFragment = TimePickerFragment.<JavaTimeInstant>newInstance(floatingLabelInstantPicker.getId(), floatingLabelInstantPicker.getSelectedInstant());
                pickerFragment.show(getChildFragmentManager(), myContext.getResources().getString(R.string.start_time_picker_name));

            }
        });


        endDatePicker.setSelectedInstant(new JavaDateInstant());
        endDatePicker.setInstantPickerListener(this);
        endDatePicker.setWidgetListener(new FloatingLabelInstantPicker.OnWidgetEventListener<JavaDateInstant>() {
            @Override
            public void onShowInstantPickerDialog(FloatingLabelInstantPicker<JavaDateInstant> javaDateInstantFloatingLabelInstantPicker) {
                DatePickerFragment<JavaDateInstant> pickerFragment = DatePickerFragment.<JavaDateInstant>newInstance(javaDateInstantFloatingLabelInstantPicker.getId(), javaDateInstantFloatingLabelInstantPicker.getSelectedInstant());
                pickerFragment.setTargetFragment(CreateTaskFragment.this, 0);
                pickerFragment.show(getChildFragmentManager(), myContext.getResources().getString(R.string.end_date_picker));
            }
        });


        endTimePicker.setSelectedInstant(new JavaTimeInstant());
        endTimePicker.setInstantPickerListener(this);
        endTimePicker.setWidgetListener(new FloatingLabelInstantPicker.OnWidgetEventListener<JavaTimeInstant>() {
            @Override
            public void onShowInstantPickerDialog(FloatingLabelInstantPicker<JavaTimeInstant> floatingLabelInstantPicker) {
                TimePickerFragment<JavaTimeInstant> pickerFragment = TimePickerFragment.<JavaTimeInstant>newInstance(floatingLabelInstantPicker.getId(), floatingLabelInstantPicker.getSelectedInstant());
                pickerFragment.show(getChildFragmentManager(), myContext.getResources().getString(R.string.end_time_picker));

            }
        });


        durationTimePicker.setWidgetListener(new FloatingLabelDurationPicker.OnWidgetEventListener() {
            @Override
            public void onShowDurationPickerDialog(FloatingLabelBaseDurationPicker source) {
                DurationTimePickerFragment durationTimePickerFragment = DurationTimePickerFragment.newInstance(myContext.getResources().getString(R.string.duration_time_picker), source.getId(), "", new MyDurationPickerListener());
                durationTimePickerFragment.show(getFragmentManager(), null);

            }
        });


        refusableCheckTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refusableCheckTextView.toggle();
            }
        });


        //restore state
        if (newTaskFlatRequest != null) {
            if (newTaskFlatRequest.getName() != null)
                nameEditText.getInputWidgetText().append(newTaskFlatRequest.getName());
            if (newTaskFlatRequest.getCanBeRefused() != null)
                refusableCheckTextView.setChecked(newTaskFlatRequest.getCanBeRefused());
            if (newTaskFlatRequest.getDeadline() != null) {
                DateTime date = newTaskFlatRequest.getDeadline();
                endDatePicker.setSelectedInstant(new JavaDateInstant(date.getYear(), date.getMonthOfYear() - 2, date.getDayOfMonth()));
                endTimePicker.setSelectedInstant(new JavaTimeInstant(date.getHourOfDay(), date.getMinuteOfHour(), date.getSecondOfMinute()));

            }
            if (newTaskFlatRequest.getStart() != null) {
                DateTime date = newTaskFlatRequest.getStart();
                startDatePicker.setSelectedInstant(new JavaDateInstant(date.getYear(), date.getMonthOfYear() - 2, date.getDayOfMonth()));
                startTimePicker.setSelectedInstant(new JavaTimeInstant(date.getHourOfDay(), date.getMinuteOfHour(), date.getSecondOfMinute()));

            }
            if (newTaskFlatRequest.getDescription() != null)
                descriptionEditText.getInputWidgetText().append(newTaskFlatRequest.getDescription());
            if (newTaskFlatRequest.getDuration() != null)
                durationTimePicker.setSelectedInstant(newTaskFlatRequest.getDuration() + "");
        }


        if (ShowTipsPreferences.getInstance(myContext).shouldShowTips(CreateTaskFragment.class.getSimpleName())) {

            RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
            lps.setMargins(margin, margin, margin, margin);


            ShowcaseView sv = new ShowcaseView.Builder(myContext, true).setTarget(new ViewTarget(fab)).setContentTitle(myContext.getString(R.string.create_task_desc)).setStyle(R.style.CustomShowcaseTheme).hideOnTouchOutside().build();
            sv.setButtonPosition(lps);
            ShowTipsPreferences.getInstance(myContext).setShouldShowTips(CreateTaskFragment.class.getSimpleName(), false);
        }

        return root;

    }


    @Override
    public void onCancelled(int i) {

    }

    @Override
    public void onInstantSelected(int pickerId, Instant instant) {
        if (pickerId == R.id.task_start_date) {
            startDatePicker.setSelectedInstant((JavaDateInstant) instant);
        } else if (pickerId == R.id.task_end_date) {
            endDatePicker.setSelectedInstant((JavaDateInstant) instant);
        } else if (pickerId == R.id.task_start_time) {
            startTimePicker.setSelectedInstant((JavaTimeInstant) instant);
        } else if (pickerId == R.id.task_end_time) {
            endTimePicker.setSelectedInstant((JavaTimeInstant) instant);
        }
    }

    @Override
    public void onInstantChanged(FloatingLabelInstantPicker floatingLabelInstantPicker, Instant instant) {

    }

    public static Fragment newInstance(FragmentSwitcher fragmentSwitcher) {
        CreateTaskFragment f = new CreateTaskFragment();
        f.setFragmentSwitcher(fragmentSwitcher);
        return f;
    }

    public static Fragment newInstance(TaskFlatRequest newTaskFlatRequest, FragmentSwitcher fragmentSwitcher) {
        CreateTaskFragment f = new CreateTaskFragment();
        f.setFragmentSwitcher(fragmentSwitcher);
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));
        f.setArguments(args);
        return f;
    }

    public void setFragmentSwitcher(FragmentSwitcher fragmentSwitcher) {
        this.fragmentSwitcher = fragmentSwitcher;
    }




    private class MyDurationPickerListener implements DurationPickerListener {
        @Override
        public void onDurationSelected(int pickerId, String duration) {
            durationTimePicker.setSelectedInstant(duration);
        }
    }


}
