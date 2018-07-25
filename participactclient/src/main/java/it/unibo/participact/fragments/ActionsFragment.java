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
import com.nispok.snackbar.Snackbar;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import it.unibo.participact.R;
import it.unibo.participact.activities.CreateTaskActivity;
import it.unibo.participact.activities.interfaces.FragmentSwitcher;
import it.unibo.participact.domain.persistence.ActionType;
import it.unibo.participact.domain.rest.ActionFlatRequest;
import it.unibo.participact.domain.rest.TaskFlatRequest;
import it.unibo.participact.views.adapters.ActionsAdapter;

/**
 * Created by alessandro on 22/11/14.
 */
public class ActionsFragment extends Fragment implements ActionsAdapter.ActionRemoveListener {

    private static ActionBarActivity myContext;


    private TaskFlatRequest newTaskFlatRequest;
    private FragmentSwitcher fragmentSwitcher;
    private ActionsAdapter actionsAdapter;
    private ObservableListView actionsListView;
    private TextView errorTextView;
    private List<ActionFlatRequest> list;
    private int lastScrollPosition;
    private boolean animating;
    private FloatingActionsMenu menu;
    private FloatingActionButton fab;
    private MenuItem confirmItem;

    public static Fragment newInstance(TaskFlatRequest newTaskFlatRequest, FragmentSwitcher fragmentSwitcher) {

        ActionsFragment f = new ActionsFragment();
        f.setFragmentSwitcher(fragmentSwitcher);

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));
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
        myContext.getSupportActionBar().setTitle(myContext.getResources().getString(R.string.action_bar_action_title));
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE))
                newTaskFlatRequest = Parcels.unwrap(args.getParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE));
        } else if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE))
                newTaskFlatRequest = Parcels.unwrap(savedInstanceState.getParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE));
        }

        list = new ArrayList<ActionFlatRequest>(newTaskFlatRequest.getActions());
        animating = false;
        lastScrollPosition = 0;

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

        View root = inflater.inflate(R.layout.fragment_actions, container, false);
        errorTextView = (TextView) root.findViewById(R.id.textViewError);
        actionsListView = (ObservableListView) root.findViewById(R.id.list_actions);
        actionsListView.setScrollViewCallbacks(new ObservableScrollViewCallbacks() {
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

        if (list.size() == 0)
            errorTextView.setVisibility(View.VISIBLE);

        actionsAdapter = new ActionsAdapter(getActivity(), list, this);
        actionsListView.setAdapter(actionsAdapter);

        actionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ActionFlatRequest item = (ActionFlatRequest) adapterView.getItemAtPosition(position);
                Fragment f = null;
                switch (item.getType()) {
                    case ACTIVITY_DETECTION:
                        f = ActivityDetectionFragment.newInstance(newTaskFlatRequest, item, fragmentSwitcher);
                        fragmentSwitcher.switchContent(f, false);
                        break;
                    case PHOTO:
                        f = PhotoActionFragment.newInstance(newTaskFlatRequest, item, fragmentSwitcher);
                        fragmentSwitcher.switchContent(f, false);
                        break;
                    case QUESTIONNAIRE:
                        f = QuestionaireActionFragment.newInstance(newTaskFlatRequest, item, fragmentSwitcher);
                        fragmentSwitcher.switchContent(f, false);
                        break;
                    case SENSING_MOST:
                        f = PassiveSensingActionFragment.newInstance(newTaskFlatRequest, item, fragmentSwitcher);
                        fragmentSwitcher.switchContent(f, false);
                        break;
                }

            }
        });
        menu = (FloatingActionsMenu) root.findViewById(R.id.multiple_actions);
        fab = (FloatingActionButton) root.findViewById(R.id.action_activity_detection);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasActivityDetection(newTaskFlatRequest)) {
                    Fragment f = ActivityDetectionFragment.newInstance(newTaskFlatRequest, fragmentSwitcher);
                    fragmentSwitcher.switchContent(f, false);
                } else
                    Snackbar.with(myContext).text(myContext.getResources().getString(R.string.action_activity_detection_added)).show(myContext);

            }
        });

        fab = (FloatingActionButton) root.findViewById(R.id.action_photo);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment f = PhotoActionFragment.newInstance(newTaskFlatRequest, fragmentSwitcher);
                fragmentSwitcher.switchContent(f, false);
            }
        });

        fab = (FloatingActionButton) root.findViewById(R.id.action_questionnaire);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment f = QuestionaireActionFragment.newInstance(newTaskFlatRequest, fragmentSwitcher);
                fragmentSwitcher.switchContent(f, false);
            }
        });

        fab = (FloatingActionButton) root.findViewById(R.id.action_sensing);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment f = PassiveSensingActionFragment.newInstance(newTaskFlatRequest, fragmentSwitcher);
                fragmentSwitcher.switchContent(f, false);
            }
        });

        return root;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CreateTaskActivity.KEY_NEW_TASKFLAT_PARCELABLE, Parcels.wrap(newTaskFlatRequest));
    }


    private boolean hasActivityDetection(TaskFlatRequest newTaskFlatRequest) {
        for (ActionFlatRequest n : newTaskFlatRequest.getActions()) {
            if (n.getType().equals(ActionType.ACTIVITY_DETECTION))
                return true;
        }
        return false;
    }

    public void setFragmentSwitcher(FragmentSwitcher fragmentSwitcher) {
        this.fragmentSwitcher = fragmentSwitcher;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_actions, menu);
        if(newTaskFlatRequest.getActions()!= null && newTaskFlatRequest.getActions().size() > 0)
            confirmItem = menu.findItem(R.id.confirm_action).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment f = null;
        f = CreateTaskFragment.newInstance(newTaskFlatRequest, fragmentSwitcher);
        fragmentSwitcher.switchContent(f, false);
        return true;
    }

    @Override
    public void onActionRemoved(int position) {
        newTaskFlatRequest.getActions().remove(list.get(position));
        list.remove(position);
        actionsAdapter.notifyDataSetChanged();
        boolean sensingIn = false;
        for (ActionFlatRequest a : newTaskFlatRequest.getActions()) {
            if (a.getType().equals(ActionType.SENSING_MOST))
                sensingIn = true;
        }
        if (!sensingIn)
            newTaskFlatRequest.setSensingDuration(null);
        if(newTaskFlatRequest.getActions()!= null && newTaskFlatRequest.getActions().size() == 0)
            confirmItem.setVisible(false);
    }
}
