package com.nxtopencube;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

/* loaded from: classes.dex */
public class ActivityManual extends Activity {
    ProgressDialog dialog;
    private View.OnClickListener onClickListenerToggle = new View.OnClickListener() { // from class: com.example.firstapp.ActivityManu.1
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            ToggleButton button = (ToggleButton) ActivityManual.this.findViewById(view.getId());
            ToggleButton tbNS = (ToggleButton) ActivityManual.this.findViewById(R.id.toggleButtonNS);
            ToggleButton tbEW = (ToggleButton) ActivityManual.this.findViewById(R.id.toggleButtonEW);
            ToggleButton tbN = (ToggleButton) ActivityManual.this.findViewById(R.id.toggleButtonN);
            ToggleButton tbS = (ToggleButton) ActivityManual.this.findViewById(R.id.toggleButtonS);
            ToggleButton tbE = (ToggleButton) ActivityManual.this.findViewById(R.id.toggleButtonE);
            ToggleButton tbW = (ToggleButton) ActivityManual.this.findViewById(R.id.toggleButtonW);
            if (view.getId() == R.id.toggleButtonNS && button.isChecked()) {
                tbN.setChecked(false);
                tbS.setChecked(false);
            }
            if (view.getId() == R.id.toggleButtonEW && button.isChecked()) {
                tbE.setChecked(false);
                tbW.setChecked(false);
            }
            if (view.getId() == R.id.toggleButtonN && button.isChecked()) {
                tbNS.setChecked(false);
            }
            if (view.getId() == R.id.toggleButtonS && button.isChecked()) {
                tbNS.setChecked(false);
            }
            if (view.getId() == R.id.toggleButtonE && button.isChecked()) {
                tbEW.setChecked(false);
            }
            if (view.getId() != R.id.toggleButtonW || !button.isChecked()) {
                return;
            }
            tbEW.setChecked(false);
        }
    };
    private View.OnClickListener onClickListenerGripper = new View.OnClickListener() { // from class: com.example.firstapp.ActivityManu.2
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            Button button = (Button) ActivityManual.this.findViewById(view.getId());
            ToggleButton tbNS = (ToggleButton) ActivityManual.this.findViewById(R.id.toggleButtonNS);
            ToggleButton tbEW = (ToggleButton) ActivityManual.this.findViewById(R.id.toggleButtonEW);
            ToggleButton tbN = (ToggleButton) ActivityManual.this.findViewById(R.id.toggleButtonN);
            ToggleButton tbS = (ToggleButton) ActivityManual.this.findViewById(R.id.toggleButtonS);
            ToggleButton tbE = (ToggleButton) ActivityManual.this.findViewById(R.id.toggleButtonE);
            ToggleButton tbW = (ToggleButton) ActivityManual.this.findViewById(R.id.toggleButtonW);
            if (button.getTag().equals("OPEN") || button.getTag().equals("CLOSE")) {
                if (tbNS.isChecked()) {
                    NxtMain.connNxtSM1.sendCommand(String.valueOf(button.getTag().toString()) + "(NS,1,1)");
                    NxtMain.connNxtSM2.sendCommand(String.valueOf(button.getTag().toString()) + "(NS,1,1)");
                }
                if (tbEW.isChecked()) {
                    NxtMain.connNxtSM1.sendCommand(String.valueOf(button.getTag().toString()) + "(EW,1,1)");
                    NxtMain.connNxtSM2.sendCommand(String.valueOf(button.getTag().toString()) + "(EW,1,1)");
                }
                if (tbN.isChecked() || tbS.isChecked()) {
                    NxtMain.connNxtSM1.sendCommand(String.valueOf(button.getTag().toString()) + "(NS," + (tbN.isChecked() ? "1" : "0") + "," + (tbS.isChecked() ? "1" : "0") + ")");
                    NxtMain.connNxtSM2.sendCommand(String.valueOf(button.getTag().toString()) + "(NS," + (tbN.isChecked() ? "1" : "0") + "," + (tbS.isChecked() ? "1" : "0") + ")");
                }
                if (tbE.isChecked() || tbW.isChecked()) {
                    NxtMain.connNxtSM1.sendCommand(String.valueOf(button.getTag().toString()) + "(EW," + (tbE.isChecked() ? "1" : "0") + "," + (tbW.isChecked() ? "1" : "0") + ")");
                    NxtMain.connNxtSM2.sendCommand(String.valueOf(button.getTag().toString()) + "(EW," + (tbE.isChecked() ? "1" : "0") + "," + (tbW.isChecked() ? "1" : "0") + ")");
                    return;
                }
                return;
            }
            String text = button.getTag().toString();
            String invtext = text.length() == 2 ? text.substring(1) : "-" + text;
            if (tbNS.isChecked()) {
                NxtMain.connNxtSM2.sendCommand("TURN(NS," + text + "," + invtext + ")");
            }
            if (tbEW.isChecked()) {
                NxtMain.connNxtSM1.sendCommand("TURN(EW," + text + "," + invtext + ")");
            }
            if (tbN.isChecked() || tbS.isChecked()) {
                NxtMain.connNxtSM2.sendCommand("TURN(NS," + (tbN.isChecked() ? text : "0") + "," + (tbS.isChecked() ? text : "0") + ")");
            }
            if (tbE.isChecked() || tbW.isChecked()) {
                NxtConnectBluetooth nxtConnectBluetooth = NxtMain.connNxtSM1;
                StringBuilder append = new StringBuilder("TURN(EW,").append(tbE.isChecked() ? text : "0").append(",");
                if (!tbW.isChecked()) {
                    text = "0";
                }
                nxtConnectBluetooth.sendCommand(append.append(text).append(")").toString());
            }
        }
    };
    private View.OnClickListener onClickListenerLifter = new View.OnClickListener() { // from class: com.example.firstapp.ActivityManu.3
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            Button button = (Button) ActivityManual.this.findViewById(view.getId());
            String text = button.getTag().toString();
            NxtMain.connNxtSM1.sendCommand(text);
        }
    };

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);
    }

    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        findViewById(R.id.toggleButtonNS).setOnClickListener(this.onClickListenerToggle);
        findViewById(R.id.toggleButtonEW).setOnClickListener(this.onClickListenerToggle);
        findViewById(R.id.toggleButtonN).setOnClickListener(this.onClickListenerToggle);
        findViewById(R.id.toggleButtonS).setOnClickListener(this.onClickListenerToggle);
        findViewById(R.id.toggleButtonE).setOnClickListener(this.onClickListenerToggle);
        findViewById(R.id.toggleButtonW).setOnClickListener(this.onClickListenerToggle);
        findViewById(R.id.buttonOpen).setOnClickListener(this.onClickListenerGripper);
        findViewById(R.id.buttonClose).setOnClickListener(this.onClickListenerGripper);
        findViewById(R.id.buttonMinusEins).setOnClickListener(this.onClickListenerGripper);
        findViewById(R.id.buttonEins).setOnClickListener(this.onClickListenerGripper);
        findViewById(R.id.buttonMinusZwei).setOnClickListener(this.onClickListenerGripper);
        findViewById(R.id.buttonZwei).setOnClickListener(this.onClickListenerGripper);
        findViewById(R.id.buttonLifterInit).setOnClickListener(this.onClickListenerLifter);
        findViewById(R.id.buttonLifterMinusEins).setOnClickListener(this.onClickListenerLifter);
        findViewById(R.id.buttonLifterPlusEins).setOnClickListener(this.onClickListenerLifter);
        findViewById(R.id.buttonLifterPlusZehn).setOnClickListener(this.onClickListenerLifter);
        findViewById(R.id.buttonLifterPlusVierzehn).setOnClickListener(this.onClickListenerLifter);
        findViewById(R.id.buttonLifterMinusZwoelf).setOnClickListener(this.onClickListenerLifter);
    }
}
