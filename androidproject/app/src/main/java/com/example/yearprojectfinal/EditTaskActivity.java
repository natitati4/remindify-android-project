package com.example.yearprojectfinal;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

// This class represents the activity for editing an existing task
public class EditTaskActivity extends AppCompatActivity implements View.OnClickListener
{
    ImageButton btnBack;

    EditText taskNameEditText;
    EditText taskDescEditText;
    TextView taskDeadlineDateTextView;
    TextView taskDeadlineTimeTextView;
    Button editTaskSubmitButton;
    TaskClass currentTaskClass;

    Button btnDeadlineDate;
    Button btnDeadlineTime;

    Calendar selectedDateAndTime = Calendar.getInstance();

    private final String activityName = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        btnBack = findViewById(R.id.btnEditTaskBack);

        btnBack = findViewById(R.id.btnEditTaskBack);

        taskNameEditText = findViewById(R.id.etEditTaskName);
        taskDescEditText = findViewById(R.id.etEditTaskDesc);
        taskDeadlineDateTextView = findViewById(R.id.tvEditTaskDeadlineDate);
        taskDeadlineTimeTextView = findViewById(R.id.tvEditTaskDeadlineTime);

        btnDeadlineDate = findViewById(R.id.btnEditTaskDeadlineDate);
        btnDeadlineDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                // Show the date picker dialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        EditTaskActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
                            {

                                taskDeadlineDateTextView.setText
                                        (String.format("%d/%d/%d", dayOfMonth, month+1, year));
                                selectedDateAndTime.set(
                                        year,
                                        month,
                                        dayOfMonth,
                                        selectedDateAndTime.get(Calendar.HOUR_OF_DAY),
                                        selectedDateAndTime.get(Calendar.MINUTE));
                            }
                        },
                        // Set the default date to the current selected date
                        selectedDateAndTime.get(Calendar.YEAR),
                        selectedDateAndTime.get(Calendar.MONTH),
                        selectedDateAndTime.get(Calendar.DAY_OF_MONTH)
                );
                long currentDate = System.currentTimeMillis() - 1000;  // Set the minimum selectable date to the current date
                datePickerDialog.getDatePicker().setMinDate(currentDate);
                datePickerDialog.show();
            }
        });

        btnDeadlineTime = findViewById(R.id.btnEditTaskDeadlineTime);
        btnDeadlineTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                // Show the time picker dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        EditTaskActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                            {
                                // Handle the selected time
                                taskDeadlineTimeTextView.setText(String.format("%02d:%02d", hourOfDay, minute));
                                selectedDateAndTime.set
                                        (
                                                selectedDateAndTime.get(Calendar.YEAR),
                                                selectedDateAndTime.get(Calendar.MONTH),
                                                selectedDateAndTime.get(Calendar.DAY_OF_MONTH),
                                                hourOfDay,
                                                minute
                                        );
                            }
                        },
                        // Set the default time to the current selected time
                        selectedDateAndTime.get(Calendar.HOUR_OF_DAY),
                        selectedDateAndTime.get(Calendar.MINUTE),
                        true // Use 24 hour format
                );
                timePickerDialog.show();
            }
        });

        editTaskSubmitButton = findViewById(R.id.btnEditTaskSubmit);

        currentTaskClass = (TaskClass) getIntent().getSerializableExtra("entire current task object");

        taskNameEditText.setText(currentTaskClass.getName());
        taskDescEditText.setText(currentTaskClass.getDescription());
        taskDeadlineDateTextView.setText(currentTaskClass.getDeadLineDate());
        taskDeadlineTimeTextView.setText(currentTaskClass.getDeadLineTime());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        // set current selected date and time
        try
        {
            Date date = dateFormat.parse(currentTaskClass.getDeadLineDate());
            Date time = timeFormat.parse(currentTaskClass.getDeadLineTime());
            selectedDateAndTime.setTime(date);
            selectedDateAndTime.set(Calendar.HOUR_OF_DAY, time.getHours());
            selectedDateAndTime.set(Calendar.MINUTE, time.getMinutes());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        editTaskSubmitButton.setOnClickListener(this);
    }

    // Handle button clicks
    @Override
    public void onClick(View view) {

        Intent previousIntent = getIntent();

        String updatedTaskNameString = taskNameEditText.getText().toString().trim();
        String updatedTaskDescString = taskDescEditText.getText().toString().trim();
        String updatedTaskStartTimeString = taskDeadlineDateTextView.getText().toString().trim();
        String updatedTaskDeadLineString = taskDeadlineTimeTextView.getText().toString().trim();

        // Check if any of the fields are empty
        if (updatedTaskNameString.isEmpty() || updatedTaskDescString.isEmpty()) {
            // Show an error message
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the date and time are in the correct format
        if (!UtilityClass.isValidDate(updatedTaskStartTimeString) || !UtilityClass.isValidTime(updatedTaskDeadLineString)) {
            // Show an error message
            Toast.makeText(this, "Please choose date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDateAndTime.before(Calendar.getInstance()))
        {
            Toast.makeText(EditTaskActivity.this,
                    "Please select a time in the future",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // saving task to db
        TaskClass newTaskClass = new TaskClass
                (updatedTaskNameString,
                updatedTaskDescString,
                updatedTaskStartTimeString,
                updatedTaskDeadLineString,
                currentTaskClass.getId());

        // get current location to change
        LocationClass currentLocationClass =
                (LocationClass) previousIntent.getSerializableExtra("entire current location object");
        ArrayList<TaskClass> locationsTasksList = currentLocationClass.getTasksList();

        // get current task that's being edited
        TaskClass currentTaskClass = (
                TaskClass) previousIntent.getSerializableExtra("entire current task object");

        for (int i = 0; i < locationsTasksList.size(); i++)
        {
            if (locationsTasksList.get(i).getId() == currentTaskClass.getId())
            {
                locationsTasksList.set(i, newTaskClass);
            }
        }

        // updated tasks list (with edited task)
        currentLocationClass.setTasksList(locationsTasksList);

        // get the old location json string, to update (it's task list)
        String oldLocationJsonString = previousIntent.getExtras().getString("old location json string");

        // new one
        Gson gson = new Gson();
        String editedLocationJsonString = gson.toJson(currentLocationClass);

        // using | cuz LatLng has comma in toString
        String dataStrToSend = oldLocationJsonString + "|" + editedLocationJsonString;

        // send data to server
        String resultStr = UtilityClass.sendAndReceive(UtilityClass.constructString(activityName, dataStrToSend));

        if (resultStr.equals("location updated successfully"))
        {
            Toast.makeText(getApplicationContext(),
                    "Task updated.",Toast.LENGTH_SHORT).show();

            previousIntent.putExtra("updated task", newTaskClass);
            setResult(RESULT_OK, previousIntent);
            finish();
        }

        else
        {
            Toast.makeText(getApplicationContext(),
                    "Something went wrong updating the task. Try refreshing the app.",Toast.LENGTH_SHORT).show();
        }
    }
}
