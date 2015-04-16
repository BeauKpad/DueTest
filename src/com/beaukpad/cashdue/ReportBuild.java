package com.beaukpad.cashdue;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.RadioButton;

/**
 * Created by lebeau on 4/13/15.
 */
public class ReportBuild extends Activity implements View.OnClickListener {
    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reportbuild);
        Button bReportBuild = (Button)findViewById(R.id.bReports);
        RadioButton rbDinLun = (RadioButton)findViewById(R.id.Radiolunchdinner);
        RadioButton rbLunchOnly = (RadioButton)findViewById(R.id.RadioOnlyLunch);
        RadioButton rbDinnerOnly = (RadioButton)findViewById(R.id.RadioOnlyDinner);
        CheckBox cbSunday = (CheckBox)findViewById(R.id.Sunday);
        CheckBox cbMonday = (CheckBox)findViewById(R.id.Monday);
        CheckBox cbTuesday = (CheckBox)findViewById(R.id.Tuesday);
        CheckBox cbWednesday = (CheckBox)findViewById(R.id.Wednesday);
        CheckBox cbThursday = (CheckBox)findViewById(R.id.Thursday);
        CheckBox cbFriday = (CheckBox)findViewById(R.id.Friday);
        CheckBox cbSaturday = (CheckBox)findViewById(R.id.Saturday);
        DatePicker dpStart = (DatePicker)findViewById(R.id.dpStartDate);
        DatePicker dpEnd = (DatePicker)findViewById(R.id.dpEndDate);

    }
}
