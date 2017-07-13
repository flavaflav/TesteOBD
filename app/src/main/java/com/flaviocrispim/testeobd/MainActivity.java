package com.flaviocrispim.testeobd;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends Activity {

    private BluetoothDevice device;

    private ObdAsyncTask runner;

    private Handler obdDataHandler = new Handler() {

        @Override
        public void handleMessage(Message message) {

            Bundle b = message.getData();

            int viewId = b.getInt("viewId");
            String data = b.getString("data");

            TextView txtViewObdDevice = (TextView) findViewById(viewId);
            txtViewObdDevice.setText(data);
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        BluetoothAsyncTask runner = new BluetoothAsyncTask(this);
        runner.execute();
	}

    public void connectObd(View view) {

            if (this.runner == null) {
                try {
                    this.runner = new ObdAsyncTask(obdDataHandler, device);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            this.runner.execute();

            Button btnConnect = (Button) findViewById(R.id.btnConnectObd);
            btnConnect.setEnabled(false);

            Button btnDisconnect = (Button) findViewById(R.id.btnDisconnectObd);
            btnDisconnect.setEnabled(true);

    }

    public void disconnectObd(View view) {

        if(!this.runner.isCancelled()) {
            this.runner.disconnect();
            this.runner.cancel(false);
            this.runner = null;
        }

        clearObdInfo();

        Button btnConnect = (Button) findViewById(R.id.btnConnectObd);
        btnConnect.setEnabled(true);

        Button btnDisconnect = (Button) findViewById(R.id.btnDisconnectObd);
        btnDisconnect.setEnabled(false);
    }

    public void onDisabledBluetooth() {

        TextView txtViewObdDevice = (TextView) findViewById(R.id.editObdDevice);
        txtViewObdDevice.setText(R.string.message_please_enable_bluetooth);
        this.device = null;

        Button btnConnect = (Button) findViewById(R.id.btnConnectObd);
        btnConnect.setEnabled(false);
    }

    public void onBluetoothAdapterNotFound() {

        TextView txtViewObdDevice = (TextView) findViewById(R.id.editObdDevice);
        txtViewObdDevice.setText(R.string.error_no_bluetooth);
        this.device = null;

        Button btnConnect = (Button) findViewById(R.id.btnConnectObd);
        btnConnect.setEnabled(false);
    }

    public void onBluetoothSelected(BluetoothDevice device) {

        this.device = device;
        TextView txtViewObdDevice = (TextView) findViewById(R.id.editObdDevice);
        String deviceLabel = String.format("%s (%s)", device.getName(), device.getAddress());
        txtViewObdDevice.setText(deviceLabel);

        Button btnConnect = (Button) findViewById(R.id.btnConnectObd);
        btnConnect.setEnabled(true);
    }

    private void clearObdInfo() {

        int[] viewIds = new int[] {
                R.id.editObdProtocol,
                R.id.editVin,
                R.id.editDistanceSinceLastMilReset,
                R.id.editFuelLevel,
                R.id.editPercAlcohol,
        };

        for (int viewId : viewIds) {
            TextView txtView = (TextView) findViewById(viewId);
            txtView.setText("");
        }

    }
}
