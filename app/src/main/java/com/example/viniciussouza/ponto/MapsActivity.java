package com.example.viniciussouza.ponto;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import android.util.Log;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MapsActivity extends FragmentActivity   implements OnMapReadyCallback {

    private GoogleMap mMap;
    Double latitude = 0.0;
    Double longitude = 0.0;
    FirebaseDatabase database;
    DatabaseReference myRef;
    HashMap<String,String> rating;
    HashMap<String,Places> places;

    String vicinity;
    String name;
    String opening;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        // check if GPS enabled
        GPSTracker gpsTracker = new GPSTracker(this) ;

        if (gpsTracker.getIsGPSTrackingEnabled())
        {
            latitude = Double.valueOf(gpsTracker.latitude);
            longitude = Double.valueOf(gpsTracker.longitude);

           // getLocalRating();

            GetPlacesNearby(latitude, longitude);

        }
        else
        {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gpsTracker.showSettingsAlert();
        }
    }

    public List<Local> GetPlacesNearbyDatabase(double latitude, double longitude){

        List<Local> locals = new ArrayList<Local>();

        if (latitude != 0 && longitude != 0) {

        }

        return locals;
    }

    public void GetPlacesNearby(double latitude, double longitude){
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location="+ latitude +","+ longitude);
        sb.append("&radius=1000");
        sb.append("&types="+"night_club|bar");
        sb.append("&sensor=true");
        sb.append("&key=AIzaSyCcOOjKM7LjDUYdHEGm0j6uN4B1T0UnMyw");

        // Creating a new non-ui thread task to download json data
        PlacesTask placesTask = new PlacesTask();

        // Invokes the "doInBackground()" method of the class PlaceTask
        placesTask.execute(sb.toString());

    }

    public void getLocalRating(){

        StringBuilder sb = new StringBuilder("https://ponto-162519.firebaseio.com/" +
                "points.json?" +
                "auth=YD9GxeXMicxPWqpLbzuvrowtKcWlGI4L2cnPsJrI");

        // Creating a new non-ui thread task to download json data
        RatingTask ratingTask = new RatingTask();

        // Invokes the "doInBackground()" method of the class PlaceTask
        ratingTask.execute(sb.toString());
    }

    /** A class, to download Google Places */
    private class PlacesTask extends AsyncTask<String, Integer, String> {

        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try{
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result){
            ParserTask parserTask = new ParserTask();

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTask.execute(result);
        }

    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException, IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception URK", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {
            List<HashMap<String, String>> places = null;
            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try{
                jObject = new JSONObject(jsonData[0]);

                /** Getting the parsed data as a List construct */
                places = placeJsonParser.parse(jObject);

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String,String>> list){

            // Clears all the existing markers
            mMap.clear();

            places = new HashMap<>();

            for(int i=0;i<list.size();i++){

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Getting a place from the places list
                HashMap<String, String> hmPlace = list.get(i);

                // Getting latitude of the place
                double lat = Double.parseDouble(hmPlace.get("lat"));

                // Getting longitude of the place
                double lng = Double.parseDouble(hmPlace.get("lng"));

                // Getting name
                name = hmPlace.get("place_name");

                // Getting vicinity
                vicinity = hmPlace.get("vicinity");

                LatLng latLng = new LatLng(lat, lng);
                String id = "";
                if(hmPlace.containsKey("place_id"))
                    id = hmPlace.get("place_id");

                if(hmPlace.containsKey("weekday_text"))
                   opening = hmPlace.get("weekday_text");


                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker.
                //This will be displayed on taping the marker
                markerOptions.title(name + " : " + vicinity);

                markerOptions.snippet(id);

                // Placing a marker on the touched position
                mMap.addMarker(markerOptions);

                Places localPlace = new Places();
                localPlace.Name = name;
                localPlace.Adress = vicinity;

                places.put(id,localPlace);
            }

            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            LatLng currentLocal = new LatLng(latitude ,longitude);

            mMap.addMarker(new MarkerOptions().position(currentLocal).title("Marker in Sydney").snippet("32").icon(BitmapDescriptorFactory.fromResource(R.drawable.man)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocal));
            //Move the camera to the user's location and zoom in!
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16.0f));
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
            {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    //if(arg0.getSnippet().equals("32")) // if marker source is clicked
                   // Toast.makeText(MapsActivity.this, marker.getTitle(), Toast.LENGTH_SHORT).show();// display toast
                   // return true;

                    Places localPlace = places.get(marker.getSnippet());

                    if (localPlace != null){
                        Intent myIntent = new Intent(MapsActivity.this, DetailsPlaceActivity.class);

                        myIntent.putExtra("place_id", marker.getSnippet());
                        myIntent.putExtra("adress",localPlace.Adress);
                        //myIntent.putExtra("opening",opening);
                        myIntent.putExtra("name",localPlace.Name);
                        MapsActivity.this.startActivity(myIntent);
                        return  true;
                    }else{
                        return  false;
                    }
                }
            });
        }
    }


    private class RatingTask extends AsyncTask<String, Integer, String> {

        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try{
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;

            // Start parsing the Google places in JSON format
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result){
            RatingParseTask ratingParserTask = new RatingParseTask();

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            ratingParserTask.execute(result);
        }

    }

    /** A class to parse the Google Places in JSON format */
    private class RatingParseTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {
            List<HashMap<String, String>> places = null;
            PointsJSONParser placeJsonParser = new PointsJSONParser();

            try{
                jObject = new JSONObject(jsonData[0]);

                /** Getting the parsed data as a List construct */
                places = placeJsonParser.parse(jObject);

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String,String>> list){

            for(int i=0;i<list.size();i++){

                // Getting a place from the places list
                HashMap<String, String> hmPlace = list.get(i);

                if(hmPlace.containsKey("rating") && hmPlace.containsKey("id"))
                    rating.put(hmPlace.get("id"),hmPlace.get("rating") );
            }
        }
    }

    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
}


