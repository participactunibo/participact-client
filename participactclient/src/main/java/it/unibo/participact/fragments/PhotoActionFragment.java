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
import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;
import com.nispok.snackbar.Snackbar;

import org.parceler.Parcels;

import it.unibo.participact.R;
import it.unibo.participact.activities.CreateTaskActivity;
import it.unibo.participact.activities.interfaces.FragmentSwitcher;
import it.unibo.participact.domain.persistence.ActionType;
import it.unibo.participact.domain.rest.ActionFlatRequest;
import it.unibo.participact.domain.rest.TaskFlatRequest;

/**
 * Created by alessandro on 22/11/14.
 */
public class PhotoActionFragment extends Fragment {

    private FloatingLabelEditText descriptionLabelText;
    private FloatingLabelEditText numPhotosLabetText;

    private FragmentSwitcher fragmentSwitcher;
    private TaskFlatRequest newTaskFlatRequest;
    private ActionFlatRequest currentAction;
    private static ActionBarActivity myContext;

    private FloatingActionButton fab;


    public static Fragment newInstance(TaskFlatRequest newTaskFlatRequest, FragmentSwitcher fragmentSwitcher) {
        PhotoActionFragment f = new PhotoActionFragment();

        f.setFragmentSwitcher(fragmentSwitcher);

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));
        f.setArguments(args);
        return f;
    }

    public static Fragment newInstance(TaskFlatRequest newTaskFlatRequest, ActionFlatRequest actionFlatRequest, FragmentSwitcher fragmentSwitcher) {
        PhotoActionFragment f = new PhotoActionFragment();

        f.setFragmentSwitcher(fragmentSwitcher);

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));
        args.putParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE, Parcels.wrap(actionFlatRequest));

        f.setArguments(args);
        return f;

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
        myContext.getSupportActionBar().setTitle(myContext.getResources().getString(R.string.action_photo_title));
        Bundle args = getArguments();
        newTaskFlatRequest = Parcels.unwrap(args.getParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE));

        if (args != null) {
            if (args.containsKey(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE))
                currentAction = Parcels.unwrap(args.getParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE));
        } else if (savedInstanceState != null)
            if (savedInstanceState.containsKey(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE))
                currentAction = Parcels.unwrap(savedInstanceState.getParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE));


        if (!args.containsKey(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE)) {
            currentAction = new ActionFlatRequest();
            currentAction.setType(ActionType.PHOTO);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_add_photo_action, container, false);
        descriptionLabelText = (FloatingLabelEditText) root.findViewById(R.id.photo_description);
        numPhotosLabetText = (FloatingLabelEditText) root.findViewById(R.id.photo_number);

        if (currentAction != null) {
            if (currentAction.getName() != null)
                descriptionLabelText.getInputWidgetText().append(currentAction.getName());
            if (currentAction.getNumeric_threshold() != null)
                numPhotosLabetText.getInputWidgetText().append("" + currentAction.getNumeric_threshold());
        }

        fab = (FloatingActionButton) root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validatePhotoAction()) {
                    updateTask();
                    Fragment f = ActionsFragment.newInstance(newTaskFlatRequest, fragmentSwitcher);
                    fragmentSwitcher.switchContent(f, false);
                }

            }
        });

        return root;
    }

    public void setFragmentSwitcher(FragmentSwitcher fragmentSwitcher) {
        this.fragmentSwitcher = fragmentSwitcher;
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

    private boolean validatePhotoAction() {
        if (descriptionLabelText.getInputWidgetText() == null || org.apache.commons.lang3.StringUtils.isBlank(descriptionLabelText.getInputWidgetText().toString())) {
            Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validate_action_photo_description_not_empty)).show(myContext);

            return false;
        }
        if (numPhotosLabetText.getInputWidgetText() == null) {
            Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validate_action_photo_number_not_empty)).show(myContext);
            return false;
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String description = descriptionLabelText.getInputWidgetText().toString();
        String numString = numPhotosLabetText.getInputWidgetText().toString();
        currentAction.setName(description);
        if (numString != null && numString != "")
            currentAction.setNumeric_threshold(Integer.parseInt(numString));
        outState.putParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE, Parcels.wrap(currentAction));
        outState.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));
    }

    private void updateTask() {
        String description = descriptionLabelText.getInputWidgetText().toString();
        String numString = numPhotosLabetText.getInputWidgetText().toString();
        currentAction.setName(description);
        if (numString != null && numString != "")
            currentAction.setNumeric_threshold(Integer.parseInt(numString));
        newTaskFlatRequest.getActions().add(currentAction);
    }


}