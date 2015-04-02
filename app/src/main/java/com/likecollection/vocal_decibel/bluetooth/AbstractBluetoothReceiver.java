/**
 * 
 */
package com.likecollection.vocal_decibel.bluetooth;

import android.app.Activity;

/**
 * @author Mauricio Dau mauricio.dau@endeeper.com
 *
 */
public abstract class AbstractBluetoothReceiver implements IBluetoothReceiver {

	protected static final int MAXIMUM_DECIBEL_VALUE = 99;
	
	protected IBluetoothReceiveListener listener;
	
	protected Activity activity;
	
	protected boolean isRunning = false;
	
	public AbstractBluetoothReceiver() {
		
	}
	
	@Override
	public final void setOnBluetoothReceiveListener(IBluetoothReceiveListener listener) {

		this.listener = listener;
	}
	
	@Override
	public final void setContext(Activity activity) {

		this.activity = activity;
	}
	
	@Override
	public final void start() {

		this.isRunning = true;
		
		this.onStart();
	}
	
	@Override
	public final void stop() {

		this.isRunning = false;
		
		this.onStop();
	}

	public abstract void onStart();
	
	public abstract void onStop();
	
}

