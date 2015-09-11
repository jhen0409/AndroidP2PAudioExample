package jhen.example.audio.codec;

import android.util.Log;
import jhen.example.audio.app.MyThread;
import jhen.example.audio.config.AudioContext;

public class Decoder extends MyThread {
	private Codec codec = Codec.instance();
	private AudioContext context;
	private volatile int leftSize = 0;
	private boolean isTracking;
	private byte[] data = new byte[2048];
	private byte[] decodeData = new byte[2048];
	
	public Decoder(AudioContext context) {
		this.context = context;
	}
	
	public void run() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

		int getSize = 0;
		while (isTracking()) {
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
				Log.i("TAG", "decode size = " + leftSize);
				getSize = codec.decode(this.data, 0, leftSize, decodeData, 0);
				Log.i("TAG", "get size = " + getSize);
				setIdle();
			}
			if (getSize > 0) {
				context.putTrackData(decodeData, getSize);
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
	
	public void setTracking(boolean isTracking) {
		this.isTracking = isTracking;
	}

	public boolean isTracking() {
		return isTracking;
	}
}
