package com.uci.ble_application;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    Button startScanningButton;
    Button stopScanningButton;
    TextView peripheralTextView;
    BluetoothDevice mDevice;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    Boolean btScanning = false;
    int deviceIndex = 0;
    ArrayList<BluetoothDevice> devicesDiscovered = new ArrayList<BluetoothDevice>();
    EditText deviceIndexInput;
    Button connectToDevice;
    Button disconnectDevice;
    BluetoothGatt bluetoothGatt;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public Map<String, String> uuids = new HashMap<String, String>();

    // For converting
    private static final String STRING_HEX_NUM = "0123456789ABCDEF";
    private static final char[] CHAR_ARRAY_HEX_NUM = STRING_HEX_NUM.toCharArray();


    //graphing
    private GraphView linegraph;
    private LineGraphSeries<DataPoint> xyseries;
    Double time;
    private long startTime;
    private boolean datatransfer = false;
    long MillisecondTime, StartTime;
    int inputcounter = 0;
    private final Handler mHandlergraph = new Handler();
    private Runnable mTimer1;
    private String writeout;
    DataPoint[] circlebuffer = new DataPoint[200];
    double oldsize = 0;
    int delaycounter = 0;
    private double dataint = 0;
    private  String broken;
    private  int brokenlength = 0;
    private boolean previousinput = false;


    public static String LOG_TAG = "MainActivity";
    private boolean connected_to_device = false;

    public static UUID UART_UUID      = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static UUID TX_UUID        = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static UUID RX_UUID        = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    public static UUID CLIENT_UUID    = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");
    public static UUID DIS_UUID       = UUID.fromString("000001530-1212-EFDE-1523-785FEABCD123");
    public static UUID DIS_MANUF_UUID = UUID.fromString("00002A29-0000-1000-8000-00805F9B34FB");
    public static UUID DIS_MODEL_UUID = UUID.fromString("00002A24-0000-1000-8000-00805F9B34FB");
    public static UUID DIS_HWREV_UUID = UUID.fromString("00002A26-0000-1000-8000-00805F9B34FB");
    public static UUID DIS_SWREV_UUID = UUID.fromString("00002A28-0000-1000-8000-00805F9B34FB");


    //writing to txt file variables
    private static final String File_Name = "data.txt";
    Button savedata;
    FileOutputStream fos = null;
    File f = null;
    String DIRECTORY_PATH = Environment.getExternalStorageDirectory().toString();
    PrintWriter writer=null;


    // Stops scanning after 2 seconds.
    private Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 3000; //original 5000
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    private BluetoothGattCharacteristic tx;
    private BluetoothGattCharacteristic rx;
    private BluetoothGattCharacteristic HW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Boolean boolstorage = isStoragePermissionGranted();

        if(boolstorage == true)
        {
            try {


                FileOutputStream fileOutputStream2 = openFileOutput(File_Name,MODE_PRIVATE);
                writer = new PrintWriter(fileOutputStream2);


 //opens outputstream

            }
            catch (Exception e)
            {

            }

        }


        peripheralTextView = (TextView) findViewById(R.id.PeripheralTextView);
        peripheralTextView.setMovementMethod(new ScrollingMovementMethod());
     //   deviceIndexInput = (EditText) findViewById(R.id.InputIndex);
     //   deviceIndexInput.setText("0");

        linegraph = (GraphView) findViewById(R.id.scatterPlot);
        startGraph();


        savedata = findViewById(R.id.SaveButton);
        savedata.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


            }
        });


        connectToDevice = (Button) findViewById(R.id.ConnectButton);
        connectToDevice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String connectionname = "E1:99:80:26:C3:D4";

                Log.d(LOG_TAG, "connect pressed ");
                for(int i = 0; i < devicesDiscovered.size(); i ++)
                {

                    String Flora = devicesDiscovered.get(i).toString();
                    Log.d(LOG_TAG, "." + Flora + ".");
                    if(Flora.contentEquals(connectionname))
                    {
                        if(connected_to_device == false)
                        {
                            Log.d(LOG_TAG, "connected");
                            bluetoothGatt = devicesDiscovered.get(i).connectGatt(getApplicationContext(), true, btleGattCallback);
                            connected_to_device = true;


                        }
                    }
                    else
                    {
                        Log.d(LOG_TAG, "not connected: " + Flora);

                    }

                }


                //connectToDeviceSelected();
            }
        });

        disconnectDevice = (Button) findViewById(R.id.DisconnectButton);
        disconnectDevice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                disconnectDeviceSelected();
                writer.flush();
                writer.close();
            }
        });

        startScanningButton = (Button) findViewById(R.id.StartScanButton);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();


            }
        });


        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }


    }

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            peripheralTextView.append("Index: " + deviceIndex + ", Device Name: " + result.getDevice().getName() + " rssi: " + result.getRssi() + "\n");
            devicesDiscovered.add(result.getDevice());
            deviceIndex++;
            // auto scroll for text view
            final int scrollAmount = peripheralTextView.getLayout().getLineTop(peripheralTextView.getLineCount()) - peripheralTextView.getHeight();
            // if there is no need to scroll, scrollAmount will be <=0
            if (scrollAmount > 0) {
                peripheralTextView.scrollTo(0, scrollAmount);
            }
        }
    };

    // Device connect call back
    //also reads serial data sent from ble device
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {


        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            System.out.println(newState);
            switch (newState) {
                case 0:
                    System.out.println("Disconected");
                    break;
                case 2:
                    System.out.println("Conected");
                    // discover services and characteristics for this device
                    bluetoothGatt.discoverServices();

                    break;
                default:
                    System.out.println("Unknown");
                    break;
            }
        }


        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {

            tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
            rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);

            System.out.println("Discovered: TX" + tx.toString());
            System.out.println("Discovered: RX" + rx.toString());
            gatt.readCharacteristic(tx);


            System.out.println("Discovered");
            BluetoothGattService  service = gatt.getService(UART_UUID);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(RX_UUID);
            gatt.setCharacteristicNotification(characteristic,true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_UUID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);

    }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                onDataAvailable(characteristic);
            } else {
                Log.d(LOG_TAG, "onCharacteristicRead() : Error status = " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation

            byte[] data = characteristic.getValue();
           // System.out.println(data.toString() + " BYTE " );
            final String datastring = new String(data, Charset.forName("UTF-8"));
           // System.out.println(datastring + "   " + characteristic.getUuid().toString());


            adddataset(datastring);

        }



        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }
    };


    protected void onDataAvailable(BluetoothGattCharacteristic characteristic){
        UUID thisUUID = characteristic.getUuid();
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {

        System.out.println(characteristic.getUuid());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    public void startScanning() {
        System.out.println("start scanning");
        btScanning = true;
        deviceIndex = 0;
        devicesDiscovered.clear();
        peripheralTextView.setText("");
        peripheralTextView.append("Started Scanning\n");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScanning();
                Toast toast = Toast.makeText(getApplicationContext(), "Scan finished", Toast.LENGTH_SHORT);
                toast.show();
            }
        }, SCAN_PERIOD);
    }

    public void stopScanning() {
        System.out.println("stopping scanning");
        peripheralTextView.append("Stopped Scanning\n");
        btScanning = false;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }

    public void connectToDeviceSelected() {
      //  peripheralTextView.append("Trying to connect to device at index: " + deviceIndexInput.getText() + "\n");
       // int deviceSelected = Integer.parseInt(deviceIndexInput.getText().toString());
     //   String Flora = devicesDiscovered.get(deviceSelected).toString();
     //   System.out.print(Flora);
     //   bluetoothGatt = devicesDiscovered.get(deviceSelected).connectGatt(this, true, btleGattCallback);
    }

    public void disconnectDeviceSelected() {
        peripheralTextView.append("Disconnecting from device\n");
        bluetoothGatt.disconnect();
        connected_to_device = false;
    }



    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    private void startGraph() {
        xyseries = new LineGraphSeries<>();
        linegraph.addSeries(xyseries);
        linegraph.getViewport().setMinX(0);
       linegraph.getViewport().setMaxX(10);
        linegraph.getViewport().setMinY(1.5);
        linegraph.getViewport().setMaxY(3);
        linegraph.setClickable(false);
        linegraph.getViewport().setXAxisBoundsManual(true);
        linegraph.getViewport().setYAxisBoundsManual(true);
        GridLabelRenderer gridLabel = linegraph.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("Time (S)");
        gridLabel.setVerticalAxisTitle("Volt (V)");
        //set some properties
        xyseries.setColor(Color.BLUE);
        xyseries.setThickness(5);

        //set Scrollable and Scaleable
       // linegraph.getViewport().setScalable(true);
      //  linegraph.getViewport().setScalableY(true);
       // linegraph.getViewport().setScrollable(true);
       // linegraph.getViewport().setScrollableY(true);


        //sets padding to show larger numbers on the left hand side
        GridLabelRenderer glr = linegraph.getGridLabelRenderer();
        glr.setPadding(150);


    }

    private void adddataset(final String data)
    {

        if(datatransfer == false)
        {
            startTime = SystemClock.uptimeMillis();
            datatransfer = true;
            Log.d(LOG_TAG, "mReceiver: data transfered turned on" );
            xyseries.resetData(new DataPoint[]{});
            double highestx = xyseries.getHighestValueX();
            Log.d(LOG_TAG, "mReceiver: Highest X:" + highestx );
            StartTime = SystemClock.uptimeMillis();

        }

        if(data.endsWith(";"))
        {
           writeout = removeLastChar(data);
           if(writeout.length() == 3 && !writeout.contains(";") )
           {

               mTimer1 = new Runnable() {
                   @Override
                   public void run() {



                       long endTime = SystemClock.uptimeMillis();
                       long elapsedMilliSeconds = endTime - startTime;
                       time = elapsedMilliSeconds / 1000.0;




                       //System.out.print( "Graph Output: " + writeout);
                       //error coming from here double.parseDouble("");
                       try{
                           dataint = Double.parseDouble(writeout);
                           dataint = dataint/100;



                           if(dataint > 1)
                           {
                               String Stringtowrite = String.valueOf(dataint);
                               long millis= System.currentTimeMillis();
                               String timestamp = calculateDifference(millis);






                               if (inputcounter >= 200) {
                                   for (int i = 1; i < 200; i++) {
                                       circlebuffer[i - 1] = circlebuffer[i];
                                   }

                                   circlebuffer[199] = new DataPoint(time, dataint);
                                   writer.print(Stringtowrite  + " "  + timestamp + System.getProperty("line.separator"));

                                   Log.d(LOG_TAG, "Graph Input: " + dataint + " time: " + time);
                                   xyseries.resetData(circlebuffer);



                                   if (xyseries.getHighestValueX() > oldsize - 1) {

                                       linegraph.getViewport().setMinX(xyseries.getLowestValueX());
                                       linegraph.getViewport().setMaxX(xyseries.getHighestValueX() + 1);
                                       oldsize = xyseries.getHighestValueX() + 1;
                                   }


                               } else {
                                   for (int i = 1; i < 200; i++) {
                                       circlebuffer[i - 1] = circlebuffer[i];
                                   }

                                   circlebuffer[199] = new DataPoint(time, dataint);
                                   writer.print(Stringtowrite  + " "  + timestamp  + System.getProperty("line.separator"));



                                   inputcounter++;
                                   Log.d(LOG_TAG, "Graph Input: " + dataint + " time: " + time);
                                   xyseries.appendData(new DataPoint(time, dataint), true, 200);

                               }
                               delaycounter = 0;

                           }

                       }
                       catch (Exception e)
                       {
                           e.printStackTrace();
                           Log.d(LOG_TAG, "Error caught " + writeout);


                       }



                   }





               };

               mHandlergraph.post(mTimer1);
           }

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(fos != null)
        {
            try{
                fos.flush();
                fos.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }

    private static String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }


    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(LOG_TAG,"Permission is granted");
                return true;
            } else {

                Log.v(LOG_TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(LOG_TAG,"Permission is granted");
            return true;
        }
    }



    private String calculateDifference(long timeInMillis){

        int hours = (int) ((timeInMillis / (1000 * 60 * 60)));
        int minutes = (int) ((timeInMillis / (1000 * 60)) % 60);
        int seconds = (int) ((timeInMillis / 1000) % 60);
        return hours+":"+minutes+":"+seconds;
    }

}