/**
 * 
 */
package com.likecollection.vocal_decibel.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.likecollection.vocal_decibel.bluetooth.BluetoothReceiverFactory;
import com.likecollection.vocal_decibel.bluetooth.IBluetoothReceiveListener;
import com.likecollection.vocal_decibel.bluetooth.IBluetoothReceiver;
import com.likecollection.vocal_decibel.data.MyData;
import com.likecollection.vocal_decibel.R;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author Mauricio L. Dau <mauricioldau@gmail.com>
 * @edited by <walterkyo@likecollection.com>Walter Lam</walterkyo@likecollection.com>
 */
public class AcquirementActivity extends Activity implements IBluetoothReceiveListener {

	private final IBluetoothReceiver bluetoothReceiver;
	
	private TextView valueTextView, environment_tv;
	private TimeSeries dataset;
	private XYMultipleSeriesDataset mDataset;
	private XYSeriesRenderer renderer;
	private XYMultipleSeriesRenderer mRenderer;
	private GraphicalView mChartView;
	Calendar calendar = new GregorianCalendar();
	
	public AcquirementActivity() {
		
		this.bluetoothReceiver = BluetoothReceiverFactory.newFakeBTReceiver();
		
		this.bluetoothReceiver.setOnBluetoothReceiveListener(this);
		this.bluetoothReceiver.setContext(this);
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.acquirement_layout);	
		this.valueTextView = (TextView) this.findViewById(R.id.textView1);
        this.environment_tv = (TextView) this.findViewById(R.id.environment_tv);

		//Grafico dinamico
		dataset = new TimeSeries("Valores");
		mDataset = new XYMultipleSeriesDataset();
		
		renderer = new XYSeriesRenderer();
		mRenderer = new XYMultipleSeriesRenderer();
		
		mDataset.addSeries(dataset);
		
		renderer.setColor(Color.GREEN);
		renderer.setPointStyle(PointStyle.CIRCLE);
		renderer.setFillPoints(true);
		
		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setXTitle("Second");
		mRenderer.setYTitle("Decibel");
		mRenderer.setAxisTitleTextSize(20);
	    //mRenderer.setYAxisMin(30);
	    //mRenderer.setYAxisMax(80);
		mRenderer.setShowGrid(true);
		mRenderer.setGridColor(Color.DKGRAY);
		
		mRenderer.addSeriesRenderer(renderer);
		// END Graph code
		//////////////////////////////////////////
		
	}

	@Override
	protected void onStart() {

		super.onStart();
		
		this.bluetoothReceiver.start();
		
		if (mChartView == null) {
		    LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
		    mChartView = ChartFactory.getLineChartView(this, mDataset, mRenderer);
		    layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		} else {
		    mChartView.repaint();
		}
		
	}
	
	@Override
	protected void onStop() {
		
		super.onStop();
		
		this.bluetoothReceiver.stop();
	}
	
	@Override
	protected void onDestroy() {

		super.onDestroy();
		
		this.bluetoothReceiver.close();
	}
	
	@Override
	public void onNewValue(int newValue) {
		dataset.add(calendar.get(Calendar.SECOND),newValue);
        dataset.add(calendar.get(Calendar.SECOND),MyData.getEnvironment());
		mChartView.repaint();
		this.valueTextView.setText(getString(R.string.vocal)+String.valueOf(newValue)+getString(R.string.db));
        this.environment_tv.setText(getString(R.string.environment)+String.valueOf(MyData.getEnvironment())+getString(R.string.db));
	}

}
