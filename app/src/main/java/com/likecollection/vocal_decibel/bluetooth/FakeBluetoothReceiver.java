/**
 * 
 */
package com.likecollection.vocal_decibel.bluetooth;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import com.likecollection.vocal_decibel.data.MyData;

/**
 * @author Mauricio Dau mauricio.dau@endeeper.com
 * @edited by <walterkyo@likecollection.com>Walter Lam</walterkyo@likecollection.com>
 */
class FakeBluetoothReceiver extends AbstractBluetoothReceiver {

	private FakeValueGenerator generatorThread;
	final int MAX_RECORDING = 3;    //Set the number of recording samples. More the samples takes more time

    static final int SAMPLE_RATE_IN_HZ = 8000;
    static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
        RECORDER_CHANNELS, AudioFormat.ENCODING_PCM_16BIT);
    int bufferSizeInShorts = (BUFFER_SIZE/2); //Convert to 8bit
    AudioRecord mAudioRecord, mAudioRecordEnvironment;
	
	public FakeBluetoothReceiver() {
		this.generatorThread = new FakeValueGenerator();
	}
	
	@Override
	public void onStart() {
        initRecorder();
        this.generatorThread = new FakeValueGenerator();
		this.generatorThread.execute("");
	}

	@Override
	public void onStop() {
        releaseRecorder();
	}

    private void initRecorder(){
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                SAMPLE_RATE_IN_HZ, RECORDER_CHANNELS,
                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);

        mAudioRecordEnvironment = new AudioRecord(MediaRecorder.AudioSource.CAMCORDER,
                SAMPLE_RATE_IN_HZ, RECORDER_CHANNELS,
                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
    }

    private void releaseRecorder(){
        if(mAudioRecord != null){
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }

        if(mAudioRecordEnvironment != null){
            mAudioRecordEnvironment.stop();
            mAudioRecordEnvironment.release();
            mAudioRecordEnvironment = null;
        }
    }
	
	private class FakeValueGenerator extends AsyncTask<String, Integer, Void> {
		@Override
		protected Void doInBackground(String... params) {
			while(isRunning) {
                double[] recordVocal = new double[MAX_RECORDING];
                double[] recordEnvironment = new double[MAX_RECORDING];
                int vocalCounter = 0;
                int environmentCounter = 0;
                double averageVocal = 0;
                double averageEnvironment = 0;
                boolean vocalChecked = false;
                boolean environmentChecked = false;

                for(int d = 0; d<MAX_RECORDING;d++){
                    recordVocal[d] = -1;
                    recordEnvironment[d] = -1;
                }

				try {
                    boolean isFilledAll = false;
	                while (!isFilledAll) {
                        //Vocal detection
                        int vocal = 0;
                        int environment = 0;

                        if(mAudioRecord != null) {
                            mAudioRecord.startRecording();
                            vocal = getDecibel(mAudioRecord);

                            if (vocal > 0) {
                                recordVocal[vocalCounter % MAX_RECORDING] = vocal;
                                vocalCounter++;
                            }
                            if(mAudioRecord != null){mAudioRecord.stop();}

                            //Calculate average decibel
                            if(vocalCounter%MAX_RECORDING==0){
                                averageVocal = 0;
                                for(double data : recordVocal){
                                    averageVocal += data;
                                    //Log.e("myLog","Each vocal: "+data);
                                }
                                averageVocal = averageVocal/MAX_RECORDING;
                                Log.e("myLog","Avg vocal: "+(int)averageVocal);
                                vocalChecked = true;
                            }
                        }else{
                            initRecorder();
                        }
                        //Environment sound detection
                        if(mAudioRecordEnvironment != null) {
                            mAudioRecordEnvironment.startRecording();
                            environment = getDecibel(mAudioRecordEnvironment);

                            if (environment > 0) {
                                MyData.setEnvironment(environment); //Display both of the vocal and environment decibel
                                publishProgress(vocal);

                                recordEnvironment[environmentCounter % MAX_RECORDING] = environment;
                                environmentCounter++;
                            }
                            if(mAudioRecordEnvironment != null){mAudioRecordEnvironment.stop();}

                            //Calculate average decibel
                            if(environmentCounter%MAX_RECORDING==0){
                                averageEnvironment = 0;
                                for(double data : recordEnvironment){
                                    averageEnvironment += data;
                                    //Log.e("myLog","Each environment:al "+data);
                                }
                                averageEnvironment = averageEnvironment/MAX_RECORDING;
                                Log.e("myLog","Avg environmental: "+(int)averageEnvironment);
                                environmentChecked = true;
                            }
                        }else{
                            initRecorder();
                        }

                        //Ensure both vocal and environmental samples are recorded properly
                        if(environmentChecked && vocalChecked){
                            environmentChecked = false;
                            vocalChecked = false;
                            //isFilledAll = false;
                        }
	                }

                    //Another method to detect decibel, easily get affected by extreme value
                    /*MediaRecorder mRecorder;
                    mRecorder = new MediaRecorder();
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    mRecorder.setOutputFile("/dev/null");   //This option means we do not save the recording
                    try {
                        mRecorder.prepare();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    mRecorder.start();
                    double getAmp = mRecorder.getMaxAmplitude();
					if(getAmp<=0){
						publishProgress(0);
					}else{
						int base = 700;
						double powerDb = 20 * Math.log10(getAmp / base);


						publishProgress((int)powerDb);
					}*/
				} catch (Exception e) {
                    e.printStackTrace();
                }
			}

            //Close all recorders
            releaseRecorder();
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {

			super.onProgressUpdate(values);
			
			listener.onNewValue(values[0]);
		}
	}

	@Override
	public void close() {

		this.generatorThread = null;
	}

    private int getDecibel(AudioRecord audioRecord){
        double volume = 0;
        short[] buffer = new short[bufferSizeInShorts];
        int r = audioRecord.read(buffer, 0, bufferSizeInShorts);
        long v = 0;
        for (int i = 0; i < buffer.length; i++) {
            v += buffer[i] * buffer[i];
        }
        double mean = v / (double) r;
        volume = 10 * Math.log10(mean);
        return (int)volume;
    }

}
