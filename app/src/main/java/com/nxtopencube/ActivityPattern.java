package com.nxtopencube;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import android.app.Activity;

public class ActivityPattern extends Activity {
    final Context context = this;
    //private String[] colour = {"Yellow", "Orange", "Green", "White", "Red", "Blue"};
    private String[] patterns;

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern);
        patterns = new String[]{ActivityPattern.this.getString(R.string.pattern_pons_asinorum),
                ActivityPattern.this.getString(R.string.pattern_pons_superflip),
                ActivityPattern.this.getString(R.string.pattern_superflip),
                ActivityPattern.this.getString(R.string.pattern_crosses),
                ActivityPattern.this.getString(R.string.pattern_slash),
                ActivityPattern.this.getString(R.string.pattern_cube_2),
                ActivityPattern.this.getString(R.string.pattern_cube_3),
                ActivityPattern.this.getString(R.string.pattern_ts),
                ActivityPattern.this.getString(R.string.pattern_stripes),
                ActivityPattern.this.getString(R.string.pattern_exchanged_peaks),
                ActivityPattern.this.getString(R.string.pattern_anaconda),
                ActivityPattern.this.getString(R.string.pattern_black_mamba),
                ActivityPattern.this.getString(R.string.pattern_python),
                ActivityPattern.this.getString(R.string.pattern_swap_centers),
                ActivityPattern.this.getString(R.string.pattern_union_jack),
                ActivityPattern.this.getString(R.string.pattern_kilt),
                ActivityPattern.this.getString(R.string.pattern_conway)
        };
    }

    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        Spinner spinnerPatterns=findViewById(R.id.spinner_pattern);
        //ArrayAdapter<CharSequence>adapter=ArrayAdapter.createFromResource(this, R.array.patterns, android.R.layout.simple_spinner_item);
        ArrayAdapter adapter
                = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                patterns);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerPatterns.setAdapter(adapter);
        spinnerPatterns.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!NxtMain.pattern.equals(get_pattern_instructions((patterns)[position]))){
                    //Don't bother displaying the pattern, if no change.
                    //Only really applies to first display.
                    //Ah - but this will override any saved settings.
                    //TODO find a way to choose the default item on first load.
                    //I have made this 10x more complicated, because I am only saving the moves, not the name.
                    Toast.makeText(getApplicationContext(),
                                    //getResources().getStringArray(R.array.patterns)[position],
                                    get_pattern_instructions((patterns)[position]),
                                    Toast.LENGTH_LONG)
                            .show();
                    NxtMain.pattern = get_pattern_instructions((patterns)[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        Button buttonInitSave = (Button) findViewById(R.id.buttonPatternsSave);
        buttonInitSave.setOnClickListener(new View.OnClickListener() { // from class: com.example.firstapp.ActivitySettings.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {

                SharedPreferences settings = ActivityPattern.this.getSharedPreferences("Settings", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("Pattern", NxtMain.pattern);
                editor.commit();
            }
        });
    }

    private String get_pattern_instructions(String pattern_name) {
        if (pattern_name.equals(ActivityPattern.this.getString(R.string.pattern_pons_asinorum))) {return ActivityPattern.this.getString(R.string.solution_pons_asinorum);}
        if (pattern_name.equals(ActivityPattern.this.getString(R.string.pattern_pons_superflip))) {return ActivityPattern.this.getString(R.string.solution_pons_superflip);}
        if (pattern_name.equals(ActivityPattern.this.getString(R.string.pattern_superflip))) {return ActivityPattern.this.getString(R.string.solution_superflip);}
        if (pattern_name.equals(ActivityPattern.this.getString(R.string.pattern_crosses))) {return ActivityPattern.this.getString(R.string.solution_crosses);}
        if (pattern_name.equals(ActivityPattern.this.getString(R.string.pattern_slash))) {return ActivityPattern.this.getString(R.string.solution_slash);}
        if (pattern_name.equals(ActivityPattern.this.getString(R.string.pattern_cube_2))) {return ActivityPattern.this.getString(R.string.solution_cube_2);}
        if (pattern_name.equals(ActivityPattern.this.getString(R.string.pattern_cube_3))) {return ActivityPattern.this.getString(R.string.solution_cube_3);}
        if (pattern_name.equals(ActivityPattern.this.getString(R.string.pattern_ts))) {return ActivityPattern.this.getString(R.string.solution_ts);}
        if (pattern_name.equals(ActivityPattern.this.getString(R.string.pattern_stripes))) {return ActivityPattern.this.getString(R.string.solution_stripes);}
        if (pattern_name.equals(ActivityPattern.this.getString(R.string.pattern_exchanged_peaks))) {return ActivityPattern.this.getString(R.string.solution_exchanged_peaks);}
        if (pattern_name.equals(ActivityPattern.this.getString(R.string.pattern_anaconda))) {return ActivityPattern.this.getString(R.string.solution_anaconda);}
        if (pattern_name.equals(ActivityPattern.this.getString(R.string.pattern_black_mamba))) {return ActivityPattern.this.getString(R.string.solution_black_mamba);}
        if (pattern_name.equals(ActivityPattern.this.getString(R.string.pattern_python))) {return ActivityPattern.this.getString(R.string.solution_python);}
        if (pattern_name.equals(ActivityPattern.this.getString(R.string.pattern_swap_centers))) {return ActivityPattern.this.getString(R.string.solution_swap_centers);}
        if (pattern_name.equals(ActivityPattern.this.getString(R.string.pattern_union_jack))) {return ActivityPattern.this.getString(R.string.solution_union_jack);}
        if (pattern_name.equals(ActivityPattern.this.getString(R.string.pattern_kilt))) {return ActivityPattern.this.getString(R.string.solution_kilt);}
        if (pattern_name.equals(ActivityPattern.this.getString(R.string.pattern_conway))) {return ActivityPattern.this.getString(R.string.solution_conway);}
        return "not coded";
    }
}
