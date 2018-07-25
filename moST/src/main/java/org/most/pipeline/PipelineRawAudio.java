/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */
package org.most.pipeline;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;

import org.most.DataBundle;
import org.most.MoSTApplication;
import org.most.input.Input;
import org.most.input.InputAudio;

import android.util.Log;

/**
 * This pipeline receives audio data. Currently it does not propagate received
 * data to clients.
 *
 */
public class PipelineRawAudio extends Pipeline {

    private static final String TAG = PipelineRawAudio.class.getSimpleName();

    public PipelineRawAudio(MoSTApplication context) {
        super(context);
    }

    FileOutputStream fos = null;
    FileChannel fc;

    @Override
    public boolean onActivate() {
        /* BEGIN TESTING CODE */
        // try {
        // fos = new FileOutputStream(new
        // File(Environment.getExternalStorageDirectory(), "rawaudio"), true);
        // fc = fos.getChannel();
        // } catch (FileNotFoundException e) {
        // e.printStackTrace();
        // }
		/* END TESTING CODE */
        return super.onActivate();
    }

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        Log.d(TAG, "Deactivating");
        // try {
        // fc.close();
        // fos.close();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
    }

    public void onData(DataBundle b) {
        int size = b.getInt(InputAudio.KEY_AUDIODATA_LENGTH);
		/*
		 * data is a reference to the audio data contained in the DataBundle, it
		 * can be used as long as we do not call the b.release() method.
		 */
        short[] data = b.getShortArray(InputAudio.KEY_AUDIODATA);
        Log.d(TAG, String.format("Received %d shorts, first short data: %d",
                size, data[0]));

		/* BEGIN TESTING CODE */
		/*
		 * The following code dumps the received array to the output file as
		 * fast as possible
		 */
        if (fos != null) {
            try {
                byte[] byteData = new byte[size * 2];
                ByteBuffer bb = ByteBuffer.wrap(byteData);
                bb.asShortBuffer().put(data, 0, size);
                fc.write(bb);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		/* END TESTING CODE */
        b.release();
    }

    @Override
    public Set<Input.Type> getInputs() {
        Set<Input.Type> result = new HashSet<Input.Type>();
        result.add(Input.Type.AUDIO);
        return result;
    }

    @Override
    public Type getType() {
        return Type.RAW_AUDIO;
    }

}
