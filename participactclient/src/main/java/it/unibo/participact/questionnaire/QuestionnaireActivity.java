/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.questionnaire;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.unibo.participact.R;
import it.unibo.participact.domain.data.Data;
import it.unibo.participact.domain.data.DataQuestionaireClosedAnswer;
import it.unibo.participact.domain.data.DataQuestionaireOpenAnswer;
import it.unibo.participact.domain.persistence.DataQuestionnaireFlat;
import it.unibo.participact.domain.persistence.Question;
import it.unibo.participact.domain.persistence.support.DomainDBHelper;

public class QuestionnaireActivity extends FragmentActivity {

    public static final String EXTRA_TASK_ID = "taskId";
    public static final String EXTRA_ACTION_ID = "actionId";
    public static final String EXTRA_QUESTION = "question";

    ViewPager mPager;

    private StepPagerStrip mStepPagerStrip;
    private MyPagerAdapter mPagerAdapter;

    Button nextButton;
    Button prevButton;

    Long taskId;
    Long actionId;
    List<Question> questions;
    Map<Integer, QuestionaireFragment> fragments;
    Map<Long, List<Data>> answers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }

        taskId = intent.getExtras().getLong(EXTRA_TASK_ID);
        actionId = intent.getExtras().getLong(EXTRA_ACTION_ID);
        questions = (List<Question>) intent.getExtras().getSerializable(EXTRA_QUESTION);

        fragments = new LinkedHashMap<Integer, QuestionaireFragment>();
        answers = new LinkedHashMap<Long, List<Data>>();

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);

        mStepPagerStrip = (StepPagerStrip) findViewById(R.id.strip);
        mStepPagerStrip.setOnPageSelectedListener(new StepPagerStrip.OnPageSelectedListener() {
            @Override
            public void onPageStripSelected(int position) {
                position = Math.min(mPagerAdapter.getCount() - 1,
                        position);
                if (mPager.getCurrentItem() != position) {
                    mPager.setCurrentItem(position);
                }
            }
        });
        mStepPagerStrip.setPageCount(questions.size());

        nextButton = (Button) findViewById(R.id.next_button);
        prevButton = (Button) findViewById(R.id.prev_button);

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mStepPagerStrip.setCurrentPage(position);
                updateBottomBar();
            }
        });


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPager.getCurrentItem() == questions.size() - 1) {
                    if (fragments.get(mPager.getCurrentItem()).isCompleted()) {

                        answers.put(fragments.get(mPager.getCurrentItem()).getQuestion().getId(), fragments.get(mPager.getCurrentItem()).getData());

                        DialogFragment dg = new DialogFragment() {
                            @Override
                            public Dialog onCreateDialog(Bundle savedInstanceState) {
                                return new AlertDialog.Builder(getActivity())
                                        .setMessage("Vuoi confermare?")
                                        .setPositiveButton(
                                                android.R.string.ok,
                                                new OnClickListener() {

                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        persistQuestionnaire();
                                                    }
                                                })
                                        .setNegativeButton(android.R.string.cancel,
                                                null).create();
                            }
                        };
                        dg.show(getSupportFragmentManager(), "place_order_dialog");
                    }
                } else {
                    if (fragments.get(mPager.getCurrentItem()).isCompleted()) {
                        answers.put(fragments.get(mPager.getCurrentItem()).getQuestion().getId(), fragments.get(mPager.getCurrentItem()).getData());
                        mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                    }
                }
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
            }
        });

        updateBottomBar();

    }

    private void persistQuestionnaire() {
        DomainDBHelper dbHelper;
        try {

            dbHelper = OpenHelperManager.getHelper(this, DomainDBHelper.class);
            RuntimeExceptionDao<DataQuestionnaireFlat, Long> dao = dbHelper.getRuntimeExceptionDao(DataQuestionnaireFlat.class);

            for (Map.Entry<Long, List<Data>> answer : answers.entrySet()) {

                for (Data data : answer.getValue()) {
                    DataQuestionnaireFlat quest = new DataQuestionnaireFlat();
                    quest.setTaskId(taskId);
                    quest.setActionId(actionId);
                    quest.setQuestionId(answer.getKey());
                    quest.setAnswerId(-1L);
                    quest.setClosedAnswerValue(false);
                    quest.setOpenAnswerValue("");

                    if (data instanceof DataQuestionaireOpenAnswer) {
                        DataQuestionaireOpenAnswer dataOpen = (DataQuestionaireOpenAnswer) data;
                        quest.setType(DataQuestionnaireFlat.TYPE_OPEN_ANSWER);
                        quest.setOpenAnswerValue(dataOpen.isAnswer_value());
                        quest.setTimestamp(dataOpen.getSampleTimestamp());

                    }

                    if (data instanceof DataQuestionaireClosedAnswer) {
                        DataQuestionaireClosedAnswer dataClosed = (DataQuestionaireClosedAnswer) data;
                        quest.setType(DataQuestionnaireFlat.TYPE_CLOSED_ANSWER);
                        quest.setTimestamp(dataClosed.getSampleTimestamp());
                        quest.setAnswerId(dataClosed.getClosedAnswer().getId());
                        quest.setClosedAnswerValue(dataClosed.isAnswer_value());
                    }
                    dao.create(quest);

                }

            }
            finishAndReturn();
        } finally {
            OpenHelperManager.releaseHelper();
        }
    }

    private void finishAndReturn() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_TASK_ID, taskId);
        returnIntent.putExtra(EXTRA_ACTION_ID, actionId);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Attenzione").setMessage("Sei sicuro di voler uscire dal questionario? Tutte i progressi verrano persi.").setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
                finish();
            }
        }).setNegativeButton(android.R.string.cancel, null);

        builder.create().show();

    }

    private void updateBottomBar() {
        int position = mPager.getCurrentItem();
        if (position == questions.size() - 1) {
            nextButton.setText("Finish");
            nextButton.setBackgroundColor(getResources().getColor(R.color.primary_dark));
            //nextButton.setBackgroundResource(R.drawable.finish_background);
            nextButton.setTextColor(getResources().getColor(R.color.icons));
        } else {
            nextButton.setText("Next");
            //nextButton.setBackgroundResource(R.drawable.selectable_item_background);
            //TypedValue v = new TypedValue();
            //getTheme().resolveAttribute(android.R.attr.textAppearanceMedium, v, true);
            //nextButton.setTextAppearance(this, v.resourceId);
            nextButton.setEnabled(true);
        }

        prevButton.setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);
    }


    public class MyPagerAdapter extends FragmentStatePagerAdapter {
        private int mCutOffPage;
        private Fragment mPrimaryItem;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i >= questions.size()) {
                return new Fragment();
            }

            Fragment fragment = FragmentFactory.getFragment(questions.get(i));
            fragments.put(i, (QuestionaireFragment) fragment);
            return fragment;
        }

        @Override
        public int getItemPosition(Object object) {
            // TODO: be smarter about this
            if (object == mPrimaryItem) {
                // Re-use the current fragment (its position never changes)
                return POSITION_UNCHANGED;
            }

            return POSITION_NONE;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position,
                                   Object object) {
            super.setPrimaryItem(container, position, object);
            mPrimaryItem = (Fragment) object;
        }

        @Override
        public int getCount() {
//			return Math.min(mCutOffPage + 1, questions == null ? 1
//					: questions.size() + 1);
            return questions.size();
        }

        public void setCutOffPage(int cutOffPage) {
            if (cutOffPage < 0) {
                cutOffPage = Integer.MAX_VALUE;
            }
            mCutOffPage = cutOffPage;
        }

        public int getCutOffPage() {
            return mCutOffPage;
        }
    }

}
