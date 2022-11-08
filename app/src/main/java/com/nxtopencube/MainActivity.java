package com.nxtopencube;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {
    Camera camera;
    Camera.PictureCallback jpegCallback;
    //Camera.PictureCallback rawCallback;
    //Camera.ShutterCallback shutterCallback;
    int photocount = 0;
    char[] filenames = {'U', 'D', 'L', 'R', 'B', 'F'};
    final Context context = this;
    //Set this to true to skip directly to colour detection based on files already on the phone.
    boolean simulation = false;
    //Set this to true to log appendLog output to a file when running on the actual phone.
    boolean logging = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appendLog("starting");
        NxtMain.connectionEstablished = false;
        NxtMain.lifterStatus = 0;
        NxtMain.stop = false;
        NxtMain.starttime = 0L;
        NxtMain.runtime = false;
        NxtMain.pattern = getString(R.string.solution_pons_asinorum); //Default to simplest pattern
        this.setContentView(R.layout.activity_main);
        Toast.makeText(this.getApplicationContext(), this.getString(R.string.app_name), Toast.LENGTH_LONG).show();
        this.camera = Camera.open();
        Camera.Parameters param = this.camera.getParameters();
        param.setPreviewSize(352, 288);
        param.setFlashMode("on");
        param.setFocusMode("macro");
        param.setWhiteBalance("fluorescent");
        param.setSceneMode("party");
        this.camera.setParameters(param);
        this.camera.setDisplayOrientation(90);
        this.camera.startPreview();
        this.jpegCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream outStream = null;
                try {
                    outStream = new FileOutputStream(NxtMain.picpath + filenames[photocount] + ".jpg");
                    photocount++;
                    outStream.write(data);
                    outStream.close();
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_picsaved) + ": " + filenames[photocount - 1] + " (" + data.length + ").", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }
                camera.stopPreview();

                camera.startPreview();
                switch (photocount) {
                    case 1: //U -> D (Yellow -> White)
                        NxtMain.connNxtSM2.sendCommand("TURN(NS,2,-2)");
                        break;
                    case 2: //D -> L (White -> Red)
                        NxtMain.connNxtSM2.sendCommand("TURN(NS,-1,1)");
                        NxtMain.connNxtSM1.sendCommand("CLOSE(EW,1,1)");
                        NxtMain.connNxtSM1.sendCommand("OPEN(NS,1,1)");
                        NxtMain.connNxtSM2.sendCommand("TURN(NS,-1,1)");
                        break;
                    case 3: //L -> R (Red -> Orange)
                        NxtMain.connNxtSM1.sendCommand("TURN(EW,-2,2)");
                        break;
                    case 4: //R -> B (Orange -> Blue)
                        NxtMain.connNxtSM2.sendCommand("TURN(NS,-1,1)");
                        NxtMain.connNxtSM1.sendCommand("CLOSE(NS,1,1)");
                        NxtMain.connNxtSM1.sendCommand("OPEN(EW,1,1)");
                        NxtMain.connNxtSM1.sendCommand("TURN(EW,-1,1)");
                        NxtMain.connNxtSM1.sendCommand("CLOSE(EW,1,1)");
                        NxtMain.connNxtSM1.sendCommand("OPEN(NS,1,1)");
                        NxtMain.connNxtSM2.sendCommand("TURN(NS,-1,1)");
                        NxtMain.connNxtSM1.sendCommand("TURN(EW,-1,1)");
                        break;
                    case 5: //B -> F (Blue -> Green)
                        NxtMain.connNxtSM1.sendCommand("TURN(EW,-2,2)");
                        break;
                    case 6:
                        NxtMain.connNxtSM1.sendCommand("TURN(EW,1,-1)");
                        NxtMain.connNxtSM2.sendCommand("TURN(NS,-1,1)");
                        NxtMain.connNxtSM1.sendCommand("CLOSE(NS,1,1)");
                        NxtMain.connNxtSM1.sendCommand("OPEN(EW,1,1)");
                        NxtMain.connNxtSM2.sendCommand("TURN(NS,-1,1)");
                        NxtMain.connNxtSM1.sendCommand("CLOSE(EW,1,1)");
                        NxtMain.connNxtSM1.sendCommand("OPEN(NS,1,1)");
                        NxtMain.connNxtSM2.sendCommand("TURN(NS,-1,1)");
                        NxtMain.connNxtSM1.sendCommand("CLOSE(NS,1,1)");
                        break;
                }
                if (photocount < 6) {
                    camera.takePicture(null, null, jpegCallback);
                } else {
                    camera.lock();
                    new Thread(new Runnable() {
                        public void run() {
                            lastStep();
                        }
                    }).start();
                }
            }

        };
    }

    public void firstStep() {
        appendLog("first step");
        final Button buttonStart = (Button) findViewById(R.id.buttonStart);
        final Button buttonStop = (Button) findViewById(R.id.buttonStop);
        final TextView textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        if (!NxtMain.stop) {
            runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityMain.11
                @Override // java.lang.Runnable
                public void run() {
                    buttonStart.setEnabled(false);
                    buttonStop.setEnabled(true);
                    textViewStatus.setText(String.valueOf(MainActivity.this.getString(R.string.status_start)) + "\r\n");
                }
            });
            NxtMain.connNxtSM1.sendCommand("SETWAIT(OPEN," + NxtMain.waitOpen + ")");
            NxtMain.connNxtSM1.sendCommand("SETWAIT(CLOSE," + NxtMain.waitClose + ")");
            NxtMain.connNxtSM1.sendCommand("SETWAIT(TURN,300)");
            NxtMain.connNxtSM1.sendCommand("SETWAIT(DOUBLETURN,600)");
            NxtMain.connNxtSM2.sendCommand("SETWAIT(TURN,300)");
            NxtMain.connNxtSM2.sendCommand("SETWAIT(DOUBLETURN,600)");
            if (NxtMain.lifterStatus == 0) {
                if (!NxtMain.stop) {
                    NxtMain.connNxtSM1.sendCommand("LIFTERINIT()");
                    NxtMain.lifterStatus = 1;
                    if (!NxtMain.stop) {
                        runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityMain.12
                            @Override // java.lang.Runnable
                            public void run() {
                                textViewStatus.setText(((Object) textViewStatus.getText()) + MainActivity.this.getString(R.string.status_lifterinit) + "\r\n");
                            }
                        });
                    } else {
                        programCanceled();
                        return;
                    }
                } else {
                    programCanceled();
                    return;
                }
            }
            if (NxtMain.lifterStatus == 1) {
                if (!NxtMain.stop) {
                    runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityMain.13
                        @Override // java.lang.Runnable
                        public void run() {
                            textViewStatus.setText(((Object) textViewStatus.getText()) + MainActivity.this.getString(R.string.status_lifterup) + "\r\n");
                        }
                    });
                    NxtMain.connNxtSM1.sendCommand("OPEN(NS,1,1)");
                    NxtMain.connNxtSM1.sendCommand("OPEN(EW,1,1)");
                    NxtMain.connNxtSM1.sendCommand("LIFT(24)");
                    NxtMain.connNxtSM2.sendCommand("TURN(NS,-1,-1)");
                    NxtMain.lifterStatus = 2;
                } else {
                    programCanceled();
                    return;
                }
            }
            if (NxtMain.lifterStatus == 2) {
                if (!NxtMain.stop) {
                    runOnUiThread(new UserInsertsCube());
                    NxtMain.connNxtSM1.sendCommand("ALERT(WUERFEL,1,100)"); //WUERFEL = CUBE
                    return;
                }
                programCanceled();
                return;
            }
            return;
        }
        programCanceled();
    }

    public void lastStep() {
        appendLog("last step");
        final Button buttonStart = (Button) findViewById(R.id.buttonStart);
        final Button buttonStop = (Button) findViewById(R.id.buttonStop);
        final TextView textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        if (!NxtMain.stop) {
            runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityMain.17
                @Override // java.lang.Runnable
                public void run() {
                    textViewStatus.setText(((Object) textViewStatus.getText()) + MainActivity.this.getString(R.string.status_photossaved) + "\r\n");
                }
            });
            //I think I get to here, and then it fails in the detection of the cube.
            //I do not even get detection failed message.
            //appendLog("about to start colour detection");
            //This is born out by the new logging.  We fail here.
            //Best option - load pics into emulator.
            //Then just jump straight to laststep from the init button.
            NxtColorDetection colordetection = new NxtColorDetection();
            NxtMain.cubeDefinition = colordetection.getCube(NxtMain.picpath, true);
            appendLog("colour detection finished");
            appendLog(NxtMain.cubeDefinition);
            if (!NxtMain.stop) {
                runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityMain.18
                    @Override // java.lang.Runnable
                    public void run() {
                        textViewStatus.setText(((Object) textViewStatus.getText()) + "\r\n" + MainActivity.this.getString(R.string.status_colorsdetected) + "\r\n");
                        textViewStatus.setText(((Object) textViewStatus.getText()) + NxtMain.cubeDefinition + "\r\n");
                    }
                });
                if (!NxtMain.cubeDefinition.equals(MainActivity.this.getString(R.string.solved_state))) {
                    appendLog("Cube shuffled. Starting Search");
                    CsSearch search = new CsSearch();
                    appendLog("Created searcher.");
                    NxtMain.cubeSolve = search.solution(NxtMain.cubeDefinition, 21, 100L, 0L, 0);
                    appendLog("Solve produced:" + NxtMain.cubeSolve);
                    if (NxtMain.cubeSolve.substring(0, 1).equals("E")) {
                        //Then the solve has failed and returned an error message.
                        appendLog("Error in colour detection: " + NxtMain.cubeSolve);
                        if (!NxtMain.stop) {
                            runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityMain.19
                                @Override // java.lang.Runnable
                                public void run() {
                                    textViewStatus.setText(((Object) textViewStatus.getText()) + "\r\n" + MainActivity.this.getString(R.string.status_cubeerror) + "\r\n");
                                    textViewStatus.setText(((Object) textViewStatus.getText()) + MainActivity.this.getString(R.string.status_cubeerrortext) + "\r\n");
                                    textViewStatus.setText(((Object) textViewStatus.getText()) + get_error_message(NxtMain.cubeSolve) + "\r\n");
                                    //NxtMain.cubeSolve
                                }
                            });
                        } else {
                            programCanceled();
                            return;
                        }
                    } else if (!NxtMain.stop) {
                        appendLog("Solution found.");
                        //We are ready to send the solution.
                        solve_cube(NxtMain.cubeSolve, textViewStatus);
                        /*runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityMain.20
                            @Override // java.lang.Runnable
                            public void run() {
                                textViewStatus.setText(((Object) textViewStatus.getText()) + "\r\n" + MainActivity.this.getString(R.string.status_cubesolved) + "\r\n");
                                textViewStatus.setText(((Object) textViewStatus.getText()) + NxtMain.cubeSolve + "\r\n");
                            }
                        });
                        //Define our waits for cube manipulation
                        NxtMain.connNxtSM1.sendCommand("SETWAIT(OPEN," + NxtMain.waitOpen + ")");
                        NxtMain.connNxtSM1.sendCommand("SETWAIT(CLOSE," + NxtMain.waitClose + ")");
                        NxtMain.connNxtSM1.sendCommand("SETWAIT(TURN," + NxtMain.waitTurn + ")");
                        NxtMain.connNxtSM1.sendCommand("SETWAIT(DOUBLETURN," + NxtMain.waitDoubleTurn + ")");
                        NxtMain.connNxtSM2.sendCommand("SETWAIT(TURN," + NxtMain.waitTurn + ")");
                        NxtMain.connNxtSM2.sendCommand("SETWAIT(DOUBLETURN," + NxtMain.waitDoubleTurn + ")");
                        if (!NxtMain.stop) {
                            NxtMain.runtime = true;
                            NxtMachine machine = new NxtMachine();
                            machine.moveCube(NxtMain.cubeSolve);
                            NxtMain.runtime = false;
                            runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityMain.21
                                @Override // java.lang.Runnable
                                public void run() {
                                    textViewStatus.setText(((Object) textViewStatus.getText()) + "\r\n" + MainActivity.this.getString(R.string.status_cubemoved) + "\r\n");
                                }
                            });
                        } else {
                            programCanceled();
                            return;
                        }*/
                    } else {
                        programCanceled();
                        return;
                    }
                } else {
                    //We are looking at a solved cube.  We COULD just give up...
                    //Or we can reconfigure the cube to something special...
                    appendLog("Cube solved. Applying pattern");
                    solve_cube(NxtMain.pattern, textViewStatus);
                }
                NxtMain.connNxtSM1.sendCommand("LIFT(10)");
                NxtMain.connNxtSM1.sendCommand("OPEN(NS,1,1)");
                NxtMain.connNxtSM1.sendCommand("OPEN(EW,1,1)");
                NxtMain.connNxtSM1.sendCommand("LIFT(14)");
                NxtMain.connNxtSM2.sendCommand("TURN(NS,-1,-1)");
                NxtMain.lifterStatus = 2;
                if (!NxtMain.stop) {
                    runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityMain.22
                        @Override // java.lang.Runnable
                        public void run() {
                            buttonStop.setEnabled(false);
                            buttonStart.setEnabled(true);
                            textViewStatus.setText(((Object) textViewStatus.getText()) + MainActivity.this.getString(R.string.status_finished) + "\r\n");
                        }
                    });
                    return;
                } else {
                    programCanceled();
                    return;
                }
            }
            programCanceled();
            return;
        }
        programCanceled();
    }

    private String get_error_message(String solve_result) {
        switch (solve_result.charAt(6)){
            case 1: return MainActivity.this.getString(R.string.error1);
            case 2: return MainActivity.this.getString(R.string.error2);
            case 3: return MainActivity.this.getString(R.string.error3);
            case 4: return MainActivity.this.getString(R.string.error4);
            case 5: return MainActivity.this.getString(R.string.error5);
            case 6: return MainActivity.this.getString(R.string.error6);
            case 7: return MainActivity.this.getString(R.string.error7);
            case 8: return MainActivity.this.getString(R.string.error8);
        }
        return "";
    }

    public void solve_cube(String solution, TextView textViewStatus) {
        appendLog("Applying solution: " + solution);
        runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityMain.20
            @Override // java.lang.Runnable
            public void run() {
                textViewStatus.setText(((Object) textViewStatus.getText()) + "\r\n" + MainActivity.this.getString(R.string.status_cubesolved) + "\r\n");
                textViewStatus.setText(((Object) textViewStatus.getText()) + solution + "\r\n");
            }
        });
        //Define our waits for cube manipulation
        NxtMain.connNxtSM1.sendCommand("SETWAIT(OPEN," + NxtMain.waitOpen + ")");
        NxtMain.connNxtSM1.sendCommand("SETWAIT(CLOSE," + NxtMain.waitClose + ")");
        NxtMain.connNxtSM1.sendCommand("SETWAIT(TURN," + NxtMain.waitTurn + ")");
        NxtMain.connNxtSM1.sendCommand("SETWAIT(DOUBLETURN," + NxtMain.waitDoubleTurn + ")");
        NxtMain.connNxtSM2.sendCommand("SETWAIT(TURN," + NxtMain.waitTurn + ")");
        NxtMain.connNxtSM2.sendCommand("SETWAIT(DOUBLETURN," + NxtMain.waitDoubleTurn + ")");
        if (!NxtMain.stop) {
            NxtMain.runtime = true;
            NxtMachine machine = new NxtMachine();
            machine.moveCube(solution);
            NxtMain.runtime = false;
            runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityMain.21
                @Override // java.lang.Runnable
                public void run() {
                    textViewStatus.setText(((Object) textViewStatus.getText()) + "\r\n" + MainActivity.this.getString(R.string.status_cubemoved) + "\r\n");
                }
            });
        } else {
            programCanceled();
            return;
        }
    }

    public void startSolve() {
        final TextView textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollViewStatus);
        if (!NxtMain.stop) {
            runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityMain.15
                @Override // java.lang.Runnable
                public void run() {
                    textViewStatus.setText(((Object) textViewStatus.getText()) + MainActivity.this.getString(R.string.status_takecube) + "\r\n");
                }
            });
            NxtMain.connNxtSM1.sendCommand("LIFT(-13)");
            NxtMain.connNxtSM1.sendCommand("CLOSE(NS,1,1)");
            NxtMain.connNxtSM1.sendCommand("LIFT(-11)");
            NxtMain.connNxtSM1.sendCommand("TURN(EW,-1,1)");
            NxtMain.lifterStatus = 0;
            if (!NxtMain.stop) {
                runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityMain.16
                    @Override // java.lang.Runnable
                    public void run() {
                        textViewStatus.setText(((Object) textViewStatus.getText()) + MainActivity.this.getString(R.string.status_startposition) + "\r\n");
                    }
                });
                this.photocount = 0;
                this.camera.takePicture(null, null, this.jpegCallback);
                return;
            }
            programCanceled();
            return;
        }
        programCanceled();
    }

    @Override // android.app.Activity
    public void onResume() {
        super.onResume();

        Button buttonStart = (Button) findViewById(R.id.buttonStart);
        Button buttonStop = (Button) findViewById(R.id.buttonStop);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/ericssonga628.ttf");
        TextView tv = (TextView) findViewById(R.id.textViewTime);
        tv.setTypeface(tf);
        //These are stored on the filesystem of the phone, and persist between settings.
        //Saved in ActivitySettings
        SharedPreferences settings = getSharedPreferences("Settings", 0);
        NxtMain.nxtName1 = settings.getString("NxtName1", getString(R.string.value_nxtname1)).toString();
        NxtMain.nxtName2 = settings.getString("NxtName2", getString(R.string.value_nxtname2)).toString();
        NxtMain.progName = settings.getString("ProgName", getString(R.string.value_progname)).toString();
        NxtMain.waitOpen = Integer.parseInt(settings.getString("WaitOpen", getString(R.string.value_waitopen)).toString());
        NxtMain.waitClose = Integer.parseInt(settings.getString("WaitClose", getString(R.string.value_waitclose)).toString());
        NxtMain.waitTurn = Integer.parseInt(settings.getString("WaitTurn", getString(R.string.value_waitturn)).toString());
        NxtMain.waitDoubleTurn = Integer.parseInt(settings.getString("WaitDoubleTurn", getString(R.string.value_waitdoubleturn)).toString());
        NxtMain.pattern = settings.getString("Pattern", getString(R.string.solution_pons_asinorum)).toString();
        buttonStart.setEnabled(true);
        buttonStop.setEnabled(false);

        if (NxtMain.connectionEstablished) {
            buttonStart.setText(getString(R.string.main_buttonstart1));
            Toast.makeText(this.getApplicationContext(), this.getString(R.string.message_start), Toast.LENGTH_LONG).show();
        } else {
            buttonStart.setText(getString(R.string.main_buttonstart2));
        }

        buttonStop.setOnClickListener(new View.OnClickListener() { // from class: com.example.firstapp.ActivityMain.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                final Button buttonStart2 = (Button) MainActivity.this.findViewById(R.id.buttonStart);
                final Button buttonStop2 = (Button) MainActivity.this.findViewById(R.id.buttonStop);
                final TextView textViewStatus = (TextView) MainActivity.this.findViewById(R.id.textViewStatus);
                NxtMain.stop = true;
                NxtMain.lifterStatus = 0;
                NxtMain.starttime = 0L;
                NxtMain.runtime = false;
                NxtMain.connNxtSM1.stopProgram();
                NxtMain.connNxtSM2.stopProgram();
                MainActivity.this.runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityMain.2.1
                    @Override // java.lang.Runnable
                    public void run() {
                        buttonStart2.setEnabled(false);
                        buttonStop2.setEnabled(false);
                        textViewStatus.setText(((Object) textViewStatus.getText()) + "\r\n" + MainActivity.this.getString(R.string.status_canceled) + "\r\n");
                    }
                });
            }
        });
        buttonStart.setOnClickListener(new View.OnClickListener() { // from class: com.example.firstapp.ActivityMain.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (NxtMain.connectionEstablished) {
                    if (NxtMain.lifterStatus == 0) {
                        AlertDialog.Builder alertDialogBuilder4 = new AlertDialog.Builder(MainActivity.this.context);
                        alertDialogBuilder4.setTitle(MainActivity.this.getString(R.string.dialog_cubetitle)).setMessage(MainActivity.this.getString(R.string.dialog_lifter)).setCancelable(false).setNegativeButton(MainActivity.this.getString(R.string.dialog_lifterbottom), new DialogInterface.OnClickListener() { // from class: com.example.firstapp.ActivityMain.3.1
                            @Override // android.content.DialogInterface.OnClickListener
                            public void onClick(DialogInterface dialog, int id) {
                                NxtMain.lifterStatus = 0;
                                dialog.cancel();
                                MainActivity.this.startNxtMachine();
                            }
                        }).setPositiveButton(MainActivity.this.getString(R.string.dialog_liftertop), new DialogInterface.OnClickListener() { // from class: com.example.firstapp.ActivityMain.3.2
                            @Override // android.content.DialogInterface.OnClickListener
                            public void onClick(DialogInterface dialog, int id) {
                                NxtMain.lifterStatus = 2;
                                dialog.cancel();
                                MainActivity.this.startNxtMachine();
                            }
                        });
                        AlertDialog alert4 = alertDialogBuilder4.create();
                        alert4.show();
                        return;
                    }
                    MainActivity.this.startNxtMachine();
                    return;
                }
                if (simulation) {
                    MainActivity.this.startNxtMachine();
                } else {
                    Intent nextScreen = new Intent(MainActivity.this.getApplicationContext(), ActivityInit.class);
                    MainActivity.this.startActivity(nextScreen);
                }
                return;
            }
        });
    }

    public void startNxtMachine() {
        new Thread(new Runnable() { // from class: com.example.firstapp.ActivityMain.4
            @Override // java.lang.Runnable
            public void run() {
                if (simulation) {
                    MainActivity.this.lastStep();
                } else {
                    MainActivity.this.firstStep();
                }
            }
        }).start();
        new Thread(new Runnable() { // from class: com.example.firstapp.ActivityMain.5
            @Override // java.lang.Runnable
            public void run() {
                MainActivity.this.printTime();
            }
        }).start();
    }

    public void printTime() {
        final TextView textViewTime = (TextView) findViewById(R.id.textViewTime);
        final ScrollView scrollViewStatus = (ScrollView) findViewById(R.id.scrollViewStatus);
        runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityMain.6
            @Override // java.lang.Runnable
            public void run() {
                textViewTime.setText("0:00.00");
            }
        });
        while (!NxtMain.runtime) {
            runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityMain.7
                @Override // java.lang.Runnable
                public void run() {
                    scrollViewStatus.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
            }
        }
        NxtMain.starttime = System.currentTimeMillis();
        while (NxtMain.runtime) {
            runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityMain.8
                @Override // java.lang.Runnable
                public void run() {
                    SimpleDateFormat sdf = new SimpleDateFormat("mm:ss.SS");
                    Date d = new Date(System.currentTimeMillis() - NxtMain.starttime);
                    String x = String.valueOf(sdf.format((java.util.Date) d)) + "0000";
                    textViewTime.setText(x.substring(1, 8));
                    scrollViewStatus.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
            try {
                Thread.sleep(20L);
            } catch (InterruptedException e2) {
            }
        }
        while (!NxtMain.runtime) {
            runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityMain.9
                @Override // java.lang.Runnable
                public void run() {
                    scrollViewStatus.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e3) {
            }
        }
    }

    public void programCanceled() {
        final Button buttonStart = (Button) findViewById(R.id.buttonStart);
        NxtMain.stop = false;
        runOnUiThread(new Runnable() { // from class: com.example.firstapp.ActivityMain.10
            @Override // java.lang.Runnable
            public void run() {
                buttonStart.setEnabled(true);
            }
        });
    }

    @Override // android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        this.camera.release();
        Toast.makeText(getApplicationContext(), getString(R.string.status_closed), Toast.LENGTH_SHORT).show();
    }

    @Override // android.app.Activity
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_menu) {
            return true;
        }
        if (id == R.id.menuitem_init) {
            Intent nextScreen = new Intent(getApplicationContext(), ActivityInit.class);
            startActivity(nextScreen);
            return true;
        } else if (id == R.id.menuitem_settings) {
            Intent nextScreen2 = new Intent(getApplicationContext(), ActivitySettings.class);
            startActivity(nextScreen2);
            return true;
        } else if (id == R.id.menuitem_patterns) {
            Intent nextScreen4 = new Intent(getApplicationContext(), ActivityPattern.class);
            startActivity(nextScreen4);
            return true;
        } else if (id == R.id.menuitem_manual) {
            Intent nextScreen3 = new Intent(getApplicationContext(), ActivityManual.class);
            startActivity(nextScreen3);
            return true;
        } else if (id == R.id.menuitem_quit) {
            //Quit?
            if(!simulation){
                NxtMain.connNxtSM1.stopProgram();
                NxtMain.connNxtSM2.stopProgram();
                NxtMain.programRunning = false;
                try {
                    NxtMain.connNxtSM1.nxtSocket.close();
                    NxtMain.connNxtSM2.nxtSocket.close();
                } catch (java.io.IOException error) {
                    //do something?
                }
            }
            this.finishAffinity();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void appendLog(String text)
    {
        if(this.logging){
            Log.d("MAIN:", text);
            File logFile = new File("sdcard/log.txt");
            if (!logFile.exists())
            {
                try
                {
                    logFile.createNewFile();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try
            {
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(text);
                buf.newLine();
                buf.close();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.example.firstapp.ActivityMain$14  reason: invalid class name */
    /* loaded from: classes.dex */
    public class UserInsertsCube implements Runnable {
        UserInsertsCube() {
        }

        @Override // java.lang.Runnable
        public void run() {
            AlertDialog.Builder alertDialogBuilder3 = new AlertDialog.Builder(MainActivity.this.context);
            alertDialogBuilder3.setTitle(MainActivity.this.getString(R.string.dialog_cubetitle)).setMessage(MainActivity.this.getString(R.string.dialog_cubetext)).setCancelable(false).setPositiveButton(MainActivity.this.getString(R.string.dialog_ok), new DialogInterface.OnClickListener() { // from class: com.example.firstapp.ActivityMain.14.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    new Thread(new Runnable() { // from class: com.example.firstapp.ActivityMain.14.1.1
                        @Override // java.lang.Runnable
                        public void run() {
                            MainActivity.this.startSolve();
                        }
                    }).start();
                }
            });
            AlertDialog alert3 = alertDialogBuilder3.create();
            alert3.show();
        }
    }
}