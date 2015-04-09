package com.goalzero.goalzero_android;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.goalzero.service.BluetoothDeviceListAdapter;
import com.goalzero.service.BluetoothService;

import java.util.List;
import android.os.Handler;


public class DeviceFound extends ActionBarActivity
{
	private static DeviceFound last = null;

	private AdapterView.OnItemClickListener itemClicked = new AdapterView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id)
		{
			BluetoothService.connectToDevice((BluetoothDevice)adapterView.getItemAtPosition(pos));
			BluetoothService.instance().selectedPeripherals = (BluetoothDevice)adapterView.getItemAtPosition(pos);
			BluetoothService.instance().foundPeripherals.remove(pos);


			Handler handler = new Handler();
			handler.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					NavUtils.navigateUpFromSameTask(last);
				}
			}, 1000);

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		last = this;

		setContentView(R.layout.activity_device_found);

		List<BluetoothDevice> peripherals = BluetoothService.instance().foundPeripherals;

		BluetoothDeviceListAdapter adapter = new BluetoothDeviceListAdapter(this, R.layout.blelistitem, peripherals);


		ListView devices = (ListView) findViewById(R.id.devicesListView);
		devices.setAdapter(adapter);
		devices.setOnItemClickListener(itemClicked);
	}

	@Override
	public void onBackPressed()
	{
		Log.d("test", "yes");
		NavUtils.navigateUpFromSameTask(this);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_device_found, menu);
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
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
