package com.nxtopencube;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/* loaded from: classes.dex */
public class ActivitySettings extends Activity {
    final Context context = this;
    ProgressDialog dialog;

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        EditText editTextNxt1 = (EditText) findViewById(R.id.editTextNxt1);
        editTextNxt1.setText(NxtMain.nxtName1);
        EditText editTextNxt2 = (EditText) findViewById(R.id.editTextNxt2);
        editTextNxt2.setText(NxtMain.nxtName2);
        EditText editTextProgname = (EditText) findViewById(R.id.editTextProgname);
        editTextProgname.setText(NxtMain.progName);
        EditText editTextWaitOpen = (EditText) findViewById(R.id.editTextWaitOpen);
        editTextWaitOpen.setText(new StringBuilder(String.valueOf(NxtMain.waitOpen)).toString());
        EditText editTextWaitClose = (EditText) findViewById(R.id.editTextWaitClose);
        editTextWaitClose.setText(new StringBuilder(String.valueOf(NxtMain.waitClose)).toString());
        EditText editTextWaitTurn = (EditText) findViewById(R.id.editTextWaitTurn);
        editTextWaitTurn.setText(new StringBuilder(String.valueOf(NxtMain.waitTurn)).toString());
        EditText editTextWaitDoubleTurn = (EditText) findViewById(R.id.editTextWaitDoubleTurn);
        editTextWaitDoubleTurn.setText(new StringBuilder(String.valueOf(NxtMain.waitDoubleTurn)).toString());
        Button buttonInitSave = (Button) findViewById(R.id.buttonSettingsSave);
        buttonInitSave.setOnClickListener(new View.OnClickListener() { // from class: com.example.firstapp.ActivitySettings.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                EditText editTextNxt12 = (EditText) ActivitySettings.this.findViewById(R.id.editTextNxt1);
                NxtMain.nxtName1 = new StringBuilder().append((Object) editTextNxt12.getText()).toString();
                EditText editTextNxt22 = (EditText) ActivitySettings.this.findViewById(R.id.editTextNxt2);
                NxtMain.nxtName2 = new StringBuilder().append((Object) editTextNxt22.getText()).toString();
                EditText editTextProgname2 = (EditText) ActivitySettings.this.findViewById(R.id.editTextProgname);
                NxtMain.progName = new StringBuilder().append((Object) editTextProgname2.getText()).toString();
                EditText editTextWaitOpen2 = (EditText) ActivitySettings.this.findViewById(R.id.editTextWaitOpen);
                NxtMain.waitOpen = Integer.parseInt(new StringBuilder().append((Object) editTextWaitOpen2.getText()).toString());
                EditText editTextWaitClose2 = (EditText) ActivitySettings.this.findViewById(R.id.editTextWaitClose);
                NxtMain.waitClose = Integer.parseInt(new StringBuilder().append((Object) editTextWaitClose2.getText()).toString());
                EditText editTextWaitTurn2 = (EditText) ActivitySettings.this.findViewById(R.id.editTextWaitTurn);
                NxtMain.waitTurn = Integer.parseInt(new StringBuilder().append((Object) editTextWaitTurn2.getText()).toString());
                EditText editTextWaitDoubleTurn2 = (EditText) ActivitySettings.this.findViewById(R.id.editTextWaitDoubleTurn);
                NxtMain.waitDoubleTurn = Integer.parseInt(new StringBuilder().append((Object) editTextWaitDoubleTurn2.getText()).toString());
                SharedPreferences settings = ActivitySettings.this.getSharedPreferences("Settings", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("NxtName1", NxtMain.nxtName1);
                editor.putString("NxtName2", NxtMain.nxtName2);
                editor.putString("ProgName", NxtMain.progName);
                editor.putString("WaitOpen", new StringBuilder(String.valueOf(NxtMain.waitOpen)).toString());
                editor.putString("WaitClose", new StringBuilder(String.valueOf(NxtMain.waitClose)).toString());
                editor.putString("WaitTurn", new StringBuilder(String.valueOf(NxtMain.waitTurn)).toString());
                editor.putString("WaitDoubleTurn", new StringBuilder(String.valueOf(NxtMain.waitDoubleTurn)).toString());
                editor.commit();
            }
        });
    }
}
