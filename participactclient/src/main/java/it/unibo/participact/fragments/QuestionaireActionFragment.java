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

import org.apache.commons.lang3.StringUtils;
import org.parceler.Parcels;

import java.util.ArrayList;

import it.unibo.participact.R;
import it.unibo.participact.activities.CreateTaskActivity;
import it.unibo.participact.activities.interfaces.FragmentSwitcher;
import it.unibo.participact.domain.persistence.ActionType;
import it.unibo.participact.domain.rest.ActionFlatRequest;
import it.unibo.participact.domain.rest.QuestionRequest;
import it.unibo.participact.domain.rest.TaskFlatRequest;

/**
 * Created by alessandro on 26/11/14.
 */
public class QuestionaireActionFragment extends Fragment {


    private TaskFlatRequest newTaskFlatRequest;
    private FragmentSwitcher fragmentSwitcher;
    private ActionFlatRequest currentQuestionnaire;

    private FloatingLabelEditText titleTextView;
    private FloatingLabelEditText descriptionTextView;


    private FloatingActionButton fab;

    private static ActionBarActivity myContext;


    public static Fragment newInstance(TaskFlatRequest newTaskFlatRequest, FragmentSwitcher fragmentSwitcher) {

        QuestionaireActionFragment f = new QuestionaireActionFragment();
        f.setFragmentSwitcher(fragmentSwitcher);

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));
        f.setArguments(args);
        return f;

    }


    public static Fragment newInstance(TaskFlatRequest taskFlatRequest, ActionFlatRequest questionRequest, FragmentSwitcher fragmentSwitcher) {

        QuestionaireActionFragment f = new QuestionaireActionFragment();
        f.setFragmentSwitcher(fragmentSwitcher);

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(taskFlatRequest));
        args.putParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE, Parcels.wrap(questionRequest));
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
        myContext.getSupportActionBar().setTitle(myContext.getResources().getString(R.string.action_questionnaire_title));
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE))
                newTaskFlatRequest = Parcels.unwrap(args.getParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE));
            if (args.containsKey(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE))
                currentQuestionnaire = Parcels.unwrap(args.getParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE));
        } else if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE))
                newTaskFlatRequest = Parcels.unwrap(savedInstanceState.getParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE));
            if (savedInstanceState.containsKey(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE))
                currentQuestionnaire = Parcels.unwrap(savedInstanceState.getParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE));
        }
        if (!args.containsKey(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE)) {
            currentQuestionnaire = new ActionFlatRequest();
            currentQuestionnaire.setQuestions(new ArrayList<QuestionRequest>());
            currentQuestionnaire.setType(ActionType.QUESTIONNAIRE);

        }

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if ( currentQuestionnaire.getQuestions() != null && currentQuestionnaire.getQuestions().size() > 0)
            fab.setVisibility(View.VISIBLE);
        else
            fab.setVisibility(View.INVISIBLE);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_action_questionaire, menu);
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
            case R.id.question:
                currentQuestionnaire.setTitle(titleTextView.getInputWidgetText().toString());
                currentQuestionnaire.setDescription(descriptionTextView.getInputWidgetText().toString());
                f = QuestionsFragment.newInstance(newTaskFlatRequest, currentQuestionnaire, fragmentSwitcher);
                this.fragmentSwitcher.switchContent(f, false);
                break;

        }

        return super.onOptionsItemSelected(item);

    }

    private boolean validateActionQuestionnaire() {

        if (titleTextView.getInputWidgetText() == null || StringUtils.isBlank(titleTextView.getInputWidgetText().toString())) {
            Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validation_questionnaire_title)).show(myContext);

            return false;
        }
        if (descriptionTextView.getInputWidgetText() == null || StringUtils.isBlank(descriptionTextView.getInputWidgetText().toString())) {
            Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validation_questionnaire_description)).show(myContext);
            return false;
        }

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        currentQuestionnaire.setTitle(titleTextView.getInputWidgetText().toString());
        currentQuestionnaire.setDescription(descriptionTextView.getInputWidgetText().toString());

        outState.putParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE, Parcels.wrap(currentQuestionnaire));
        outState.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));

    }

    private void updateTask() {
        currentQuestionnaire.setTitle(titleTextView.getInputWidgetText().toString());
        currentQuestionnaire.setDescription(descriptionTextView.getInputWidgetText().toString());
        newTaskFlatRequest.getActions().add(currentQuestionnaire);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_questionaire_action, container, false);
        titleTextView = (FloatingLabelEditText) root.findViewById(R.id.questionaire_title);
        descriptionTextView = (FloatingLabelEditText) root.findViewById(R.id.questionaire_description);

        if (currentQuestionnaire != null) {
            if (currentQuestionnaire.getTitle() != null) {
                titleTextView.getInputWidgetText().append(currentQuestionnaire.getTitle());
            }
            if (currentQuestionnaire.getDescription() != null) {
                descriptionTextView.getInputWidgetText().append(currentQuestionnaire.getDescription());
            }
        }
        fab = (FloatingActionButton) root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateActionQuestionnaire()) {
                    updateTask();
                    Fragment f = ActionsFragment.newInstance(newTaskFlatRequest, fragmentSwitcher);
                    fragmentSwitcher.switchContent(f, false);
                }
            }
        });

        return root;
    }
}
