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

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.marvinlabs.widget.floatinglabel.FloatingLabelTextViewBase;
import com.marvinlabs.widget.floatinglabel.LabelAnimator;
import com.marvinlabs.widget.floatinglabel.anim.TextViewLabelAnimator;

import it.unibo.participact.R;

/**
 * Created by alessandro on 29/11/14.
 */
public abstract class FloatingLabelBaseDurationPicker extends FloatingLabelTextViewBase<TextView> {


    private static final String SAVE_STATE_KEY_DURATION = "saveStateDuration";


    public interface OnWidgetEventListener {
        public void onShowDurationPickerDialog(FloatingLabelBaseDurationPicker source);
    }

    public interface OnDurationPickerEventListener {
        public void onDurationChanged(FloatingLabelBaseDurationPicker source, String duration);
    }


    protected String durationString;

    protected OnWidgetEventListener widgetListener;

    protected OnDurationPickerEventListener durationPickerListener;


    // =============================================================================================
    // Lifecycle
    // ==

    public FloatingLabelBaseDurationPicker(Context context) {
        super(context);
    }

    public FloatingLabelBaseDurationPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatingLabelBaseDurationPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    // =============================================================================================
    // Overridden methods
    // ==

    @Override
    protected int getDefaultLayoutId() {
        return R.layout.flw_widget_floating_label_duration_picker;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        getInputWidget().setClickable(true);
        getInputWidget().setOnClickListener(inputWidgetClickListener);

    }

    @Override
    protected void restoreInputWidgetState(Parcelable inputWidgetState) {
        getInputWidget().onRestoreInstanceState(inputWidgetState);
    }

    @Override
    protected Parcelable saveInputWidgetInstanceState() {
        return getInputWidget().onSaveInstanceState();
    }

    @Override
    protected void putAdditionalInstanceState(Bundle saveState) {
        if (durationString != null) {
            saveState.putString(SAVE_STATE_KEY_DURATION, durationString);
        }
    }

    @Override
    protected void restoreAdditionalInstanceState(Bundle savedState) {
        durationString = savedState.getString(SAVE_STATE_KEY_DURATION);
    }

    @Override
    protected void setInitialWidgetState() {
        if (durationString == null) {
            setLabelAnchored(true);
            getInputWidget().setText("");
        } else {
            setLabelAnchored(false);
            getInputWidget().setText(durationString + " min");
        }
    }

    @Override
    protected LabelAnimator<TextView> getDefaultLabelAnimator() {
        return new TextViewLabelAnimator<TextView>();
    }


    // =============================================================================================
    // Instant picking
    // ==


    public void setSelectedInstant(String duration) {
        this.durationString = duration;
        onSelectedInstantChanged();
    }

    /**
     * Get the selected instant
     *
     * @return the instant (date or time)
     */
    public String getSelectedInstant() {
        return durationString;
    }

    /**
     * Refreshes the widget state when the selection changes
     */
    protected void onSelectedInstantChanged() {
        if (durationString == null) {
            getInputWidget().setText("");
            anchorLabel();
        } else {
            getInputWidget().setText(durationString + " min");
            floatLabel();
        }

        if (durationPickerListener != null)
            durationPickerListener.onDurationChanged(this, durationString);
    }

    /**
     * Show the item picker
     */
    protected void requestShowPicker() {
        if (widgetListener != null) widgetListener.onShowDurationPickerDialog(this);
    }

    // =============================================================================================
    // Other methods
    // ==

    public OnDurationPickerEventListener getDurationPickerListener() {
        return durationPickerListener;
    }

    public void setInstantPickerListener(OnDurationPickerEventListener durationPickerListener) {
        this.durationPickerListener = durationPickerListener;
    }

    public OnWidgetEventListener getWidgetListener() {
        return widgetListener;
    }

    public void setWidgetListener(OnWidgetEventListener widgetListener) {
        this.widgetListener = widgetListener;
    }


    /**
     * Listen to click events on the input widget
     */
    OnClickListener inputWidgetClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            requestShowPicker();
        }
    };
}
