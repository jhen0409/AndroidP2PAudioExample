package jhen.example.audio.app;

public abstract class MyThread extends Thread {
	protected final Object lock = new Object();
	
	public void lockNotify() {
		synchronized (lock) {
			lock.notify();
		}
	}
}
