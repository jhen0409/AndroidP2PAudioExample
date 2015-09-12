package jhen.example.audio.codec;

import android.util.Log;
import jhen.example.audio.app.MyThread;
import jhen.example.audio.config.AudioContext;

public class Encoder extends MyThread {
	private Codec codec = Codec.instance();
	private AudioContext context;
	private volatile int leftSize = 0;
	private boolean isRecording;
	private byte[] data = new byte[2048];
	private byte[] encodeData = new byte[2048];
	
	public Encoder(AudioContext context) {
		this.context = context;
	}
	
	public void run() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

		int getSize = 0;
		while (this.isRecording()) {
			synchronized (lock) {
				while (isIdle()) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			synchronized (lock) {
				Log.i("TAG", "encode size=" + leftSize);
				getSize = codec.encode(data, 0, leftSize, encodeData, 0);
				Log.i("TAG", "get size=" + getSize);
				setIdle();
			}
			if (getSize > 0) {
				context.putRecordData(encodeData, getSize);
			}
		}
	}
	
	public void putData(byte[] data, int size) {
		synchronized (lock) {
			System.arraycopy(data, 0, this.data, 0, size);
			this.leftSize = size;
			lockNotify();
		}
	}
	
	public boolean isIdle() {
		synchronized (lock) {
			return leftSize == 0 ? true : false;
		}
	} 

	public void setIdle() {
		leftSize = 0;
	}
	
	public void setRecording(boolean isRecording) {
		this.isRecording = isRecording;
	}

	public boolean isRecording() {
		return isRecording;
	}
}
