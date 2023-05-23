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

// This class represents the activity for editing an existing location
public class EditLocationActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton btnBack;
    TextView EditLocationTextView;

    EditText etLocationName;
    EditText etLocationDesc;
    EditText etLocationRadius;

    Button chooseLocationButton;
    Button btnSubmit;

    String locationAddressName;
    Double locationLat;
    Double locationLong;

    private final String activityName = this.getClass().getSimpleName();

    LocationClass currentLocationObjectClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);

        btnBack = findViewById(R.id.btnEditLocationBack);

        EditLocationTextView = findViewById(R.id.tvEditLocation);

        etLocationName = findViewById(R.id.etEditlocationName);
        etLocationDesc = findViewById(R.id.etEditLocationDesc);
        etLocationRadius = findViewById(R.id.etEditLocationRadius);
        chooseLocationButton = findViewById(R.id.btnEditLocationChooseLocation);

        currentLocationObjectClass = (LocationClass) getIntent().getSerializableExtra("entire location object");

        // Set the current details of the location
        etLocationName.setText(currentLocationObjectClass.getName());
        etLocationDesc.setText(currentLocationObjectClass.getDescription());
        etLocationRadius.setText(String.valueOf(currentLocationObjectClass.getRadius()));

        locationAddressName = currentLocationObjectClass.getAddressName();

        locationLat = currentLocationObjectClass.getLatitude();
        locationLong = currentLocationObjectClass.getLongitude();

        chooseLocationButton.setText("Choose location\n Current location: "
                + locationAddressName + ", \n" + locationLat + ", " + locationLong);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        chooseLocationButton.setOnClickListener(this);

        btnSubmit = findViewById(R.id.btnEditLocationSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                Intent previousIntent = getIntent();

                String updatedLocationNameString = etLocationName.getText().toString().trim();
                String updatedLocationDescString = etLocationDesc.getText().toString().trim();
                String updatedLocationRadiusString = etLocationRadius.getText().toString().trim();

                if (updatedLocationNameString.isEmpty() || updatedLocationDescString.isEmpty()) {
                    // Show an error message
                    Toast.makeText(getApplicationContext(), "Please fill in all the fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // check if null. Just in case.
                if (locationAddressName == null || locationLong == null || locationLong == null) {
                    // Show an error message
                    Toast.makeText(getApplicationContext(), "Please choose a location", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!UtilityClass.isStringPositiveInteger(updatedLocationRadiusString))
                {
                    Toast.makeText(getApplicationContext(),
                            "Radius must be a positive integer",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                int updatedLocationRadius = Integer.parseInt(updatedLocationRadiusString);

                // this is to get the task list
                LocationClass currentLocationClass = (LocationClass) getIntent().getSerializableExtra("entire location object");

                // use the constructor which receives id. Because we're editing, we do not want to
                // change the id.
                LocationClass editedAddedLocationClass =
                            new LocationClass(updatedLocationNameString,
                                    updatedLocationDescString,
                                    locationAddressName,
                                    locationLat,
                                    locationLong,
                                    updatedLocationRadius,
                                    currentLocationClass.getTasksList(),
                                    currentLocationClass.getId());

                // get the old location json string, to update
                String oldLocationJsonString = previousIntent.getExtras().getString("old location json string");

                // converting location to Json
                Gson gson = new Gson();
                String editedLocationJsonString = gson.toJson(editedAddedLocationClass);

                // using | cuz LatLng has comma in toString
                String dataStrToSend = oldLocationJsonString + "|" + editedLocationJsonString;

                // send data to server
                String resultStr = UtilityClass.sendAndReceive(UtilityClass.constructString(activityName, dataStrToSend));

                if (resultStr.equals("location updated successfully"))
                {
                    Toast.makeText(getApplicationContext(),
                            "Location updated.",Toast.LENGTH_SHORT).show();

                    setResult(RESULT_OK, previousIntent);
                    previousIntent.putExtra("actually edited location", "yes");
                    finish();
                }

                else if (resultStr.equals("Problem with connecting to MongoDB"))
                {
                    Toast.makeText(getApplicationContext(), "Problem with connecting to database", Toast.LENGTH_SHORT).show();
                }

                else
                {
                    Toast.makeText(getApplicationContext(),
                            "Something went wrong updating the location. Try refreshing the app.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == chooseLocationButton)
        {
            Intent intent =
                    new Intent(EditLocationActivity.this, ChooseLocationActivity.class);
            intent.putExtra("entire location object", currentLocationObjectClass);
            startActivityForResult(intent, 1);
        }
    }

    // Update data and UI upon returning from the other activities
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


                // set chosen location details
                chooseLocationButton.setText("Choose location\n Current location: "
                        + locationAddressName + ", \n" + locationLat + ", " + locationLong);
            }
        }
    }
}
