package com.example.yearprojectfinal;

import android.util.Log;

import java.io.Serializable;

// This class represents a task in the app
public class TaskClass implements Serializable
{
    private static int currentId = 0;
    private final String className = this.getClass().getSimpleName();

    private int id;
    private String name;
    private String description;
    private String deadLineDate;
    private String deadLineTime;
    private boolean success;

    public TaskClass(String name, String description, String deadLineDate,
                     String deadLineTime)
    {
        this.success = getCurrentId(); // putting current id in currentId variable.

        this.id = currentId;
        this.name = name;
        this.description = description;
        this.deadLineDate = deadLineDate;
        this.deadLineTime = deadLineTime;
        currentId++;

        this.success = saveCurrentId(); // saving to db what's in currentId variable
    }

    public TaskClass(String name, String description, String deadLineDate,
                     String deadLineTime, int id)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.deadLineDate = deadLineDate;
        this.deadLineTime = deadLineTime;
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

    public String getDeadLineDate() {
        return deadLineDate;
    }

    public void setDeadLineDate(String deadLineDate) {
        this.deadLineDate = deadLineDate;
    }

    public String getDeadLineTime() {
        return deadLineTime;
    }

    public void setDeadLineTime(String deadLineTime) {
        this.deadLineTime = deadLineTime;
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

    // Turn the task to a string representation
    @Override
    public String toString() {
        return "TaskClass{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", deadLineDate='" + deadLineDate + '\'' +
                ", deadLineTime='" + deadLineTime +
                '}';
    }

    // Save the current task id
    private boolean saveCurrentId()
    {
        String dataStrToSend = "saving current id|" + currentId;

        // send data to server
        String resultStr = UtilityClass.sendAndReceive(UtilityClass.constructString(className, dataStrToSend));

        if (resultStr.equals("id saved successfully"))
        {
            Log.d(className, "id saved successfully");
            return true;
        }
        // something unexpected failed
        else
        {
            Log.e(className, "problem saving id");
            return false;
        }
    }

    // Get the current (an unused) id for a newly created task
    private boolean getCurrentId()
    {
        String dataStrToSend = "getting current id|";

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
                getCurrentId(); // bytes (data) lost. Try again.
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
