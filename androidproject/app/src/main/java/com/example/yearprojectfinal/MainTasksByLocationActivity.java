package com.example.yearprojectfinal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.ArrayList;

// This class represents the main locations activity, where the user can see all the tasks they
// added for a certain location
public class MainTasksByLocationActivity extends AppCompatActivity {

    TextView etLocationName;
    ListView taskListView;
    ImageButton btnBack;

    ArrayList<TaskClass> taskClassList;
    TaskAdapter taskAdapter;

    TaskClass currentTaskClass;
    TaskClass taskClassToDelete;

    String currentLocationJsonString;
    LocationClass currentLocationClass;

    int currentTaskIndex;

    private final String activityName = this.getClass().getSimpleName();

    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tasks_by_location);

        btnBack = findViewById(R.id.btnActivityMainTasksByLocationBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                setResult(RESULT_OK, getIntent());
                finish();
            }
        });

        etLocationName = findViewById(R.id.locationNameInTasksScreen);

        Intent previousIntent = getIntent();

        String locationName = previousIntent.getExtras().getString("location name");
        etLocationName.setText("All tasks to do at " + locationName);

        currentLocationJsonString = previousIntent.getExtras().getString("current location json string");
        currentLocationClass = (LocationClass) previousIntent.getSerializableExtra("entire location object");

        taskListView = (ListView) findViewById(R.id.locationsListView);

        taskClassList = (ArrayList<TaskClass>) previousIntent.getSerializableExtra("location tasks list");
        taskAdapter = new TaskAdapter(this, 0, 0, taskClassList);

        taskListView.setAdapter(taskAdapter);

        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            // Go to the edit task activity of a task upon clicking that task in the list view
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                currentTaskClass = currentLocationClass.getTasksList().get(position);
                currentTaskIndex = position;

                Intent editTaskActivity = new Intent(MainTasksByLocationActivity.this, EditTaskActivity.class);

                // sending location information, so it could be sent to the server to be updated.
                editTaskActivity.putExtra("entire current location object", currentLocationClass);
                editTaskActivity.putExtra("old location json string", currentLocationJsonString);

                // sending task information so it can be displayed, and to know which task to update.
                editTaskActivity.putExtra("entire current task object", currentTaskClass);

                startActivityForResult(editTaskActivity, 3);
            }
        });

        taskListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            // Call the function to show the task deletion dialog upon long clicking a task
            // in the list view
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {

                taskClassToDelete = taskAdapter.getItem(position);
                showTaskDeleteConfirmationDialog();
                return true;
            }
        });

        startRunnable(); // to update listview every few seconds

    }


    // When the activity is no longer in the foreground, stop the runnable that updates the tasks
    // list view
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(activityName, "Stopping runnable");
        handler.removeCallbacks(runnable); // remove updates when the activity is paused
    }

    // Start the runnable that updates the tasks list view
    private void startRunnable()
    {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {

                taskListView.invalidateViews(); // updating the list view here
                handler.postDelayed(this, 2000); // delay 2000ms
            }
        };
        handler.postDelayed(runnable, 2000); // start the updates
    }

    // Create the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tasks_by_location_screen_menu, menu);
        return true;
    }

    // Handle menu items click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle menu item clicks here
        switch (item.getItemId()) {
            case (R.id.actionAddTask):
                Intent addTaskActivity = new Intent(MainTasksByLocationActivity.this, AddTaskActivity.class);

                addTaskActivity.putExtra("entire location object", currentLocationClass);
                addTaskActivity.putExtra("old location json string", currentLocationJsonString);

                startActivityForResult(addTaskActivity, 1);
                return true;

            case (R.id.actionChangeLocation):
                Intent editLocationActivity =
                        new Intent(MainTasksByLocationActivity.this, EditLocationActivity.class);

                // this is to send to the server the json string to change
                editLocationActivity.putExtra("old location json string", currentLocationJsonString);
                editLocationActivity.putExtra("entire location object", currentLocationClass);

                startActivityForResult(editLocationActivity, 2);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Function to show the task deletion dialog
    private void showTaskDeleteConfirmationDialog()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove task");
        builder.setMessage("Are you sure you want to remove the task: " + taskClassToDelete.getName());

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                removeTask(taskClassToDelete);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void removeTask(TaskClass taskToDelete)
    {
        ArrayList<TaskClass> updatedTaskListClass = currentLocationClass.getTasksList();

        for (int i = 0; i < updatedTaskListClass.size(); i++)
        {
            if (updatedTaskListClass.get(i).getId() == taskToDelete.getId())
            {
                updatedTaskListClass.remove(i);
                Log.d(activityName, "Removed task: " + taskToDelete.getName());
                break;
            }
        }

        // updating the location object
        currentLocationClass.setTasksList(updatedTaskListClass);

        Gson gson = new Gson();
        String deletedLocationJsonString = gson.toJson(currentLocationClass);

        // old, then new
        String dataStrToSend = currentLocationJsonString + "|" + deletedLocationJsonString;

        // updating the json string of the location, after making the final str.
        currentLocationJsonString = deletedLocationJsonString;

        // send data to server
        String resultStr = UtilityClass.sendAndReceive(UtilityClass.constructString(activityName, dataStrToSend));

        if (resultStr.equals("location updated successfully")) {

            // updating the adapter, and the listView.
            taskClassList = updatedTaskListClass;
            taskAdapter = new TaskAdapter(MainTasksByLocationActivity.this, 0, 0, taskClassList);
            taskListView.setAdapter(taskAdapter);

            Toast.makeText(MainTasksByLocationActivity.this,
                    "Removed task: " + taskToDelete.getName(), Toast.LENGTH_LONG).show();
        }

        else if (resultStr.equals("Problem with connecting to MongoDB"))
        {
            Toast.makeText(getApplicationContext(), "Problem with connecting to database", Toast.LENGTH_SHORT).show();
        }

        else
        {
            Toast.makeText(getApplicationContext(),
                    "Something went wrong with removing the task. Try refreshing the app.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Update data and UI upon returning from other activities
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);

        // added task
        if (requestCode == 1)
        {
            if (resultCode == RESULT_OK) {

                // get updated task
                TaskClass newTaskClass = (TaskClass) dataIntent.getSerializableExtra("new task");

                taskClassList.add(newTaskClass);
                taskAdapter.setList(taskClassList);
                taskAdapter.notifyDataSetChanged();

                // updating the location and the location json string
                currentLocationClass.setTasksList(taskClassList);
                Gson gson = new Gson();
                currentLocationJsonString = gson.toJson(currentLocationClass);

                startRunnable(); // starts runnable checking for task time

            }
        }

        // Edited location
        if (requestCode == 2)
        {

            if (resultCode == RESULT_OK)
            {
                Intent previousIntent = getIntent();
                setResult(RESULT_OK, previousIntent);
                if (dataIntent.getExtras() != null) {
                    if (dataIntent.getExtras().getString("actually edited location") != null) {
                        finish();
                    }
                }
                startRunnable(); // starts runnable checking for task time
            }

        }

        // edited task
        if (requestCode == 3)
        {
            if (resultCode == RESULT_OK)
            {

                // update the current pressed task
                TaskClass newTaskClass = (TaskClass) dataIntent.getSerializableExtra("updated task");

                taskClassList.set(currentTaskIndex, newTaskClass);
                taskAdapter.setList(taskClassList);
                taskAdapter.notifyDataSetChanged();

                // updating the location and the location json string
                currentLocationClass.setTasksList(taskClassList);
                Gson gson = new Gson();
                currentLocationJsonString = gson.toJson(currentLocationClass);

                startRunnable(); // starts runnable checking for task time

            }
        }
    }
}
