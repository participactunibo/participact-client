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
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckedTextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;
import com.nispok.snackbar.Snackbar;

import org.apache.commons.lang3.StringUtils;
import org.parceler.Parcels;

import java.util.ArrayList;

import it.unibo.participact.R;
import it.unibo.participact.activities.CreateTaskActivity;
import it.unibo.participact.activities.interfaces.FragmentSwitcher;
import it.unibo.participact.domain.rest.ActionFlatRequest;
import it.unibo.participact.domain.rest.ClosedAnswerRequest;
import it.unibo.participact.domain.rest.QuestionRequest;
import it.unibo.participact.domain.rest.TaskFlatRequest;
import it.unibo.participact.views.adapters.ClosedAnswerAdapter;

/**
 * Created by alessandro on 28/11/14.
 */
public class ClosedAnswerQuestionFragment extends Fragment implements ClosedAnswerAdapter.ClosedAnswerRemovedListener {


    private FragmentSwitcher fragmentSwitcher;
    private ActionFlatRequest currentQuestionaire;
    private TaskFlatRequest newTaskFlatRequest;
    private QuestionRequest currentQuestion;

    private ClosedAnswerAdapter adapter;

    private FloatingLabelEditText questionView;
    private FloatingLabelEditText answerView;
    private CheckedTextView isMulipleChoice;
    private ObservableListView answersListView;

    private FloatingActionButton fab;
    private boolean animating;

    private static ActionBarActivity myContext;
    private int lastScrollPosition;


