package com.goalzero.service;

import android.bluetooth.BluetoothDevice;

public interface BluetoothServiceDelegate
{
	void didDiscoverDevice(BluetoothDevice peripheral);

	void didConnectoToPeripheral(BluetoothDevice peripheral);

	void didReceiveDataForPeripheral(String data, BluetoothDevice peripheral);
}

