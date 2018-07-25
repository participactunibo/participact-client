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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.nispok.snackbar.Snackbar;

import org.apache.commons.lang3.StringUtils;
import org.parceler.Parcels;

import it.unibo.participact.R;
import it.unibo.participact.activities.CreateTaskActivity;
import it.unibo.participact.activities.interfaces.FragmentSwitcher;
import it.unibo.participact.domain.persistence.ActionType;
import it.unibo.participact.domain.rest.ActionFlatRequest;
import it.unibo.participact.domain.rest.TaskFlatRequest;
import it.unibo.participact.views.floating_duration.DurationPickerListener;
import it.unibo.participact.views.floating_duration.DurationTimePickerFragment;
import it.unibo.participact.views.floating_duration.FloatingLabelBaseDurationPicker;
import it.unibo.participact.views.floating_duration.FloatingLabelDurationPicker;


/**
 * Created by alessandro on 23/11/14.
 */
public class ActivityDetectionFragment extends Fragment {

    private TaskFlatRequest newTaskFlatRequest;
    private FragmentSwitcher fragmentSwitcher;
    private FloatingLabelDurationPicker durationTimePicker;
    private int durationTime;

    private ActionFlatRequest currentAction;
    private static ActionBarActivity myContext;

    private FloatingActionButton fab;


    public static Fragment newInstance(TaskFlatRequest newTaskFlatRequest, FragmentSwitcher fragmentSwitcher) {
        ActivityDetectionFragment f = new ActivityDetectionFragment();

        f.setFragmentSwitcher(fragmentSwitcher);

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));
        f.setArguments(args);
        return f;
    }

    public static Fragment newInstance(TaskFlatRequest newTaskFlatRequest, ActionFlatRequest actionFlatRequest, FragmentSwitcher fragmentSwitcher) {
        ActivityDetectionFragment f = new ActivityDetectionFragment();

        f.setFragmentSwitcher(fragmentSwitcher);

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));
        args.putParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE, Parcels.wrap(actionFlatRequest));
        f.setArguments(args);
        return f;
    }

    public void setFragmentSwitcher(FragmentSwitcher fragmentSwitcher) {
        this.fragmentSwitcher = fragmentSwitcher;
    }

    @Override
    public void onAttach(Activity activity) {
        myContext = (ActionBarActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        myContext.getSupportActionBar().setTitle(myContext.getResources().getString(R.string.action_activity_detection_title));
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE))
                newTaskFlatRequest = Parcels.unwrap(args.getParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE));
            if (args.containsKey(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE))
                currentAction = Parcels.unwrap(args.getParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE));
        } else if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE))
                newTaskFlatRequest = Parcels.unwrap(savedInstanceState.getParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE));
            if (savedInstanceState.containsKey(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE))
                currentAction = Parcels.unwrap(savedInstanceState.getParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE));
        }

        if (!args.containsKey(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE)) {
            currentAction = new ActionFlatRequest();
            currentAction.setType(ActionType.ACTIVITY_DETECTION);
            durationTime = -1;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_activity_detection_action, container, false);
        durationTimePicker = (FloatingLabelDurationPicker) root.findViewById(R.id.activity_detection_duration);
        if (currentAction != null) {
            if (currentAction.getDuration_threshold() != null) {
                durationTime = currentAction.getDuration_threshold();
                durationTimePicker.setSelectedInstant(durationTime + "");

            }

        }
        durationTimePicker.setWidgetListener(new FloatingLabelDurationPicker.OnWidgetEventListener() {
            @Override
            public void onShowDurationPickerDialog(FloatingLabelBaseDurationPicker source) {
                DurationTimePickerFragment durationTimePickerFragment = DurationTimePickerFragment.newInstance(myContext.getResources().getString(R.string.duration_time_picker_title), source.getId(), "", new MyDurationPickerListener());
                durationTimePickerFragment.show(getFragmentManager(), null);

            }
        });


        fab = (FloatingActionButton) root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateTask()) {
                    updateTask();
                    Fragment f = ActionsFragment.newInstance(newTaskFlatRequest, fragmentSwitcher);
                    fragmentSwitcher.switchContent(f, false);
                }

            }
        });


        return root;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (durationTime >= 0)
            currentAction.setDuration_threshold(durationTime);
        outState.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));
        outState.putParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE, Parcels.wrap(currentAction));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment f = null;

        switch (item.getItemId()) {
            case android.R.id.home:
                f = ActionsFragment.newInstance(newTaskFlatRequest, fragmentSwitcher);
                fragmentSwitcher.switchContent(f, false);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean validateTask() {
        if (durationTimePicker.getSelectedInstant() == null || StringUtils.isBlank(durationTimePicker.getSelectedInstant())) {
            Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validation_activity_detection_threshold_not_empty)).show(myContext);
            return false;
        }
        if (Integer.parseInt(durationTimePicker.getSelectedInstant()) <= 0) {
            Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validation_activity_detection_threshold_not_null)).show(myContext);
            return false;
        }
        return true;
    }

    private void updateTask() {
        currentAction.setDuration_threshold(durationTime);
        newTaskFlatRequest.getActions().add(currentAction);
    }

    private class MyDurationPickerListener implements DurationPickerListener {

        @Override
        public void onDurationSelected(int pickerId, String duration) {
            durationTimePicker.setSelectedInstant(duration);
            durationTime = Integer.parseInt(duration);
        }
    }
}
