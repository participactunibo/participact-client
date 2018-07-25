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

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import it.unibo.participact.R;
import it.unibo.participact.domain.data.Data;
import it.unibo.participact.domain.data.DataQuestionaireClosedAnswer;
import it.unibo.participact.domain.persistence.ClosedAnswer;
import it.unibo.participact.domain.persistence.Question;
import it.unibo.participact.domain.persistence.support.DomainDBHelper;

public class SingleChoiceFragment extends ListFragment implements QuestionaireFragment {

    private static final String ARG_KEY = "key";

    private Question question;
    private List<String> mChoices;
    private ClosedAnswer selectedAnswer;
    private List<Data> answers;
    private ListView listView;
    private QuestionnaireActivity qActivity;
    private ArrayList<ClosedAnswer> closedAns;

    public static Fragment create(Question question) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_KEY, question);

        SingleChoiceFragment fragment = new SingleChoiceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SingleChoiceFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        question = (Question) args.getSerializable(ARG_KEY);

        DomainDBHelper databaseHelper = OpenHelperManager.getHelper(getActivity(), DomainDBHelper.class);
        RuntimeExceptionDao<Question, Long> questionDao = databaseHelper.getRuntimeExceptionDao(Question.class);

        questionDao.refresh(question);
        closedAns = new ArrayList<ClosedAnswer>(question.getClosed_answers());

        OpenHelperManager.releaseHelper();


        mChoices = new ArrayList<String>();
        for (int i = 0; i < closedAns.size(); i++) {
            mChoices.add(closedAns.get(i).getAnswerDescription());
        }

        selectedAnswer = null;
        answers = new LinkedList<Data>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_page, container, false);
        ((TextView) rootView.findViewById(android.R.id.title)).setText(question.getQuestion());

        listView = (ListView) rootView.findViewById(android.R.id.list);
        setListAdapter(new ArrayAdapter<String>(getActivity(),
                R.layout.questionnaire_single_choice_row_layout,
                android.R.id.text1,
                mChoices));
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        initView();
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if ((activity instanceof QuestionnaireActivity)) {
            qActivity = (QuestionnaireActivity) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        qActivity = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
//    	Toast.makeText(getActivity(), "Selected " + closedAns.get(position).getAnswerDescription(), Toast.LENGTH_LONG).show();
        answers.clear();
        for (int i = 0; i < closedAns.size(); i++) {
            DataQuestionaireClosedAnswer answer = new DataQuestionaireClosedAnswer();
            answer.setClosedAnswer(closedAns.get(i));
            answer.setSampleTimestamp(System.currentTimeMillis());
            answer.setAnswer_value((i == position) ? true : false);
            answers.add(answer);
        }
        selectedAnswer = closedAns.get(position);
    }

    @Override
    public boolean isCompleted() {
        if (selectedAnswer != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<Data> getData() {
        return answers;
    }

    public void initView() {
        if (qActivity != null) {
            List<Data> data = qActivity.answers.get(question.getId());

            if (data != null) {
                answers = data;
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {

                        if (answers == null || answers.size() == 0) {
                            return;
                        }
                        String selected = "";
                        for (Data d : answers) {
                            if (d instanceof DataQuestionaireClosedAnswer) {
                                DataQuestionaireClosedAnswer closed = (DataQuestionaireClosedAnswer) d;
                                if (closed.isAnswer_value()) {
                                    selected = closed.getClosedAnswer().getAnswerDescription();
                                    selectedAnswer = closed.getClosedAnswer();
                                }
                            }
                        }
                        for (int i = 0; i < mChoices.size(); i++) {
                            if (selected.equals(mChoices.get(i))) {
                                listView.setItemChecked(i, true);
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public Question getQuestion() {
        return question;
    }
}
