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

import org.most.input.Input;
import org.most.input.InputBus;
import org.most.input.InputBus.SingleInputBus;
import org.most.pipeline.Pipeline;

import android.util.Log;

/**
 * One stop shop for activating and deactivating {@link Pipeline}s. It takes
 * care of instantiating them, wiring them to Inputs, starting inputs if needed
 * and shutting them down when no Pipeline are attached to them.
 * 
 */
public class Controller {

	private final String TAG = Controller.class.getSimpleName();

	private final MoSTApplication _context;
	private final InputBus _inputBus;
	private final PipelineManager _pipelineManager;

	public Controller(MoSTApplication context) {
		_context = context;
		_inputBus = _context.getInputBus();
		_pipelineManager = _context.getPipelineManager();
	}

	/**
	 * Activates a {@link Pipeline}. All the {@link Input}s needed by the
	 * pipeline will be automatically instantiated and inited. If for any reason
	 * an Input can not be inited (e.g., setting up a pipeline that needs access
	 * to the microphone while a call is in place), then its instatiation will
	 * be deferred until it is available again.
	 * 
	 * @param pipelineType
	 *            Pipeline to activate.
	 * @return <code>true</code> if the Pipeline was already available or was
	 *         successfully loaded and inited, <code>false</code> otherwise.
	 */
	public boolean activatePipeline(Pipeline.Type pipelineType) {
		Pipeline pipeline = _pipelineManager.getPipeline(pipelineType);
		if (pipeline == null) {
			Log.w(TAG, String.format("Unable to find pipeline %s", pipelineType));
			return false;
		}
		if (pipeline.getState() == Pipeline.State.ACTIVATED) {
			Log.i(TAG, String.format("Pipeline %s is already active", pipelineType));
			return true;
		}
		for (Input.Type inputType : pipeline.getInputs()) {
			_context.getInputsArbiter().setSensingVote(inputType, true);
		}
		_pipelineManager.activatePipeline(pipeline);
		return true;
	}

	/**
	 * Deactivates a {@link Pipeline}. All {@link Input}s that it uses are shut
	 * down, too, unless they are being currently used by another Pipeline.
	 * 
	 * @param pipelineType
	 *            The pipeline to deactivate.
	 * @return Always returns <code>true</code>.
	 */
	public boolean deactivatePipeline(Pipeline.Type pipelineType) {
		if (_pipelineManager.isPipelineAvailable(pipelineType)) {
			Pipeline pipeline = _pipelineManager.getPipeline(pipelineType);
			_pipelineManager.deactivatePipeline(pipeline);
			/*
			 * Deactivate unused inputs.
			 */
			for (Input.Type inputType : pipeline.getInputs()) {
				SingleInputBus singleInputBus = _inputBus.getBus(inputType);
				if (singleInputBus.getListenerCount() == 0) {
					_context.getInputsArbiter().setSensingVote(inputType, false);
				}
			}
		}
		return true;
	}
}
