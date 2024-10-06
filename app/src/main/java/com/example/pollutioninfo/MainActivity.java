package com.example.pollutioninfo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextInputEditText locationEditText;
    TextView AirIndex;
    TextView AQItextView;
    String PlacesAPI = ""; //API key
    CardView AQIcolor;
    RelativeLayout loadingPanel;


    void colorChange(String AQI) {
        if (AQI == "" || AQI=="-") {
            AQIcolor.setCardBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.AirIndexNA));
            AQItextView.setText("");
        }
        else if (Integer.parseInt(AQI) <= 50) {
            AQIcolor.setCardBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.AirIndex0));
            AQItextView.setText("Good");
        }
        else if (Integer.parseInt(AQI) <= 100) {
            AQIcolor.setCardBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.AirIndex1));
            AQItextView.setText("Moderate");
        }
        else if (Integer.parseInt(AQI) <= 150) {
            AQIcolor.setCardBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.AirIndex2));
            AQItextView.setText("Unhealthy for Sensitive Groups");
        }
        else if (Integer.parseInt(AQI) <= 200) {
            AQIcolor.setCardBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.AirIndex3));
            AQItextView.setText("Unhealthy");
        }
        else if (Integer.parseInt(AQI) <= 300) {
            AQIcolor.setCardBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.AirIndex4));
            AQItextView.setText("Very Unhealthy");
        }
        else {
            AQIcolor.setCardBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.AirIndex4));
            AQItextView.setText("Hazardous");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationEditText = findViewById(R.id.locationEditText);
        AirIndex = findViewById(R.id.locationTextView);
        AQItextView = findViewById(R.id.AQItextView);
        AQIcolor = findViewById(R.id.CardViewAQI);
        loadingPanel = findViewById(R.id.loadingPanel);

        final AirQualityService airQualityService = new AirQualityService(MainActivity.this);

        //instead of deprecated function startActivityForResult
        final ActivityResultLauncher<Intent> ActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            loadingPanel.setVisibility(View.VISIBLE);
                            // There are no request codes
                            Intent data = result.getData();

                            Place place = Autocomplete.getPlaceFromIntent(data);
                            //set city (town) on EditText
                            locationEditText.setText(place.getAddress());

                            //callback
                            airQualityService.getAQI(place.getName(), new AirQualityService.VolleyResponseListener() {
                                @Override
                                public void onError(String message) {
                                    AirIndex.setText("N/A");
                                    loadingPanel.setVisibility(View.GONE);
                                }

                                @Override
                                public void onResponse(String AQI) {
                                    if (AQI == "" || AQI == "-")
                                        AirIndex.setText("N/A");
                                    else
                                        AirIndex.setText(AQI);
                                    colorChange(AQI);
                                    loadingPanel.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                });

        //Initialize places
        Places.initialize(getApplicationContext(), PlacesAPI);

        locationEditText.setFocusable(false);
        locationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Initialize place field list
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS,
                        Place.Field.LAT_LNG, Place.Field.NAME);

                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList)
                        //.setCountry("PL")  //get suggestions only for Poland
                        .setTypeFilter(TypeFilter.CITIES) //get only city suggestions
                        .build(MainActivity.this);
                ActivityResultLauncher.launch(intent);
            }
        });
    }
}
