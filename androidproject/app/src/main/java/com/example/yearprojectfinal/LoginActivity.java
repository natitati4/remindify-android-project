package com.example.yearprojectfinal;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

// This class represents the login activity where users can choose to login, signup or change their
// password if they forgot it.
public class LoginActivity extends AppCompatActivity implements View.OnClickListener
{
    EditText loginEtUsername;
    EditText loginEtPassword;

    Button btnForgotPassword_LoginScreen;
    Button btnLogin_LoginScreen;
    Button btnSignup_LoginScreen;

    private final String activityName = this.getClass().getSimpleName();
    String[] necessary_permissions = {
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.SEND_SMS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEtUsername = findViewById(R.id.etLoginUsername);
        loginEtPassword = findViewById(R.id.etLoginPassword);

        btnForgotPassword_LoginScreen = findViewById(R.id.btnLoginForgotPassword);
        btnLogin_LoginScreen = findViewById(R.id.btnLoginLoginScreen);
        btnSignup_LoginScreen = findViewById(R.id.btnSignUpLoginScreen);

        SharedPreferences sharedPref = getSharedPreferences("remindify_sp", MODE_PRIVATE);

        String connectedUsername = sharedPref.getString("connected username", null);
        String connectedPassword = sharedPref.getString("connected password", null);

        // check if there's a user logged in.
        if (connectedUsername != null)
        {
            Intent mainLocationsIntent = new Intent(LoginActivity.this, MainLocationsActivity.class);
            mainLocationsIntent.putExtra("connected username", connectedUsername);

            Intent previousIntent = getIntent();

            // if there's extras
            if (previousIntent.getExtras() != null)
            {
                Boolean fromNotification = previousIntent.getExtras().getBoolean("From location notification");
                mainLocationsIntent.putExtra("From location notification", fromNotification);

                // check if came from notification. If yes, put the details of the location (that the
                // notification was about).
                if (fromNotification)
                {
                    mainLocationsIntent.putExtra("location name",
                            previousIntent.getExtras().getString("location name"));

                    mainLocationsIntent.putExtra("location tasks list",
                            previousIntent.getSerializableExtra("location tasks list"));

                    mainLocationsIntent.putExtra("current location json string",
                            previousIntent.getExtras().getString("current location json string"));

                    mainLocationsIntent.putExtra("entire location object",
                            previousIntent.getSerializableExtra("entire location object"));
                }
            }
            if (UtilityClass.hasPermissions(this, necessary_permissions))
                startActivityForResult(mainLocationsIntent, 1);
            else {
                Toast.makeText(this, "Enable location, SMS and notification " +
                        "permissions and relaunch the app.", Toast.LENGTH_SHORT).show();
                loginEtUsername.setText(connectedUsername);
                loginEtPassword.setText(connectedPassword);
            }

        }

        btnForgotPassword_LoginScreen.setOnClickListener(this);
        btnLogin_LoginScreen.setOnClickListener(this);
        btnSignup_LoginScreen.setOnClickListener(this);

        requestNecessaryPermissions();
    }

    // Handle button clicks
    @Override
    public void onClick(View btnPressed) {


        if (btnPressed == btnForgotPassword_LoginScreen)
        {
            Intent resetPasswordIntent = new Intent(this, ResetPasswordActivity.class);
            startActivity(resetPasswordIntent);
        }

        if (btnPressed == btnLogin_LoginScreen)
        {
            String usernameString = loginEtUsername.getText().toString();
            String passwordString = loginEtPassword.getText().toString();

            if (usernameString.isEmpty() || passwordString.isEmpty()) {
                // At least one of the EditText views is empty
                // Show a toast message to notify the user
                Toast.makeText(this, "Please fill everything", Toast.LENGTH_SHORT).show();
                return;
            }

            else if (!UtilityClass.validate_only_letters_and_numbers(usernameString) ||
                    !UtilityClass.validate_only_letters_and_numbers(passwordString))
            {
                // Get a reference to the root view of the activity
                Toast.makeText(this,
                        "username and password should contain only letters and digits.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!UtilityClass.hasPermissions(this, necessary_permissions))
            {
                Toast.makeText(this, "Enable location, SMS, and notification " +
                        "permissions before logging in.", Toast.LENGTH_SHORT).show();
                return;
            }

            // if everything is okay (in correct format)
            String dataStrToSend = usernameString + "|" + passwordString;

            // send data to server
            String resultStr = UtilityClass.sendAndReceive(UtilityClass.constructString(activityName, dataStrToSend));

            if (resultStr.equals("user successful login"))
            {
                Toast.makeText(getApplicationContext(),
                        "Success! logged in as " + usernameString,Toast.LENGTH_SHORT).show();

                // put user username in share preference
                SharedPreferences sharedPref = getSharedPreferences("remindify_sp", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("connected username", usernameString);
                editor.putString("connected password", passwordString);
                editor.apply();

                Intent mainLocationsIntent = new Intent(this, MainLocationsActivity.class);
                mainLocationsIntent.putExtra("connected username", usernameString);
                startActivityForResult(mainLocationsIntent, 1);
            }

            else if (resultStr.equals("wrong password"))
            {
                Toast.makeText(getApplicationContext(),
                        "The password you entered is incorrect." +
                                " Please try again or reset your password.",Toast.LENGTH_SHORT).show();
            }

            else if (resultStr.equals("user does not exist"))
            {
                Toast.makeText(getApplicationContext(),
                        "The username you entered does not exist." +
                                " try again or create a new account.",Toast.LENGTH_SHORT).show();
            }

            else if (resultStr.equals("Problem with connecting to MongoDB"))
            {
                Toast.makeText(getApplicationContext(), "Problem with connecting to database", Toast.LENGTH_SHORT).show();
            }

            // something unexpected happened
            else
            {
                Toast.makeText(getApplicationContext(),"Login failed.",Toast.LENGTH_SHORT).show();
            }
        }

        if (btnPressed == btnSignup_LoginScreen)
        {
            Intent signUpActivity = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(signUpActivity);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent)
    {
        super.onActivityResult(requestCode, resultCode, dataIntent);

        // if returned from the activity the comes from pressing location (MainTasksByLocationActivity)
        if (requestCode == 1)
        {
            loginEtUsername.setText("");
            loginEtPassword.setText("");

            // remove from shared preferences
            SharedPreferences sharedPref = getSharedPreferences("remindify_sp", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove("connected username");
            editor.apply();
        }
    }

    // This function does not let the user login until all necessary permissions are granted
    private void requestNecessaryPermissions()
    {

        String[] all_permissions = {
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS
        };

        if (!UtilityClass.hasPermissions(this, all_permissions))
        {
            Toast.makeText(getApplicationContext(),
                    "Please give the app the location, SMS and notification permissions, then relaunch it.",
                    Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, all_permissions, 12);
        }

        // Any Android apps targeting API 30 or higher are now no longer allowed to ask for
        // BACKGROUND_PERMISSION at the same time as regular location permission. So we have to split
        // it into different requests.

        String[] background_location_permission = {
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        };

        if (!UtilityClass.hasPermissions(this, background_location_permission))
        {
            Toast.makeText(getApplicationContext(),
                    "Please give the app background location permissions, then relaunch it.",
                    Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, background_location_permission, 13);
        }
    }
}
