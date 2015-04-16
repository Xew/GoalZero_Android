package com.goalzero.goalzero_android;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.goalzero.service.BluetoothService;
import com.goalzero.service.BluetoothServiceDelegate;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends ActionBarActivity implements BluetoothServiceDelegate
{
	private ArrayMap<String, String> deviceStrings = new ArrayMap<>();
	private static Pattern yetiPattern = Pattern.compile(".*([0-9]{4}).+([0-9]{4})v.+([0-9]{4})i.+([0-9]{4})tb.+([0-9]{4})ti.+chg([0-9]{1,3}).*dsg.*");
	private static Pattern sherpaPattern = Pattern.compile(".*(\\d+),V_IN=(\\d+),V_OUT=(\\d+),V_CELL=(\\d+),I_CHG=(\\d+),I_DSG=(\\d+),Temp=(\\d+),CURR\\(mA\\)=(\\d+),([a-zA-Z]*),I_AVG_CHG\\(mA\\)=(\\d+),Volt\\(mV\\)=(\\d+),Watt=(\\d+),RM=(\\d+),Safety=(.*),.*");
	private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		BluetoothService.init(this, false);
		BluetoothService.addDelegate(this);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		TextView deviceName = (TextView)findViewById(R.id.deviceName);
		if(BluetoothService.instance().selectedPeripherals != null)
		{
			GZDeviceView deviceView = (GZDeviceView)findViewById(R.id.gz_device);
			deviceView.setVisibility(View.VISIBLE);
			BluetoothService.addDelegate(this);
			BluetoothService.connectToDevice(BluetoothService.instance().selectedPeripherals);

			String name = BluetoothService.instance().selectedPeripherals.getName();
			name = name.split("\n")[0];
			deviceName.setText(name);
		}
		else
			deviceName.setText("");
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		switch (id)
		{
			case R.id.action_settings:
				return true;
			case R.id.action_find_devices:
				Intent i = new Intent(getApplicationContext(), AddDevices.class);
				startActivity(i);
				return true;
			case R.id.action_12v_toggle:
				BluetoothService.instance().SendOutlet(1);
				return true;
			case R.id.action_usb_toggle:
				BluetoothService.instance().SendOutlet(2);
				return true;
			case R.id.action_ac_toggle:
				BluetoothService.instance().SendOutlet(0);
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		BluetoothService.close();
		BluetoothService.removeDelegate(this);
	}

	@Override
	public void didDiscoverDevice(BluetoothDevice peripheral)
	{

	}

	@Override
	public void didConnectToPeripheral(BluetoothGatt peripheral)
	{
		deviceStrings.put(peripheral.getDevice().getAddress(), "");
	}

	@Override
	public void didReceiveDataForPeripheral(String data, BluetoothGatt peripheral)
	{
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(deviceStrings.get(peripheral.getDevice().getAddress()));
		sBuilder.append(data);
		String currentString = sBuilder.toString();

		Log.i("BlueDebug", peripheral.getDevice().getName() + "(" + peripheral.getDevice().getAddress() + ") => " + currentString);
		final Matcher yetiMatcher = yetiPattern.matcher(currentString);
		final Matcher sherpaMatcher = sherpaPattern.matcher(currentString);

		deviceStrings.put(peripheral.getDevice().getAddress(),currentString);

		if(yetiMatcher.find())
		{
			Log.i("BlueDebug", "frame "+ yetiMatcher.group(1));
			mHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					GZDeviceView deviceView = (GZDeviceView)findViewById(R.id.gz_device);
					deviceView.setBatteryPercent(Integer.parseInt(yetiMatcher.group(6)));
					deviceView.setVoltage(Float.parseFloat(yetiMatcher.group(2))/100);
					deviceView.setTemperature(Integer.parseInt(yetiMatcher.group(4))/10);
				}
			});
			deviceStrings.put(peripheral.getDevice().getAddress(), "");

		}
		else if(sherpaMatcher.find())
		{
			Log.i("BlueDebug", "frame " + sherpaMatcher.group(1));
			mHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					GZDeviceView deviceView = (GZDeviceView)findViewById(R.id.gz_device);
					deviceView.setBatteryPercent(100);
					deviceView.setVoltage(Float.parseFloat(sherpaMatcher.group(11).substring(0,sherpaMatcher.group(11).length()-1))/100);
					deviceView.setTemperature(Integer.parseInt(sherpaMatcher.group(7))/10);
				}
			});
			deviceStrings.put(peripheral.getDevice().getAddress(), "");
		}
	}
}
