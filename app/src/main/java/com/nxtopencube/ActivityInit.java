package com.nxtopencube;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.security.Security;

/* loaded from: classes.dex */
public class ActivityInit extends Activity {
    final Context context = this;
    ProgressDialog dialog;

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NxtMain.nxtBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (NxtMain.nxtBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_nobluetooth), Toast.LENGTH_LONG).show();
        }
        if (!NxtMain.nxtBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
            startActivityForResult(enableBtIntent, 0);
        }
        setContentView(R.layout.activity_init);
    }

    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        Button buttonInitConnection = (Button) findViewById(R.id.buttonInitConnection);
        Button buttonInitStart = (Button) findViewById(R.id.buttonInitStart);
        Button buttonInitStop = (Button) findViewById(R.id.buttonInitStop);
        Button buttonInitLifter = (Button) findViewById(R.id.buttonInitLifter);
        buttonInitStart.setEnabled(NxtMain.connectionEstablished);
        buttonInitStop.setEnabled(NxtMain.connectionEstablished);
        //This next one is wrong.  We should have a test for IF NXT program has started.
        buttonInitLifter.setEnabled(NxtMain.programRunning);
        buttonInitConnection.setOnClickListener(new InitBluetoothConnection(buttonInitStart, buttonInitStop, buttonInitLifter));
        buttonInitStart.setOnClickListener(new View.OnClickListener() { // from class: com.example.firstapp.ActivityInit.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                String text = String.valueOf(NxtMain.progName) + ".rxe";
                NxtMain.connNxtSM1.startProgram(text);
                NxtMain.connNxtSM2.startProgram(text);
                NxtMain.programRunning = true;
                buttonInitLifter.setEnabled(NxtMain.programRunning);
                Toast.makeText(ActivityInit.this.getApplicationContext(), ActivityInit.this.getString(R.string.words_program) + " " + text + " " + ActivityInit.this.getString(R.string.words_started), Toast.LENGTH_SHORT).show();
            }
        });
        buttonInitStop.setOnClickListener(new View.OnClickListener() { // from class: com.example.firstapp.ActivityInit.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                NxtMain.connNxtSM1.stopProgram();
                NxtMain.connNxtSM2.stopProgram();
                NxtMain.programRunning = false;
                buttonInitLifter.setEnabled(NxtMain.programRunning);
                Toast.makeText(ActivityInit.this.getApplicationContext(), ActivityInit.this.getString(R.string.status_stopped), Toast.LENGTH_SHORT).show();
            }
        });
        buttonInitLifter.setOnClickListener(new View.OnClickListener() { // from class: com.example.firstapp.ActivityInit.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                NxtMain.connNxtSM1.sendCommand("LIFTERINIT()");
                NxtMain.lifterStatus = 1;
                Toast.makeText(ActivityInit.this.getApplicationContext(), ActivityInit.this.getString(R.string.status_lifterinit), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* renamed from: com.example.firstapp.ActivityInit$1  reason: invalid class name */
    /* loaded from: classes.dex */
    class InitBluetoothConnection implements View.OnClickListener {
        private final /* synthetic */ Button val$buttonInitLifter;
        private final /* synthetic */ Button val$buttonInitStart;
        private final /* synthetic */ Button val$buttonInitStop;

        InitBluetoothConnection(Button button, Button button2, Button button3) {
            this.val$buttonInitStart = button;
            this.val$buttonInitStop = button2;
            this.val$buttonInitLifter = button3;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            ActivityInit.this.dialog = new ProgressDialog(ActivityInit.this.context);
            ActivityInit.this.dialog.setTitle(ActivityInit.this.getString(R.string.dialog_bluetoothtitle));
            ActivityInit.this.dialog.setMessage(ActivityInit.this.getString(R.string.dialog_bluetooth));
            ActivityInit.this.dialog.setIndeterminate(true);
            ActivityInit.this.dialog.setCancelable(false);
            ActivityInit.this.dialog.show();
            final Button button = this.val$buttonInitStart;
            final Button button2 = this.val$buttonInitStop;
            final Button button3 = this.val$buttonInitLifter;
            new Thread(new Runnable() { // from class: com.example.firstapp.ActivityInit.1.1
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        NxtMain.pairedDevices = NxtMain.nxtBluetoothAdapter.getBondedDevices();
                        for (BluetoothDevice device : NxtMain.pairedDevices) {
                            if (device.getName().equals(NxtMain.nxtName1)) {
                                NxtMain.nxtSM1 = device;
                            }
                            if (device.getName().equals(NxtMain.nxtName2)) {
                                NxtMain.nxtSM2 = device;
                            }
                        }
                    }
                    catch(SecurityException error){
                        //do something with the error?
                    }

                    if (NxtMain.nxtSM1 == null || NxtMain.nxtSM2 == null) {
                        ActivityInit.this.dialog.dismiss();
                        ActivityInit.this.runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityInit.1.1.2
                            @Override // java.lang.Runnable
                            public void run() {
                                AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(ActivityInit.this.context);
                                alertDialogBuilder2.setTitle(ActivityInit.this.getString(R.string.dialog_bluetoothtitle)).setMessage(ActivityInit.this.getString(R.string.dialog_bluetoothproblem)).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.example.firstapp.ActivityInit.1.1.2.1
                                    @Override // android.content.DialogInterface.OnClickListener
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                                AlertDialog alert2 = alertDialogBuilder2.create();
                                alert2.show();
                            }
                        });
                        return;
                    }
                    NxtMain.connNxtSM1 = new NxtConnectBluetooth(NxtMain.nxtSM1);
                    NxtMain.connNxtSM1.start();
                    NxtMain.connNxtSM2 = new NxtConnectBluetooth(NxtMain.nxtSM2);
                    NxtMain.connNxtSM2.start();
                    int i = 0;
                    while (!NxtMain.connNxtSM1.connectionEstablished) {
                        try {
                            Thread.sleep(80L);
                        } catch (InterruptedException e) {
                        }
                        i++;
                        if (i == 100) {
                            break;
                        }
                    }
                    int i2 = 0;
                    while (!NxtMain.connNxtSM2.connectionEstablished) {
                        try {
                            Thread.sleep(80L);
                        } catch (InterruptedException e2) {
                        }
                        i2++;
                        if (i2 == 100) {
                            break;
                        }
                    }
                    ActivityInit.this.dialog.dismiss();
                    ActivityInit activityInit = ActivityInit.this;
                    final Button button4 = button;
                    final Button button5 = button2;
                    final Button button6 = button3;
                    activityInit.runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityInit.1.1.1
                        @Override // java.lang.Runnable
                        public void run() {
                            if (NxtMain.connNxtSM1.connectionEstablished && NxtMain.connNxtSM2.connectionEstablished) {
                                NxtMain.connectionEstablished = true;
                                button4.setEnabled(true);
                                button5.setEnabled(true);
                                //We will enable this button once the program is running.
                                //button6.setEnabled(true);
                                Toast.makeText(ActivityInit.this.getApplicationContext(), ActivityInit.this.getString(R.string.toast_connection), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            NxtMain.connectionEstablished = false;
                            NxtMain.connNxtSM1.cancel();
                            NxtMain.connNxtSM2.cancel();
                            button4.setEnabled(false);
                            button5.setEnabled(false);
                            button6.setEnabled(false);
                            AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(ActivityInit.this.context);
                            alertDialogBuilder1.setTitle(ActivityInit.this.getString(R.string.dialog_bluetoothtitle)).setMessage(ActivityInit.this.getString(R.string.dialog_bluetoothnoconn)).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.example.firstapp.ActivityInit.1.1.1.1
                                @Override // android.content.DialogInterface.OnClickListener
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                            AlertDialog alert1 = alertDialogBuilder1.create();
                            alert1.show();
                        }
                    });
                }
            }).start();
        }
    }
}
