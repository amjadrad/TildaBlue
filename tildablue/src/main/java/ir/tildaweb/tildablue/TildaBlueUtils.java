package ir.tildaweb.tildablue;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TildaBlueUtils {

    private String TAG = this.getClass().getName();
    private Activity activity;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isSearching = false;
    private List<BlueDevice> searchedBlueDevices;
    private StringBuilder stringBuilder = new StringBuilder();
    private ConnectedThread mConnectedThread;
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket bluetoothSocket;
    private OnMessageReceiveListener onMessageReceiveListener;


    public TildaBlueUtils(Activity activity) {
        this.activity = activity;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.searchedBlueDevices = new ArrayList<>();
    }


    public boolean isEnable() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.isEnabled();
    }

    public void enable() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        assert bluetoothAdapter != null;
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
    }


    public void disable() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        assert bluetoothAdapter != null;
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        }
    }

    public List<BlueDevice> getBondedDevices() {
        List<BlueDevice> blueDevices = new ArrayList<>();
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            BlueDevice blueDevice = new BlueDevice();
            blueDevice.setName(device.getName());
            blueDevice.setMacAddress(device.getAddress());
            blueDevices.add(blueDevice);
        }
        return blueDevices;
    }


    public void search(int secondsToScan, final OnSearchDeviceListener onSearchDeviceListener) {
        if (!isSearching) {
            onSearchDeviceListener.onSearchStart();
            isSearching = true;
            activity.registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            bluetoothAdapter.startDiscovery();
            onSearchDeviceListener.onSearchSearch();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isSearching = false;
                    activity.unregisterReceiver(bReceiver);
                    bluetoothAdapter.cancelDiscovery();
                    onSearchDeviceListener.onSearchFinish(searchedBlueDevices);

                }
            }, secondsToScan * 1000);

        }
    }


    public boolean connect(String macAddress, OnMessageReceiveListener onMessageReceiveListener) {
        this.onMessageReceiveListener = onMessageReceiveListener;
        return connectDevice(macAddress);

    }

    public void sendData(String data) {
        mConnectedThread.write(data);
    }

    //Privates
    private boolean connectDevice(String deviceMacAddress) {

        //Create Bonds
        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceMacAddress);
        try {
            boolean bond = createBond(bluetoothAdapter.getRemoteDevice(deviceMacAddress));
        } catch (Exception e) {
            e.printStackTrace();
        }
        connectBluetooth(bluetoothDevice);

        //Connect
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceMacAddress);
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            Toast.makeText(activity, "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try {
            bluetoothSocket.connect();
        } catch (IOException e) {
            try {
                bluetoothSocket.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        mConnectedThread = new ConnectedThread(bluetoothSocket);
        mConnectedThread.start();
        return true;
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {

            byte[] buffer = new byte[256];
            int bytes;

            while (true) {
                try {
                    if (mmInStream != null && mmInStream.available() > 0) {
                        bytes = mmInStream.read(buffer);
                        String readMessage = new String(buffer, 0, bytes);
//                    // Send the obtained bytes to the UI Activity via handler
                        Log.d(TAG, "run:Read message: " + readMessage);
                        onMessageReceiveListener.onMessageReceived(readMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
            }
        }
    }


    private boolean createBond(BluetoothDevice btDevice)
            throws Exception {
        Class aClass = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = aClass.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    private void connectBluetooth(BluetoothDevice bdDevice) {
        try {
            Class cl = Class.forName("android.bluetooth.BluetoothDevice");
            Class[] par = {};
            Method method = cl.getMethod("createBond", par);
            method.invoke(bdDevice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BlueDevice blueDevice = new BlueDevice(device.getName(), device.getAddress());
                searchedBlueDevices.add(blueDevice);
            }
        }
    };


}
