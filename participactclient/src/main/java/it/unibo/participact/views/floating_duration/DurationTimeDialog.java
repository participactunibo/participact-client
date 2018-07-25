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
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

import it.unibo.participact.R;

/**
 * Created by alessandro on 16/11/14.
 */
public class DurationTimeDialog extends Dialog {

    private int mCurrentDay = 0; // 0-99
    private int mCurrentHour = 0; // 0-23
    private int mCurrentMinute = 0; // 0-59


    private NumberPicker mDayPicker;
    private NumberPicker mHourPicker;
    private NumberPicker mMinutePicker;

    private Button confirmBtn;
    private Button dismissBtn;

    private String title;

    private DurationTimeListener durationTimeListener;

    public DurationTimeDialog(Context context, String title, DurationTimeListener durationTimeListener) {
        super(context);
        this.title = title;
        this.durationTimeListener = durationTimeListener;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.duration_dialogfragment);
        setTitle(title);

        mDayPicker = (NumberPicker) findViewById(R.id.day);
        mHourPicker = (NumberPicker) findViewById(R.id.hour);
        mMinutePicker = (NumberPicker) findViewById(R.id.minute);
        confirmBtn = (Button) findViewById(R.id.ok_btn);
        dismissBtn = (Button) findViewById(R.id.canc_btn);

        mDayPicker.setMaxValue(365);
        mDayPicker.setMinValue(0);

        mHourPicker.setMaxValue(23);
        mHourPicker.setMinValue(0);

        mMinutePicker.setMaxValue(59);
        mMinutePicker.setMinValue(0);


        mDayPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                mCurrentDay = newVal;

            }
        });
        mHourPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                mCurrentHour = newVal;
            }
        });
        mMinutePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                mCurrentMinute = newVal;
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //calling on listener
                durationTimeListener.onDurationTimeSubmitted(mCurrentDay, mCurrentHour, mCurrentMinute);
                dismiss();
            }
        });

        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }

    private void setDurationTimeListener(DurationTimeListener durationTimeListener) {
        this.durationTimeListener = durationTimeListener;
    }


}
