/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.views.cards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import it.unibo.participact.R;
import it.unibo.participact.domain.rest.ResponseMessage;
import it.unibo.participact.network.request.ApacheHttpSpiceService;

public class ButtonCard extends Card {

    private SpiceManager spiceManager = new SpiceManager(ApacheHttpSpiceService.class);


    public ButtonCard(String title, String desc) {
        super(title, desc);
    }

    @Override
    public View getCardContent(final Context context) {

        View view = LayoutInflater.from(context).inflate(R.layout.card_button, null);

        TextView titleTextView = (TextView) view.findViewById(R.id.title);
        titleTextView.setText(title);
        TextView descriptionTextView = (TextView) view.findViewById(R.id.description);

        descriptionTextView.setText(desc);
        descriptionTextView.setTextColor(context.getResources().getColor(R.color.secondary_text));

        ((Button) view.findViewById(R.id.button)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    File src = v.getContext().getDatabasePath("domain.db");
                    FileInputStream inStream = new FileInputStream(src);
                    FileOutputStream outStream = new FileOutputStream(new File(v.getContext().getExternalFilesDir(null) + "/domaincopy.db"));
                    FileChannel inChannel = inStream.getChannel();
                    FileChannel outChannel = outStream.getChannel();
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                    inStream.close();
                    outStream.close();
//				    
//					org.most.StateUtility.deleteState(context);

//				    File src = new File(v.getContext().getExternalFilesDir(null) + "/domaincopy.db");
//					FileInputStream inStream = new FileInputStream(src);
//				    FileOutputStream outStream = new FileOutputStream(v.getContext().getDatabasePath("domain.db"));
//				    FileChannel inChannel = inStream.getChannel();
//				    FileChannel outChannel = outStream.getChannel();
//				    inChannel.transferTo(0, inChannel.size(), outChannel);
//				    inStream.close();
//				    outStream.close();

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
//				Context context = v.getContext();
//				if(!spiceManager.isStarted()){
//					spiceManager.start(context);
//				}
//				
//				DataUploader.getInstance(context).uploadDataAccelerometer();
//				DataUploader.getInstance(context).uploadOverWifi();
            }
        });

        return view;
    }

    class DataListener implements RequestListener<ResponseMessage> {

        @Override
        public void onRequestFailure(SpiceException arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onRequestSuccess(ResponseMessage arg0) {
            // TODO Auto-generated method stub
        }

    }


}
