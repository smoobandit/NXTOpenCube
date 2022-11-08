package com.nxtopencube;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import androidx.annotation.NonNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/* loaded from: classes.dex */
public class NxtConnectBluetooth extends Thread {
    public boolean messageReceived;
    private InputStream nxtInputStream;
    public String nxtMessage;
    private OutputStream nxtOutputStream;
    public BluetoothSocket nxtSocket;
    public boolean connectionEstablished = false;
    private UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public NxtConnectBluetooth(BluetoothDevice device) {
        try {
            this.nxtSocket = device.createRfcommSocketToServiceRecord(this.myUUID);
        } catch (IOException e) {
            //do something with exception?
        } catch (SecurityException broken1) {
            //do something with exception?
        }
    }

    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
        try {
            this.nxtSocket.connect();
            this.connectionEstablished = true;
        } catch (IOException e) {
            //do something with exception?
        }catch (SecurityException broken2) {
            //do something with exception?
        }
        try {
            this.nxtOutputStream = new DataOutputStream(this.nxtSocket.getOutputStream());
        } catch (IOException e2) {
            //do something with exception?
        }
    }

    public void cancel() {
        try {
            this.nxtSocket.close();
            this.connectionEstablished = false;
        } catch (IOException e) {
            //do something with exception?
        }
    }

    /*public boolean startProgram(String text) {
        char[] charArray;
        int byteslength = text.length();
        ByteBuffer buffer = ByteBuffer.allocate(byteslength + 8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short) (byteslength + 6));
        buffer.put(Byte.MIN_VALUE);
        buffer.put((byte) 0);
        for (char c : text.toCharArray()) {
            buffer.put((byte) c);
        }
        buffer.put((byte) 0);
        try {
            this.nxtOutputStream.write(buffer.array());
            return true;
        } catch (IOException e) {
            cancel();
            return false;
        }
    }*/
    public boolean startProgram(String text) {
        boolean retval = false;
        ByteBuffer buffer;
        int byteslength = text.length();

        buffer = ByteBuffer.allocate(byteslength + 8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short) (byteslength + 6));
        buffer.put((byte) 0x80);
        buffer.put((byte) 0x00);

        for (char c : text.toCharArray()) {buffer.put((byte) c);}

        buffer.put((byte) 0x00);

        try {
            nxtOutputStream.write(buffer.array());
            retval = true;
        } catch (IOException writeException) {
            cancel();
        }
        return retval;
    }

    public boolean stopProgram() {
        boolean retval = false;
        ByteBuffer buffer;

        buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short) (6));
        buffer.put((byte) 0x80);
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x00);

        try {
            nxtOutputStream.write(buffer.array());
            retval = true;
        } catch (IOException writeException) {
            cancel();
        }
        return retval;
    }
/*    public boolean stopProgram() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short) 6);
        buffer.put(Byte.MIN_VALUE);
        buffer.put((byte) 1);
        buffer.put((byte) 0);
        try {
            this.nxtOutputStream.write(buffer.array());
            return true;
        } catch (IOException e) {
            cancel();
            return false;
        }
    }*/

    public boolean sendCommand(@NonNull String text) {
        boolean retval = false;
        int byteslength = text.length();
        byte[] readbuffer = new byte[1024];
        int bytes = 0;
        if (NxtMain.stop) {
            return false;
        }
        this.nxtInputStream = null;
        try {
            this.nxtInputStream = this.nxtSocket.getInputStream();
        } catch (IOException e) {
            cancel();
        }
        try {
            if (this.nxtInputStream.available() > 0) {
                try {
                    bytes = this.nxtInputStream.read(readbuffer);
                } catch (IOException e2) {
                }
            }
        } catch (IOException e3) {
        }
        ByteBuffer buffer = ByteBuffer.allocate(byteslength + 10);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short) (byteslength + 8));
        buffer.put(Byte.MIN_VALUE);
        buffer.put((byte) 9);
        buffer.put((byte) 0);
        buffer.put((byte) (byteslength + 1));
        for (char c : text.toCharArray()) {
            buffer.put((byte) c);
        }
        buffer.put((byte) 0);
        try {
            this.nxtOutputStream.write(buffer.array());
        } catch (IOException e4) {
            cancel();
        }
        this.nxtInputStream = null;
        try {
            this.nxtInputStream = this.nxtSocket.getInputStream();
        } catch (IOException e5) {
            cancel();
        }
        int i = 0;
        do {
            try {
                if (this.nxtInputStream.available() != 0) {
                    break;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e6) {
                }
                i++;
            } catch (IOException e7) {
            }
        } while (i != 300);
        try {
            if (this.nxtInputStream.available() > 0) {
                try {
                    bytes = this.nxtInputStream.read(readbuffer);
                } catch (IOException e8) {
                    cancel();
                }
                if (new String(readbuffer, 0, bytes).substring(6).equals("OK")) {
                    retval = true;
                }
            }
        } catch (IOException e9) {
        }
        return retval;
    }

    //The variant below, although more wordy, does not work.
    //It sends the message fine, but does NOT return an OK.
    /*public boolean sendCommand(String text) {
        char[] charArray;
        boolean retval = false;
        int byteslength = text.length();
        byte[] readbuffer = new byte[1024];
        int bytes = 0;
        if (NxtMain.stop) {
            return false;
        }
        this.nxtInputStream = null;
        try {
            this.nxtInputStream = this.nxtSocket.getInputStream();
        } catch (IOException e) {
            cancel();
        }
        try {
            if (this.nxtInputStream.available() > 0) {
                try {
                    bytes = this.nxtInputStream.read(readbuffer);
                } catch (IOException e2) {
                }
            }
        } catch (IOException e3) {
        }
        ByteBuffer buffer = ByteBuffer.allocate(byteslength + 10);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short) (byteslength + 8));
        buffer.put(Byte.MIN_VALUE);
        buffer.put((byte) 9);
        buffer.put((byte) 0);
        buffer.put((byte) (byteslength + 1));
        for (char c : text.toCharArray()) {
            buffer.put((byte) c);
        }
        buffer.put((byte) 0);
        try {
            this.nxtOutputStream.write(buffer.array());
        } catch (IOException e4) {
            cancel();
        }
        this.nxtInputStream = null;
        try {
            this.nxtInputStream = this.nxtSocket.getInputStream();
        } catch (IOException e5) {
            cancel();
        }
        int i = 0;
        try {
            do {
                try {
                    //available() -> Returns an estimate of the number of bytes that can be read (or skipped over) from this input stream without blocking by the next invocation of a method for this input stream.
                    if (this.nxtInputStream.available() == 0) {
                        try {
                            Thread.sleep(50L);
                        } catch (InterruptedException e6) {
                            //do something with exception?
                        }
                        i++;
                    }
                } catch (IOException e7) {
                    //do something with exception?
                }
                //Not sure why the decomplier has a break in here:
                break;
            } while (i != 300);
            //Decomplier had a break in here, not sure why.  Maybe in the catch above?
            //break;
            if (this.nxtInputStream.available() > 0) {
                try {
                    bytes = this.nxtInputStream.read(readbuffer);
                } catch (IOException e8) {
                    cancel();
                }
                String readMessage = new String(readbuffer, 0, bytes);
                if (readMessage.substring(6).equals("OK")) {
                    retval = true;
                }
            }
        } catch (IOException e9) {
            //do something with exception?
        }
        return retval;
    }*/
}
