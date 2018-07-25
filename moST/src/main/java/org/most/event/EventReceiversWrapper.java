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

import java.util.LinkedList;
import java.util.List;

import android.content.Context;

public class EventReceiversWrapper {

	private Context _context;
	private List<EventReceiver> _receivers;
	
	public EventReceiversWrapper(Context context){
		_context = context;
		_receivers = new LinkedList<EventReceiver>();
		_receivers.add(new IncomingCallReceiver());
		_receivers.add(new OutgoingCallReceiver());
		_receivers.add(new SpeechRecognitionReceiver());
		_receivers.add(new ScreenOnReceiver());
		_receivers.add(new ScreenOffReceiver());
	}
	
	public void registerAllEventReceivers(){
		for(EventReceiver receiver: _receivers)
			_context.registerReceiver(receiver, receiver.getIntentFilter());
	}
	
	public void unregisterAllEventReceivers(){
		for(EventReceiver receiver: _receivers)
			_context.unregisterReceiver(receiver);
	}
}
