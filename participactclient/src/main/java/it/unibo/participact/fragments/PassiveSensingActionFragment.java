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
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;
import com.marvinlabs.widget.floatinglabel.itempicker.FloatingLabelItemPicker;
import com.marvinlabs.widget.floatinglabel.itempicker.ItemPickerListener;
import com.marvinlabs.widget.floatinglabel.itempicker.StringPickerDialogFragment;
import com.nispok.snackbar.Snackbar;

import org.apache.commons.lang3.StringUtils;
import org.most.pipeline.Pipeline;
import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;

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
 * Created by alessandro on 25/11/14.
 */
public class PassiveSensingActionFragment extends Fragment implements ItemPickerListener<String> {


    private static final int AUDIO_CLASSIFIER = 0;
    private static final int ACTIVITY_RECOGNITION_COMPARE = 1;
    private static final int ACCELEROMETER = 2;
    private static final int ACCELEROMETER_CLASSIFIER = 3;
    private static final int RAW_AUDIO = 4;
    private static final int AVERAGE_ACCELEROMETER = 5;
    private static final int APP_ON_SCREEN = 6;
    private static final int APPS_NET_TRAFFIC = 7;
    private static final int BATTERY = 8;
    private static final int BLUETOOTH = 9;
    private static final int CELL = 10;
    private static final int CONNECTION_TYPE = 11;
    private static final int DEVICE_NET_TRAFFIC = 12;
    private static final int GOOGLE_ACTIVITY_RECOGNITION = 13;
    private static final int GYROSCOPE = 14;
    private static final int INSTALLED_APPS = 15;
    private static final int LIGHT = 16;
    private static final int LOCATION = 17;
    private static final int MAGNETIC_FIELD = 18;
    private static final int PHONE_CALL_DURATION = 19;
    private static final int PHONE_CALL_EVENT = 20;
    private static final int SYSTEM_STATS = 21;
    private static final int WIFI_SCAN = 22;
    private static final int DR = 23;
    private static final int TEST = 24;


    private static ActionBarActivity myContext;
    private TaskFlatRequest newTaskFlatRequest;
    private ActionFlatRequest currentAction;

    private FragmentSwitcher fragmentSwitcher;
    private FloatingLabelDurationPicker durationTimePicker;
    private FloatingLabelEditText descriptionTextView;
    private FloatingLabelItemPicker<String> inputTypePicker;
    private TextView infoTextView;
    private TextView taskDurationTextView;

    private FloatingActionButton fab;


    private int pipeline;
    private long sensingDuration;

