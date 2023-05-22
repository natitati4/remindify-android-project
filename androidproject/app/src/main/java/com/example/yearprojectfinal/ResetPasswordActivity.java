package com.example.yearprojectfinal;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

// This class represents the reset password activity, where the user enters their username and
// receive a code to then change their password, if they forgot it
public class ResetPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton btnBack;

    EditText etResetPasswordUsername;
    Button btnResetPasswordSendCode;

    private final String activityName = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        btnBack = findViewById(R.id.btnResetPasswordBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        etResetPasswordUsername = findViewById(R.id.etResetPasswordUsername);
        btnResetPasswordSendCode = findViewById(R.id.btnResetPasswordSendCode);

        btnResetPasswordSendCode.setOnClickListener(this);
    }

    // Handle the button clicks
    @Override
    public void onClick(View btnPressed)
    {
        if (btnPressed == btnResetPasswordSendCode) {

            String usernameString = etResetPasswordUsername.getText().toString();

            if (usernameString.isEmpty()) {
                // Username EditText views is empty
                // Show a toast message to notify the user
                Toast.makeText(this, "Please fill in the field", Toast.LENGTH_SHORT).show();
                return;
            }

            else if (!UtilityClass.validate_only_letters_and_numbers(usernameString))
            {

                Toast.makeText(this, "Username should contain only letters and digits.", Toast.LENGTH_SHORT).show();
                return;
            }

            // if everything is okay (in correct format)

            String dataStrToSend = usernameString;

            // send data to server
            String resultStr = UtilityClass.sendAndReceive(UtilityClass.constructString(activityName, dataStrToSend));


            if (resultStr.equals("user does not exist"))
            {
                Toast.makeText(getApplicationContext(),
                        "The username you entered does not exist." +
                                " Try again or create a new account.",Toast.LENGTH_SHORT).show();
            }

            else if (resultStr.equals(""))
            {
                Toast.makeText(getApplicationContext(), "Something went wrong.",Toast.LENGTH_SHORT).show();
            }

            else
            {
                // sending code to user and starting the reset password code activity
                String phoneNumber = resultStr;
                String generatedCode = generateRandomNumberCode();
                String codeMessage = "Hello from the remindify app! Your code is: " + generatedCode;

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage
                        (phoneNumber, null, codeMessage, null, null);
                Toast.makeText(getApplicationContext(),"Code sent.",Toast.LENGTH_SHORT).show();

                Intent codeReceivedActivity = new Intent(this, ResetPasswordCodeActivity.class);

                codeReceivedActivity.putExtra("code", generatedCode);
                codeReceivedActivity.putExtra("username", usernameString);

                startActivityForResult(codeReceivedActivity, 1);
            }
        }
    }

    // Update data and UI upon returning from other activities
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == 1)
        {
            // meaning reset password was completed successfully
            if (resultCode == RESULT_OK)
            {
                finish();
            }
        }
    }

    // Generate a 6-digit code to sent to the user
    public String generateRandomNumberCode()
    {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }
}
