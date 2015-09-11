package jhen.example.audio.config;

import android.media.AudioFormat;

public interface Constants {
	public final String DISPLAY_MESSAGE_ACTION = "jhen.example.app.DISPLAY_MESSAGE";
	public final String EXTRA_MESSAGE = "message";
	
	public final String SOCKET_TAG = "Audio P2P Socket";
	public final String SERVER_IP = "192.168.1.200";
	public final int SERVER_PORT = 2008;
	
	public final String AUDIO_TAG = "Audio P2P";
	public final int FREQUENCY = 8000;
	public final int CHANNEL_CONFIGURATION = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	public final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
}
