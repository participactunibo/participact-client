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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;
import com.nispok.snackbar.Snackbar;

import org.apache.commons.lang3.StringUtils;
import org.parceler.Parcels;

import it.unibo.participact.R;
import it.unibo.participact.activities.CreateTaskActivity;
import it.unibo.participact.activities.interfaces.FragmentSwitcher;
import it.unibo.participact.domain.rest.ActionFlatRequest;
import it.unibo.participact.domain.rest.QuestionRequest;
import it.unibo.participact.domain.rest.TaskFlatRequest;

/**
 * Created by alessandro on 30/11/14.
 */
public class OpenAnswerQuestionFragment extends Fragment {

    private TaskFlatRequest newTaskFlatRequest;
    private ActionFlatRequest currentQuestionnaire;
    private QuestionRequest currentQuestion;
    private FragmentSwitcher fragmentSwitcher;

    private FloatingLabelEditText floatingLabelEditText;
    private static ActionBarActivity myContext;
    private FloatingActionButton fab;

    public static Fragment newInstance(TaskFlatRequest newTaskFlatRequest, ActionFlatRequest currentQuestionaire, FragmentSwitcher fragmentSwitcher) {
        OpenAnswerQuestionFragment f = new OpenAnswerQuestionFragment();


        f.setFragmentSwitcher(fragmentSwitcher);

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE, Parcels.wrap(currentQuestionaire));
        args.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));
        f.setArguments(args);
        return f;
    }

    public static Fragment newInstance(TaskFlatRequest newTaskFlatRequest, ActionFlatRequest currentQuestionaire, QuestionRequest currentQuestion, FragmentSwitcher fragmentSwitcher) {
        OpenAnswerQuestionFragment f = new OpenAnswerQuestionFragment();


        f.setFragmentSwitcher(fragmentSwitcher);

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE, Parcels.wrap(currentQuestionaire));
        args.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));
        args.putParcelable(CreateTaskActivity.KEY_NEW_QUESTION_PARCELABLE, Parcels.wrap(currentQuestion));
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
        myContext.getSupportActionBar().setTitle(myContext.getResources().getString(R.string.action_open_answer_question_title));
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE))
                this.newTaskFlatRequest = Parcels.unwrap(args.getParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE));
            if (args.containsKey(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE))
                this.currentQuestionnaire = Parcels.unwrap(args.getParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE));
            if (args.containsKey(CreateTaskActivity.KEY_NEW_QUESTION_PARCELABLE))
                this.currentQuestion = Parcels.unwrap(args.getParcelable(CreateTaskActivity.KEY_NEW_QUESTION_PARCELABLE));
        } else if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE))
                this.newTaskFlatRequest = Parcels.unwrap(savedInstanceState.getParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE));
            if (savedInstanceState.containsKey(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE))
                this.currentQuestionnaire = Parcels.unwrap(savedInstanceState.getParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE));
            if (savedInstanceState.containsKey(CreateTaskActivity.KEY_NEW_QUESTION_PARCELABLE))
                this.currentQuestion = Parcels.unwrap(savedInstanceState.getParcelable(CreateTaskActivity.KEY_NEW_QUESTION_PARCELABLE));
        }


        if (!args.containsKey(CreateTaskActivity.KEY_NEW_QUESTION_PARCELABLE)) {
            this.currentQuestion = new QuestionRequest();
            currentQuestion.setIsClosedAnswers(false);
            currentQuestion.setIsMultipleAnswers(false);
            currentQuestion.setQuestion_order(currentQuestionnaire.getNextQuestionOrder());

        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_open_answer, container, false);
        floatingLabelEditText = (FloatingLabelEditText) root.findViewById(R.id.question);


        if (currentQuestion != null) {
            if (currentQuestion.getQuestion() != null)
                floatingLabelEditText.getInputWidgetText().append(currentQuestion.getQuestion());
        }

        fab = (FloatingActionButton) root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateOpenQuestion()) {
                    updateQuestionaire();
                    Fragment f = QuestionsFragment.newInstance(newTaskFlatRequest, currentQuestionnaire, fragmentSwitcher);
                    fragmentSwitcher.switchContent(f, false);
                }
            }
        });
        return root;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        currentQuestion.setQuestion(floatingLabelEditText.getInputWidgetText().toString());
        outState.putParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE, Parcels.wrap(currentQuestionnaire));
        outState.putParcelable(CreateTaskActivity.KEY_NEW_QUESTION_PARCELABLE, Parcels.wrap(currentQuestion));
        outState.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment f = null;
        switch (item.getItemId()) {
            case android.R.id.home:
                f = QuestionsFragment.newInstance(newTaskFlatRequest, currentQuestionnaire, fragmentSwitcher);
                fragmentSwitcher.switchContent(f, false);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private boolean validateOpenQuestion() {
        if (floatingLabelEditText.getInputWidgetText() == null || StringUtils.isBlank(floatingLabelEditText.getInputWidgetText().toString())) {
            Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validation_open_answer_not_empty)).show(myContext);
            return false;
        }
        return true;
    }

    private void updateQuestionaire() {
        currentQuestion.setQuestion(floatingLabelEditText.getInputWidgetText().toString());
        if (!currentQuestionnaire.getQuestions().contains(currentQuestion))
            currentQuestionnaire.getQuestions().add(currentQuestion);

    }
}
