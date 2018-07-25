/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.views.floating_duration;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by alessandro on 29/11/14.
 */
public class DurationTimePickerFragment extends DialogFragment implements DurationPicker, DurationTimeListener {


    public static final String ARG_TITLE = "PickerTitle";

    public static final String ARG_SELECTED_DURATION = "SelectedDuration";
    public static final String ARG_PICKER_ID = "PickerId";

    protected int pickerId;
    protected String selectedDuration;
    protected String title;
    protected DurationPickerListener durationPickerListener;


    public static DurationTimePickerFragment newInstance(String title, int pickerId, String selectedInstant, DurationPickerListener durationPickerListener) {
        DurationTimePickerFragment f = new DurationTimePickerFragment();

        f.setDurationPickerListener(durationPickerListener);

        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_PICKER_ID, pickerId);
        args.putString(ARG_SELECTED_DURATION, selectedInstant);
        f.setArguments(args);

        return f;
    }

    private void setDurationPickerListener(DurationPickerListener durationPickerListener) {

        this.durationPickerListener = durationPickerListener;
    }


    protected void readArguments() {
        final Bundle args = getArguments();

        pickerId = args.getInt(ARG_PICKER_ID);
        selectedDuration = args.getString(ARG_SELECTED_DURATION);
        title = args.getString(ARG_TITLE);

        setSelectedDuration(selectedDuration);

        if (selectedDuration == null) {
            throw new RuntimeException("Missing picker argument: selected duration");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        readArguments();
        return new DurationTimeDialog(getActivity(), title, this);
    }

    @Override
    public void onPause() {
        // Persist the new selected items in the arguments
        getArguments().putString(ARG_SELECTED_DURATION, getSelectedDuration());

        super.onPause();
    }

    @Override
    public int getPickerId() {
        return pickerId;
    }

    @Override
    public void setSelectedDuration(String duration) {
        this.selectedDuration = duration;
    }

    @Override
    public String getSelectedDuration() {
        return this.selectedDuration;
    }

    @Override
    public boolean isSelectionEmpty() {
        return this.selectedDuration == null;
    }

    @Override
    public void onDurationTimeSubmitted(int days, int hours, int min) {
        int duration = days * 24 * 60 + hours * 60 + min;
        selectedDuration = "" + duration;
        notifyDurationSelected();
    }

    private void notifyDurationSelected() {
        durationPickerListener.onDurationSelected(getPickerId(), getSelectedDuration());
    }
}
