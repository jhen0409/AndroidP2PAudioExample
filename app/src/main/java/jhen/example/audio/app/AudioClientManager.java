package jhen.example.audio.app;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

import jhen.example.audio.app.MyThread;
import jhen.example.audio.config.AudioContext;
import jhen.example.audio.codec.Decoder;
import jhen.example.audio.io.PCMRecorder;
import jhen.example.audio.io.PCMTracker;
import jhen.example.audio.io.net.SocketUDPClient;

public class AudioClientManager implements AudioContext {
	private List<AudioData> recordList, trackList;
	private MyThread recordWriter, trackReader;
	private PCMRecorder recorder;
	private PCMTracker tracker;
	private SocketUDPClient client;
	private volatile boolean isRecording;
	private volatile boolean isTracking;
	
	public class AudioData {
		private int size;
		private byte[] data = new byte[2048];
		
		public AudioData(byte[] data, int size) {
			this.size = size;
			System.arraycopy(data, 0, this.data, 0, size);
		}
		
		public byte[] getData() {
			return data;
		}
		
		public int size() {
			return size;
		}
	}

	private class RecordWriter extends MyThread {

		public void run() {
			startPcmRecorder();

			while (isRecording()) {
				while (recordList.size() == 0) {
					synchronized (lock) {
						try {
							lock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				if (recordList.size() > 0) {
					AudioData tag = recordList.remove(0);
					try {
						Log.i("TAG", "send udp.");
						client.doSend(null, tag.data, tag.size);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			recorder.recordStop();
			
			AudioClientManager.this.stop();
		}
	};

	private class TrackReader extends MyThread {

		public void run() {
			startPcmTracker();

			Decoder decoder = new Decoder(AudioClientManager.this);
			decoder.setTracking(true);
			new Thread(decoder).start();

			byte[] buffer = new byte[2048];
			DatagramPacket rec = new DatagramPacket(buffer, buffer.length);

			while (isTracking()) {
				try {
					client.getDatagramSocket().receive(rec);
					if (decoder.isIdle()) {
						Log.i("TAG", "pcm decode size=" + rec.getLength());
						decoder.putData(rec.getData(), rec.getLength());
					}
				} catch (IOException e) {
					e.printStackTrace();
					isTracking = false;
				}
			}

			tracker.trackStop();
			decoder.setTracking(false);
			AudioClientManager.this.stop();
		}
	};
	
	public AudioClientManager(SocketUDPClient client)
			throws Exception {
		if (!client.isRegister()) {
			throw new Exception("Client not register.");
		}
		this.client = client;
		
		recordList = Collections.synchronizedList(new LinkedList<AudioData>());
		trackList = Collections.synchronizedList(new LinkedList<AudioData>());
	}

	public void start() {
		if (isRecording() && isTracking()) {
			recordWriter = new RecordWriter();
			recordWriter.start();
			trackReader = new TrackReader();
			trackReader.start();
		}
	}

	private void startPcmRecorder() {
		recorder = new PCMRecorder(this);
		new Thread(recorder).start();
	}

	private void startPcmTracker() {
		tracker = new PCMTracker(this);
		new Thread(tracker).start();
	}

	@Override
	public void putRecordData(byte[] data, int size) {
		recordList.add(new AudioData(data, size));
		recordWriter.lockNotify();
	}

	@Override
	public void putTrackData(byte[] data, int size) {
		trackList.add(new AudioData(data, size));
		tracker.lockNotify();
	}

	@Override
	public List<AudioData> getDataList() {
		return trackList;
	}

	public void stop() {
		if (!client.getDatagramSocket().isClosed()) {
			client.getDatagramSocket().close();
		}
	}

	public void setRecording(boolean isRecording) {
		this.isRecording = isRecording;
	}

	public boolean isRecording() {
		return isRecording;
	}

	public void setTracking(boolean isTracking) {
		this.isTracking = isTracking;
	}

	public boolean isTracking() {
		return isTracking;
	}
}
