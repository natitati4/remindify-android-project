package com.example.yearprojectfinal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

// This class is the adapter for the task list view
public class TaskAdapter extends ArrayAdapter<TaskClass>
{
    Context context;
    List<TaskClass> objects;
    Calendar selectedDateAndTime = Calendar.getInstance();

    public TaskAdapter(Context context,
                       int resource,
                       int textViewResourceId,
                       List<TaskClass> objects)
    {
        super(context, resource, textViewResourceId, objects);

        this.context=context;
        this.objects=objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.task_layout, parent, false);
        }

        TextView tvTaskName = convertView.findViewById(R.id.tvTaskName);
        TextView tvTaskDesc = convertView.findViewById(R.id.tvTaskDescription);
        TextView tvTaskDateDeadline = convertView.findViewById(R.id.tvTaskDateDeadline);
        TextView tvTaskTimeDeadLine = convertView.findViewById(R.id.tvTaskTimeDeadLine);
        ImageView ivTaskTimeUp = convertView.findViewById(R.id.ivTaskTimeUp);

        TaskClass taskClass = objects.get(position);

        tvTaskName.setText(taskClass.getName());
        tvTaskDesc.setText(taskClass.getDescription());
        tvTaskDateDeadline.setText(taskClass.getDeadLineDate());
        tvTaskTimeDeadLine.setText(taskClass.getDeadLineTime());

        Button btnDone = convertView.findViewById(R.id.btnTaskDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                // Call removeTask method - handles both server DB and listview
                ((MainTasksByLocationActivity)context).removeTask(taskClass);

            }
        });

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        // set current selected date and time
        try
        {
            Date date = dateFormat.parse(taskClass.getDeadLineDate());
            Date time = timeFormat.parse(taskClass.getDeadLineTime());
            selectedDateAndTime.setTime(date);
            selectedDateAndTime.set(Calendar.HOUR_OF_DAY, time.getHours());
            selectedDateAndTime.set(Calendar.MINUTE, time.getMinutes());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        if (selectedDateAndTime.before(Calendar.getInstance()))
        {
            ivTaskTimeUp.setImageResource(R.drawable.ic_baseline_access_time_red_24);
        }
        else
        {
            ivTaskTimeUp.setImageResource(R.drawable.ic_baseline_access_time_green_24);
        }

        return convertView;
    }

    public List<TaskClass> getList() {
        return this.objects;
    }

    public void setList(List<TaskClass> list) {
        this.objects = list;
    }
}

