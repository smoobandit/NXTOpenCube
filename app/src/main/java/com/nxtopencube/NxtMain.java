package com.nxtopencube;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import java.util.Set;

/* loaded from: classes.dex */
public class NxtMain {
    public static NxtConnectBluetooth connNxtSM1;
    public static NxtConnectBluetooth connNxtSM2;
    public static String cubeDefinition;
    public static String cubeSolve;
    public static BluetoothAdapter nxtBluetoothAdapter;
    public static String nxtName1;
    public static String nxtName2;
    public static BluetoothDevice nxtSM1;
    public static BluetoothDevice nxtSM2;
    public static Set<BluetoothDevice> pairedDevices;
    public static String progName;
    public static int waitClose;
    public static int waitDoubleTurn;
    public static int waitOpen;
    public static int waitTurn;
    public static boolean connectionEstablished = false;
    public static boolean programRunning = false;
    public static boolean stop = false;
    public static long starttime = 0;
    public static boolean runtime = false;
    public static String picpath = "/sdcard/";
    public static int lifterStatus = 0;
    public static String pattern;
}
