package com.goalzero.goalzero_android;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


public class DeviceFound extends ActionBarActivity
{

	private String[] Devices;
	private int selectedDevice = -1;
	private AdapterView.OnItemClickListener itemClicked = new AdapterView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id)
		{

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_found);

		Devices = new String[2];
		Devices[0] = "Yeti 150";
		Devices[1] = "Yeti 151";

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Devices);

		ListView devices = (ListView) findViewById(R.id.devicesListView);
		devices.setAdapter(adapter);
		devices.setOnItemClickListener(itemClicked);
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