    public static Fragment newInstance(TaskFlatRequest newTaskFlatRequest, ActionFlatRequest currentQuestionaire, FragmentSwitcher fragmentSwitcher) {
        ClosedAnswerQuestionFragment f = new ClosedAnswerQuestionFragment();

        f.setFragmentSwitcher(fragmentSwitcher);

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE, Parcels.wrap(currentQuestionaire));
        args.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));
        f.setArguments(args);
        return f;
    }

    public static Fragment newInstance(TaskFlatRequest newTaskFlatRequest, ActionFlatRequest currentQuestionaire, QuestionRequest questionRequest, FragmentSwitcher fragmentSwitcher) {
        ClosedAnswerQuestionFragment f = new ClosedAnswerQuestionFragment();

        f.setFragmentSwitcher(fragmentSwitcher);

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE, Parcels.wrap(currentQuestionaire));
        args.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));
        args.putParcelable(CreateTaskActivity.KEY_NEW_QUESTION_PARCELABLE, Parcels.wrap(questionRequest));
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
        myContext.getSupportActionBar().setTitle(myContext.getResources().getString(R.string.action_closed_answer_question_title));
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE))
                this.currentQuestionaire = Parcels.unwrap(args.getParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE));
            if (args.containsKey(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE))
                this.newTaskFlatRequest = Parcels.unwrap(args.getParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE));
            if (args.containsKey(CreateTaskActivity.KEY_NEW_QUESTION_PARCELABLE))
                this.currentQuestion = Parcels.unwrap(args.getParcelable(CreateTaskActivity.KEY_NEW_QUESTION_PARCELABLE));

        } else if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE))
                this.currentQuestionaire = Parcels.unwrap(savedInstanceState.getParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE));
            if (savedInstanceState.containsKey(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE))
                this.newTaskFlatRequest = Parcels.unwrap(savedInstanceState.getParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE));
            if (savedInstanceState.containsKey(CreateTaskActivity.KEY_NEW_QUESTION_PARCELABLE))
                this.currentQuestion = Parcels.unwrap(savedInstanceState.getParcelable(CreateTaskActivity.KEY_NEW_QUESTION_PARCELABLE));
        }

        if (!args.containsKey(CreateTaskActivity.KEY_NEW_QUESTION_PARCELABLE)) {
            this.currentQuestion = new QuestionRequest();
            currentQuestion.setIsClosedAnswers(true);
            currentQuestion.setClosed_answers(new ArrayList<ClosedAnswerRequest>());
            currentQuestion.setQuestion_order(currentQuestionaire.getNextQuestionOrder());

        }
        lastScrollPosition = 0;
        animating = false;
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

        View root = inflater.inflate(R.layout.fragment_closed_answers, container, false);
        questionView = (FloatingLabelEditText) root.findViewById(R.id.question);
        answerView = (FloatingLabelEditText) root.findViewById(R.id.answer);
        isMulipleChoice = (CheckedTextView) root.findViewById(R.id.multiple_answer);
        answersListView = (ObservableListView) root.findViewById(R.id.list_answers);

        isMulipleChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isMulipleChoice.toggle();
            }
        });


        adapter = new ClosedAnswerAdapter(getActivity(), R.layout.fragment_closed_answer_item, currentQuestion.getClosed_answers(), this);

        answersListView.setAdapter(adapter);

        if (currentQuestion != null) {
            if (currentQuestion.getQuestion() != null)
                questionView.setInputWidgetText(currentQuestion.getQuestion());
            if (currentQuestion.getIsMultipleAnswers() != null)
                isMulipleChoice.setChecked(currentQuestion.getIsMultipleAnswers());
        }

        answersListView.setScrollViewCallbacks(new ObservableScrollViewCallbacks() {
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
                String answer = answerView.getInputWidgetText().toString();
                if (!StringUtils.isBlank(answer)) {
                    ClosedAnswerRequest closedAnswerRequest = new ClosedAnswerRequest();
                    closedAnswerRequest.setAnswerDescription(answer);
                    closedAnswerRequest.setAnswerOrder(currentQuestion.getNextClosedAnswerOrder());
                    currentQuestion.getClosed_answers().add(closedAnswerRequest);
                } else
                    Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validation_closed_answer_empty)).show(myContext);

                adapter.notifyDataSetChanged();

            }
        });

        return root;

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        currentQuestion.setQuestion(questionView.getInputWidgetText().toString());
        currentQuestion.setIsMultipleAnswers(isMulipleChoice.isChecked());
        outState.putParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE, Parcels.wrap(currentQuestionaire));
        outState.putParcelable(CreateTaskActivity.KEY_NEW_QUESTION_PARCELABLE, Parcels.wrap(currentQuestion));
        outState.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_closed_answers, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment f = null;
        switch (item.getItemId()) {
            case android.R.id.home:
                f = QuestionsFragment.newInstance(newTaskFlatRequest, currentQuestionaire, fragmentSwitcher);
                fragmentSwitcher.switchContent(f, false);
                return true;
            case R.id.confirm_action:
                if (validateClosedAnswer()) {
                    updateQuestionaire();
                    f = QuestionsFragment.newInstance(newTaskFlatRequest, currentQuestionaire, fragmentSwitcher);
                    fragmentSwitcher.switchContent(f, false);
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private boolean validateClosedAnswer() {
        if (adapter.getCount() < 2) {
            Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validation_closed_answer_at_least_two)).show(myContext);
            return false;
        }
        if (questionView.getInputWidgetText().toString() == null || StringUtils.isBlank(questionView.getInputWidgetText().toString())) {
            Snackbar.with(myContext).text(myContext.getResources().getString(R.string.validation_closed_answer_not_empty)).show(myContext);
            return false;
        }

        return true;
    }

    private void updateQuestionaire() {
        currentQuestion.setQuestion(questionView.getInputWidgetText().toString());
        currentQuestion.setIsMultipleAnswers(isMulipleChoice.isChecked());

        if (!currentQuestionaire.getQuestions().contains(currentQuestion))
            currentQuestionaire.getQuestions().add(currentQuestion);

    }


    @Override
    public void onClosedAnswerRemoved(int position) {
        currentQuestion.getClosed_answers().remove(position);
        adapter.notifyDataSetChanged();
    }
}
