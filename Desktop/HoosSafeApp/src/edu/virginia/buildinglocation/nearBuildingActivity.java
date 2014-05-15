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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class nearBuildingActivity extends Activity {
	
	private GoogleMap map2;

	ListView nearestBuildingsList;
	Intent intent = getIntent();
	
	String name;
	

	String webserviceURL;
	ArrayList<Building> values;
	ArrayAdapter<Building> adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("onCreate", "start up");
		setContentView(R.layout.activity_nearest_buildings);
		Bundle extras = getIntent().getExtras();
		if (extras!=null) {
		String currentLatitude = extras.getString("EXTRA_LATITUDE");
		String currentLongitude = extras.getString("EXTRA_LONGITUDE");
		webserviceURL = "http://nearestbuilding.azurewebsites.net/"+currentLatitude+"/"+currentLongitude;
		map2 = ((MapFragment)getFragmentManager().findFragmentById(R.id.map2)).getMap();
		map2.setMyLocationEnabled(true);
		
		LatLng currentLocation = new LatLng(Double.parseDouble(currentLatitude),Double.parseDouble(currentLongitude));
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLocation,17);
		map2.animateCamera(update);

		initView();
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_course_list, menu);
		return true;
	}

	public void initView() {
		nearestBuildingsList = (ListView) findViewById(R.id.nearestBuildings);
		values = new ArrayList<Building>();

		// Adjust the URL with the appropriate parameters
		String url = webserviceURL;

		// First paramenter - Context
		// Second parameter - Layout for the row
		// Third parameter - ID of the TextView to which the data is written
		// Forth - the Array of data
		//Log.d("HTTP", url);
		adapter = new ArrayAdapter<Building>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, values);

		// Assign adapter to ListView
		nearestBuildingsList.setAdapter(adapter);

		new GetCoursesTask().execute(url);
		
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

	// The definition of our task class
	private class GetCoursesTask extends AsyncTask<String, Integer, String> {
		@Override
		protected void onPreExecute() {
		}

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			ArrayList<Building> lcs = new ArrayList<Building>();

			try {

				String webJSON = getJSONfromURL(url);
				Log.d("JSON", webJSON);
				Gson gson = new Gson();

				JsonParser parser = new JsonParser();
				JsonArray Jarray = parser.parse(webJSON).getAsJsonArray();

				for (JsonElement obj : Jarray) {
					Building cse = gson.fromJson(obj, Building.class);
					Log.d("COURSE", cse.toString());
					lcs.add(cse);
				}

			} catch (Exception e) {
				Log.e("nearestBuilding", "JSONPARSE:" + e.toString());
			}

			values.clear();
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
			
			for ( int i = 0 ; i < values.size() ; i++) {
				System.out.println(values.get(i).toString());
				LatLng newlocation = new LatLng(Double.parseDouble(values.get(i).latitude),Double.parseDouble(values.get(i).longitude));
				double distance = Math.round(Double.parseDouble(values.get(i).distance)*5280)/100d*100;
				map2.addMarker(new MarkerOptions().position(newlocation).title(values.get(i).building_name).snippet("Distance: " + distance + " ft"));
			}
			
			
			
			
		}
	}
    
}
