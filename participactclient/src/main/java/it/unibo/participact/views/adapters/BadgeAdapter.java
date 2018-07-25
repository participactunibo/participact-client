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
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import it.unibo.participact.R;
import it.unibo.participact.domain.rest.BadgeRestResult;

public class BadgeAdapter extends ArrayAdapter<BadgeRestResult> {

    private final int resource;
    private boolean shareBadges;

    public BadgeAdapter(Context context, int resource,
                        List<BadgeRestResult> objects, boolean shareBadges) {
        super(context, resource, objects);
        if (context == null)
            throw new NullPointerException();
        if (objects == null)
            throw new NullPointerException();
        this.resource = resource;
        this.shareBadges = shareBadges;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final BadgeRestResult item = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater li = (LayoutInflater) getContext().getSystemService(
                    inflater);
            convertView = li.inflate(resource, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) convertView
                    .findViewById(R.id.badge_title);
            holder.description = (TextView) convertView
                    .findViewById(R.id.badge_description);
            convertView.setTag(holder);

        } else
            holder = (ViewHolder) convertView.getTag();

        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());

        if (shareBadges) {

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent share = new Intent(android.content.Intent.ACTION_SEND);
                    share.setType("text/plain");

                    // Add data to the intent, the receiving app will decide
                    // what to do with it.
                    StringBuilder builder = new StringBuilder(getContext().getString(R.string.unlocked_badge));
                    builder.append(" ");
                    builder.append(item.getTitle());
                    builder.append(" - ");
                    builder.append("http://participact.ing.unibo.it/");
                    share.putExtra(Intent.EXTRA_TEXT, builder.toString());

                    getContext().startActivity(Intent.createChooser(share, getContext().getString(R.string.share_badge)));
                }
            });
        }


        return convertView;
    }

    static class ViewHolder {
        TextView title;
        TextView description;
    }
}
