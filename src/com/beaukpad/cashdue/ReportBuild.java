package com.beaukpad.cashdue;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by lebeau on 4/13/15.
 */
public class ReportBuild extends Activity implements View.OnClickListener {
    Button bReportBuild;
    RadioButton rbDinLun;
    RadioButton rbLunchOnly;
    RadioButton rbDinnerOnly;
    CheckBox cbSunday;
    CheckBox cbMonday;
    CheckBox cbTuesday;
    CheckBox cbWednesday;
    CheckBox cbThursday;
    CheckBox cbFriday;
    CheckBox cbSaturday;
    Calendar myCalFirst;
    Calendar myCalLast;
    String DFormat;
    SimpleDateFormat sdf;
    EditText etStart;
    EditText etEnd;
    Shift[] allShifts;
    CheckBox[] weekDaysArray;
    private DataHelperPrime dh;
    Shift earlyShift;
    Shift lateShift;

    public void onClick(View v) {
        switch(v.getId()){
            case R.id.etStartDate:
                new DatePickerDialog(this,starter,myCalFirst.get(Calendar.YEAR),myCalFirst.get(Calendar.MONTH),myCalFirst.get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.etEndDate:
                new DatePickerDialog(this,ender,myCalLast.get(Calendar.YEAR),myCalLast.get(Calendar.MONTH),myCalLast.get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.bReports:
                break;
            default:
                break;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reportbuild);
        bReportBuild = (Button)findViewById(R.id.bReports);
      //  bReportBuild.setOnClickListener(this);
        rbDinLun = (RadioButton)findViewById(R.id.Radiolunchdinner);
        rbLunchOnly = (RadioButton)findViewById(R.id.RadioOnlyLunch);
        rbDinnerOnly = (RadioButton)findViewById(R.id.RadioOnlyDinner);
        cbSunday = (CheckBox)findViewById(R.id.Sunday);
        cbMonday = (CheckBox)findViewById(R.id.Monday);
        cbTuesday = (CheckBox)findViewById(R.id.Tuesday);
        cbWednesday = (CheckBox)findViewById(R.id.Wednesday);
        cbThursday = (CheckBox)findViewById(R.id.Thursday);
        cbFriday = (CheckBox)findViewById(R.id.Friday);
        cbSaturday = (CheckBox)findViewById(R.id.Saturday);
        etStart = (EditText)findViewById(R.id.etStartDate);
        etEnd = (EditText)findViewById(R.id.etEndDate);
        etStart.setOnClickListener(this);
        etEnd.setOnClickListener(this);
        DFormat = "MM/dd/yyyy";
        sdf = new SimpleDateFormat(DFormat, Locale.US);

        Initialize();

    }

    public void Initialize() {
        dh = MyApplication.getInstance().getDH();
        allShifts = dh.getAllShifts(MyApplication.getInstance());
        rbDinnerOnly.setChecked(true);
        weekDaysArray = new CheckBox[]{cbSunday,cbMonday,cbTuesday,cbWednesday,cbThursday,cbFriday,cbSaturday};
        for(CheckBox day : weekDaysArray){
            day.setChecked(true);
        }
        myCalFirst = allShifts[(allShifts.length)-1].getDate();
        myCalLast = allShifts[0].getDate();
        etEnd.setText(sdf.format(myCalLast.getTime()));
        etStart.setText(sdf.format(myCalFirst.getTime()));

    }
    DatePickerDialog.OnDateSetListener starter = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            myCalFirst.set(Calendar.YEAR,year);
            myCalFirst.set(Calendar.MONTH,monthOfYear);
            myCalFirst.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            etStart.setText(sdf.format(myCalFirst.getTime()));
        }
    };
    DatePickerDialog.OnDateSetListener ender = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            myCalLast.set(Calendar.YEAR,year);
            myCalLast.set(Calendar.MONTH,monthOfYear);
            myCalLast.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            etEnd.setText(sdf.format(myCalLast.getTime()));
        }
    };



}

