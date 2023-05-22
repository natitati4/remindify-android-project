package com.example.yearprojectfinal;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

// This class represents the activity for adding a new location
public class AddLocationActivity extends AppCompatActivity {

    ImageButton btnBack;
    TextView addLocationTextView;

    EditText etLocationName;
    EditText etLocationDesc;
    EditText etLocationRadius;

    Button chooseLocationButton;
    Button btnSubmit;

    String locationAddressName;
    Double locationLat;
    Double locationLong;

    private final String activityName = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        btnBack = findViewById(R.id.btnAddLocationBack);

        addLocationTextView = findViewById(R.id.tvAddLocation);

        etLocationName = findViewById(R.id.etAddlocationName);
        etLocationDesc = findViewById(R.id.etAddLocationDesc);
        etLocationRadius = findViewById(R.id.etAddLocationRadius);
        chooseLocationButton = findViewById(R.id.btnAddLocationChooseLocation);

        // back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // choose location button
        chooseLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =
                        new Intent(AddLocationActivity.this, ChooseLocationActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        // submit button
        btnSubmit = findViewById(R.id.btnAddLocationSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent previousIntent = getIntent();

                String username = previousIntent.getExtras().getString("username");

                String locationNameString = etLocationName.getText().toString().trim();
                String locationDescString = etLocationDesc.getText().toString().trim();
                String locationRadiusString = etLocationRadius.getText().toString().trim();

                if (locationNameString.isEmpty() || locationDescString.isEmpty()) {
                    // Show an error message
                    Toast.makeText(getApplicationContext(), "Please fill in all the fields", Toast.LENGTH_SHORT).show();
                    return;
                }


                // check if null. cause its a new location
                if (locationAddressName == null || locationLat == null || locationLong == null) {
                    // Show an error message
                    Toast.makeText(getApplicationContext(), "Please choose a location", Toast.LENGTH_SHORT).show();
                    return;
                }

                // check if radius is a positive integer
                if (!UtilityClass.isStringPositiveInteger(locationRadiusString))
                {
                    Toast.makeText(getApplicationContext(),
                            "Radius must be a positive integer",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                int locationRadius = Integer.parseInt(locationRadiusString);

                LocationClass newlyAddedLocationClass =
                        new LocationClass(locationNameString,
                                locationDescString,
                                locationAddressName,
                                locationLat,
                                locationLong,
                                locationRadius,
                                new ArrayList<TaskClass>());

                // If there was a problem created the class
                if (!newlyAddedLocationClass.getSuccess())
                {
                    Toast.makeText(getApplicationContext(),
                            "Something went wrong adding the location.",Toast.LENGTH_SHORT).show();
                    return;
                }

                // converting location to Json
                Gson gson = new Gson();
                String locationJsonString = gson.toJson(newlyAddedLocationClass);

                // using | cuz LatLng has comma in toString
                String dataStrToSend = username + "|" + locationJsonString;

                // send data to server
                String resultStr = UtilityClass.sendAndReceive(UtilityClass.constructString(activityName, dataStrToSend));


                if (resultStr.equals("location added successfully"))
                {
                    Toast.makeText(getApplicationContext(),
                            "Location saved.",Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK, previousIntent);

                    finish();
                }

                else
                {
                    Toast.makeText(getApplicationContext(),
                            "Something went wrong saving the location. Try refreshing the app.",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    // Update data and UI upon returning from other activities
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1)
        {
            if (resultCode == RESULT_OK)
            {
                locationAddressName = data.getExtras().getString("location address name");
                locationLat = data.getExtras().getDouble("location lat");
                locationLong = data.getExtras().getDouble("location long");

                chooseLocationButton.setText("Choose location\n Current location: "
                        + locationAddressName + ", \n" + locationLat + ", " + locationLong);
            }
        }
    }
}
