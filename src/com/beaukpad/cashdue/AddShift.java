package com.beaukpad.cashdue;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class AddShift extends Activity {
	//ID for calling showDialog
	static final int DATE_DIALOG_ID = 0;
	private DatePickerDialog.OnDateSetListener mDateSetListener =
		new OnDateSetListener() {
			
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				thisShift.getDate().set(year, monthOfYear, dayOfMonth);
				populateActivity();
			}
		};
	//The datahelper for opening the database
	DataHelperPrime dh;
	//UI elements
	Button pickDateButton;
	RadioGroup radioGroup;
	EditText salesEdit;
	Button saveButton;
	Button cancelButton;
	CheckBox duplicateCheckBox;
	TextView existsTextView;
	Button replaceButton;
	//The shift this activity is creating/editing
	Shift thisShift;
	//the obligatory context and intent
	//I don't understand why I can assign here, outside of a method. But I'm scared to change it.
	Context context = this;
	Intent intent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_shift);
		//pack the goods (datahelper, ui elements, calling intent)
		dh = MyApplication.getInstance().getDH();
		dh.open();
		pickDateButton = (Button)findViewById(R.id.DateBut);
		pickDateButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
				
			}
		});
		radioGroup = (RadioGroup)findViewById(R.id.RadioGroup01);
		existsTextView = (TextView)findViewById(R.id.ExistsText);
		salesEdit = (EditText)findViewById(R.id.EditText01);
		saveButton = (Button)findViewById(R.id.SaveButton);
		cancelButton = (Button)findViewById(R.id.CancelButton);
		duplicateCheckBox = (CheckBox)findViewById(R.id.CheckBox01);
		replaceButton = (Button)findViewById(R.id.ReplaceButton);
		intent = getIntent();
		//Is this a new shift? DBROW = rowid = 0 means yes
		long rowid = intent.getLongExtra("DBROW", 0);
		//If this is NOT a new shift, get values from calling intent
		if (rowid > 0){
			Calendar date = Calendar.getInstance();
			date.setTimeInMillis(intent.getLongExtra("DATE", 0));
			double sales = intent.getDoubleExtra("SALES", 0);
			thisShift = new Shift(sales, date, rowid);
			} else {
				thisShift = new Shift();
			}
		if(thisShift.isLunch()){
			thisShift.setIsLunch(Shift.LUNCH);
		} else{
			thisShift.setIsLunch(Shift.DINNER);
		}
			cancelButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					Intent anIntent = new Intent(context, PastShifts.class);
					startActivity(anIntent);
				}
			});
			replaceButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					if(thisShift.getDBRow() > 0){
						try{
							thisShift.setSales(Double.valueOf(salesEdit.getText().toString()));
						} catch (NumberFormatException d) {return;}
						if (thisShift.getSales() == (Double)0.0){
							return;
						}
						if(radioGroup.getCheckedRadioButtonId() == R.id.RadioDinner){
							thisShift.setIsLunch(Shift.DINNER);
						} else {
							thisShift.setIsLunch(Shift.LUNCH);
						}
						dh.updateThisShift(thisShift);
					} else {
						try{
							thisShift.setSales(Double.valueOf(salesEdit.getText().toString()));
						} catch (NumberFormatException d) {return;}
						if (thisShift.getSales() == (Double)0.0){
							Toast.makeText(AddShift.this, "Can't save a shift with $0 sales...", Toast.LENGTH_SHORT).show();
							return;
						}
						if(radioGroup.getCheckedRadioButtonId() == R.id.RadioDinner){
							thisShift.setIsLunch(Shift.DINNER);
						} else {
							thisShift.setIsLunch(Shift.LUNCH);
						}
						dh.insertShift(thisShift);
						BackupManager.dataChanged(getBaseContext().getPackageName()); 
					}
					Intent anIntent = new Intent(context, PastShifts.class);
					startActivity(anIntent);
				}
			});
			saveButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					if(thisShift.getDBRow() > 0){
						try{
							thisShift.setSales(Double.valueOf(salesEdit.getText().toString()));
						} catch (NumberFormatException d) {return;}
						if (thisShift.getSales() == (Double)0.0){
							return;
						}
						if(radioGroup.getCheckedRadioButtonId() == R.id.RadioDinner){
							thisShift.setIsLunch(Shift.DINNER);
						} else {
							thisShift.setIsLunch(Shift.LUNCH);
						}
						dh.updateThisShift(thisShift);
					} else {
						try{
							thisShift.setSales(Double.valueOf(salesEdit.getText().toString()));
						} catch (NumberFormatException d) {return;}
						if (thisShift.getSales() == (Double)0.0){
							Toast.makeText(AddShift.this, "Can't save a shift with $0 sales...", Toast.LENGTH_SHORT).show();
							return;
						}
						if(radioGroup.getCheckedRadioButtonId() == R.id.RadioDinner){
							thisShift.setIsLunch(Shift.DINNER);
						} else {
							thisShift.setIsLunch(Shift.LUNCH);
						}
						dh.insertShift(thisShift);
					}
					Intent anIntent = new Intent(context, PastShifts.class);
					startActivity(anIntent);
				}
			});
			duplicateCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					checkDuplicateCheckbox();
				}
			});
			radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
				
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					if(group.getCheckedRadioButtonId() == R.id.RadioDinner){
						thisShift.setIsLunch(Shift.DINNER);
					} else {
						thisShift.setIsLunch(Shift.LUNCH);
					}
					checkDuplicateCheckbox();
				}
			});
			populateActivity();
	} 
	@Override
	public void onStart(){
		super.onStart();
		setContentView(R.layout.add_shift);
		//pack the goods (datahelper, ui elements, calling intent)
		dh = MyApplication.getInstance().getDH();
		dh.open();
		pickDateButton = (Button)findViewById(R.id.DateBut);
		pickDateButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
				
			}
		});
		radioGroup = (RadioGroup)findViewById(R.id.RadioGroup01);
		existsTextView = (TextView)findViewById(R.id.ExistsText);
		salesEdit = (EditText)findViewById(R.id.EditText01);
		saveButton = (Button)findViewById(R.id.SaveButton);
		cancelButton = (Button)findViewById(R.id.CancelButton);
		duplicateCheckBox = (CheckBox)findViewById(R.id.CheckBox01);
		replaceButton = (Button)findViewById(R.id.ReplaceButton);
		intent = getIntent();
		//Is this a new shift? DBROW = rowid = 0 means yes
		long rowid = intent.getLongExtra("DBROW", 0);
		//If this is NOT a new shift, get values from calling intent
		if (rowid > 0){
			Calendar date = Calendar.getInstance();
			date.setTimeInMillis(intent.getLongExtra("DATE", 0));
			double sales = intent.getDoubleExtra("SALES", 0);
			thisShift = new Shift(sales, date, rowid);
			} else {
				thisShift = new Shift();
			}
			cancelButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					Intent anIntent = new Intent(context, PastShifts.class);
					startActivity(anIntent);
				}
			});
			replaceButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					if(thisShift.getDBRow() > 0){
						try{
							thisShift.setSales(Double.valueOf(salesEdit.getText().toString()));
						} catch (NumberFormatException d) {return;}
						if (thisShift.getSales() == (Double)0.0){
							return;
						}
						if(radioGroup.getCheckedRadioButtonId() == R.id.RadioDinner){
							thisShift.setIsLunch(Shift.DINNER);
						} else {
							thisShift.setIsLunch(Shift.LUNCH);
						}
						dh.updateThisShift(thisShift);
					} else {
						try{
							thisShift.setSales(Double.valueOf(salesEdit.getText().toString()));
						} catch (NumberFormatException d) {return;}
						if (thisShift.getSales() == (Double)0.0){
							Toast.makeText(AddShift.this, "Can't save a shift with $0 sales...", Toast.LENGTH_SHORT).show();
							return;
						}
						if(radioGroup.getCheckedRadioButtonId() == R.id.RadioDinner){
							thisShift.setIsLunch(Shift.DINNER);
						} else {
							thisShift.setIsLunch(Shift.LUNCH);
						}
						dh.insertShift(thisShift);
					}
					Intent anIntent = new Intent(context, PastShifts.class);
					startActivity(anIntent);
				}
			});
			saveButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					if(thisShift.getDBRow() > 0){
						try{
							thisShift.setSales(Double.valueOf(salesEdit.getText().toString()));
						} catch (NumberFormatException d) {return;}
						if (thisShift.getSales() == (Double)0.0){
							return;
						}
						if(radioGroup.getCheckedRadioButtonId() == R.id.RadioDinner){
							thisShift.setIsLunch(Shift.DINNER);
						} else {
							thisShift.setIsLunch(Shift.LUNCH);
						}
						dh.updateThisShift(thisShift);
					} else {
						try{
							thisShift.setSales(Double.valueOf(salesEdit.getText().toString()));
						} catch (NumberFormatException d) {return;}
						if (thisShift.getSales() == (Double)0.0){
							Toast.makeText(AddShift.this, "Can't save a shift with $0 sales...", Toast.LENGTH_SHORT).show();
							return;
						}
						if(radioGroup.getCheckedRadioButtonId() == R.id.RadioDinner){
							thisShift.setIsLunch(Shift.DINNER);
						} else {
							thisShift.setIsLunch(Shift.LUNCH);
						}
						dh.insertShift(thisShift);
					}
					Intent anIntent = new Intent(context, PastShifts.class);
					startActivity(anIntent);
				}
			});
			duplicateCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					checkDuplicateCheckbox();
				}
			});
			radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
				
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					if(radioGroup.getCheckedRadioButtonId() == R.id.RadioDinner){
						thisShift.setIsLunch(Shift.DINNER);
					} else {
						thisShift.setIsLunch(Shift.LUNCH);
					}
				}
			});
			populateActivity();
	}
	@Override
	public void onRestart(){
		super.onRestart();
		setContentView(R.layout.add_shift);
		//pack the goods (datahelper, ui elements, calling intent)
		dh = MyApplication.getInstance().getDH();
		dh.open();
		pickDateButton = (Button)findViewById(R.id.DateBut);
		pickDateButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
				
			}
		});
		radioGroup = (RadioGroup)findViewById(R.id.RadioGroup01);
		existsTextView = (TextView)findViewById(R.id.ExistsText);
		salesEdit = (EditText)findViewById(R.id.EditText01);
		saveButton = (Button)findViewById(R.id.SaveButton);
		cancelButton = (Button)findViewById(R.id.CancelButton);
		duplicateCheckBox = (CheckBox)findViewById(R.id.CheckBox01);
		replaceButton = (Button)findViewById(R.id.ReplaceButton);
		intent = getIntent();
		//Is this a new shift? DBROW = rowid = 0 means yes
		long rowid = intent.getLongExtra("DBROW", 0);
		//If this is NOT a new shift, get values from calling intent
		if (rowid > 0){
			Calendar date = Calendar.getInstance();
			date.setTimeInMillis(intent.getLongExtra("DATE", 0));
			double sales = intent.getDoubleExtra("SALES", 0);
			thisShift = new Shift(sales, date, rowid);
			} else {
				thisShift = new Shift();
			}
			cancelButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					Intent anIntent = new Intent(context, PastShifts.class);
					startActivity(anIntent);
				}
			});
			replaceButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					if(thisShift.getDBRow() > 0){
						try{
							thisShift.setSales(Double.valueOf(salesEdit.getText().toString()));
						} catch (NumberFormatException d) {return;}
						if (thisShift.getSales() == (Double)0.0){
							return;
						}
						if(radioGroup.getCheckedRadioButtonId() == R.id.RadioDinner){
							thisShift.setIsLunch(Shift.DINNER);
						} else {
							thisShift.setIsLunch(Shift.LUNCH);
						}
						dh.updateThisShift(thisShift);
					} else {
						try{
							thisShift.setSales(Double.valueOf(salesEdit.getText().toString()));
						} catch (NumberFormatException d) {return;}
						if (thisShift.getSales() == (Double)0.0){
							Toast.makeText(AddShift.this, "Can't save a shift with $0 sales...", Toast.LENGTH_SHORT).show();
							return;
						}
						if(radioGroup.getCheckedRadioButtonId() == R.id.RadioDinner){
							thisShift.setIsLunch(Shift.DINNER);
						} else {
							thisShift.setIsLunch(Shift.LUNCH);
						}
						dh.insertShift(thisShift);
					}
					Intent anIntent = new Intent(context, PastShifts.class);
					startActivity(anIntent);
				}
			});
			saveButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					if(thisShift.getDBRow() > 0){
						try{
							thisShift.setSales(Double.valueOf(salesEdit.getText().toString()));
						} catch (NumberFormatException d) {return;}
						if (thisShift.getSales() == (Double)0.0){
							return;
						}
						if(radioGroup.getCheckedRadioButtonId() == R.id.RadioDinner){
							thisShift.setIsLunch(Shift.DINNER);
						} else {
							thisShift.setIsLunch(Shift.LUNCH);
						}
						dh.updateThisShift(thisShift);
					} else {
						try{
							thisShift.setSales(Double.valueOf(salesEdit.getText().toString()));
						} catch (NumberFormatException d) {return;}
						if (thisShift.getSales() == (Double)0.0){
							Toast.makeText(AddShift.this, "Can't save a shift with $0 sales...", Toast.LENGTH_SHORT).show();
							return;
						}
						if(radioGroup.getCheckedRadioButtonId() == R.id.RadioDinner){
							thisShift.setIsLunch(Shift.DINNER);
						} else {
							thisShift.setIsLunch(Shift.LUNCH);
						}
						dh.insertShift(thisShift);
					}
					Intent anIntent = new Intent(context, PastShifts.class);
					startActivity(anIntent);
				}
			});
			duplicateCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					checkDuplicateCheckbox();
				}
			});
			radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
				
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					if(radioGroup.getCheckedRadioButtonId() == R.id.RadioDinner){
						thisShift.setIsLunch(Shift.DINNER);
					} else {
						thisShift.setIsLunch(Shift.LUNCH);
					}
				}
			});
			populateActivity();
	
	}
	public boolean duplicateExists(){
		for(Shift tShift : MyApplication.getInstance().getGlobalArray()){
			int tShiftDayYear = tShift.getDate().get(Calendar.DAY_OF_YEAR);
			int tShiftYear = tShift.getDate().get(Calendar.YEAR);
			int thisDayYear = thisShift.getDate().get(Calendar.DAY_OF_YEAR);
			int thisYear = thisShift.getDate().get(Calendar.YEAR);
			if((tShiftDayYear == thisDayYear) && (tShiftYear == thisYear)){
				if(tShift.isLunch() == thisShift.isLunch()){
				return true;}
			} 
		}
		return false;
	}
	public void checkDuplicateCheckbox(){
		boolean isChecked = duplicateCheckBox.isChecked();
		if(isChecked){
			saveButton.setEnabled(true);
			saveButton.setVisibility(View.VISIBLE);
			if(duplicateExists()){
				replaceButton.setEnabled(true);
				existsTextView.setVisibility(View.VISIBLE);
				existsTextView.setText(getString(R.string.shift_exists_allowed));
			} else {
				existsTextView.setVisibility(View.INVISIBLE);
				replaceButton.setEnabled(false);
			}
		} else {
			if(duplicateExists()){
				saveButton.setEnabled(false);
				saveButton.setVisibility(View.INVISIBLE);
				replaceButton.setEnabled(true);
				existsTextView.setVisibility(View.VISIBLE);
				existsTextView.setText(getString(R.string.shift_exists));
			} else {
				existsTextView.setVisibility(View.VISIBLE);
				existsTextView.setText(getString(R.string.shift_non_exists));
				saveButton.setEnabled(true);
				saveButton.setVisibility(View.VISIBLE);
				replaceButton.setEnabled(false);
				existsTextView.setVisibility(View.INVISIBLE);
			}
		}
		
	
	}
	public void populateActivity(){
		pickDateButton.setText(thisShift.getDateString());
		if(thisShift.isLunch()){
			radioGroup.check(R.id.RadioLunch);
		} else{
			radioGroup.check(R.id.RadioDinner);}
		if (thisShift.getSales() > 0){
			salesEdit.setText("" + thisShift.getSales());
		}
		checkDuplicateCheckbox();
	}
	@Override protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(context, mDateSetListener, (int)thisShift.getDate().get(Calendar.YEAR), (int)thisShift.getDate().get(Calendar.MONTH), (int)thisShift.getDate().get(Calendar.DAY_OF_MONTH));
		}
		return null;
	}
}
