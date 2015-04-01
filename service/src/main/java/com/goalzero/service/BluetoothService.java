package com.goalzero.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Josh on 3/22/2015.
 */
public class BluetoothService /*implements CBCenteralManagerDelegate, CBPeripheralDelegate*/
{
	private boolean isScanning;
	private ArrayList delegates;
	private List<BluetoothDevice> foundPeripherals;

	private BluetoothGatt uartService;
	private BluetoothGattCharacteristic rxCharacteristic;
	private BluetoothGattCharacteristic txCharacteristic;
	private String receivedData;

	private BluetoothDevice connectedDevice;
	private List<BluetoothDevice> peripherals;
	private BluetoothManager centralManager;
	private BluetoothAdapter adapter;

	private static String uartServiceUUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
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
	}

	public static boolean isScanningForDevices()
	{
		return false;
	}

	public static void startScanningForDevices()
	{

	}

	public static void stopScanningForDevices()
	{

	}

	public static void connectToDevice(BluetoothDevice peripheral)
	{

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