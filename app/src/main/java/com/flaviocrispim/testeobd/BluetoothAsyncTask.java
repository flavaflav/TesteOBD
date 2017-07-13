package com.flaviocrispim.testeobd;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static android.bluetooth.BluetoothClass.Device.Major.UNCATEGORIZED;
import static com.flaviocrispim.testeobd.App.SERIAL_CLASS_UUID;

/**
 * Created by Flavio on 08/07/2017.
 */

public class BluetoothAsyncTask extends AsyncTask<String, String, Void> {

    private MainActivity activity;

    public BluetoothAsyncTask(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(String ... strings) {

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {

            if (!adapter.isEnabled()) {
                activity.onDisabledBluetooth();
            } else {

                for(BluetoothDevice posibleObdDevice : posibleObdDevices(adapter)) {

                    if (posibleObdDevice.getName().toUpperCase().contains("OBD")) {
                        activity.onBluetoothSelected(posibleObdDevice);
                    }
                }

            }

        } else {
            activity.onBluetoothAdapterNotFound();
        }

        return null;
    }

    protected List<BluetoothDevice> posibleObdDevices(BluetoothAdapter adapter) {

        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        if (pairedDevices.size() > 0) {

            List<BluetoothDevice> posibleObdDevices = new ArrayList<BluetoothDevice>();

            // Looking for OBDII devices, whose device class are 'Uncategorized' and then look for one capable of serial I/O.
            for (BluetoothDevice device : pairedDevices) {

                int majorDeviceClass = device.getBluetoothClass().getMajorDeviceClass();
                if (majorDeviceClass == UNCATEGORIZED) {

                    ParcelUuid[] features = device.getUuids();
                    for (ParcelUuid feature : features) {

                        // Posible OBD2
                        if (feature.getUuid().equals(SERIAL_CLASS_UUID)) {
                            posibleObdDevices.add(device);
                            break;
                        }
                    }
                }
            }
            return posibleObdDevices;
        }

        return Collections.emptyList();
    }
}
