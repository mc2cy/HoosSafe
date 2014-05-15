package edu.virginia.buildinglocation;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import org.json.JSONException;
import org.json.JSONObject;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import android.os.CountDownTimer;
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

public class betweenUsersActivity extends Activity {
	private final LatLng LOCATION_UVA = new LatLng(38.033553,-78.507977);
	private GoogleMap map3;

	ListView nearestBuildingsList;
	Intent intent = getIntent();
	
	AutoCompleteTextView user1autoCompleteTextView;
	AutoCompleteTextView user2autoCompleteTextView;
	
	Button enterUsersButton;
	
	String webserviceURL;
	String otherserviceURL;
	
	ArrayList<Building> values;
	ArrayAdapter<Building> adapter;
	String user1;
	String user2;
	JSONObject jsonMid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("onCreate", "start up");
		setContentView(R.layout.activity_mid_point);
		
		String[] usersListed = getResources().
				   getStringArray(R.array.list_of_users);
		
		ArrayAdapter adapter = new ArrayAdapter
				   (this,android.R.layout.simple_list_item_1,usersListed);
		
		user1autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.user1autoCompleteTextView);
		user2autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.user2autoCompleteTextView);
		
		user1autoCompleteTextView.setAdapter(adapter);
		user2autoCompleteTextView.setAdapter(adapter);
		
		enterUsersButton = (Button) findViewById(R.id.enterUsersButton);
		enterUsersButton.setOnClickListener(enterUsersButtonListener);
		
		otherserviceURL = "http://midpointzucchini.azurewebsites.net/Odin/meeting?";
		webserviceURL = "http://nearestbuilding.azurewebsites.net/";
		map3 = ((MapFragment)getFragmentManager().findFragmentById(R.id.map3)).getMap();
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(LOCATION_UVA,15);
		
		map3.animateCamera(update);
		map3.setMyLocationEnabled(true);
			
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_course_list, menu);
		return true;
	}

	public void initView(String user1, String user2){
		values = new ArrayList<Building>();
		
		//Executor executor;
		//System.out.println("CALLING GETMIDPOINT TASK TO GET MIDPOINT LAT,LONG");
		new GetMidpointTask().execute(user1,user2);
	
		 
		
	}

	public static String getJSONfromURL(String url) {

		// initialize
		InputStream is = null;
		String result = "";

		// http post
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url);
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();

		} catch (Exception e) {
			Log.e("nearestBuilding", "Error in http connection " + e.toString());
		}

		// convert response to string
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			result = sb.toString();
		} catch (Exception e) {
			Log.e("nearestBuilding", "Error converting result " + e.toString());
		}

		return result;
	}
	private class GetMidpointTask extends AsyncTask<String, Integer, String> {
		@Override
		protected void onPreExecute() {
		}

		@Override
		protected String doInBackground(String... params) {
			String user1 = params[0];
			String user2 = params[1];
			String otherurl = "";
			String url= "";

			try {
				otherurl = otherserviceURL+"user1="+user1+"&user2="+user2;
				System.out.println("otherserviceURL(GEtMidpoinTask): "+otherurl);
				String stringMid = getJSONfromURL(otherurl); 
				stringMid = stringMid.substring(1,stringMid.length()-1);
				System.out.println(stringMid);
				jsonMid = new JSONObject(stringMid);
				// Adjust the URL with the appropriate parameters
				url = webserviceURL + jsonMid.getString("latitude") + "/"
						+ jsonMid.getString("longitude");
				//webserviceURL = webserviceURL + jsonMid.getString("latitude") + "/"
				//+ jsonMid.getString("longitude");
				
				System.out.println("sending url to get near buildings(GETMIDPOINT):"+webserviceURL);
				//new GetBuildingsTask().execute(webserviceURL);
				

			} catch (Exception e) {
				Log.e("midPoint", "JSONPARSE:" + e.toString());
			}

			return url;
		}
		@Override
		protected void onProgressUpdate(Integer... ints) {
		}

		@Override
		protected void onPostExecute(final String result) {
			System.out.println("ON MIDPOINT POST EXECUTE!");
			
			LatLng midLocation;
			try {
				midLocation = new LatLng(Double.parseDouble(jsonMid.getString("latitude")),
						Double.parseDouble(jsonMid.getString("longitude")));
				CameraUpdate update = CameraUpdateFactory.newLatLngZoom(midLocation,17);
				map3.animateCamera(update);
				map3.addMarker(new MarkerOptions().position(midLocation).title("Midpoint between "+user1+" and "+user2)
						.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			new CountDownTimer(5000, 1000) {

			     public void onTick(long millisUntilFinished) {
			     }

			     public void onFinish() {
			    	 System.out.println(result);
			    	 new GetBuildingsTask().execute(result);
			     }
			  }.start();
		}
	}
	// The definition of our task class
	public class GetBuildingsTask extends AsyncTask<String, Integer, String> {
		@Override
		protected void onPreExecute() {
		}

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			ArrayList<Building> lcs = new ArrayList<Building>();

			try {
				System.out.println("IN GETBUILDING BACKGROUND!");
				String webJSON = getJSONfromURL(url);
				Log.d("JSON", webJSON);
				Gson gson = new Gson();

				JsonParser parser = new JsonParser();
				JsonArray Jarray = parser.parse(webJSON).getAsJsonArray();
				System.out.println("PARSING PART!");
				for (JsonElement obj : Jarray) {
					Building cse = gson.fromJson(obj, Building.class);
					Log.d("COURSE", cse.toString());
					lcs.add(cse);
				}

			} catch (Exception e) {
				Log.e("nearestBuilding", "JSONPARSE:" + e.toString());
			}
			System.out.println("ADDING BUILDINGS TO ARRAYLIST!");
			values.clear();
			values.addAll(lcs);
			System.out.println("end of background");
			return url;
		}

		@Override
		protected void onProgressUpdate(Integer... ints) {

		}

		@Override
		protected void onPostExecute(String result) {
			// tells the adapter that the underlying data has changed and it
			// needs to update the view
			//adapter.notifyDataSetChanged();
			System.out.println("ON POST EXECUTE OF GETBUILDINGS!");
			for ( int i = 0 ; i < values.size() ; i++) {
				System.out.println(values.get(i).toString());
				LatLng newlocation = new LatLng(Double.parseDouble(values.get(i).latitude),Double.parseDouble(values.get(i).longitude));
				double distance = Math.round(Double.parseDouble(values.get(i).distance)*5280)/100d*100;
				map3.addMarker(new MarkerOptions().position(newlocation).title(values.get(i).building_name).snippet("Distance: " + distance + " ft"));
			}
			
		}
	}
	public OnClickListener enterUsersButtonListener = new OnClickListener(){

		@Override
		public void onClick(View theView) {
			
			
			if(user1autoCompleteTextView.getText().length() > 0 
					&&user2autoCompleteTextView.getText().length() > 0){
				
				user1 = user1autoCompleteTextView.getText().toString();
				user2 = user2autoCompleteTextView.getText().toString();
				
				initView(user1,user2);
				user1autoCompleteTextView.setText("");
				user2autoCompleteTextView.setText("");// Clear EditText box
				
				// Force the keyboard to close
				InputMethodManager imm = (InputMethodManager)getSystemService(
					      Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(user1autoCompleteTextView.getWindowToken(), 0);
			} else {
				
				// Create an alert dialog box
				AlertDialog.Builder builder = new AlertDialog.Builder(betweenUsersActivity.this);
				
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
