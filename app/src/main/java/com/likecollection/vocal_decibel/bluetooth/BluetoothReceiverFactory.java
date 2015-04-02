/**
 * 
 */
package com.likecollection.vocal_decibel.bluetooth;

/**
 * @author Mauricio Dau mauricio.dau@endeeper.com
 *
 */
public final class BluetoothReceiverFactory {

	private BluetoothReceiverFactory() { }
	
	public static IBluetoothReceiver newArduinoBTReceiver() {
		
		final IBluetoothReceiver instance = new ArduinoBluetooth();
		
		return instance;
	}
	
	public static IBluetoothReceiver newFakeBTReceiver() {
		
		final IBluetoothReceiver instance = new FakeBluetoothReceiver();
		
		return instance;
	}
	
}
