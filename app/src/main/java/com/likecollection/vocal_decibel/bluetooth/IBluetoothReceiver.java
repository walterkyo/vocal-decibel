/**
 * 
 */
package com.likecollection.vocal_decibel.bluetooth;

import android.app.Activity;

/**
 * @author Mauricio Dau mauricio.dau@endeeper.com
 *
 */
public interface IBluetoothReceiver {

	public void setOnBluetoothReceiveListener(IBluetoothReceiveListener listener);
	
	public void setContext(Activity activity);
	
	public void start();
	
	public void stop();

	public void close();
}
