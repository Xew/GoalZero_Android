package com.goalzero.service;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Josh on 3/22/2015.
 */
public class BluetoothService /*implements CBCenteralManagerDelegate, CBPeripheralDelegate*/
{
	static final int REQUEST_ENABLE_BT = 0;
	private boolean isScanning;
	private ArrayList delegates;
	public List<BluetoothDevice> foundPeripherals;

	public List<BluetoothGatt> peripherals;
	private BluetoothManager centralManager;
	private BluetoothAdapter adapter;

	private static Handler handler;


	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback =
			new BluetoothAdapter.LeScanCallback()
	{
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
												 byte[] scanRecord)
		{
			((Activity)_context).runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					boolean found = false;
					for(BluetoothDevice _device : foundPeripherals)
					{
						if(_device.getAddress().equalsIgnoreCase(device.getAddress()))
						{
							found = true;
							break;
						}
					}
					if(!found)
						foundPeripherals.add(device);
				}
			});
		}
	};

	private BluetoothGattCallback mgattCallback =  new BluetoothGattCallback()
	{
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic)
		{

		}

		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState)
		{
			if(status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED)
				gatt.discoverServices();
		}

		@Override
		public void onServicesDiscovered(final BluetoothGatt gatt, final int status)
		{
			BluetoothGattService service = gatt.getService(uartServiceUUIDs[0]);
			if(service == null)
				return;
			BluetoothGattCharacteristic rxCharacteristic = service.getCharacteristic(rxCharacteristicUUID);
			BluetoothGattCharacteristic txCharacteristic = service.getCharacteristic(txCharacteristicUUID);
			if(rxCharacteristic == null || txCharacteristic == null)
				return;
			peripherals.add(gatt);
			rxCharacteristic.
		}
	};

	private static final String TAG = "test";

	private static String uartServiceUUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
	private static UUID[] uartServiceUUIDs = new UUID[]{UUID.fromString(uartServiceUUID)};
	private static String rxCharacteristicUUIDstr = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
	private static String txCharacteristicUUIDstr = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
	private static UUID rxCharacteristicUUID = UUID.fromString(rxCharacteristicUUIDstr);
	private static UUID txCharacteristicUUID = UUID.fromString(txCharacteristicUUIDstr);

	private static BluetoothService _instance;
	private static Context _context;

	private BluetoothService()
	{
	}

	public static BluetoothService instance()
	{
		if (_instance == null)
			init(null);
		return _instance;
	}

	public static void init(Context context)
	{
		_context = context;
		if (_instance == null)
		{
			_instance = new BluetoothService();
			_instance.getCentralManager();
			_instance.foundPeripherals = new ArrayList<BluetoothDevice>();
			handler = new Handler();
			_instance.peripherals = new ArrayList<BluetoothGatt>();
		}
		_instance.foundPeripherals.clear();
		_instance.peripherals.clear();
	}

	public static boolean isScanningForDevices()
	{
		return _instance.isScanning;
	}

	public static void startScanningForDevices()
	{
		if (!_instance.adapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			((Activity)_context).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			return;
		}
		if(_instance.isScanning)
			return;
		_instance.isScanning = true;
		handler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				stopScanningForDevices();
			}
		}, 5000);
		_instance.adapter.startLeScan(uartServiceUUIDs, _instance.mLeScanCallback);
	}

	public static void stopScanningForDevices()
	{
		if(_instance.isScanning)
		{
			_instance.adapter.stopLeScan(_instance.mLeScanCallback);
			_instance.isScanning = false;
		}
	}

	public static void connectToDevice(BluetoothDevice peripheral)
	{
		peripheral.connectGatt(_context, false, _instance.mgattCallback);
	}

	public static void addDelegate(BluetoothService delegate)
	{

	}

	public static void removeDelegate(BluetoothService delegate)
	{

	}

	public BluetoothManager getCentralManager()
	{
		if (centralManager == null)
		{
			centralManager = (BluetoothManager) _context.getSystemService(Context.BLUETOOTH_SERVICE);
			adapter = centralManager.getAdapter();
		}
		return centralManager;
	}
}

