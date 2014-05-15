package edu.virginia.buildinglocation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import edu.virginia.buildinglocation.R;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class searchBuildingActivity extends Activity {
    private GoogleMap map1;
   
	ListView buildingList;
	Button enterBuildingButton;
	
	AutoCompleteTextView buildingEditText;
		
	String webserviceURL = "http://plato.cs.virginia.edu/~cs4720s14beet/tracks/view/";
	ArrayList<Building> values = new ArrayList<Building>();
	ArrayAdapter<Building> adapter;
	String name;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("onCreate", "start up");
		
		 String[] buildingsListed = getResources().
				   getStringArray(R.array.list_of_buildings);
		setContentView(R.layout.activity_building_list);
		
		ArrayAdapter adapter = new ArrayAdapter
				   (this,android.R.layout.simple_list_item_1,buildingsListed);
		
		buildingEditText = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
		
		buildingEditText.setAdapter(adapter);
		enterBuildingButton = (Button) findViewById(R.id.enterBuildingButton);
		//buildingEditText = (EditText) findViewById(R.id.buildingEditText);
		
		enterBuildingButton.setOnClickListener(enterBuildingButtonListener);
		
		
		map1 = ((MapFragment)getFragmentManager().findFragmentById(R.id.map1)).getMap();
		map1.setMyLocationEnabled(true);
		LatLng LOCATION_UVA = new LatLng(38.033553,-78.507977);
		CameraUpdate defaultLocation = CameraUpdateFactory.newLatLngZoom(LOCATION_UVA,16);
		map1.animateCamera(defaultLocation);
		
			
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_building_list, menu);
		return true;
	}

	public void initView(String name) {
		//buildingList = (ListView) findViewById(R.id.buildingList);
		

		// Adjust the URL with the appropriate parameters
		String url = webserviceURL + name;

		// First paramenter - Context
		// Second parameter - Layout for the row
		// Third parameter - ID of the TextView to which the data is written
		// Forth - the Array of data
		//Log.d("HTTP", url);
		//adapter = new ArrayAdapter<Building>(this,
		//		android.R.layout.simple_list_item_1, android.R.id.text1, values);

		// Assign adapter to ListView
		//buildingList.setAdapter(adapter);

		new GetBuildingsTask().execute(url);

	}
	public String getJSON(String url){
		String webJSON="";
		try{
			Document document = Jsoup.connect(url).get();
			Elements element = document.select("div#content");
			webJSON = element.text().toString();
			webJSON = webJSON.substring(9,webJSON.length()-1);
		} catch (Exception e){
			Log.e("LousList", "Building name does not exist! "+ e.toString());
			return "";
		}
		if(webJSON.length() > 0){
			return webJSON;
		}else{
			return "";
		}
	}
	// The definition of our task class
	private class GetBuildingsTask extends AsyncTask<String, Integer, String> {
		@Override
		protected void onPreExecute() {
		}

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			ArrayList<Building> lcs = new ArrayList<Building>();
			//ArrayList<String> jsons = new ArrayList<String>();
			try {
				String webJSON = getJSON(url);
				//int first = webJSON.indexOf('{');
				//int second = webJSON.indexOf('}');
				//System.out.println("index of {:" + first + ". index of }:" + second);
				System.out.println(webJSON);
				Log.d("JSON", webJSON);
				if("".equals(webJSON)){
					return "Not Valid Building Name!";
				}else {
					Gson gson = new GsonBuilder().create();
					Building b = gson.fromJson(webJSON, Building.class);
					lcs.add(b);
				}
			} catch (Exception e) {
				Log.e("LousList", "JSONPARSE:" + e.toString());
			}

			
			values.addAll(lcs);

			return "Done!";
		}

		@Override
		protected void onProgressUpdate(Integer... ints) {

		}

		@Override
		protected void onPostExecute(String result) {
			// tells the adapter that the underlying data has changed and it
			// needs to update the view
			//adapter.notifyDataSetChanged();
			
			System.out.println("From post execute!");
			for ( int i = 0 ; i < values.size() ; i++) {
				System.out.println(values.get(i).toString());
				LatLng newlocation = new LatLng(Double.parseDouble(values.get(i).latitude),Double.parseDouble(values.get(i).longitude));
				map1.addMarker(new MarkerOptions().position(newlocation).title(values.get(i).building_name));
			}
			int last = values.size()-1;
			LatLng lastLocation = new LatLng(Double.parseDouble(values.get(last).latitude),Double.parseDouble(values.get(last).longitude));
			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(lastLocation,18);
			map1.animateCamera(update);
			
			
			
			
		}
	}

	public OnClickListener enterBuildingButtonListener = new OnClickListener(){

		@Override
		public void onClick(View theView) {
			
			// If there is a stock symbol entered into the EditText
			// field
			
			
			if(buildingEditText.getText().length() > 0){
				
				name = buildingEditText.getText().toString();
				
				initView(name);
				buildingEditText.setText(""); // Clear EditText box
				
				// Force the keyboard to close
				InputMethodManager imm = (InputMethodManager)getSystemService(
					      Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(buildingEditText.getWindowToken(), 0);
			} else {
				
				// Create an alert dialog box
				AlertDialog.Builder builder = new AlertDialog.Builder(searchBuildingActivity.this);
				
				// Set alert title 
				builder.setTitle(R.string.invalid_building_name);
				
				// Set the value for the positive reaction from the user
				// You can also set a listener to call when it is pressed
				builder.setPositiveButton(R.string.ok, null);
				
				// The message
				builder.setMessage(R.string.missing_building_name);
				
				// Create the alert dialog and display it
				AlertDialog theAlertDialog = builder.create();
				theAlertDialog.show();
				
			}
			
		}
		
	};


	




	
	
    
}
