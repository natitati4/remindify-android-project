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

import java.util.ArrayList;
import java.util.Calendar;

// This class represents the activity for adding a new task
public class AddTaskActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton btnBack;
    TextView addTaskTextView;
    EditText taskNameEditText;
    EditText taskDescEditText;
    TextView taskDeadlineDateTextView;
    TextView taskDeadlineTimeTextView;
    Button taskSubmitButton;

    Button btnDeadlineDate;
    Button btnDeadlineTime;

    LocationClass currentLocationClass;

    Calendar selectedDateAndTime = Calendar.getInstance();

    private final String activityName = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        btnBack = findViewById(R.id.btnAddTaskBack);
        addTaskTextView = findViewById(R.id.locationNameInTasksScreen);
        taskNameEditText = findViewById(R.id.etAddTaskName);
        taskDescEditText = findViewById(R.id.etAddTaskDesc);
        taskDeadlineDateTextView = findViewById(R.id.tvAddTaskDeadlineDate);
        taskDeadlineTimeTextView = findViewById(R.id.tvAddTaskDeadlineTime);
        taskSubmitButton = findViewById(R.id.btnAddTaskSubmit);

        btnDeadlineDate = findViewById(R.id.btnAddTaskDeadlineDate);
        btnDeadlineDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                // Show the date picker dialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        AddTaskActivity.this,
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
                        // Set the default date to the current date
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                );
                long currentDate = System.currentTimeMillis() - 1000;  // Set the minimum selectable date to the current date
                datePickerDialog.getDatePicker().setMinDate(currentDate);
                datePickerDialog.show();
            }
        });

        btnDeadlineTime = findViewById(R.id.btnAddTaskDeadlineTime);
        btnDeadlineTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                // Show the time picker dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        AddTaskActivity.this,
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
                        // Set the default time to the current time
                        Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                        Calendar.getInstance().get(Calendar.MINUTE),
                        true // Use 24 hour format
                );
                timePickerDialog.show();
            }
        });

        currentLocationClass = (LocationClass) getIntent().getSerializableExtra("entire location object");
        addTaskTextView.setText("Add task for: " + currentLocationClass.getName());

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        taskSubmitButton.setOnClickListener(this);
    }

    // Handle button clicks
    @Override
    public void onClick(View view)
    {
        if (view == taskSubmitButton)
        {
            Intent previousIntent = getIntent();

            String taskName = taskNameEditText.getText().toString().trim();
            String taskDesc = taskDescEditText.getText().toString().trim();
            String taskDeadlineDate = taskDeadlineDateTextView.getText().toString().trim();
            String taskDeadlineTime = taskDeadlineTimeTextView.getText().toString().trim();

            // Check if any of the fields are empty
            if (taskName.isEmpty() || taskDesc.isEmpty()) {
                // Show an error message
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the date and time are in the correct format
            if (!UtilityClass.isValidDate(taskDeadlineDate) || !UtilityClass.isValidTime(taskDeadlineTime)) {
                // Show an error message
                Toast.makeText(this, "Please choose date and time", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedDateAndTime.before(Calendar.getInstance()))
            {
                Toast.makeText(AddTaskActivity.this,
                        "Please select a time in the future",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // saving task to db
            TaskClass newTaskClass = new TaskClass(taskName,
                    taskDesc,
                    taskDeadlineDate,
                    taskDeadlineTime);

            // If there was a problem created the class
            if (!newTaskClass.getSuccess())
            {
                Toast.makeText(getApplicationContext(),
                        "Something went wrong adding the task.",Toast.LENGTH_SHORT).show();
                return;
            }


            Gson gson = new Gson();

            // converting old location to Json
            String oldLocationJsonString = previousIntent.getExtras().getString("old location json string");

            ArrayList<TaskClass> updatedTaskListClass = currentLocationClass.getTasksList();
            updatedTaskListClass.add(newTaskClass);

            currentLocationClass.setTasksList(updatedTaskListClass);

            // converting new (updated) location to Json
            String updatedTaskListLocationJsonString = gson.toJson(currentLocationClass);

            // using | cuz LatLng has comma in toString
            String dataStrToSend = oldLocationJsonString + "|" + updatedTaskListLocationJsonString;

            // send data to server
            String resultStr = UtilityClass.sendAndReceive(UtilityClass.constructString(activityName, dataStrToSend));

            if (resultStr.equals("location updated successfully"))
            {
                Toast.makeText(getApplicationContext(),
                        "Task added.",Toast.LENGTH_SHORT).show();

                // sent to previous activity to add to list view
                previousIntent.putExtra("new task", newTaskClass);
                setResult(RESULT_OK, previousIntent);
                finish();
            }

            else
            {
                Toast.makeText(getApplicationContext(),
                        "Something went wrong adding the task. Try again later.",Toast.LENGTH_SHORT).show();
            }
        }
    }
}