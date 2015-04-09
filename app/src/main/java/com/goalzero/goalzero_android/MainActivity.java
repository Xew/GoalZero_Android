package com.goalzero.goalzero_android;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.goalzero.service.BluetoothService;
import com.goalzero.service.BluetoothServiceDelegate;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends ActionBarActivity implements BluetoothServiceDelegate
{
	private ArrayMap<String, String> deviceStrings = new ArrayMap<>();
	private static Pattern pattern = Pattern.compile(".*([0-9]{4}).+([0-9]{4})v.+([0-9]{4})i.+([0-9]{4})tb.+([0-9]{4})ti.+chg([0-9]{1,3}).*dsg.*");

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
		if(BluetoothService.instance().selectedPeripherals != null)
		{
			GZDeviceView deviceView = (GZDeviceView)findViewById(R.id.gz_device);
			deviceView.setVisibility(View.VISIBLE);
			BluetoothService.addDelegate(this);
			BluetoothService.connectToDevice(BluetoothService.instance().selectedPeripherals);
		}
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
		//BluetoothService.close();
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
		Matcher matcher = pattern.matcher(currentString);

		deviceStrings.put(peripheral.getDevice().getAddress(),currentString);

		if(matcher.find())
		{
			Log.i("BlueDebug", "frame "+ matcher.group(1));
			GZDeviceView deviceView = (GZDeviceView)findViewById(R.id.gz_device);
			deviceView.setBatteryPercent(Integer.parseInt(matcher.group(6)));
			deviceView.setVoltage(Float.parseFloat(matcher.group(2))/100);
			deviceView.setTemperature(Integer.parseInt(matcher.group(4))/10);

		}

		if(currentString.matches(pattern.toString()))
		{
			deviceStrings.put(peripheral.getDevice().getAddress(), "");
		}
	}
}
