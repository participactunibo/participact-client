/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.views.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import it.unibo.participact.R;

public class DrawerMenuAdapter extends BaseAdapter {

    private final int resource;
    private int selected;
    private String[] mMenu;
    private Context context;

    private TextView taskHistoryView;
    private TextView taskAvailableView;
    private TextView taskActiveView;
    private TextView taskCreatedView;

    private TextView currentSelectedView;

    private static final int TASK_INDEX = 1;
    private static final int TASK_CREATED_INDEX = 2;
    private static final int TASK_ACTIVE_INDEX = 3;
    private static final int TASK_AVAILABLE_INDEX = 4;
    private static final int TASK_HISTORY_INDEX = 5;


    public DrawerMenuAdapter(Context context, int resource, String[] objects) {
        if (context == null)
            throw new NullPointerException();
        if (objects == null)
            throw new NullPointerException();
        this.resource = resource;
        mMenu = objects;
        this.context = context;

    }


    @Override
    public int getCount() {
        return mMenu.length;
    }

    @Override
    public Object getItem(int position) {
        return mMenu[position];
    }

    @Override
    public int getItemViewType(int position) {
        if (position == TASK_CREATED_INDEX || position == TASK_ACTIVE_INDEX || position == TASK_AVAILABLE_INDEX || position == TASK_HISTORY_INDEX)
            return 1;
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        ViewHolderWithIcon holderWithIcon = null;

        String title = mMenu[position];

        if (convertView == null) {
            convertView = new FrameLayout(context);
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater li = (LayoutInflater) context.getSystemService(
                    inflater);
            if (getItemViewType(position) == 0) {
                li.inflate(resource, (ViewGroup) convertView, true);
                holderWithIcon = new ViewHolderWithIcon();
                holderWithIcon.textView = (TextView) convertView.findViewById(R.id.drawer_menu_item);
                holderWithIcon.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                convertView.setTag(holderWithIcon);
            } else {
                li.inflate(R.layout.drawer_nav_subitem_layout, (ViewGroup) convertView, true);
                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(R.id.drawer_sub_menu_item);
                if (position == TASK_AVAILABLE_INDEX)
                    taskAvailableView = holder.textView;
                else if (position == TASK_ACTIVE_INDEX)
                    taskActiveView = holder.textView;
                else if (position == TASK_HISTORY_INDEX)
                    taskHistoryView = holder.textView;
                else if (position == TASK_CREATED_INDEX)
                    taskCreatedView = holder.textView;
                convertView.setTag(holder);
            }

        } else {
            if (getItemViewType(position) == 0) {
                holderWithIcon = (ViewHolderWithIcon) convertView.getTag();
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
        }

        if (holderWithIcon != null) {
            holderWithIcon.textView.setText(title);
            holderWithIcon.imageView.setImageDrawable(getDrawableIdForItem(position, ((position == selected) || (position == TASK_INDEX && (selected == TASK_ACTIVE_INDEX || selected == TASK_AVAILABLE_INDEX || selected == TASK_CREATED_INDEX || selected == TASK_HISTORY_INDEX)))));
            if (selected == position) {
                holderWithIcon.textView.setTypeface(null, Typeface.BOLD);
                holderWithIcon.textView.setTextColor(context.getResources().getColor(R.color.primary));
                currentSelectedView = holderWithIcon.textView;
            } else {
                holderWithIcon.textView.setTypeface(null, Typeface.NORMAL);
                holderWithIcon.textView.setTextColor(context.getResources().getColor(R.color.secondary_text));

            }

        }

        if (holder != null) {
            holder.textView.setText(title);
            if (selected == position) {
                holder.textView.setTypeface(null, Typeface.BOLD);
                holder.textView.setTextColor(context.getResources().getColor(R.color.primary));
                currentSelectedView = holder.textView;
            } else {
                holder.textView.setTypeface(null, Typeface.NORMAL);
                holder.textView.setTextColor(context.getResources().getColor(R.color.secondary_text));
            }

        }

        return convertView;
    }

    public void setSelected(View view, int selected) {

        TextView textView;
        if (selected == TASK_CREATED_INDEX || selected == TASK_ACTIVE_INDEX || selected == TASK_AVAILABLE_INDEX || selected == TASK_HISTORY_INDEX)
            textView = (TextView) view.findViewById(R.id.drawer_sub_menu_item);
        else
            textView = (TextView) view.findViewById(R.id.drawer_menu_item);
        currentSelectedView.setTypeface(null, Typeface.NORMAL);
        currentSelectedView = textView;
        currentSelectedView.setTypeface(null, Typeface.BOLD);
        this.selected = selected;
        notifyDataSetChanged();
    }

    public void toggleTaskSubMenu() {

        if (taskAvailableView.isShown() && taskActiveView.isShown() && taskHistoryView.isShown() && taskCreatedView.isShown()) {
            AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
            fadeOut.setDuration(400);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    return;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    taskActiveView.setVisibility(View.GONE);
                    taskAvailableView.setVisibility(View.GONE);
                    taskHistoryView.setVisibility(View.GONE);
                    taskCreatedView.setVisibility(View.GONE);


                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    return;
                }
            });
            taskActiveView.startAnimation(fadeOut);
            taskAvailableView.startAnimation(fadeOut);
            taskHistoryView.startAnimation(fadeOut);
            taskCreatedView.startAnimation(fadeOut);

        } else {

            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setDuration(400);

            taskActiveView.startAnimation(fadeIn);
            taskAvailableView.startAnimation(fadeIn);
            taskHistoryView.startAnimation(fadeIn);
            taskCreatedView.startAnimation(fadeIn);

            taskActiveView.setVisibility(View.VISIBLE);
            taskAvailableView.setVisibility(View.VISIBLE);
            taskHistoryView.setVisibility(View.VISIBLE);
            taskCreatedView.setVisibility(View.VISIBLE);


        }
    }

    private Drawable getDrawableIdForItem(int position, boolean selected) {

        if (selected) {
            Drawable d = null;
            switch (position) {
                case 0:
                    d = context.getResources().getDrawable(R.drawable.ic_home_red_36dp);
                    return d;
                case 1:
                    d = context.getResources().getDrawable(R.drawable.ic_work_red_36dp);
                    return d;
                case 6:
                    d = context.getResources().getDrawable(R.drawable.ic_settings_input_composite_red_36dp);
                    return d;
                case 7:
                    d = context.getResources().getDrawable(R.drawable.ic_person_red_36dp);
                    return d;
                case 8:
                    d = context.getResources().getDrawable(R.drawable.ic_poll_red_36dp);
                    return d;
                case 9:
                    d = context.getResources().getDrawable(R.drawable.ic_group_red_36dp);
                    return d;
                case 10:
                    d = context.getResources().getDrawable(R.drawable.ic_trending_up_red_36dp);
                    return d;
                case 11:
                    d = context.getResources().getDrawable(R.drawable.ic_question_answer_red_36dp);
                    return d;
                case 12:
                    d = context.getResources().getDrawable(R.drawable.ic_info_red_36dp);
                    return d;
            }

        } else {
            switch (position) {
                case 0:
                    return context.getResources().getDrawable(R.drawable.ic_home_grey600_36dp);
                case 1:
                    return context.getResources().getDrawable(R.drawable.ic_work_grey600_36dp);
                case 6:
                    return context.getResources().getDrawable(R.drawable.ic_settings_input_composite_grey600_36dp);
                case 7:
                    return context.getResources().getDrawable(R.drawable.ic_person_grey600_36dp);
                case 8:
                    return context.getResources().getDrawable(R.drawable.ic_poll_grey600_36dp);
                case 9:
                    return context.getResources().getDrawable(R.drawable.ic_group_grey600_36dp);
                case 10:
                    return context.getResources().getDrawable(R.drawable.ic_trending_up_grey600_36dp);
                case 11:
                    return context.getResources().getDrawable(R.drawable.ic_question_answer_grey600_36dp);
                case 12:
                    return context.getResources().getDrawable(R.drawable.ic_info_grey600_36dp);
            }
        }

        return null;

    }

    static class ViewHolderWithIcon {
        TextView textView;
        ImageView imageView;
    }

    static class ViewHolder {
        TextView textView;
    }


}
