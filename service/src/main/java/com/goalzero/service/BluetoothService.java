package com.goalzero.service;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Josh on 3/22/2015. A
 */
public class BluetoothService
{
	static final int REQUEST_ENABLE_BT = 0;
	private boolean isScanning;
	private List<BluetoothServiceDelegate> delegates;
	public List<BluetoothDevice> foundPeripherals;

	public List<BluetoothGatt> peripherals;
	public BluetoothDevice selectedPeripherals;
	private BluetoothManager centralManager;
	private BluetoothAdapter adapter;

	private static final String TAG = "test";

	private static UUID uartServiceUUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
	private static UUID rxCharacteristicUUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
	private static UUID txCharacteristicUUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
	private static UUID configUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	private static BluetoothService _instance;
	private static Context _context;

	private static Handler handler;

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback =	new BluetoothAdapter.LeScanCallback()
	{
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord)
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
			{
				for(BluetoothGatt _device : peripherals)
				{
					if(_device.getDevice().getAddress().equalsIgnoreCase(device.getAddress()))
					{
						found = true;
						break;
					}
				}
				if(!found)
					foundPeripherals.add(device);
				for(BluetoothServiceDelegate delegate : delegates)
				{
					delegate.didDiscoverDevice(device);
				}
			}
		}
	};

	private BluetoothGattCallback mGattCallback =  new BluetoothGattCallback()
	{
		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
		{
			super.onCharacteristicRead(gatt, characteristic, status);

		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
		{
			super.onDescriptorWrite(gatt, descriptor, status);
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic)
		{
			byte[] data = characteristic.getValue();
			try
			{
				String value = new String(data, "US-ASCII");
				for(BluetoothServiceDelegate delegate : delegates)
				{
					delegate.didReceiveDataForPeripheral(value, gatt);
				}
			} catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}

		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState)
		{
			if(gatt == null)
				return;
			if(status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED)
				gatt.discoverServices();
			else
				Log.i("error","failure to connect");
		}

		@Override
		public void onServicesDiscovered(final BluetoothGatt gatt, final int status)
		{
			BluetoothGattService service = gatt.getService(uartServiceUUID);
			if(service == null)
				return;
			BluetoothGattCharacteristic txCharacteristic = service.getCharacteristic(txCharacteristicUUID);
			BluetoothGattCharacteristic rxCharacteristic = service.getCharacteristic(rxCharacteristicUUID);

			if(rxCharacteristic == null || txCharacteristic == null)
				return;
			boolean found = false;
			for(BluetoothGatt per : peripherals)
			{
				if(per.getDevice().getAddress().equalsIgnoreCase(gatt.getDevice().getAddress()))
				{
					found = true;
					break;
				}
			}
			if(!found)
			{
				peripherals.add(gatt);
				for (BluetoothServiceDelegate delegate : delegates)
				{
					delegate.didConnectToPeripheral(gatt);
				}
				gatt.setCharacteristicNotification(txCharacteristic, true);
				BluetoothGattDescriptor descriptor = txCharacteristic.getDescriptor(configUUID);

				descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				gatt.writeDescriptor(descriptor);
			}
		}
	};



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
		init(context, true);
	}

	public static void init(Context context, boolean clearPeripherals)
	{
		_context = context;
		if (_instance == null)
		{
			_instance = new BluetoothService();
			_instance.getCentralManager();
			_instance.foundPeripherals = new ArrayList<>();
			handler = new Handler();
			_instance.peripherals = new ArrayList<>();
			_instance.delegates = new ArrayList<>();
		}
		if(clearPeripherals)
		{
			_instance.foundPeripherals.clear();
			_instance.peripherals.clear();
		}
	}

	public static void close()
	{
		if(_instance == null || _instance.peripherals == null)
			return;

		for(BluetoothGatt device : _instance.peripherals)
		{
			BluetoothGattCharacteristic txCharacteristic = device.getService(uartServiceUUID).getCharacteristic(txCharacteristicUUID);
			//device.setCharacteristicNotification(txCharacteristic, false);

			for(BluetoothGattDescriptor descriptor : txCharacteristic.getDescriptors())
			{
				//descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
				//device.writeDescriptor(descriptor);
			}
			device.disconnect();
			device.close();
		}
	}

	public static void clear()
	{
		if(_instance == null || _instance.peripherals == null)
			return;

		close();

		_instance.peripherals.clear();
	}

	public static boolean isScanningForDevices()
	{
		return _instance != null && _instance.isScanning;
	}

	public static void startScanningForDevices()
	{
		if(_instance == null)
			return;
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
		}, 3500);
		_instance.adapter.startLeScan(new UUID[]{uartServiceUUID}, _instance.mLeScanCallback);
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
		if(_instance == null)
			return;
		peripheral.connectGatt(_context, true, _instance.mGattCallback);
	}

	public static void addDelegate(BluetoothServiceDelegate delegate)
	{
		if(!_instance.delegates.contains(delegate))
			_instance.delegates.add(delegate);
	}

	public static void removeDelegate(BluetoothServiceDelegate delegate)
	{
		if(_instance.delegates.contains(delegate))
			_instance.delegates.remove(delegate);
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

