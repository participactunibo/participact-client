/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */
package org.most.event;

import org.most.MoSTApplication;
import org.most.input.Input;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.speech.RecognizerIntent;
import android.util.Log;

public class SpeechRecognitionReceiver extends EventReceiver {

	private final String TAG = SpeechRecognitionReceiver.class.getSimpleName();
	
	public final static String PREF_KEY_SPEECHRECOGNITION_OFF_TIMER_MS = "SpeechRecognitionOffTimerMs";
	public final static long PREF_DEFAULT_SPEECHRECOGNITION_OFF_TIMER_MS = 1*60*1000; //one minute
	
	public static final String INTENT_SPEECHRECOGNITION_END = "Intent.SpeechRecognitionEnd";
	public static final int INTENT_SPEECHRECOGNITION_END_REQUCODE = 52461;
	
	@Override
	public IntentFilter getIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		// XXX: readd this filter
//		filter.addAction(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE);
		filter.addAction(RecognizerIntent.ACTION_WEB_SEARCH);
//		filter.addAction(Intent.ACTION_VOICE_COMMAND);
//		filter.addAction(Intent.ACTION_ASSIST);
		filter.addAction(INTENT_SPEECHRECOGNITION_END);
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		return filter;
	}

    @Override
    public void onReceive(Context context, Intent intent) {
    	MoSTApplication application = (MoSTApplication) context.getApplicationContext();
    	if(intent.getAction().equals(INTENT_SPEECHRECOGNITION_END)){
    		//End Speech recognition intent received -> vote to turn on microphone
    		application.getInputsArbiter().setEventVote(Input.Type.AUDIO, true);
	        Log.i(TAG, "End speech recognition intent received -> vote to turn on microphone");
    	}else{
	        //Speech recognition intent received -> vote to turn off microphone
	    	application.getInputsArbiter().setEventVote(Input.Type.AUDIO, false);
	        Log.i(TAG, "Speech recognition intent received -> vote to turn off microphone");
	        //set timer to restore microphone state after N seconds
	    	Intent i = new Intent();
			i.setAction(INTENT_SPEECHRECOGNITION_END);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, INTENT_SPEECHRECOGNITION_END_REQUCODE, i, PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			long next = context.getSharedPreferences(MoSTApplication.PREF_INPUT, Context.MODE_PRIVATE).getLong(PREF_KEY_SPEECHRECOGNITION_OFF_TIMER_MS, PREF_DEFAULT_SPEECHRECOGNITION_OFF_TIMER_MS);
			mgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+next, pendingIntent);
    	}
    }

}
