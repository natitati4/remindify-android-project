package com.example.yearprojectfinal;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

// This class contains functions that are commonly used
public class UtilityClass
{
    private static final String className = UtilityClass.class.getSimpleName();

    private static final int RAILS = 3;
    private static final String KEY = "THE_ULTIMATE_KEY";

    // Constructs the string to send in the correct format to send to the server
    public static String constructString(String activityName, String data)
    {
        // length of bytes, not of str.
        return String.format("%05d", activityName.getBytes(StandardCharsets.UTF_8).length) + activityName
                + String.format("%05d", data.getBytes(StandardCharsets.UTF_8).length) + data;
    }

    // Send and received the data from the server (encrypts, sends, receives and decrypts)
    public static String sendAndReceive(String strToSend)
    {
        try
        {
            // encrypting and sending
            Log.d(className, "\nEncrypting: " + strToSend + "\n ");
            String encrypted = encrypt(strToSend);
            Log.d(className, "Encrypted: " + encrypted + "\n ");

            SocketTask socketTask = new SocketTask(
                    String.format("%05d", encrypted.getBytes(StandardCharsets.UTF_8).length) + encrypted);

            // getting and decrypting
            String strReceived = socketTask.execute().get();
            if (strReceived.equals(""))
                return "";

            Log.d(className, "\nDecrypting: " + strReceived + "\n ");
            String decrypted = decrypt(strReceived);
            Log.d(className, "Decrypted: " + decrypted + "\n ");

            return decrypted;
        }

        catch (ExecutionException | InterruptedException e)
        {
            Log.e("Exception", e.toString());
        }
        return "something went wrong with execution";
    }

    // Get the location list for a user by their username
    public static ArrayList<LocationClass> getLocationListForUser(String activityName,
                                                                  String connectedUsername)
    {
        String dataStrToSend = "requesting location list|" + connectedUsername;

        // send data to server
        String resultStr = UtilityClass.sendAndReceive(UtilityClass.constructString(activityName, dataStrToSend));
        if (resultStr.equals(""))
            return null;

        try
        {
            Gson gson = new Gson();
            Type stringListType = new TypeToken<ArrayList<String>>() {}.getType();
            ArrayList<String> locationsListByUsernameJsonStrings = gson.fromJson(resultStr, stringListType);

            ArrayList<LocationClass> locationsListByUsername = new ArrayList<>();

            for (String json : locationsListByUsernameJsonStrings)
            {
                LocationClass locationClass = gson.fromJson(json, LocationClass.class);
                locationsListByUsername.add(locationClass);
            }

            return locationsListByUsername;
        }

        // something unexpected failed
        catch (Exception e)
        {
            return null;
        }
    }

    // Calculates the zoom level by the radius
    public static float zoomLevelByRadius(int radius)
    {
        return (float)(16 - Math.log(radius / 300f) / Math.log(2));
    }

    // Verify if a string is a positive integer
    public static boolean isStringPositiveInteger(String s)
    {
        try
        {
            int result = Integer.parseInt(s);
            return result > 0;
        }

        catch (NumberFormatException e)
        {
            return false;
        }
    }

    // Find a location in a list of locations
    public static boolean searchLocationInListById(ArrayList<LocationClass> locationsList, LocationClass location)
    {
        for (LocationClass loc : locationsList)
        {
            if (location.getId() == loc.getId())
            {
                // found
                return true;
            }
        }
        // not found
        return false;
    }

    // Verify if a string contains only letters and numbers
    public static boolean validate_only_letters_and_numbers(String str)
    {
        // Iterate through each character in the string
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // Check if the character is a letter or a digit
            if (!Character.isLetterOrDigit(c)) {
                // If the character is not a letter or a digit, return false
                return false;
            }
        }
        // If the loop completes without returning false, return true
        return true;
    }

    // Very if a string is a valid american phone number
    public static boolean isValidPhoneNumber(String phoneNumber) {
        // Use a regular expression to check if the phone number is in the correct format
        return phoneNumber.matches("\\+\\d{11}");
    }

    // Verify if a string is a valid date
    public static boolean isValidDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            sdf.parse(date);
            return true;
        } catch (ParseException e)
        {
            return false;
        }
    }

    // Verify if a string is a valid time
    public static boolean isValidTime(String time)
    {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            sdf.setLenient(false);
            sdf.parse(time);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    // Verify if an activity has certain permissions
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                {
                    return false;
                }
            }
        }
        return true;
    }

    // Encrypt a string using base64, rail fence from bottom and xor with a key
    private static String encrypt(String str)
    {
        // encrypting base64
        String encrypted_base64 = Base64.getEncoder().encodeToString(str.getBytes());

        // encrypting Rail Fence
        String encrypted_railFence = RailFenceFromBottom.encrypt(encrypted_base64, RAILS);

        // encrypting with xors
        StringBuilder final_encrypted = new StringBuilder();
        for (int i = 0; i < encrypted_railFence.length(); i++)
        {
            final_encrypted.append((char)(encrypted_railFence.charAt(i) ^ KEY.charAt(i % KEY.length())));
        }

        // returning the final encrypted
        return final_encrypted.toString();
    }

    // Encrypt a string that was encrypted using base64, rail fence from bottom and xor with a key
    private static String decrypt(String str)
    {
        // decrypting xor encryption
        StringBuilder decrypted_xors = new StringBuilder();
        for (int i = 0; i < str.length(); i++)
        {
            decrypted_xors.append((char)(str.charAt(i) ^ KEY.charAt(i % KEY.length())));
        }

        // decrypting Rail Fence
        String decrypted_railFence = RailFenceFromBottom.decrypt(decrypted_xors.toString(), RAILS);

        // decrypting base64 and returning the final decrypted
        return new String(Base64.getDecoder().decode(decrypted_railFence));
    }
}
