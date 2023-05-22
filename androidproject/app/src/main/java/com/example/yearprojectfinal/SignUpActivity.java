package com.example.yearprojectfinal;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

// This class represents the signup activity, where the user can create a new account
public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    EditText signupEtPhoneNumber;
    EditText signupEtUsername;
    EditText signupEtPassword;

    ImageButton btnBack;
    Button btnSignup_Signup;

    private final String activityName = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        btnBack = findViewById(R.id.btnSignUpBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        signupEtPhoneNumber = findViewById(R.id.etSignupPhoneNumber);
        signupEtUsername = findViewById(R.id.etSignupUsername);
        signupEtPassword = findViewById(R.id.etSignupPassword);

        btnSignup_Signup = findViewById(R.id.btnSignupSignupScreen);
        btnSignup_Signup.setOnClickListener(this);
    }


    // Handle button clicks
    @Override
    public void onClick(View btnPressed) {
        if (btnPressed == btnSignup_Signup) {

            String phoneNumberString = signupEtPhoneNumber.getText().toString();
            String usernameString = signupEtUsername.getText().toString();
            String passwordString = signupEtPassword.getText().toString();

            if (phoneNumberString.isEmpty() ||
                    usernameString.isEmpty() ||
                    passwordString.isEmpty())
            {
                // At least one of the EditText views is empty
                // Show a toast message to notify the user
                Toast.makeText(this, "Please fill everything", Toast.LENGTH_SHORT).show();
                return;
            }

            if (passwordString.length() < 8)
            {
                Toast.makeText(this, "Password should be at least 8 characters long"
                        ,Toast.LENGTH_SHORT).show();
                return;
            }

            else if (!UtilityClass.validate_only_letters_and_numbers(usernameString) ||
                    !UtilityClass.validate_only_letters_and_numbers(passwordString) ||
                    !UtilityClass.isValidPhoneNumber(phoneNumberString))
            {
                // Get a reference to the root view of the activity
                View rootView = findViewById(android.R.id.content);

                // Display a snackbar
                Snackbar snackbar = Snackbar.make(rootView,
                        "- Username and password should contain only letters and digits" +" \n" +
                                "- Phone number format should be +15551234567", Snackbar.LENGTH_SHORT);

                View snackbarView = snackbar.getView();
                TextView textView = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setMaxLines(3);  // show multiple lines

                snackbar.show();
                return;
            }

            // if everything is okay (in correct format) send to db
            String dataStrToSend = phoneNumberString + "|" + usernameString + "|" + passwordString;

            // send data to server
            String resultStr = UtilityClass.sendAndReceive(UtilityClass.constructString(activityName, dataStrToSend));

            if (resultStr.equals("user added successfully"))
            {
                Toast.makeText(getApplicationContext(), "Success! You signed up.", Toast.LENGTH_SHORT).show();
                finish();
            }
            else if (resultStr.equals("username already exists")) {
                Toast.makeText(getApplicationContext(), "Username already exists.", Toast.LENGTH_SHORT).show();
            }
            // something unexpected failed
            else {
                Toast.makeText(getApplicationContext(), "Signup failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
