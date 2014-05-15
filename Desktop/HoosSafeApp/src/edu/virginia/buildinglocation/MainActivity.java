package edu.virginia.buildinglocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

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
import android.net.Uri;
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

public class MainActivity extends Activity {
   
	private final LatLng LOCATION_UVA = new LatLng(38.033553,-78.507977);
	private final LatLng LOCATION_OLSSON = new LatLng(38.0320075,-78.5105309);
	
	private GoogleMap map;
	
	Button nearBuildingButton;
	Button trackBuildingButton;
	Button searchBuildingButton;
	
	TextView textLat;
	TextView textLong;
	
	//to add to record current point 
	//url = http://plato.cs.virginia.edu/~cs4720s14beet/points/add?latitude=___&longitude=

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("onCreate", "start up");
		setContentView(R.layout.activity_main);
		
		nearBuildingButton = (Button) findViewById(R.id.nearBuildingButton);
		
		trackBuildingButton = (Button) findViewById(R.id.trackBuildingButton);
		
		searchBuildingButton = (Button) findViewById(R.id.searchBuildingButton);
		
		textLat = (TextView)findViewById(R.id.textLat);

		textLong = (TextView)findViewById(R.id.textLong);

		LocationManager lm= (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		
		LocationListener ll = new mylocationlistener();

		//parameter (gps provider, mintime(milliseconds), mindistance (10meter), locationlistener)

		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300, 20, ll);
		
		CameraUpdate defaultLocation = CameraUpdateFactory.newLatLngZoom(LOCATION_UVA,16);
		
		map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		
		map.moveCamera(defaultLocation);
		
		map.setMyLocationEnabled(true);
	
		/*
		map.addMarker(new MarkerOptions().position(LOCATION_OLSSON).title("Olsson Hall"));*/
		
		
			
	}
	
	 private class mylocationlistener implements LocationListener {

			@Override
			public void onLocationChanged(Location location) {
				if (location !=null) 
				{
				double pLong = location.getLongitude();
				double pLat = location.getLatitude();
				System.out.println(pLong);
				System.out.println(pLat);
				textLat.setText(Double.toString(pLat));
				textLong.setText(Double.toString(pLong));
				LatLng latLng = new LatLng(pLat,pLong);
				map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
				map.animateCamera(CameraUpdateFactory.zoomTo(17));

				}
				System.out.println(location);
			}

			@Override
			public void onProviderDisabled(String provider) {
	AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				
				// Set alert title 
				builder.setTitle("GPS Disabled!");
				
				// Set the value for the positive reaction from the user
				// You can also set a listener to call when it is pressed
				builder.setPositiveButton(R.string.ok, null);
				
				// The message
				builder.setMessage("Please enable GPS for application to work!");
				
				// Create the alert dialog and display it
				AlertDialog theAlertDialog = builder.create();
				theAlertDialog.show();
			}

			@Override
			public void onProviderEnabled(String provider) {
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				
				// Set alert title 
				builder.setTitle("GPS Enabled!");
				
				// Set the value for the positive reaction from the user
				// You can also set a listener to call when it is pressed
				builder.setPositiveButton(R.string.ok, null);
				
				// The message
				builder.setMessage("GPS is Enabled!");
				
				// Create the alert dialog and display it
				AlertDialog theAlertDialog = builder.create();
				theAlertDialog.show();
				
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				// TODO Auto-generated method stub
				
			}

		 }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_building_list, menu);
		return true;
	}
		//Button function for finding the nearest building to your current location
		public void nearLocation(View arg0) {
			
			Context context = this;
			Intent intent = new Intent(context, nearBuildingActivity.class);
			//sends latitude and longitude as extras 
			intent.putExtra("EXTRA_LONGITUDE", textLong.getText().toString());
			intent.putExtra("EXTRA_LATITUDE", textLat.getText().toString());
			//intent.putExtra("EXTRA_LONGITUDE", "-78.50852014351746");
			//intent.putExtra("EXTRA_LATITUDE", "38.030113874129455");

			startActivity(intent);
	
		}
		
		//button function to get to place where you can add a building to track
		public void trackBuilding(View arg0){
			Context context = this;
			Intent intent = new Intent(context, trackBuildingActivity.class);
			intent.putExtra("EXTRA_LONGITUDE", textLong.getText().toString());
			intent.putExtra("EXTRA_LATITUDE", textLat.getText().toString());
			startActivity(intent);
			
		}
		//button function get top 5 most recently recorded points
		public void recentlyRecorded(View arg0){
			Context context = this;
			Intent intent = new Intent(context, recordedPointsActivity.class);
			startActivity(intent);
		}
		
		public void betweenUsers(View arg0){
			Context context = this;
			Intent intent = new Intent(context, betweenUsersActivity.class);
			startActivity(intent);
		}
		
		public void recordPoint(View arg0){
			String url = "http://plato.cs.virginia.edu/~cs4720s14beet/points/add?latitude="+
					textLat.getText().toString()+"&longitude=" + textLong.getText().toString();
			System.out.println(url);

			new RecordPointTask().execute(url);
			//Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(request));
			// startActivity(i);
			//Intent intent = new Intent(this, MainActivity.class);
			//startActivity(intent);
	
			
		}
		//button function to get to activity where you can search a building
		public void searchBuilding(View arg0){
			Context context = this;
			Intent intent = new Intent(context, searchBuildingActivity.class);
			startActivity(intent);
			
		}
		
		public void emergencyMessage(View arg0){
			final String user = "hoosafe@gmail.com";
			
			
			String location = "Latitude: " + textLat.getText().toString() + " Longitude: "+ textLong.getText().toString();
			 String to = "hoosafe@gmail.com";
			 String to1 = "6465321898@vtext.com";
			  String subject = "Help! Please call 911!";
			  String message = "Please call 911 and report my location , " +location  ;

			  Intent email = new Intent(Intent.ACTION_SEND);
			  email.putExtra(Intent.EXTRA_EMAIL, new String[]{ to,to1});
			
			  email.putExtra(Intent.EXTRA_SUBJECT, subject);
			  email.putExtra(Intent.EXTRA_TEXT, message);

			  //need this to prompts email client only
			  email.setType("message/rfc822");

			  startActivity(Intent.createChooser(email, "Choose an Email client :"));
			
		}
		private class RecordPointTask extends AsyncTask<String, Integer, String> {
			@Override
			protected void onPreExecute() {
			}

			@Override
			protected String doInBackground(String... params) {
				String url = params[0];
				//ArrayList<Points> lcs = new ArrayList<Points>();

				try {
					HttpClient httpclient = new DefaultHttpClient();
					HttpPost httppost = new HttpPost(url);
					HttpResponse response = httpclient.execute(httppost);
					HttpEntity entity = response.getEntity();
					//is = entity.getContent();

				} catch (Exception e) {
					Log.e("pointsList", "Error in http connection " + e.toString());
				}

				return "Done!";
			}

			@Override
			protected void onProgressUpdate(Integer... ints) {

			}

			@Override
			protected void onPostExecute(String result) {
				// Create an alert dialog box
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				
				// Set alert title 
				builder.setTitle("Recorded current location point!");
				
				// Set the value for the positive reaction from the user
				// You can also set a listener to call when it is pressed
				builder.setPositiveButton(R.string.ok, null);
				
				// The message
				builder.setMessage("Recorded point:("+textLat.getText().toString()+","+textLong.getText().toString()+")");
				
				// Create the alert dialog and display it
				AlertDialog theAlertDialog = builder.create();
				theAlertDialog.show();
			}
		}
	




   
}
