package jhen.example.audio.io;

import jhen.example.audio.app.AudioClientManager.AudioData;
import jhen.example.audio.app.MyThread;
import jhen.example.audio.config.AudioContext;
import jhen.example.audio.config.Constants;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class PCMTracker extends MyThread implements Constants {
	private AudioTrack audioTrack;
	private AudioContext context;
	private AudioData tData;
	private boolean isTracking = false;
	
	public PCMTracker(AudioContext context) {
		this.context = context;
	}

	@Override
	public void run() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		
		isTracking = true;
		
		try {
			int bufferSize = AudioTrack.getMinBufferSize(FREQUENCY,
					CHANNEL_CONFIGURATION, AUDIO_ENCODING);
			
			audioTrack = new AudioTrack(
					AudioManager.STREAM_VOICE_CALL, FREQUENCY,
					CHANNEL_CONFIGURATION, AUDIO_ENCODING, bufferSize,
					AudioTrack.MODE_STREAM);
			audioTrack.play();
			
			while (isTracking) {
				while (context.getDataList().size() == 0) {
					synchronized (lock) {
						try {
							lock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				if (context.getDataList().size() > 0) {
					tData = context.getDataList().remove(0);
					audioTrack.write(tData.getData(), 0, tData.size());
				}
			}
			
			audioTrack.stop();
		} catch (Exception e) {
			Log.e(AUDIO_TAG + " AudioTrack", e.getMessage());
		}
	}
	
	public void trackStop() {
		isTracking = false;
	}
	
	public boolean isTracking() {
		return isTracking;
	}
}