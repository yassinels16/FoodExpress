package com.example.foodexpress;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {

    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 3;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String APP_NAME = "FoodExpress";
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> deviceList;
    private ArrayAdapter<String> deviceListAdapter;
    private ListView listViewDevices;
    private Button buttonScan;
    private Button buttonSendConfirmation;
    private TextView textViewStatus;

    private BluetoothDevice connectedDevice;
    private ConnectThread connectThread;
    private AcceptThread acceptThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Bluetooth Connection");

        // Initialize views
        listViewDevices = findViewById(R.id.listViewDevices);
        buttonScan = findViewById(R.id.buttonScan);
        buttonSendConfirmation = findViewById(R.id.buttonSendConfirmation);
        textViewStatus = findViewById(R.id.textViewStatus);

        // Initialize Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available on this device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize device list
        deviceList = new ArrayList<>();
        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        listViewDevices.setAdapter(deviceListAdapter);

        // Setup button listeners
        buttonScan.setOnClickListener(v -> {
            checkBluetoothPermission();
        });

        buttonSendConfirmation.setOnClickListener(v -> {
            if (connectedDevice != null) {
                sendConfirmation();
            } else {
                Toast.makeText(this, "No device connected", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup list item click listener
        listViewDevices.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice device = deviceList.get(position);
            connectToDevice(device);
        });

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        // Start accept thread
        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    private void checkBluetoothPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT
                    },
                    BLUETOOTH_PERMISSION_REQUEST_CODE);
        } else {
            startBluetoothDiscovery();
        }
    }

    private void startBluetoothDiscovery() {
        // Enable Bluetooth if not enabled
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        // Clear previous list
        deviceList.clear();
        deviceListAdapter.clear();

        // Add paired devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceList.add(device);
                deviceListAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }

        // Start discovery
        bluetoothAdapter.startDiscovery();
        textViewStatus.setText("Scanning for devices...");
    }

    private void connectToDevice(BluetoothDevice device) {
        // Cancel discovery because it's resource intensive
        bluetoothAdapter.cancelDiscovery();

        // Cancel any previous connection attempts
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        // Start connection thread
        connectThread = new ConnectThread(device);
        connectThread.start();
        textViewStatus.setText("Connecting to " + device.getName() + "...");
    }

    private void sendConfirmation() {
        if (connectThread != null) {
            connectThread.write("ORDER_CONFIRMED".getBytes());
            Toast.makeText(this, "Confirmation sent", Toast.LENGTH_SHORT).show();
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.getName() != null) {
                    deviceList.add(device);
                    deviceListAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startBluetoothDiscovery();
            } else {
                Toast.makeText(this, "Bluetooth must be enabled to continue", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBluetoothDiscovery();
            } else {
                Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister broadcast receiver
        unregisterReceiver(receiver);

        // Cancel discovery
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }

        // Cancel threads
        if (connectThread != null) {
            connectThread.cancel();
        }
        if (acceptThread != null) {
            acceptThread.cancel();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Thread for connecting as a client
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private OutputStream mmOutStream;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        public void run() {
            try {
                mmSocket.connect();
                connectedDevice = mmDevice;

                // Get the output stream
                mmOutStream = mmSocket.getOutputStream();

                runOnUiThread(() -> {
                    textViewStatus.setText("Connected to " + mmDevice.getName());
                    buttonSendConfirmation.setEnabled(true);
                });
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
                runOnUiThread(() -> {
                    textViewStatus.setText("Connection failed");
                    buttonSendConfirmation.setEnabled(false);
                });
                return;
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Thread for accepting connections as a server
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }

                if (socket != null) {
                    // A connection was accepted
                    manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        private void manageConnectedSocket(BluetoothSocket socket) {
            BluetoothDevice device = socket.getRemoteDevice();
            connectedDevice = device;

            runOnUiThread(() -> {
                textViewStatus.setText("Connected to " + device.getName());
                buttonSendConfirmation.setEnabled(true);
            });

            // Start a thread to manage the connection
            ConnectedThread connectedThread = new ConnectedThread(socket);
            connectedThread.start();
        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Thread for managing an established connection
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String message = new String(buffer, 0, bytes);

                    // Process received message
                    runOnUiThread(() -> {
                        Toast.makeText(BluetoothActivity.this, "Received: " + message, Toast.LENGTH_SHORT).show();
                    });
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
