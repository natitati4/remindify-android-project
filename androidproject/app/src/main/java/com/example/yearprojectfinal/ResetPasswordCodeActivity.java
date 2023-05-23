package com.example.yearprojectfinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// This class represents the reset password activity, where the user enters the code they received
// as well as the new password they want to set
public class ResetPasswordCodeActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton btnBack;

    EditText etResetPasswordCode;
    EditText etResetPasswordNewPassword;

    Button btnResetPasswordSubmitCode;

    private final String activityName = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_received);

        btnBack = findViewById(R.id.btnCodeReceivedBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        etResetPasswordCode = findViewById(R.id.etCodeReceivedCode);
        etResetPasswordNewPassword = findViewById(R.id.etCodeReceivedNewPassword);

        btnResetPasswordSubmitCode = findViewById(R.id.btnCodeReceivedSubmit);

        btnResetPasswordSubmitCode.setOnClickListener(this);
    }

    // Handle the button clicks
    @Override
    public void onClick(View btnPressed)
    {
        if (btnPressed == btnResetPasswordSubmitCode)
        {

            String code = getIntent().getExtras().getString("code");

            String resetPasswordCode = etResetPasswordCode.getText().toString();
            String newPassword = etResetPasswordNewPassword.getText().toString();

            if (code.isEmpty() || resetPasswordCode.isEmpty())
            {
                Toast.makeText(getApplicationContext(),
                        "Please fill everything" ,Toast.LENGTH_SHORT).show();
                return;
            }


            // code incorrect
            if (!resetPasswordCode.equals(code))
            {
                Toast.makeText(getApplicationContext(), "Incorrect code.", Toast.LENGTH_SHORT).show();
                return;
            }


            if (newPassword.length() < 8)
            {
                Toast.makeText(this, "Password should be at least 8 characters long"
                        , Toast.LENGTH_SHORT).show();
                return;
            }

            // If the code is correct, check if password is valid
            if (!UtilityClass.validate_only_letters_and_numbers(newPassword))
            {
                Toast.makeText(getApplicationContext(), "password should contain only letters and digits.", Toast.LENGTH_SHORT).show();
                return;
            }

            String username = getIntent().getExtras().getString("username");

            String dataStrToSend = username + "|" + newPassword;
            String resultStr = UtilityClass.sendAndReceive(UtilityClass.constructString(activityName, dataStrToSend));

            if (resultStr.equals("password updated successfully"))
            {
                Toast.makeText(getApplicationContext(),
                        "Password for user " + username +
                                " reset successfully.", Toast.LENGTH_SHORT).show();

                Intent previousActivity = getIntent();
                setResult(RESULT_OK, previousActivity);
                finish();
            }

            else if (resultStr.equals("Problem with connecting to MongoDB"))
            {
                Toast.makeText(getApplicationContext(), "Problem with connecting to database", Toast.LENGTH_SHORT).show();
            }

            else
            {
                Toast.makeText(getApplicationContext(),
                        "Error reseting password", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
