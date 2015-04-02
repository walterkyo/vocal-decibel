/**
 * 
 */
package com.likecollection.vocal_decibel.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author Mauricio L. Dau <mauricioldau@gmail.com>
 *
 *	Class with static methods to handle the bluetooth connection with the Arduino.
 *
 */
class ArduinoBluetooth extends AbstractBluetoothReceiver {

	private static final String TAG = "ArduinoAndroid";
	public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private String address = "00:15:FF:F2:5A:5C"; // arduino address
	
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket btSocket = null;
    
    private InputStream inStream = null;
    
	Handler handler = new Handler();
	
	byte delimiter = 10;
	boolean stopWorker = false;
	int readBufferPosition = 0;
	byte[] readBuffer = new byte[1024];
	
//	private IBluetoothReceiveListener listener;
//	
//	public void setBluetoothListener(IBluetoothReceiveListener listener) {
//		
//		this.listener = listener;
//	}
	
	public ArduinoBluetooth() {
		
		this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();  
		
		if (!mBluetoothAdapter.isEnabled()) {
			
		    mBluetoothAdapter.enable(); 
		}
		
		mBluetoothAdapter.startDiscovery();
	}

	private void connect() {
		
        Log.d(TAG, address);
        
        final Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {

				try {
		        	
					BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
			        
			        Log.d(TAG, "Connecting to ... " + device);
			        
			        mBluetoothAdapter.cancelDiscovery();
					
		            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
		            btSocket.connect();
		            Log.d(TAG, "Connection made.");

		            beginListenForData();
		            
		        } catch (IOException e) {
		        	
		            try {
		            	
		                btSocket.close();
		                    
		            } catch (IOException e2) {
		            	
		                    Log.d(TAG, "Unable to end the connection");
		                    e2.printStackTrace();
		            }
		            
		            e.printStackTrace();
		            
		            Log.d(TAG, "Socket creation failed");
		        }
			}
		});
        
        t.start();
	}
	
	public void beginListenForData() {
		
		try {
			
			inStream = btSocket.getInputStream();
			
		} catch (IOException e) { }

		final Thread workerThread = new Thread(new Runnable() {
			
			public void run() {
				
				while (!Thread.currentThread().isInterrupted() && !stopWorker) {
					
					try {
						int bytesAvailable = inStream.available();
						
						if (bytesAvailable > 0) {
							byte[] packetBytes = new byte[bytesAvailable];
							
							inStream.read(packetBytes);
							
							final byte b = packetBytes[0];

//							byte[] encodedBytes = new byte[readBufferPosition];
							
//							System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
							
//							final String data = new String(encodedBytes, "US-ASCII");
							
							readBufferPosition = 0;
							
							handler.post(new Runnable() {
								
								public void run() {

									listener.onNewValue(Integer.valueOf(b));
								}
							});
						}
					} catch (IOException ex) {
						stopWorker = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		workerThread.start();
	}
	
	@Override
	public void onStart() {
		
		this.connect();
	}

	@Override
	public void onStop() {
		
		this.stopWorker = true;
		
		try {
			
            btSocket.close();
            
	    } catch (IOException e) { }
		
	}

	@Override
	public void close() {
		
		if(btSocket != null && btSocket.isConnected()) {

			try {
				
	            btSocket.close();
	            
		    } catch (IOException e) { }
		}
		
	}
}
