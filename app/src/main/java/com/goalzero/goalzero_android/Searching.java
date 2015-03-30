package com.goalzero.goalzero_android;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class Searching extends ActionBarActivity
{
	private ProgressBar spinner;
	private Runnable searchTimeout;
	private Handler handler;
	private Runnable foundDevice;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		actionBar.hide();
		setContentView(R.layout.activity_searching);

		final Context context = this;

		spinner = (ProgressBar) findViewById(R.id.progressBar1);
		searchTimeout = new Runnable()
		{
			@Override
			public void run()
			{
				Toast.makeText(context, "Timed out.", Toast.LENGTH_LONG).show();
				spinner.setVisibility(View.INVISIBLE);
				ImageView warning = (ImageView) findViewById(R.id.warning);
				warning.setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.textView)).setText(R.string.no_devices_text);
			}
		};

		foundDevice = new Runnable()
		{
			@Override
			public void run()
			{
				Intent i = new Intent(getApplicationContext(), DeviceFound.class);
				startActivity(i);
			}
		};

		handler = new Handler();

		handler.postDelayed(searchTimeout, 2000);
		handler.postDelayed(foundDevice, 4000);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.menu_searching, menu);
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
