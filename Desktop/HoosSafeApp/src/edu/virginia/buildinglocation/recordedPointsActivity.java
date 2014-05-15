package edu.virginia.buildinglocation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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




public class recordedPointsActivity extends Activity {
	//String webserviceURL = "http://plato.cs.virginia.edu/~mc2cy/pointTesting/";
	private final LatLng LOCATION_UVA = new LatLng(38.033553,-78.507977);
	String webserviceURL = "http://pointsbuilding.azurewebsites.net/";
	ArrayList<Points> values = new ArrayList<Points>();
	ArrayAdapter<Points> adapter;
	ListView pointsList;
	private GoogleMap map5;
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.d("onCreate", "start up");
		setContentView(R.layout.activity_record_points);
		map5 = ((MapFragment)getFragmentManager().findFragmentById(R.id.map5)).getMap();
		CameraUpdate defaultLocation = CameraUpdateFactory.newLatLngZoom(LOCATION_UVA,16);
		map5.moveCamera(defaultLocation);
		map5.setMyLocationEnabled(true);
		initView();	
	}

	public void initView() {
		pointsList = (ListView) findViewById(R.id.pointsList);
		values = new ArrayList<Points>();

		// Adjust the URL with the appropriate parameters
		String url = webserviceURL;

		// First paramenter - Context
		// Second parameter - Layout for the row
		// Third parameter - ID of the TextView to which the data is written
		// Forth - the Array of data
		//Log.d("HTTP", url);
		adapter = new ArrayAdapter<Points>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, values);

		// Assign adapter to ListView
		pointsList.setAdapter(adapter);

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
			Log.e("pointsList", "Error in http connection " + e.toString());
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
			Log.e("pointsList", "Error converting result " + e.toString());
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
			ArrayList<Points> lcs = new ArrayList<Points>();

			try {

				String webJSON = getJSONfromURL(url);
				Log.d("JSON", webJSON);
				Gson gson = new Gson();

				JsonParser parser = new JsonParser();
				JsonArray Jarray = parser.parse(webJSON).getAsJsonArray();

				for (JsonElement obj : Jarray) {
					Points cse = gson.fromJson(obj, Points.class);
					System.out.println(cse);
					Log.d("Points", cse.toString());
					lcs.add(cse);
				}

			} catch (Exception e) {
				Log.e("pointsList", "JSONPARSE:" + e.toString());
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
			adapter.notifyDataSetChanged();
			for ( int i = 0 ; i < values.size() ; i++) {
				System.out.println(values.get(i).toString());
				LatLng newlocation = new LatLng(Double.parseDouble(values.get(i).latitude),Double.parseDouble(values.get(i).longitude));
				String time = values.get(i).time;
				if ( i <= 3 ) {
					map5.addMarker(new MarkerOptions().position(newlocation).title((i+1)+": Latitude: " +values.get(i).getLatitude() +" Longitude:"+ values.get(i).getLongitude()).snippet("Time: " + time ));
				}
				else if ( i > 3 && i < 7) {
					map5.addMarker(new MarkerOptions().position(newlocation).title((i+1)+": Latitude: " +values.get(i).getLatitude() +" Longitude:"+ values.get(i).getLongitude()).snippet("Time: " + time ).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
					
				}
				else if ( i >=7) {
					map5.addMarker(new MarkerOptions().position(newlocation).title((i+1)+": Latitude: " +values.get(i).getLatitude() +" Longitude:"+ values.get(i).getLongitude()).snippet("Time: " + time ).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
					
				}
			}
			LatLng location= new LatLng ( Double.parseDouble(values.get(0).getLatitude()), Double.parseDouble(values.get(0).getLongitude()));
			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(location,17);
			map5.animateCamera(update);
		}
	}
    
}
