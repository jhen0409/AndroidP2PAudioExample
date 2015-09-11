package jhen.example.audio.app;

import jhen.example.audio.config.Constants;
import jhen.example.audio.io.net.SocketUDPClient;
import android.content.*;
import android.os.*;
import android.app.Activity;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity implements Constants,
												View.OnClickListener {
	private boolean isRegister = false;
	private TextView mDisplay;
	private Button startBtn, stopBtn;
	private SocketUDPClient socket;
	private AudioClientManager audio;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		startBtn = (Button) findViewById(R.id.start_btn);
		stopBtn = (Button) findViewById(R.id.stop_btn);
		mDisplay = (TextView) findViewById(R.id.display);
		socket = new SocketUDPClient(this);

		startBtn.setOnClickListener(this);
		stopBtn.setOnClickListener(this);

		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				DISPLAY_MESSAGE_ACTION));
	}
	
	@Override
	public void onClick(View v) {
		if (v == startBtn) {
			startRegister();
		} else if (v == stopBtn) {
			isRegister = false;
			if (audio != null) {
				audio.setRecording(false);
				audio.setTracking(false);
			}
			startBtn.setEnabled(true);
		}
	}
	
	private void startRegister() {
		startBtn.setEnabled(false);
		try {
			socket.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		new Thread() {
			
			public void run() {
				while (!isRegister)
					;
				try {
					audio = new AudioClientManager(socket);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				audio.setRecording(true);
				audio.setTracking(true);
				audio.start();
			}
		}.start();
	}

	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
			mDisplay.append(newMessage + "\n");
			if ("[System]: register success.".equals(newMessage)) {
				isRegister = true;
			}
		}
	};
}