package com.example.yearprojectfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

// This class represents the account activity where users can view and update their account information
public class AccountActivity extends AppCompatActivity implements View.OnClickListener
{

    EditText etAccountPhoneNumber;
    EditText etAccountUsername;

    ImageButton btnBack;
    Button btnAccountSubmit;
    Button btnAccountLogout;

    String connectedUsername;

    private final String activityName = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        connectedUsername = getIntent().getExtras().getString("username");

        etAccountPhoneNumber = findViewById(R.id.etAccountPhoneNumber);
        etAccountUsername = findViewById(R.id.etAccountUsername);
        btnBack = findViewById(R.id.btnAccountBack);
        btnAccountSubmit = findViewById(R.id.btnAccountSubmit);
        btnAccountLogout = findViewById(R.id.btnAccountLogout);

        getUserDetailsForUser();  // set edit texts to current details

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });

        btnAccountSubmit.setOnClickListener(this);
        btnAccountLogout.setOnClickListener(this);
    }

    // Handle the button clicks
    @Override
    public void onClick(View btnPressed)
    {
        if (btnPressed == btnAccountSubmit)
        {
            String newPhoneNumber = etAccountPhoneNumber.getText().toString();
            String newUsername = etAccountUsername.getText().toString();

            if (newPhoneNumber.isEmpty() || newUsername.isEmpty())
            {
                // At least one of the EditText views is empty
                // Show a toast message to notify the user
                Toast.makeText(this, "Please fill everything", Toast.LENGTH_SHORT).show();
                return;
            }

            else if (!UtilityClass.validate_only_letters_and_numbers(newUsername) ||
                    !UtilityClass.isValidPhoneNumber(newPhoneNumber))
            {
                // Get a reference to the root view of the activity
                View rootView = findViewById(android.R.id.content);

                // Display a snackbar
                Snackbar snackbar = Snackbar.make(rootView,
                        "- Username should contain only letters and digits" +" \n" +
                                "- Phone number format should be +15551234567", Snackbar.LENGTH_SHORT);

                View snackbarView = snackbar.getView();
                TextView textView = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setMaxLines(3);  // show multiple lines

                snackbar.show();
                return;
            }

            // if everything is okay (in correct format) send to db

            String dataStrToSend = "requesting user update|" + connectedUsername + "|" + newPhoneNumber + "|" + newUsername;

            String resultStr = UtilityClass.sendAndReceive(UtilityClass.constructString(activityName, dataStrToSend));

            // change to new username (in case something went wrong in sending success)
            if (resultStr.equals("user updated successfully"))
            {

                connectedUsername = newUsername;

                SharedPreferences sharedPref = getSharedPreferences("remindify_sp", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("connected username", connectedUsername);
                editor.apply();

                Intent previousIntent = getIntent();

                // send to change connected username to new username
                previousIntent.putExtra("new username", newUsername);

                Toast.makeText(getApplicationContext(), "Success! Details changed.", Toast.LENGTH_SHORT).show();

                setResult(RESULT_OK, previousIntent);
                finish();
            }

            else if (resultStr.equals("username already exists"))
            {
                Toast.makeText(getApplicationContext(),
                        "Username already exists.", Toast.LENGTH_SHORT).show();
            }
            // something unexpected failed
            else
            {
                Toast.makeText(getApplicationContext(), "Failed to update details.", Toast.LENGTH_SHORT).show();
            }

        }

        if (btnPressed == btnAccountLogout)
        {
            Intent previousIntent = getIntent();
            previousIntent.putExtra("logging out", true);
            setResult(RESULT_OK, previousIntent);
            finish();
        }
    }

    // Gets the details for the current connected user
    public void getUserDetailsForUser()
    {
        String dataStrToSend = "requesting user details|" + connectedUsername;

        // send data to server
        String resultStr = UtilityClass.sendAndReceive(UtilityClass.constructString(activityName, dataStrToSend));

        if (resultStr.equals("failed to get user details") || resultStr.equals(""))
        {
            Toast.makeText(this, "Failed to get user details", Toast.LENGTH_SHORT).show();
        }

        else
        {
            try
            {
                // it's actually just a '|'.
                String[] userDetails = resultStr.split("\\|");

                etAccountPhoneNumber.setText(userDetails[0]);  // 0 is phone number
                etAccountUsername.setText(userDetails[1]);  // 1 is username
            }

            catch (Exception e)
            {
                Log.e(activityName, e.toString());
                Toast.makeText(this, "Failed to get user details", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
