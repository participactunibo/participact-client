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
import android.widget.AdapterView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import org.parceler.Parcels;

import it.unibo.participact.R;
import it.unibo.participact.activities.CreateTaskActivity;
import it.unibo.participact.activities.interfaces.FragmentSwitcher;
import it.unibo.participact.domain.rest.ActionFlatRequest;
import it.unibo.participact.domain.rest.QuestionRequest;
import it.unibo.participact.domain.rest.TaskFlatRequest;
import it.unibo.participact.views.adapters.QuestionsAdapter;

/**
 * Created by alessandro on 27/11/14.
 */
public class QuestionsFragment extends Fragment implements QuestionsAdapter.QuestionRemovedListener {
    private ActionFlatRequest currentQuestionaire;
    private TaskFlatRequest newTaskFlatRequest;
    private FragmentSwitcher fragmentSwitcher;
    private ObservableListView listView;
    private TextView errorTextView;
    private QuestionsAdapter questionsAdapter;
    private static ActionBarActivity myContext;

    private FloatingActionsMenu menu;
    private FloatingActionButton fab;
    private MenuItem confirmItem;
    private int lastScrollPosition;
    private boolean animating;

    public static Fragment newInstance(TaskFlatRequest taskFlatRequest, ActionFlatRequest currentQuestionnaire, FragmentSwitcher fragmentSwitcher) {
        QuestionsFragment f = new QuestionsFragment();

        f.setFragmentSwitcher(fragmentSwitcher);

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE, Parcels.wrap(currentQuestionnaire));
        args.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(taskFlatRequest));
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
        myContext.getSupportActionBar().setTitle(myContext.getResources().getString(R.string.action_question_title));
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE))
                this.newTaskFlatRequest = Parcels.unwrap(args.getParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE));
            if (args.containsKey(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE))
                this.currentQuestionaire = Parcels.unwrap(args.getParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE));

        } else if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE))
                this.newTaskFlatRequest = Parcels.unwrap(savedInstanceState.getParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE));
            if (savedInstanceState.containsKey(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE))
                this.currentQuestionaire = Parcels.unwrap(savedInstanceState.getParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE));

        }

        lastScrollPosition = 0;
        animating = false;


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));
        outState.putParcelable(CreateTaskActivity.KEY_NEW_ACTION_PARCELABLE, Parcels.wrap(currentQuestionaire));

    }


    private void translateFabUp() {
        ViewPropertyAnimator v = menu.animate();
        v.setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                menu.setVisibility(View.VISIBLE);
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

        ViewPropertyAnimator v = menu.animate();
        v.setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                menu.setVisibility(View.INVISIBLE);
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
        View root = inflater.inflate(R.layout.fragment_questions, container, false);
        listView = (ObservableListView) root.findViewById(R.id.list_questions);
        listView.setScrollViewCallbacks(new ObservableScrollViewCallbacks() {
            @Override
            public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

                if (menu.isExpanded())
                    menu.collapse();
                if (lastScrollPosition < scrollY && lastScrollPosition >= 0) {

                    if (menu.getVisibility() != View.INVISIBLE && !animating) {
                        translateFabDown();
                    }

                } else if (lastScrollPosition > scrollY) {
                    if (menu.getVisibility() != View.VISIBLE && !animating) {
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
        errorTextView = (TextView) root.findViewById(R.id.textViewError);

        if (currentQuestionaire.getQuestions().size() == 0)
            errorTextView.setVisibility(View.VISIBLE);


        questionsAdapter = new QuestionsAdapter(getActivity(), R.layout.fragment_question_item, currentQuestionaire.getQuestions(), this);

        listView.setAdapter(questionsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                QuestionRequest item = (QuestionRequest) adapterView.getItemAtPosition(position);
                if (item.getIsClosedAnswers()) {
                    Fragment f = ClosedAnswerQuestionFragment.newInstance(newTaskFlatRequest, currentQuestionaire, item, fragmentSwitcher);
                    fragmentSwitcher.switchContent(f, false);
                } else {
                    Fragment f = OpenAnswerQuestionFragment.newInstance(newTaskFlatRequest, currentQuestionaire, item, fragmentSwitcher);
                    fragmentSwitcher.switchContent(f, false);
                }
            }
        });


        menu = (FloatingActionsMenu) root.findViewById(R.id.multiple_actions);
        fab = (FloatingActionButton) root.findViewById(R.id.open_answer);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Fragment f = OpenAnswerQuestionFragment.newInstance(newTaskFlatRequest, currentQuestionaire, fragmentSwitcher);
                fragmentSwitcher.switchContent(f, false);

            }
        });

        fab = (FloatingActionButton) root.findViewById(R.id.closed_answer);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Fragment f = ClosedAnswerQuestionFragment.newInstance(newTaskFlatRequest, currentQuestionaire, fragmentSwitcher);
                fragmentSwitcher.switchContent(f, false);
            }
        });


        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_questionnaire, menu);
        if(currentQuestionaire.getQuestions() != null && currentQuestionaire.getQuestions().size() > 0)
            confirmItem = menu.findItem(R.id.confirm_action).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment f = null;

                f = QuestionaireActionFragment.newInstance(newTaskFlatRequest, currentQuestionaire, fragmentSwitcher);
                fragmentSwitcher.switchContent(f, false);
                return true;


    }

    @Override
    public void onQuestionRemoved(int position) {
        currentQuestionaire.getQuestions().remove(position);
        questionsAdapter.notifyDataSetChanged();
        if(currentQuestionaire.getQuestions().size() == 0)
            confirmItem.setVisible(false);

    }
}
