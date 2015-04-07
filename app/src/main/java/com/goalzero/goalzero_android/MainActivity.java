package com.goalzero.goalzero_android;

import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.goalzero.service.BluetoothService;

import java.util.List;


public class MainActivity extends ActionBarActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		BluetoothService.init(this);
		List<BluetoothGatt> peripherals = BluetoothService.instance().peripherals;

		if(peripherals.size() > 0)
		{
			GZDeviceView deviceView = (GZDeviceView)findViewById(R.id.gz_device);
			deviceView.setVisibility(View.VISIBLE);
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
		BluetoothService.close();
		super.onDestroy();
	}
}
