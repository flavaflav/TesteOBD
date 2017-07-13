package com.flaviocrispim.testeobd;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.control.DistanceSinceCCCommand;
import com.github.pires.obd.commands.control.VinCommand;
import com.github.pires.obd.commands.fuel.EthanolPercentageCommand;
import com.github.pires.obd.commands.fuel.FuelLevelCommand;
import com.github.pires.obd.commands.protocol.CloseCommand;
import com.github.pires.obd.commands.protocol.DescribeProtocolCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.HeadersOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdRawCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.exceptions.UnsupportedCommandException;

import java.io.IOException;
import java.util.Map;

import static com.flaviocrispim.testeobd.App.SERIAL_CLASS_UUID;

/**
 * Created by Flavio on 08/07/2017.
 */

class ObdAsyncTask extends AsyncTask<String, String, Map<String, String>> {

    private Handler handler;

    private BluetoothSocket socket;

    ObdAsyncTask(Handler handler, BluetoothDevice bluetoothDevice) throws IOException {

        this.handler = handler;
        this.socket = bluetoothDevice.createRfcommSocketToServiceRecord(SERIAL_CLASS_UUID);


    }

    @Override
    protected Map<String, String> doInBackground(String... strings) {


        try {

            if(!socket.isConnected()) {
                socket.connect();
            }

            // Initialization
            processCommand(new ObdResetCommand());
            processCommand(new ObdRawCommand("AT D"));
            //processCommand(new EchoOffCommand());
            processCommand(new LineFeedOffCommand());
            processCommand(new HeadersOffCommand());
            processCommand(new TimeoutCommand(125));
            processCommand(new SelectProtocolCommand(ObdProtocols.AUTO));

            // Get VIN
            processCommand(new VinCommand(), R.id.editVin);

            // Get ODB Protocol
            processCommand(new DescribeProtocolCommand(), R.id.editObdProtocol);

            // Get distance since last MIL reset
            processCommand(new DistanceSinceCCCommand(), R.id.editDistanceSinceLastMilReset);

            // Get Fuel Level
            processCommand(new FuelLevelCommand(), R.id.editFuelLevel);

            // Get % alcohol
            processCommand(new EthanolPercentageCommand(), R.id.editPercAlcohol);

        } catch (Exception connectException) {
            // erro ao conectar
            System.out.println(connectException.getMessage());
        }

        return null;
    }

    private void processCommand(ObdCommand command) throws IOException, InterruptedException {

        String result;
        try {

            command.run(socket.getInputStream(), socket.getOutputStream());
            result = command.getFormattedResult();

        } catch(Exception ex) {

        }

    }

    private void processCommand(ObdCommand command, int idTextView) throws IOException, InterruptedException {

        Message message = new Message();
        Bundle b = new Bundle();
        b.putInt("viewId", idTextView);

        try {

            command.run(socket.getInputStream(), socket.getOutputStream());
            String result = command.getFormattedResult();
            b.putCharSequence("data", result);

        } catch(UnsupportedCommandException ex) {

//            textView.setText(R.string.error_notsupported);
            b.putCharSequence("data", "Not sup");

        } catch(Exception ex) {

            String rawMessage = command.getResult();
            b.putCharSequence("data", "(RAW) " + rawMessage);

        }

        message.setData(b);
        handler.sendMessage(message);

    }

    public void disconnect() {
        if(this.socket.isConnected()) {
            try {
                new CloseCommand().run(this.socket.getInputStream(), this.socket.getOutputStream());
                this.socket.close();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
