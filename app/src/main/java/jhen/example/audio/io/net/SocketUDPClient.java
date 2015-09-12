package jhen.example.audio.io.net;

import jhen.example.audio.config.Constants;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SocketUDPClient implements Runnable, Constants {
	private byte[] recbuf = new byte[2048];
	private Context context;
	private DatagramSocket ds;
	private DatagramPacket rec = new DatagramPacket(recbuf, recbuf.length);
	private SocketAddress server;
	private SocketAddress pointAddr;
	private boolean isRegister = false;
	
	public SocketUDPClient(Context context) {
		this.context = context;
		server = new InetSocketAddress(SERVER_IP, SERVER_PORT);
	}
	
	public void start() throws Exception {
		println("start");
		ds = new DatagramSocket(30000);	//Dynamic port
		new Thread(this).start();
	}
	
	public boolean isRegister() {
		return isRegister;
	}
	
	public void run() {
		try {
			doSend(server, "register".getBytes(), "register".getBytes().length);
			ds.receive(rec);
			String[] msg = new String(rec.getData(), rec.getOffset(), rec.getLength()).split(":");
			pointAddr = new InetSocketAddress(msg[0], Integer.parseInt(msg[1]));
			broadcast("[System]: register success.");
			isRegister = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void broadcast(String message) {
		Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
    	intent.putExtra(EXTRA_MESSAGE, message);
    	context.sendBroadcast(intent);
	}
	
	public void doSend(String message) {
		try {
			println(message);
			doSend(pointAddr, message.getBytes(), message.getBytes().length);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void doSend(SocketAddress addr, byte[] data, int size) throws Exception {
		if (addr == null) {
			addr = pointAddr;
		}
		DatagramPacket pack = new DatagramPacket(data, size, addr);
		ds.send(pack);
	}
	
	public DatagramSocket getDatagramSocket() {
		return ds;
	}
	
	public void println(String s) {
		Log.i(SOCKET_TAG, new Date(System.currentTimeMillis()) + ": " + s);
	}
}