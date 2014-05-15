package edu.virginia.buildinglocation;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class trackBuildingActivity extends Activity{
	EditText trackBuildingEditText;
	EditText latitudeBuildingEditText;
	EditText longitudeBuildingEditText;
	Button enterTrackBuildingButton;
	TextView currentLocation;
	String latitude;
	String longitude;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("onCreate", "start up");
		setContentView(R.layout.activity_track_building);
		
		enterTrackBuildingButton = (Button) findViewById(R.id.enterTrackBuildingButton);
		latitudeBuildingEditText = (EditText) findViewById(R.id.latitudeBuildingEditText);
		longitudeBuildingEditText = (EditText) findViewById(R.id.longitudeBuildingEditText);
		trackBuildingEditText = (EditText) findViewById(R.id.trackBuildingEditText);
	
		enterTrackBuildingButton.setOnClickListener(enterTrackBuildingButtonListener);
		Bundle extras = getIntent().getExtras();
		if (extras!=null) {
			latitude = extras.getString("EXTRA_LATITUDE");
			longitude = extras.getString("EXTRA_LONGITUDE");
		}
		else{
			latitude ="GPS OFF";
			longitude ="GPS OFF";
		}
		currentLocation = (TextView) findViewById(R.id.currentLocation);
		currentLocation.setText("Current location latitude: " + latitude +",longitude: "+longitude);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}
	
	
	public OnClickListener enterTrackBuildingButtonListener = new OnClickListener(){

		@Override
		public void onClick(View theView) {
			
			// If there is a stock symbol entered into the EditText
			// field
			if( trackBuildingEditText.getText().length() > 0 && 
					longitudeBuildingEditText.getText().length() >0 &&
					latitudeBuildingEditText.getText().length() >0){
				
				String buildingName = trackBuildingEditText.getText().toString();
				String latitude = latitudeBuildingEditText.getText().toString();
				String longitude = longitudeBuildingEditText.getText().toString();
				String url = "http://trackbuilding.azurewebsites.net/"+buildingName+"/"
						+latitude+"/"+longitude;
				new RecordBuildingTask().execute(url);
				
			}
			
		}
		
	};
	private class RecordBuildingTask extends AsyncTask<String, Integer, String> {
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
			AlertDialog.Builder builder = new AlertDialog.Builder(trackBuildingActivity.this);
			
			// Set alert title 
			builder.setTitle("Added Building Location!");
			
			// Set the value for the positive reaction from the user
			// You can also set a listener to call when it is pressed
			builder.setPositiveButton(R.string.ok, null);
			
			// The message
			builder.setMessage("Added building:"+trackBuildingEditText.getText().toString()
					+" with location ("+latitudeBuildingEditText.getText().toString()+","
					+longitudeBuildingEditText.getText().toString()+")");
			
			// Create the alert dialog and display it
			AlertDialog theAlertDialog = builder.create();
			theAlertDialog.show();
		}
	}

}
