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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.List;

import it.unibo.participact.R;
import it.unibo.participact.domain.rest.UserRestResult;

public class FriendsSocialAdapter extends ArrayAdapter<UserRestResult> {

    private final int resource;
    private final FriendAddListener friendAddListener;

    public FriendsSocialAdapter(Context context, int resource, List<UserRestResult> objects, FriendAddListener listener) {
        super(context, resource, objects);
        if (context == null)
            throw new NullPointerException();
        if (objects == null)
            throw new NullPointerException();
        this.resource = resource;
        friendAddListener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        UserRestResult item = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater li = (LayoutInflater) getContext().getSystemService(
                    inflater);
            convertView = li.inflate(resource, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView
                    .findViewById(R.id.friends_social_item_name);
            holder.image = (ImageView) convertView
                    .findViewById(R.id.imageView);
            holder.button = (ImageButton) convertView
                    .findViewById(R.id.friends_social_add_button);
            convertView.setTag(holder);

        } else
            holder = (ViewHolder) convertView.getTag();

        StringBuilder stringBuilder = new StringBuilder(item.getName());
        stringBuilder.append(" ");
        stringBuilder.append(item.getSurname());
        holder.name.setText(stringBuilder.toString());

        StringBuilder initialsBuilder = new StringBuilder();
        initialsBuilder.append(item.getName().charAt(0));
        initialsBuilder.append(item.getSurname().charAt(0));
        TextDrawable drawable = TextDrawable.builder().buildRound(initialsBuilder.toString().toUpperCase(), ColorGenerator.DEFAULT.getColor(item.getId()));
        holder.image.setImageDrawable(drawable);

        holder.button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                friendAddListener.onFriendAdd(position);

            }
        });

        return convertView;
    }

    public interface FriendAddListener {
        public void onFriendAdd(int position);
    }

    static class ViewHolder {
        TextView name;
        ImageButton button;
        ImageView image;
    }
}
