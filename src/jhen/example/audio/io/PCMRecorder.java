package jhen.example.audio.io;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import jhen.example.audio.app.MyThread;
import jhen.example.audio.config.AudioContext;
import jhen.example.audio.config.Constants;
import jhen.example.audio.codec.Encoder;

public class PCMRecorder extends MyThread implements Constants {
	private AudioRecord audioRecord;
	private AudioContext context;
	private boolean isRecording = false;
	private final int FRAME_SIZE = 1920;
	
	public PCMRecorder(AudioContext context) {
		this.context = context;
	}
	
	@Override
	public void run() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		
		Encoder encoder = new Encoder(this.context);
		encoder.setRecording(true);
		new Thread(encoder).start();
		
		isRecording = true;
		try {
			int dataReadResult, dataSize = AudioRecord.getMinBufferSize(FREQUENCY,
					CHANNEL_CONFIGURATION, AUDIO_ENCODING);
			byte[] data = new byte[dataSize];
			
			audioRecord = new AudioRecord(
					MediaRecorder.AudioSource.MIC, FREQUENCY,
					CHANNEL_CONFIGURATION, AUDIO_ENCODING, dataSize);
			
			audioRecord.startRecording();
			
			while (isRecording) {
				dataReadResult = audioRecord.read(data, 0, FRAME_SIZE);

				if (encoder.isIdle() && dataReadResult > 0) {
					Log.i("TAG", "pcm encode size=" + dataReadResult);
					encoder.putData(data, dataReadResult);
				}
			}
			
			audioRecord.stop();
			encoder.setRecording(false);
		} catch (Exception e) {
			Log.e(AUDIO_TAG + " AudioRecord", e.getMessage());
		}
	}
	
	public void recordStop() {
		isRecording = false;
	}

	public boolean isRecording() {
		return isRecording;
	}
}
