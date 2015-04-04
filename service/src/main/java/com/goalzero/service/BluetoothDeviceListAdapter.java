package com.goalzero.service;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class BluetoothDeviceListAdapter extends ArrayAdapter<BluetoothDevice>
{
	private LayoutInflater mInflater;
	private int layoutResourceId;
	private Context c;
	private BluetoothDevice[] DATA = null;

	public BluetoothDeviceListAdapter(Context context, int layoutResourceId, BluetoothDevice[] data)
	{
		super(context, layoutResourceId, data);
		this.mInflater = LayoutInflater.from(context);
		this.layoutResourceId = layoutResourceId;
		c = context;
		DATA = data;
	}

	public BluetoothDeviceListAdapter(Context context, int layoutResourceId, List<BluetoothDevice> data)
	{
		super(context, layoutResourceId, data);
		this.mInflater = LayoutInflater.from(context);
		this.layoutResourceId = layoutResourceId;
		c = context;
		DATA = data.toArray(new BluetoothDevice[data.size()]);
	}

	public BluetoothDeviceListAdapter(Context context, List<BluetoothDevice> data)
	{
		super(context, 0, data);
		this.mInflater = LayoutInflater.from(context);
		this.layoutResourceId = 0;
		c = context;
		DATA = data.toArray(new BluetoothDevice[data.size()]);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		BluetoothDeviceHolder holder;

		if(convertView == null)
		{
			int id = layoutResourceId == 0 ? R.layout.blelistitem : layoutResourceId;
			convertView = mInflater.inflate(id, parent, false);
			holder = new BluetoothDeviceHolder();
			holder.text = (TextView) convertView.findViewById(R.id.text);

			convertView.setTag(holder);
		}
		else
		{
			holder = (BluetoothDeviceHolder) convertView.getTag();
		}

		holder.text.setText(DATA[position].getName());

		return convertView;
	}

	static class BluetoothDeviceHolder
	{
		TextView text;
	}
}
