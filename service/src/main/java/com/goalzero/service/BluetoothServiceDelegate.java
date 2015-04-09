package com.goalzero.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

public interface BluetoothServiceDelegate
{
	void didDiscoverDevice(BluetoothDevice peripheral);

	void didConnectToPeripheral(BluetoothGatt peripheral);

	void didReceiveDataForPeripheral(String data, BluetoothGatt peripheral);
}

