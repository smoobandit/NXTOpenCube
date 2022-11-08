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
    //Some tags for lifter status
    private static final int LIFTER_UNKNOWN = 0;
    private static final int  LIFTER_DOWN = 1;
    private static final int LIFTER_UP = 2;

    //Rough guide to the sequence here.
    //OnResume sets the click activity for the two buttons.
    //Click Init -> link bluetooth and setup NXTs
    //Click Start -> dialog about Lifter, if Unknown
    //-> StartNXTMachine -> First Step, UI Output
    //-> First Step -> Set Waits, move lifter into place
    //-> UserInsertsCube - waits for cube confirmation.
    //-> StartSolve -> grab cube, drop lifter, start pics
    //-> OnCreate lamba for camera pics, on 6 pics, call laststep
    //-> LastStep -> colour detection, report definition, search solution or pick pattern.
    //-> pass to solve_cube
    //-> back to LastStep, lifter up, present cube
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appendLog("starting");
        NxtMain.connectionEstablished = false;
        NxtMain.lifterStatus = LIFTER_UNKNOWN;
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

    @Override
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

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                final Button buttonStart2 = (Button) MainActivity.this.findViewById(R.id.buttonStart);
                final Button buttonStop2 = (Button) MainActivity.this.findViewById(R.id.buttonStop);
                final TextView textViewStatus = (TextView) MainActivity.this.findViewById(R.id.textViewStatus);
                NxtMain.stop = true;
                //Anytime we click stop, we clear the lifter status.
                NxtMain.lifterStatus = LIFTER_UNKNOWN;
                NxtMain.starttime = 0L;
                NxtMain.runtime = false;
                //Not clear what this command sends to the NXTs - it is a byte code.
                NxtMain.connNxtSM1.stopProgram();
                NxtMain.connNxtSM2.stopProgram();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttonStart2.setEnabled(false);
                        buttonStop2.setEnabled(false);
                        textViewStatus.setText(((Object) textViewStatus.getText()) + "\r\n" + MainActivity.this.getString(R.string.status_canceled) + "\r\n");
                    }
                });
            }
        });
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (NxtMain.connectionEstablished) {//Our test for Start/Init for the button.
                    if (NxtMain.lifterStatus == LIFTER_UNKNOWN) {
                        //If we are not sure what the lifter status is, ask.
                        AlertDialog.Builder alertDialogBuilder4 = new AlertDialog.Builder(MainActivity.this.context);
                        alertDialogBuilder4
                                .setTitle(MainActivity.this.getString(R.string.dialog_cubetitle))
                                .setMessage(MainActivity.this.getString(R.string.dialog_lifter))
                                .setCancelable(false)
                                .setNegativeButton(MainActivity.this.getString(R.string.dialog_lifterbottom), new DialogInterface.OnClickListener() {
                                    @Override // android.content.DialogInterface.OnClickListener
                                    public void onClick(DialogInterface dialog, int id) {
                                        //We clicked the bottom button.  Oddly, we set status to Unknown.
                                        NxtMain.lifterStatus = LIFTER_DOWN;  //Orig was UNKNOWN..
                                        dialog.cancel();
                                        MainActivity.this.startNxtMachine();
                                }})
                                .setPositiveButton(MainActivity.this.getString(R.string.dialog_liftertop), new DialogInterface.OnClickListener() {
                                    @Override // android.content.DialogInterface.OnClickListener
                                    public void onClick(DialogInterface dialog, int id) {
                                        NxtMain.lifterStatus = LIFTER_UP;
                                        dialog.cancel();
                                        MainActivity.this.startNxtMachine();
                                    }
                        });
                        AlertDialog alert4 = alertDialogBuilder4.create();
                        alert4.show();
                        return;
                    }
                    //We get here without the prompt if lifter status was not UNKNOWN.
                    MainActivity.this.startNxtMachine();
                    return;
                } //else the connection is not made.  So we clicked INIT.
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (simulation) {
                    MainActivity.this.lastStep();
                } else {
                    MainActivity.this.firstStep();
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.printTime();
            }
        }).start();
    }

    public void firstStep() {
        appendLog("first step");
        final Button buttonStart = (Button) findViewById(R.id.buttonStart);
        final Button buttonStop = (Button) findViewById(R.id.buttonStop);
        final TextView textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        if (!NxtMain.stop) {
            runOnUiThread(new Runnable() {
                @Override
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

            //First we deal with the lifter being in an unknown state.
            if (NxtMain.lifterStatus == LIFTER_UNKNOWN) {
                //Not sure how we would get here unknown.  We have to click start to get here.
                //Start does this test, and forces us to answer if it is Unknown.
                if (!NxtMain.stop) {
                    NxtMain.connNxtSM1.sendCommand("LIFTERINIT()");
                    NxtMain.lifterStatus = LIFTER_DOWN;
                    if (!NxtMain.stop) {
                        runOnUiThread(new Runnable() {
                            @Override
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
            //Next we deal with the lifter being down.
            if (NxtMain.lifterStatus == LIFTER_DOWN) {
                if (!NxtMain.stop) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textViewStatus.setText(((Object) textViewStatus.getText()) + MainActivity.this.getString(R.string.status_lifterup) + "\r\n");
                        }
                    });
                    NxtMain.connNxtSM1.sendCommand("OPEN(NS,1,1)"); //Open Grippers North/South
                    NxtMain.connNxtSM1.sendCommand("OPEN(EW,1,1)");  //Open Grippers East/West
                    NxtMain.connNxtSM1.sendCommand("LIFT(24)"); //Lift the lifter.
                    NxtMain.connNxtSM2.sendCommand("TURN(NS,-1,-1)");  //Turn North/South sideways.
                    NxtMain.lifterStatus = LIFTER_UP;
                } else {
                    programCanceled();
                    return;
                }
            }

            //Lifter is now ready for the Cube
            if (NxtMain.lifterStatus == LIFTER_UP) {
                //This assumes that the grippers will be in the Open and N/S @ 90 degrees.
                //If we click Start right after a run, we get here as the first pause.
                //TODO - dangerous.  We should be forced to manual control lifter init, and
                // reset the arms.
                if (!NxtMain.stop) {
                    //This next command shows the UI message for the Cube to be inserted.
                    //This is the positive exit from this.
                    //TODO check that dialogue works on 'start' presses after a solve.
                    appendLog("About to run the insert dialogue");
                    runOnUiThread(new UserInsertsCube());
                    appendLog("Insert dialogue complete");
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

    public class UserInsertsCube implements Runnable {
        UserInsertsCube() {
            //No constructor needed.
        }

        @Override
        public void run() {
            AlertDialog.Builder alertDialogBuilder3 = new AlertDialog.Builder(MainActivity.this.context);
            alertDialogBuilder3.setTitle(MainActivity.this.getString(R.string.dialog_cubetitle)).setMessage(MainActivity.this.getString(R.string.dialog_cubetext)).setCancelable(false).setPositiveButton(MainActivity.this.getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    new Thread(new Runnable() {
                        @Override
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

    public void startSolve() {
        //We come here after we click to say we have inserted the Cube.
        final TextView textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollViewStatus);
        if (!NxtMain.stop) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewStatus.setText(((Object) textViewStatus.getText()) + MainActivity.this.getString(R.string.status_takecube) + "\r\n");
                }
            });
            //Part of the solution to the colour scanning issue, is to
            // stop the lifter a couple of revs higher before gripping the cube.
            //The rubber grippers then take up the slack and you get a more
            // balanced scan.
            //Original settings were Lift -13, first, and then Lift -11.
            NxtMain.connNxtSM1.sendCommand("LIFT(-11)");
            NxtMain.connNxtSM1.sendCommand("CLOSE(NS,1,1)");
            NxtMain.connNxtSM1.sendCommand("LIFT(-13)");
            NxtMain.connNxtSM1.sendCommand("TURN(EW,-1,1)");
            //Reset lifter status to unknown.  This will cause problems with reruns.
            NxtMain.lifterStatus = LIFTER_DOWN;  //Used to be Unknown.  It's not.  -24 is bottom.
            if (!NxtMain.stop) {
                runOnUiThread(new Runnable() {
                    @Override
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

    public void lastStep() {
        appendLog("last step");
        final Button buttonStart = (Button) findViewById(R.id.buttonStart);
        final Button buttonStop = (Button) findViewById(R.id.buttonStop);
        final TextView textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        if (!NxtMain.stop) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewStatus.setText(((Object) textViewStatus.getText()) + MainActivity.this.getString(R.string.status_photossaved) + "\r\n");
                }
            });
            NxtColorDetection colordetection = new NxtColorDetection();
            NxtMain.cubeDefinition = colordetection.getCube(NxtMain.picpath, true);
            appendLog("colour detection finished");
            appendLog(NxtMain.cubeDefinition);
            if (!NxtMain.stop) {
                //Report cube definition to status.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewStatus.setText(((Object) textViewStatus.getText()) + "\r\n" + MainActivity.this.getString(R.string.status_colorsdetected) + "\r\n");
                        textViewStatus.setText(((Object) textViewStatus.getText()) + NxtMain.cubeDefinition + "\r\n");
                    }
                });

                //Check for solved/not solved.
                if (!NxtMain.cubeDefinition.equals(MainActivity.this.getString(R.string.solved_state))) {
                    //If not solved, call the search routines.
                    appendLog("Cube shuffled. Starting Search");
                    CsSearch search = new CsSearch();
                    appendLog("Created searcher.");
                    NxtMain.cubeSolve = search.solution(NxtMain.cubeDefinition, 21, 100L, 0L, 0);
                    appendLog("Solve produced:" + NxtMain.cubeSolve);
                    if (NxtMain.cubeSolve.substring(0, 1).equals("E")) {
                        //Then the solve has failed and returned an error message.
                        appendLog("Error in colour detection: " + NxtMain.cubeSolve);
                        if (!NxtMain.stop) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textViewStatus.setText(((Object) textViewStatus.getText()) + "\r\n" + MainActivity.this.getString(R.string.status_cubeerror) + "\r\n");
                                    textViewStatus.setText(((Object) textViewStatus.getText()) + MainActivity.this.getString(R.string.status_cubeerrortext) + "\r\n");
                                    textViewStatus.setText(((Object) textViewStatus.getText()) + get_error_message(NxtMain.cubeSolve) + "\r\n");
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

                    } else {
                        programCanceled();
                        return;
                    }
                } else {
                    //We are looking at a solved cube.  We COULD just give up...
                    //Or we can reconfigure the cube to something special...
                    appendLog("Cube is in solved state. Applying pattern");
                    solve_cube(NxtMain.pattern, textViewStatus);
                }
                //Once we have completed solve_cube we get back here.
                NxtMain.connNxtSM1.sendCommand("LIFT(10)");
                NxtMain.connNxtSM1.sendCommand("OPEN(NS,1,1)");
                NxtMain.connNxtSM1.sendCommand("OPEN(EW,1,1)");
                NxtMain.connNxtSM1.sendCommand("LIFT(14)");
                NxtMain.connNxtSM2.sendCommand("TURN(NS,-1,-1)");
                NxtMain.lifterStatus = LIFTER_UP;
                if (!NxtMain.stop) {
                    runOnUiThread(new Runnable() {
                        @Override
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
        appendLog("Error message:" + solve_result);
        appendLog("Error Char:" + solve_result.charAt(6));
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewStatus.setText(((Object) textViewStatus.getText()) + "\r\n" + MainActivity.this.getString(R.string.status_cubesolved) + "\r\n");
                textViewStatus.setText(((Object) textViewStatus.getText()) + solution + "\r\n");
            }
        });
        //Define our waits for cube manipulation
        if(!simulation){
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewStatus.setText(((Object) textViewStatus.getText()) + "\r\n" + MainActivity.this.getString(R.string.status_cubemoved) + "\r\n");
                    }
                });
            } else {
                programCanceled();
                return;
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewStatus.setText(((Object) textViewStatus.getText()) + "\r\n" + MainActivity.this.getString(R.string.status_simulation) + "\r\n");
                }
            });
        }

    }

    public void printTime() {
        final TextView textViewTime = (TextView) findViewById(R.id.textViewTime);
        final ScrollView scrollViewStatus = (ScrollView) findViewById(R.id.scrollViewStatus);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewTime.setText("0:00.00");
            }
        });
        while (!NxtMain.runtime) {
            runOnUiThread(new Runnable() {
                @Override
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
            runOnUiThread(new Runnable() {
                @Override
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
            runOnUiThread(new Runnable() {
                @Override
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
        //Cancel the stop state
        NxtMain.stop = false;
        runOnUiThread(new Runnable() {
            @Override
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

    public void appendLog(String text) {
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


}