package com.beaukpad.cashdue;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import java.util.Locale;

public class PastShifts extends ListActivity implements
		OnCheckedChangeListener {
	private DataHelperPrime dh;
    ListView theListView;
	Shift[] theShiftArray;
	RadioButton rbBoth;
	RadioButton rbDinnerOnly;
	RadioButton rbLunchOnly;
	LinearLayout theLayout;
	TextView theAverage;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.database);
        ActionBar aBar = getActionBar();
        aBar.setDisplayHomeAsUpEnabled(true);
		// get the database helper and use it to open the database
		dh = MyApplication.getInstance().getDH();
		dh.open();
		theLayout = (LinearLayout) findViewById(R.id.llDataLayout);
		final RadioGroup rgGroup = (RadioGroup) findViewById(R.id.DataRadioGroup);
		rbBoth = (RadioButton) findViewById(R.id.RadioBoth);
		rbDinnerOnly = (RadioButton) findViewById(R.id.RadioDinnerOnly);
		rbLunchOnly = (RadioButton) findViewById(R.id.RadioLunchOnly);
		theAverage = (TextView) findViewById(R.id.tvAverage);
		rgGroup.setOnCheckedChangeListener(this);
		// Attach the listview. Because this is a list activity, the list MUST
		// be called android.R.id.list (@android:id/list in XML)
		theListView = (ListView) findViewById(android.R.id.list);
		// Set up contextMenu (for long clicks on items in list.) I must
		// override onCreateContextMenu(ContextMenu, View, ContextMenuInfo)...
		// and onContextItemSelected(MenuItem)
		registerForContextMenu(getListView());
		// Jam the array into the list
		populateAllShifts();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		// Inflate the menu. Mostly boilerplate
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.databasecontext, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// the MenuItem passed in has an ItemId that shows which menu item was
		// chosen. You must use an AdapterContextMenuInfo object
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.delete_item:
			// AdapterContextMenuInfo has ID of which list item triggered the
			// context menu. This ID is the position from the original array...
			// This value is long, so must be cast to int to index an array.
			dh.removeShift(theShiftArray[(int) info.id]);
			// jam the array into the list
			populateAllShifts();
			return true;
		case R.id.edit_item:
			// MenuItem can also hold intents
			// This intent will call the AddShift activity
			Intent tempIntent;
            tempIntent = new Intent(this, AddShift.class);
            // get the shift we're working on
			Shift tempShift = theShiftArray[(int) info.id];
			// Pack it's info into to intent so we call run the constructor in
			// the AddShift activity
			tempIntent.putExtra("DBROW", tempShift.getDBRow());
			tempIntent.putExtra("SALES", tempShift.getSales());
			tempIntent.putExtra("DATE", tempShift.getDate().getTimeInMillis());
			// pack intent into the MenuItem and call it.
			item.setIntent(tempIntent);
			startActivity(item.getIntent());
			return true;

		default:
			return super.onContextItemSelected(item);
		}
	}

	private void populateAllShifts() {
		// Get all shifts in the database from the database helper member
		theShiftArray = null;
		Shift[] temp = MyApplication.getInstance().getGlobalArray();
		theShiftArray = new Shift[temp.length];
		theShiftArray = trimArray(temp);
		// pack the shift array into the list by packing each array item the
		// textview
		setListAdapter(new CustomListAdapter(PastShifts.this,
				R.layout.shiftlistitem, theShiftArray));
		setAverage();
		theLayout.invalidate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Close Database
		dh.close();
	}

	private double getListAverage() {
		Shift[] TempArray;
		if (rbBoth.isChecked()) {
			TempArray = MyApplication.getInstance().getGlobalArray();
		} else if (rbDinnerOnly.isChecked()) {
			TempArray = dh.getAllDinnerShifts();
		} else {
			TempArray = dh.getAllLunchShifts();
		}
		int TempCount = TempArray.length;
		if(TempCount == 0){
			return 0.0;
		}
		double TempTotal = 0.0;
		for (int x = 0; x < TempCount; x++) {
			if (TempArray[x] != null) {
				TempTotal += TempArray[x].getSales();
			}else {
				TempCount = x;
				break;
			}
		}
		return TempTotal / ((double) TempCount);
	}

	private void setAverage() {
		String tempString = String.format(Locale.US, "Average: %.2f", getListAverage());
		theAverage.setText(tempString);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.data_view_menu, menu);
		menu.findItem(R.id.add_new_shift).setIntent(
				new Intent(this, AddShift.class));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		startActivity(item.getIntent());
		return true;
	}

    private class CustomListAdapter extends ArrayAdapter<Shift> {

		private Context mContext;
		private int id;
		private Shift[] items;

		public CustomListAdapter(Context context, int textViewResourceId,
				Shift[] list) {
			super(context, textViewResourceId, list);
			mContext = context;
			id = textViewResourceId;
			items = trimArray(list);
		}



		@Override
		public View getView(int position, View v, ViewGroup parent) {
			View mView = v;
			if (mView == null) {
				LayoutInflater vi = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				mView = vi.inflate(id, null);
			}
			TextView text = (TextView) mView.findViewById(R.id.row);

			if (items[position] != null) {
				Typeface pinWheel = Typeface.createFromAsset(getAssets(), MyApplication.FONT_PATH_PINWHEEL);
				text.setTypeface(pinWheel);
				text.setTextColor(Color.BLACK);
				text.setText(items[position].toString());
				text.setBackgroundColor(items[position].getAppropriateColor());
			}
			return mView;
		}
	}
	private Shift[] trimArray(Shift[] list) {
		int x = 0;
		int size = list.length;
		while (x<size) {
			if(list[x]==null){
				break;
			}
			x++;
		}
        Shift[] Result = new Shift[x];
		int y = 0;
		while(y < x){
			Result[y] = list[y];
			y++;
		}
		return Result;
	}


	private void populateDinnerShifts() {
		theShiftArray = null;
		Shift[] temp = dh.getAllDinnerShifts();
		theShiftArray = new Shift[temp.length];
		theShiftArray = trimArray(temp);
		setListAdapter(new CustomListAdapter(PastShifts.this,
				R.layout.shiftlistitem, theShiftArray));
		setAverage();
		theLayout.invalidate();
	}

	private void populateLunchShifts() {
		theShiftArray = null;
		Shift[] temp = dh.getAllLunchShifts();
		theShiftArray = new Shift[temp.length];
		theShiftArray = trimArray(temp);
		setListAdapter(new CustomListAdapter(PastShifts.this,
				R.layout.shiftlistitem, theShiftArray));
		setAverage();
		theLayout.invalidate();
	}

	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (group.getCheckedRadioButtonId() == R.id.RadioBoth) {
			populateAllShifts();
			return;
		}
		if (group.getCheckedRadioButtonId() == R.id.RadioLunchOnly) {
			populateLunchShifts();
			return;
		}
		if (group.getCheckedRadioButtonId() == R.id.RadioDinnerOnly) {
			populateDinnerShifts();
        }
	}
}
