package jhen.example.audio.config;

import java.util.List;
import jhen.example.audio.app.AudioClientManager.AudioData;

public interface AudioContext {
	
	public void putRecordData(byte[] data, int size);
	
	public void putTrackData(byte[] data, int size);
	
	public List<AudioData> getDataList();
	
	public void setRecording(boolean isRecording);
	
	public boolean isRecording();
}
