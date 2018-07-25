/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */
package org.most;

import java.util.HashMap;
import java.util.Map;

import org.most.input.Input;

import android.os.PowerManager.WakeLock;

/**
 * This class is a proxy that limits activation/deactivation of inputs. There
 * are four voters that can vote if an {@link Input} should be active or not:
 * <p/>
 * <ul>
 * <li>Sensing, i.e., pipelines that need a given input to work;</li>
 * <li>Power, i.e., duty cycling policies (e.g., {@link DutyCyclePolicy} class
 * or {@link IPowerPolicy}) that can shut down an input too save power;</li>
 * <li>Event, i.e., external events that may require the an Input to stop (e.g.,
 * locking screen, incoming phone call);</li>
 * <li>User, i.e., the user wants to pause an input for privacy reasons.</li>
 * </ul>
 * <p/>
 * If all the voters agree, the InputsArbiter acquires the {@link WakeLock} (via
 * {@link WakeLockHolder}) and starts the input. If any of the voters wants to
 * shutdown an Input, the Input is deactivated and if there are no other Inputs
 * running then the WakeLock is released.
 */
public class InputsArbiter {

    private Map<Input.Type, Boolean> _sensingVotes;
    private Map<Input.Type, Boolean> _userVotes;
    private Map<Input.Type, Boolean> _eventVotes;
    private Map<Input.Type, Boolean> _powerVotes;

    private final MoSTApplication _context;

    public InputsArbiter(MoSTApplication _context) {
        this._context = _context;
        // getting the actual state of WakeLock.
        // NOTE now disabled for debug
        // _wakeLockVote = _context.getWakeLockHolder().isAcquired();
        _sensingVotes = new HashMap<Input.Type, Boolean>();
        _userVotes = new HashMap<Input.Type, Boolean>();
        _eventVotes = new HashMap<Input.Type, Boolean>();
        _powerVotes = new HashMap<Input.Type, Boolean>();
    }

    public void setUserVote(Input.Type type, boolean vote) {
        _userVotes.put(type, vote);
        evaluation(type);
    }

    public void setEventVote(Input.Type type, boolean vote) {
        _eventVotes.put(type, vote);
        evaluation(type);
    }

    public void setPowerVote(Input.Type type, boolean vote) {
        _powerVotes.put(type, vote);
        evaluation(type);
    }

    public void setSensingVote(Input.Type type, boolean vote) {
        _sensingVotes.put(type, vote);
        evaluation(type);
        if (_context.getInputManager().getPowerPolicyForInput(type) != null) {
            if (vote)
                _context.getInputManager().getPowerPolicyForInput(type).start();
            else
                _context.getInputManager().getPowerPolicyForInput(type).stop();
        }
    }

    private void evaluation(Input.Type type) {
        // if there is no vote for a specific Input Type, the default value
        // is true.
        boolean sensingVote = _sensingVotes.containsKey(type) ? _sensingVotes.get(type) : false;
        boolean userVote = _userVotes.containsKey(type) ? _userVotes.get(type) : true;
        boolean eventVote = _eventVotes.containsKey(type) ? _eventVotes.get(type) : true;
        boolean powerVote = _powerVotes.containsKey(type) ? _powerVotes.get(type) : true;
        if (sensingVote && userVote && eventVote && powerVote) {
            if (_context.getInputManager().getInput(type).isWakeLockNeeded())
                _context.getWakeLockHolder().acquireWL();
            _context.getInputManager().activateInput(type);
        } else if (_context.getInputManager().isInputAvailable(type)) {
            _context.getInputManager().deactivateInput(type);
            if (_context.getInputManager().getInput(type).isWakeLockNeeded())
                _context.getWakeLockHolder().releaseWL();
        }
    }

}
