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
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.List;

import it.unibo.participact.R;
import it.unibo.participact.domain.rest.ScoreRestResult;

public class ScoreAdapter extends ArrayAdapter<ScoreRestResult> {

    private final int resource;
    private long highlightId;

    public ScoreAdapter(Context context, int resource,
                        List<ScoreRestResult> objects) {
        super(context, resource, objects);
        if (context == null)
            throw new NullPointerException();
        if (objects == null)
            throw new NullPointerException();
        this.resource = resource;
        this.highlightId = -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ScoreRestResult item = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            convertView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater li = (LayoutInflater) getContext().getSystemService(
                    inflater);
            li.inflate(resource, (ViewGroup) convertView, true);
            holder = new ViewHolder();
            holder.name = (TextView) convertView
                    .findViewById(R.id.leaderboard_item_name);
            holder.score = (TextView) convertView
                    .findViewById(R.id.leaderboard_item_score);
            holder.image = (ImageView) convertView
                    .findViewById(R.id.imageView);
            holder.linearLayout = (LinearLayout) convertView.findViewById(R.id.linearLayout);
            convertView.setTag(holder);

        } else
            holder = (ViewHolder) convertView.getTag();

        holder.name.setText(item.getUserName());
        holder.score.setText(String.valueOf(item.getScoreValue()));

        String[] initialsArray = item.getUserName().split(" ");
        StringBuilder initialsBuilder = new StringBuilder();
        if (initialsArray.length >= 2) {
            initialsBuilder.append(initialsArray[0].charAt(0));
            initialsBuilder.append(initialsArray[initialsArray.length - 1].charAt(0));
        } else
            initialsBuilder.append(item.getUserName().charAt(0));

        TextDrawable drawable = TextDrawable.builder().buildRound(initialsBuilder.toString().toUpperCase(), ColorGenerator.DEFAULT.getColor(item.getUserId()));
        holder.image.setImageDrawable(drawable);

        if (highlightId > 0 && highlightId == item.getUserId())
            holder.linearLayout.setBackgroundColor(adjustAlpha(getContext().getResources().getColor(R.color.accent), 0.2f));
        else
            holder.linearLayout.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
        return convertView;
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }


    public long getHighlightId() {
        return highlightId;
    }

    public void setHighlightId(long highlightId) {
        this.highlightId = highlightId;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView name;
        TextView score;
        ImageView image;
        LinearLayout linearLayout;
    }
}