    public static Fragment newInstance(TaskFlatRequest newTaskFlatRequest, FragmentSwitcher fragmentSwitcher) {

        PassiveSensingActionFragment p = new PassiveSensingActionFragment();
        p.setFragmentSwitcher(fragmentSwitcher);

        Bundle args = new Bundle();
        args.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));
        p.setArguments(args);
        return p;


    }

    public static Fragment newInstance(TaskFlatRequest newTaskFlatRequest, ActionFlatRequest actionFlatRequest, FragmentSwitcher fragmentSwitcher) {
        PassiveSensingActionFragment p = new PassiveSensingActionFragment();
        p.setFragmentSwitcher(fragmentSwitcher);

        Bundle args = new Bundle();
        args.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));
        args.putParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE, Parcels.wrap(actionFlatRequest));

        p.setArguments(args);
        return p;

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
        myContext.getSupportActionBar().setTitle(myContext.getResources().getString(R.string.action_passive_sensing_title));
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
            currentAction.setType(ActionType.SENSING_MOST);
            pipeline = -1;
            if(newTaskFlatRequest.getSensingDuration() != null)
                sensingDuration = newTaskFlatRequest.getSensingDuration();
            else
                sensingDuration = -1;
        }


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_sensing_action, container, false);

        durationTimePicker = (FloatingLabelDurationPicker) root.findViewById(R.id.sensing_duration);
        taskDurationTextView = (TextView) root.findViewById(R.id.info_task_duration_textView);

        boolean containsSensing = false;
        for (ActionFlatRequest a : newTaskFlatRequest.getActions()) {
            if (a.getType().equals(ActionType.SENSING_MOST)) {
                containsSensing = true;
                break;
            }
        }

        if(newTaskFlatRequest.getDuration()!= null)
            taskDurationTextView.setText(taskDurationTextView.getText() +" "+newTaskFlatRequest.getDuration() + " min");
        else
            taskDurationTextView.setText(myContext.getResources().getString(R.string.no_task_duration_specified));

        if (newTaskFlatRequest.getSensingDuration() != null && containsSensing) {
            durationTimePicker.setVisibility(View.GONE);
        }
        if (newTaskFlatRequest.getSensingDuration() != null && newTaskFlatRequest.getDuration() != null)
            if (newTaskFlatRequest.getSensingDuration() > newTaskFlatRequest.getDuration())
                durationTimePicker.setVisibility(View.VISIBLE);
        durationTimePicker.setWidgetListener(new FloatingLabelDurationPicker.OnWidgetEventListener() {
            @Override
            public void onShowDurationPickerDialog(FloatingLabelBaseDurationPicker source) {
                DurationTimePickerFragment durationTimePickerFragment = DurationTimePickerFragment.newInstance(myContext.getResources().getString(R.string.sensing_duration_time_picker), source.getId(), "", new MyDurationPickerListener());
                durationTimePickerFragment.show(getFragmentManager(), null);

            }
        });

        descriptionTextView = (FloatingLabelEditText) root.findViewById(R.id.sensing_description);
        inputTypePicker = (FloatingLabelItemPicker<String>) root.findViewById(R.id.input_type);
        String[] pipelines = getResources().getStringArray(R.array.sensing_most_input);
        inputTypePicker.setAvailableItems(Arrays.asList(pipelines));
        inputTypePicker.setWidgetListener(new FloatingLabelItemPicker.OnWidgetEventListener<String>() {
            @Override
            public void onShowItemPickerDialog(FloatingLabelItemPicker<String> stringFloatingLabelItemPicker) {
                StringPickerDialogFragment itemPicker = StringPickerDialogFragment.newInstance(
                        stringFloatingLabelItemPicker.getId(),
                        "Input Type Picker",
                        "Ok", "Cancel",
                        false,
                        stringFloatingLabelItemPicker.getSelectedIndices(),
                        new ArrayList<String>(stringFloatingLabelItemPicker.getAvailableItems()));

                itemPicker.show(getChildFragmentManager(), "itemPicker");


            }
        });

        if (currentAction != null) {

            if (currentAction.getName() != null)
                descriptionTextView.getInputWidgetText().append(currentAction.getName());
            if (currentAction.getInput_type() != null) {
                int index = getIndexFromPipeline(Pipeline.Type.fromInt(currentAction.getInput_type()));
                int[] indexArray = new int[]{index};
                inputTypePicker.setSelectedIndices(indexArray);
                pipeline = currentAction.getInput_type();
            }
        }

        fab = (FloatingActionButton) root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validatePassiveSensingAction()) {
                    updateTask();
                    Fragment f = ActionsFragment.newInstance(newTaskFlatRequest, fragmentSwitcher);
                    fragmentSwitcher.switchContent(f, false);
                }

            }
        });


        return root;
    }


    @Override
    public void onCancelled(int i) {

    }

    @Override
    public void onItemsSelected(int pickerId, int[] selectedIndices) {

        inputTypePicker.setSelectedIndices(selectedIndices);
        if (selectedIndices.length == 1)
            pipeline = getPipelineTypeFromPosition(selectedIndices[0]);
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

    private boolean validatePassiveSensingAction() {
        if(durationTimePicker.getVisibility() == View.VISIBLE) {
            if (durationTimePicker.getSelectedInstant() == null || StringUtils.isBlank(durationTimePicker.getSelectedInstant().toString())) {
                Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validate_action_sensing_duration_not_empty)).show(myContext);
                return false;
            }
            if(durationTimePicker.getSelectedInstant() != null) {
                if (Long.parseLong(durationTimePicker.getSelectedInstant().toString()) <= 0) {
                    Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validate_action_sensing_duration_not_null)).show(myContext);
                    return false;
                }
            }
            if (newTaskFlatRequest.getDuration() != null && durationTimePicker.getSelectedInstant() != null) {
                if (Long.parseLong(durationTimePicker.getSelectedInstant().toString()) >= newTaskFlatRequest.getDuration()) {
                    Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validate_action_sensing_duration_not_grather_than_task)).show(myContext);
                    return false;
                }
            }

        }

        if (descriptionTextView.getInputWidgetText() == null || StringUtils.isBlank(descriptionTextView.getInputWidgetText().toString())) {
            Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validate_action_sensing_description_not_null)).show(myContext);
            return false;
        }
        if (inputTypePicker.getSelectedItems() == null) {
            Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validate_action_sensing_type_not_null)).show(myContext);
            return false;

        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (sensingDuration >= 0)
            newTaskFlatRequest.setSensingDuration(sensingDuration);
        if (pipeline >= 0)
            currentAction.setInput_type(pipeline);
        currentAction.setName(descriptionTextView.getInputWidgetText().toString());
        outState.putParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE, Parcels.wrap(currentAction));
        outState.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));


    }

    private void updateTask() {
        newTaskFlatRequest.setSensingDuration(sensingDuration);
        currentAction.setInput_type(pipeline);
        currentAction.setName(descriptionTextView.getInputWidgetText().toString());
        newTaskFlatRequest.getActions().add(currentAction);
    }

    private int getPipelineTypeFromPosition(int selectedIndex) {
        switch (selectedIndex) {
            case AUDIO_CLASSIFIER:
                return Pipeline.Type.AUDIO_CLASSIFIER.toInt();
            case ACTIVITY_RECOGNITION_COMPARE:
                return Pipeline.Type.ACTIVITY_RECOGNITION_COMPARE.toInt();
            case ACCELEROMETER:
                return Pipeline.Type.ACCELEROMETER.toInt();
            case ACCELEROMETER_CLASSIFIER:
                return Pipeline.Type.ACCELEROMETER_CLASSIFIER.toInt();
            case RAW_AUDIO:
                return Pipeline.Type.RAW_AUDIO.toInt();
            case AVERAGE_ACCELEROMETER:
                return Pipeline.Type.AVERAGE_ACCELEROMETER.toInt();
            case APP_ON_SCREEN:
                return Pipeline.Type.APP_ON_SCREEN.toInt();
            case APPS_NET_TRAFFIC:
                return Pipeline.Type.APPS_NET_TRAFFIC.toInt();
            case BATTERY:
                return Pipeline.Type.BATTERY.toInt();
            case BLUETOOTH:
                return Pipeline.Type.BLUETOOTH.toInt();
            case CELL:
                return Pipeline.Type.CELL.toInt();
            case CONNECTION_TYPE:
                return Pipeline.Type.CONNECTION_TYPE.toInt();
            case DEVICE_NET_TRAFFIC:
                return Pipeline.Type.DEVICE_NET_TRAFFIC.toInt();
            case GOOGLE_ACTIVITY_RECOGNITION:
                return Pipeline.Type.GOOGLE_ACTIVITY_RECOGNITION.toInt();
            case GYROSCOPE:
                return Pipeline.Type.GYROSCOPE.toInt();
            case INSTALLED_APPS:
                return Pipeline.Type.INSTALLED_APPS.toInt();
            case LIGHT:
                return Pipeline.Type.LIGHT.toInt();
            case LOCATION:
                return Pipeline.Type.LOCATION.toInt();
            case MAGNETIC_FIELD:
                return Pipeline.Type.MAGNETIC_FIELD.toInt();
            case PHONE_CALL_DURATION:
                return Pipeline.Type.PHONE_CALL_DURATION.toInt();
            case PHONE_CALL_EVENT:
                return Pipeline.Type.PHONE_CALL_EVENT.toInt();
            case SYSTEM_STATS:
                return Pipeline.Type.SYSTEM_STATS.toInt();
            case WIFI_SCAN:
                return Pipeline.Type.WIFI_SCAN.toInt();
            case DR:
                return Pipeline.Type.DR.toInt();
            case TEST:
                return Pipeline.Type.TEST.toInt();


            default:
                return Pipeline.Type.DUMMY.toInt();

        }


    }


    private int getIndexFromPipeline(Pipeline.Type input_type) {


        switch (input_type) {

            case AUDIO_CLASSIFIER:
                return this.AUDIO_CLASSIFIER;
            case ACTIVITY_RECOGNITION_COMPARE:
                return this.ACTIVITY_RECOGNITION_COMPARE;
            case ACCELEROMETER:
                return this.ACCELEROMETER;
            case ACCELEROMETER_CLASSIFIER:
                return this.ACCELEROMETER_CLASSIFIER;
            case RAW_AUDIO:
                return this.RAW_AUDIO;
            case AVERAGE_ACCELEROMETER:
                return this.AVERAGE_ACCELEROMETER;
            case APP_ON_SCREEN:
                return this.APP_ON_SCREEN;
            case APPS_NET_TRAFFIC:
                return this.APPS_NET_TRAFFIC;
            case BATTERY:
                return this.BATTERY;
            case BLUETOOTH:
                return this.BLUETOOTH;
            case CELL:
                return this.CELL;
            case CONNECTION_TYPE:
                return this.CONNECTION_TYPE;
            case DEVICE_NET_TRAFFIC:
                return this.DEVICE_NET_TRAFFIC;
            case GOOGLE_ACTIVITY_RECOGNITION:
                return this.GOOGLE_ACTIVITY_RECOGNITION;
            case GYROSCOPE:
                return this.GYROSCOPE;
            case INSTALLED_APPS:
                return this.INSTALLED_APPS;
            case LIGHT:
                return this.LIGHT;
            case LOCATION:
                return this.LOCATION;
            case MAGNETIC_FIELD:
                return this.MAGNETIC_FIELD;
            case PHONE_CALL_DURATION:
                return this.PHONE_CALL_DURATION;
            case PHONE_CALL_EVENT:
                return this.PHONE_CALL_EVENT;
            case SYSTEM_STATS:
                return this.SYSTEM_STATS;
            case WIFI_SCAN:
                return this.WIFI_SCAN;
            case DR:
                return this.DR;
            case TEST:
                return this.TEST;

            default:
                return this.AUDIO_CLASSIFIER;

        }
    }


    private class MyDurationPickerListener implements DurationPickerListener {

        @Override
        public void onDurationSelected(int pickerId, String duration) {
            sensingDuration = Long.parseLong(duration);
            durationTimePicker.setSelectedInstant(duration);

        }
    }
}
