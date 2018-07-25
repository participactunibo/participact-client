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
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import it.unibo.participact.R;
import it.unibo.participact.domain.data.Data;
import it.unibo.participact.domain.data.DataQuestionaireOpenAnswer;
import it.unibo.participact.domain.persistence.Question;

public class TextFragment extends Fragment implements QuestionaireFragment {
    protected static final String ARG_KEY = "key";

    private Question question;
    private String answerString;
    private DataQuestionaireOpenAnswer answer;
    private QuestionnaireActivity qActivity;

    protected EditText mEditTextInput;

    public static TextFragment create(Question question) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_KEY, question);

        TextFragment fragment = new TextFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        question = (Question) args.getSerializable(ARG_KEY);
        answerString = "";
        answer = new DataQuestionaireOpenAnswer();
        answer.setQuestion(question);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_page_text,
                container, false);
        ((TextView) rootView.findViewById(android.R.id.title)).setText(question.getQuestion());

        mEditTextInput = (EditText) rootView.findViewById(R.id.editTextInput);

        initView();

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if ((activity instanceof QuestionnaireActivity)) {
            this.qActivity = (QuestionnaireActivity) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        qActivity = null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEditTextInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
//				Toast.makeText(getActivity(), editable.toString(), Toast.LENGTH_LONG).show();
                answerString = editable.toString();
                answer.setSampleTimestamp(System.currentTimeMillis());
                answer.setAnswer_value(answerString);
            }

        });
    }


    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        // In a future update to the support library, this should override
        // setUserVisibleHint
        // instead of setMenuVisibility.
        if (mEditTextInput != null) {
            InputMethodManager imm = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (!menuVisible) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }

    @Override
    public boolean isCompleted() {
        if (TextUtils.isEmpty(answerString)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public List<Data> getData() {
        List<Data> result = new LinkedList<Data>();
        result.add(answer);
        return result;
    }

    public void initView() {
        if (qActivity != null) {
            List<Data> data = qActivity.answers.get(question.getId());
            if (data != null && data.size() > 0) {
                if (data.get(0) instanceof DataQuestionaireOpenAnswer) {
                    answer = (DataQuestionaireOpenAnswer) data.get(0);
                }
            }
            if (data != null && data.size() > 0) {
                if (data.get(0) instanceof DataQuestionaireOpenAnswer) {
                    answer = (DataQuestionaireOpenAnswer) data.get(0);
                }

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mEditTextInput.setText(answer.isAnswer_value());
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
