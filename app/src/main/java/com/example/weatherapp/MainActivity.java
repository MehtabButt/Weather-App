package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private Button getLocBtn;
    private TextView latTextView;
    private TextView longTextView;
    private TextView dateTextView;
    private TextView countryTextView;
    private TextView tempTextView;
    private TextView discTextView;
    private TextView humidityTextView;
    private TextView windTextView;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.d("msg", "results are here");
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
//                locTextView.setText("Longitude: " + location.getLongitude() + " Latitude: " + location.getLatitude());
                if(location != null){
                    stopLocationUpdates();
                    getWeatherDetails(location);
                    break;
                }
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getLocBtn = findViewById(R.id.getLocBtn);
        latTextView = findViewById(R.id.latTextView);
        longTextView = findViewById(R.id.longTextView);
        dateTextView = findViewById(R.id.dateTextView);
        countryTextView = findViewById(R.id.countryTextView);
        tempTextView = findViewById(R.id.tempTextView);
        discTextView = findViewById(R.id.discTextView);
        humidityTextView = findViewById(R.id.humidityTextView);
        windTextView = findViewById(R.id.windTextView);
//        locTextView = findViewById(R.id.locTextView);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();


        getLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationPermission();
            }
        });

    }

    private void getWeatherDetails(Location location){
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        Log.d("weatherDetails", "https://api.openweathermap.org/data/2.5/weather?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() +
                "&appid=d1820247c9fd0096ffced37ab6389141");
        JsonObjectRequest jasonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                "https://api.openweathermap.org/data/2.5/weather?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() +
                "&appid=d1820247c9fd0096ffced37ab6389141",
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("weatherDetails", response.getString("weather"));

                    JSONObject coord = response.getJSONObject("coord");
                    JSONArray weather = response.getJSONArray("weather");
                    JSONObject object = weather.getJSONObject(0);
                    JSONObject main = response.getJSONObject("main");
                    JSONObject wind = response.getJSONObject("wind");
                    JSONObject sys = response.getJSONObject("sys");

                    latTextView.setText("latitude: " + coord.getString("lat"));
                    longTextView.setText("Longitude: " + coord.getString("lon"));

                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE-MMM-d-yyyy");
                    String formatted_Date = sdf.format(calendar.getTime());
                    dateTextView.setText("Date: " + formatted_Date);

                    countryTextView.setText("Country: " + sys.getString("country"));

                    double temp = main.getDouble("temp");
                    temp = (temp-275.15);
                    temp = Math.round(temp);
                    Log.d("weatherDetails", "" + temp);
                    tempTextView.setText("Temperature: " + String.valueOf((int) temp));

                    discTextView.setText("Discription: " + object.getString("description"));
                    humidityTextView.setText("Humidity: " + main.getString("humidity") + "%");
                    windTextView.setText("Wind: " + wind.getString("speed") + "km/h");



                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error","something went wrong");
            }
        });

        requestQueue.add(jasonObjectRequest);

    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void checkDeviceSettingAndStartLocationUpdates() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                startLocationUpdates();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                1001);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });

    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ) {

            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());

        }

    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void getLocation(){

        if (ActivityCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            Task<Location> locationTask = fusedLocationClient.getLastLocation();

            locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location != null){
                        Log.d("msg", "Long: " + location.getLongitude() + " Lat: " + location.getLatitude());
                    } else {
                        Log.d("msg", "location is null");
                    }
                }
            });

            locationTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Log.d("msg", "location getting failed");

                }
            });
        }

    }


    private void getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            Log.d("msg","permission already granted");
//            getLocation();
            checkDeviceSettingAndStartLocationUpdates();

        } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            Log.d("msg","why permission is necessary");

            new AlertDialog.Builder(this)
                    .setTitle("Required Location Permission")
                    .setMessage("You have to give the location permission to use this feature")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                                    REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("cancle", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();

        } else {

            Log.d("msg","asking for permission");
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                getLocation();
                checkDeviceSettingAndStartLocationUpdates();
            }
            else {
                //do something
            }
        }
    }


}



