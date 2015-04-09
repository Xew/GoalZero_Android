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
import java.util.Random;
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
	public BluetoothGatt _selectedGatt;
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

	private static Runnable fakedata = new Runnable()
	{
		int frame = 1;
		int voltage = 1240;
		int ampDraw = 1;
		int boardTemp = 680;
		int mcuTemp = 700;
		int batterypercentage = 80;

		int stage = 1;
		@Override
		public void run()
		{
			if(_instance != null && _instance._selectedGatt != null)
			{
				Random ran = new Random();
				//".*([0-9]{4}).+([0-9]{4})v.+([0-9]{4})i.+([0-9]{4})tb.+([0-9]{4})ti.+chg([0-9]{1,3}).*dsg.*"
				StringBuilder sbuilder = new StringBuilder();
				if(stage == 1)
				{
					if (ran.nextBoolean())
						sbuilder.append("z ");
					sbuilder.append(String.format("%04d", frame++));
					sbuilder.append("   ");
					voltage += (40 - ran.nextFloat() * 80);
					if(voltage < 0)
						voltage = 0;
					sbuilder.append(String.format("%04dv", voltage));
				}
				else if(stage == 2)
				{
					sbuilder.append("   ");
					ampDraw += (4 - ran.nextFloat() * 8);
					if(ampDraw < 0)
						ampDraw = 0;
					sbuilder.append(String.format("%04di", ampDraw));
					sbuilder.append("   ");
					boardTemp += (10 - ran.nextFloat() * 20);
					if(boardTemp < 0)
						boardTemp = 0;
					sbuilder.append(String.format("%04dtb", boardTemp));
				}
				else if(stage == 3)
				{
					sbuilder.append("   ");
					mcuTemp += (10 - ran.nextFloat() * 20);
					if(mcuTemp < 0)
						mcuTemp = 0;
					sbuilder.append(String.format("%04dti", mcuTemp));
					sbuilder.append("   ");
				}
				else if(stage == 4)
				{
					batterypercentage += (10 - ran.nextFloat() * 20);
					if(batterypercentage < 0)
						batterypercentage = 0;
					sbuilder.append(String.format("chg%03d", batterypercentage));
					sbuilder.append("  dsg   ");
					stage = 0;
				}

				stage++;

				for(BluetoothServiceDelegate delegate : _instance.delegates)
				{
					delegate.didReceiveDataForPeripheral(sbuilder.toString(), _instance._selectedGatt);
				}
				handler.postDelayed(this, 200);
			}
		}
	};

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
			handler.removeCallbacks(fakedata);
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
			{
				gatt.discoverServices();
				_selectedGatt = gatt;
			}
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
			}
			for (BluetoothServiceDelegate delegate : delegates)
			{
				delegate.didConnectToPeripheral(gatt);
			}
			gatt.setCharacteristicNotification(txCharacteristic, true);
			BluetoothGattDescriptor descriptor = txCharacteristic.getDescriptor(configUUID);

			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			gatt.writeDescriptor(descriptor);
			handler.postDelayed(fakedata,1500);
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
		if(_context == null)
			_context = context;
		//_context = context;
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

		handler.removeCallbacks(fakedata);

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
		peripheral.connectGatt(_context, false, _instance.mGattCallback);
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
		if (centralManager == null && _context != null)
		{
			centralManager = (BluetoothManager) _context.getSystemService(Context.BLUETOOTH_SERVICE);
			adapter = centralManager.getAdapter();
		}
		return centralManager;
	}
}

