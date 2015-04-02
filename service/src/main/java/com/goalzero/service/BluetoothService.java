package com.goalzero.service;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

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
	private List<BluetoothDevice> foundPeripherals;

	private BluetoothGatt uartService;
	private BluetoothGattCharacteristic rxCharacteristic;
	private BluetoothGattCharacteristic txCharacteristic;
	private String receivedData;

	private BluetoothDevice connectedDevice;
	public List<BluetoothDevice> peripherals;
	private BluetoothManager centralManager;
	private BluetoothAdapter adapter;

	private static Handler handler;


	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback =
			new BluetoothAdapter.LeScanCallback() {
				@Override
				public void onLeScan(final BluetoothDevice device, int rssi,
														 byte[] scanRecord) {
					((Activity)_context).runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							foundPeripherals.add(device);
						}
					});
				}
			};

	private static String uartServiceUUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
	private static UUID[] uartServiceUUIDs = new UUID[]{UUID.fromString(uartServiceUUID)};
	private static String rxCharacteristicUUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
	private static String txCharacteristicUUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";

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
		if (_instance == null)
			_instance = new BluetoothService();
		_context = context;
		_instance.getCentralManager();
		_instance.foundPeripherals = new ArrayList<BluetoothDevice>();
		handler = new Handler();
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
				_instance.adapter.stopLeScan(_instance.mLeScanCallback);
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
		peripheral.connectGatt(_context, false, new BluetoothGattCallback(){});
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