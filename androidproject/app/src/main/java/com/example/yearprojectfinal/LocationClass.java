package com.example.yearprojectfinal;

import android.util.Log;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;

// This class represents a location in the app
public class LocationClass implements Serializable
{
    private static int currentId;
    private final String className = this.getClass().getSimpleName();

    private int id;
    private String name;
    private String description;
    private String addressName;

    private Double latitude;
    private Double longitude;
    private int radius;

    private ArrayList<TaskClass> tasksList;

    private boolean success;

    public LocationClass(String name, String description, String addressName, double latitude,
                         double longitude, int radius, ArrayList<TaskClass> tasksList)
    {
        this.success = getCurrentId(); // putting current id in currentId variable.
        if (!this.success)
            return;

        this.id = currentId;
        this.name = name;
        this.description = description;
        this.addressName = addressName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.tasksList = tasksList;

        currentId++;

        this.success = saveCurrentId(); // saving to db what's in currentId variable
    }

    public LocationClass(String name, String description, String locationName, double latitude,
                             double longitude, int radius, ArrayList<TaskClass> tasksList, int id)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.addressName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.tasksList = tasksList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddressName()
    {
        return this.addressName;
    }

    public void setAddressName(String addressName)
    {
        this.addressName = addressName;
    }

    public Double getLatitude()
    {
        return this.latitude;
    }

    public void setLatitude(Double latitude)
    {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public int getRadius()
    {
        return radius;
    }

    public void setRadius(int radius)
    {
        this.radius = radius;
    }

    public ArrayList<TaskClass> getTasksList()
    {
        return tasksList;
    }

    public void setTasksList(ArrayList<TaskClass> tasksList)
    {
        this.tasksList = tasksList;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public boolean getSuccess()
    {
        return this.success;
    }

    // Turn the location to a string representation
    @Override
    public String toString() {

        // making the data
        Gson gson = new Gson();
        String jsonTasksList = gson.toJson(tasksList);

        // using underscores cuz LatLng has comma in toString
        return name + "_" + description + "_" + addressName + "_" + latitude + "_" + longitude + "_" + jsonTasksList;
    }

    // Save the current location id
    private boolean saveCurrentId()
    {
        String dataStrToSend = "saving current id|" + currentId;

        // send data to server
        String resultStr = UtilityClass.sendAndReceive(UtilityClass.constructString(className, dataStrToSend));

        if (resultStr.equals("id saved successfully"))
        {
            Log.d(className, "id saved");
            return true;
        }

        // something unexpected failed
        else
        {
            Log.e(className, "problem saving id");
            return false;
        }
    }

    // Get the current (an unused) id for a newly created location
    private boolean getCurrentId()
    {
        String dataStrToSend = "getting current id";

        // send data to server
        String resultStr = UtilityClass.sendAndReceive(UtilityClass.constructString(className, dataStrToSend));

        if (resultStr.length() == 0)
            return false;

        if(resultStr.substring(0, 14).equals("got current id"))
        {
            int length = Integer.parseInt(resultStr.substring(14, 24)); // next 10 characters are len
            try
            {
                String strId = resultStr.substring(24, 24 + length);
                currentId = Integer.parseInt(strId);
                Log.d(className, "got id: " + currentId);
                return true;
            }

            catch (Exception e)
            {
                Log.e(className, "data corrupted (getting id)");
                return false;
            }
        }

        // something unexpected failed
        else
        {
            Log.e(className, "problem getting id");
            return false;
        }
    }
}
