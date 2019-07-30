package tomhirsh2.gmail.com.easytaskapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class AddTask extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    TaskDBHelper mydb;
    DatePickerDialog dpd;
    TimePickerDialog tpd;
    int startYear = 0, startMonth = 0, startDay = 0;
    int startHour = 0, startMinute = 0, startSecond = 0;
    String dateFinal;
    String nameFinal;
    String timeFinal;

    Spinner taskPrioritySpinner;
    ArrayAdapter<String> spinnerAdapter;
    String priorityFinal;

    String locationFinal = "Location is not set";
    double latitudeValue, longitudeValue;
    String latitudeStr = "0", longitudeStr = "0";
    boolean isGetLocationClicked = false;

    Intent intent;
    Boolean isUpdate;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_add_new);

        mydb = new TaskDBHelper(getApplicationContext());
        intent = getIntent();
        isUpdate = intent.getBooleanExtra("isUpdate", false);

        dateFinal = todayDateString();
        Date your_date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(your_date);
        startYear = cal.get(Calendar.YEAR);
        startMonth = cal.get(Calendar.MONTH);
        startDay = cal.get(Calendar.DAY_OF_MONTH);

        startHour = cal.get(Calendar.HOUR);
        startMinute = cal.get(Calendar.MINUTE);
        startSecond = cal.get(Calendar.SECOND);

        final Button location_button = findViewById(R.id.location_button);
        location_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isGetLocationClicked = true;
                openMapActivity();
            }
        });

        final Button reset_location_button = findViewById(R.id.reset_location_button);
        reset_location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationFinal = "Location is not set";
                latitudeValue = 0;
                longitudeValue = 0;
                Toast.makeText(getApplicationContext(), "Location reset.", Toast.LENGTH_SHORT).show();
            }
        });

        taskPrioritySpinner = (Spinner) findViewById(R.id.task_priority);
        spinnerAdapter = new ArrayAdapter<String>(AddTask.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.priorityValues));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskPrioritySpinner.setAdapter(spinnerAdapter);

        if (isUpdate) {
            init_update();
        }
    }

    public void openMapActivity() {
       Intent intent = new Intent(this, GoogleMapsActivity.class);
       startActivity(intent);
    }

    public void init_update() {
        id = intent.getStringExtra("id");
        TextView toolbar_task_add_title = (TextView) findViewById(R.id.toolbar_task_add_title);
        EditText task_name = (EditText) findViewById(R.id.task_name);
        EditText task_date = (EditText) findViewById(R.id.task_date);
        EditText task_time = (EditText) findViewById(R.id.task_time);
        TextView task_location = (TextView) findViewById(R.id.taskShowAddress);

        toolbar_task_add_title.setText("Update");
        Cursor task = mydb.getDataSpecific(id);
        if (task != null) {
            task.moveToFirst();

            String currentPrioritySpinnerValue = task.getString(4).toString();
            if(currentPrioritySpinnerValue.equals("Low"))
                taskPrioritySpinner.setSelection(0);
            else if(currentPrioritySpinnerValue.equals("Moderate"))
                taskPrioritySpinner.setSelection(1);
            else
                taskPrioritySpinner.setSelection(2);

            task_name.setText(task.getString(1).toString());
            task_location.setText(task.getString(5).toString());

            Calendar cal = Function.Epoch2Calender(task.getString(2).toString());
            startYear = cal.get(Calendar.YEAR);
            startMonth = cal.get(Calendar.MONTH);
            startDay = cal.get(Calendar.DAY_OF_MONTH);
            task_date.setText(Function.Epoch2DateString(task.getString(2).toString(), "dd/MM/yyyy"));

            startHour = cal.get(Calendar.HOUR_OF_DAY);
            startMinute = cal.get(Calendar.MINUTE);
            startSecond = cal.get(Calendar.SECOND);
            task_time.setText(Function.Epoch2TimeString(task.getString(3).toString(), "kk:mm"));
        }
    }

    public String todayDateString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd/MM/yyyy", Locale.getDefault());
        return dateFormat.toString();
    }

    public void closeAddTask(View v) {
        finish();
    }

    public void doneAddTask(View v) {
        int errorStep = 0;
        EditText task_name = (EditText) findViewById(R.id.task_name);
        EditText task_date = (EditText) findViewById(R.id.task_date);
        EditText task_time = (EditText) findViewById(R.id.task_time);
        TextView task_location = (TextView) findViewById(R.id.taskShowAddress);
        nameFinal = task_name.getText().toString();
        dateFinal = task_date.getText().toString();
        timeFinal = task_time.getText().toString();
        priorityFinal = taskPrioritySpinner.getSelectedItem().toString();

        /*
        intent = getIntent();
        isUpdate = intent.getBooleanExtra("isUpdate", false);
        id = intent.getStringExtra("id");
        Cursor task = mydb.getDataSpecific(id);
        if (isUpdate && !task.getString(5).equals("Location is not set") && !isGetLocationClicked) {
            locationFinal = task.getString(5).toString();
        }
        */

        if(isGetLocationClicked) {
            GoogleMapsActivity gma = new GoogleMapsActivity();
            locationFinal = gma.chosenAddress;
            latitudeValue = gma.latitudeValue;
            longitudeValue = gma.longitudeValue;
            latitudeStr = String.valueOf(latitudeValue);
            longitudeStr = String.valueOf(longitudeValue);
            isGetLocationClicked = false;
            task_location.setText(locationFinal);
        }

        /* Checking */
        if (nameFinal.trim().length() < 1) {
            errorStep++;
            task_name.setError("Provide a task name.");
        } else {
            task_name.setError(null);
        }

        if (dateFinal.trim().length() < 4) {
            errorStep++;
            task_date.setError("Provide a specific date");
        } else {
            task_date.setError(null);
        }

        if (timeFinal.trim().length() < 4) {
            errorStep++;
            task_time.setError("Provide a specific time");
        } else {
            task_time.setError(null);
        }

        if (errorStep == 0) {
            if (isUpdate) {
                mydb.updateContact(id, nameFinal, dateFinal, timeFinal, priorityFinal, locationFinal, latitudeStr, longitudeStr);
                Toast.makeText(getApplicationContext(), "Task Updated.", Toast.LENGTH_SHORT).show();
                locationFinal = "Location is not set";
            } else {
                mydb.insertContact(nameFinal, dateFinal, timeFinal, priorityFinal, locationFinal, latitudeStr, longitudeStr);
                Toast.makeText(getApplicationContext(), "Task Added.", Toast.LENGTH_SHORT).show();
                locationFinal = "Location is not set";
            }

            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Try again", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteAddTask(View v) {
        AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);
        alertDlg.setMessage("Delete task?");
        alertDlg.setCancelable(false);
        alertDlg.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mydb.deleteTask(id);
                Toast.makeText(getApplicationContext(), "Task Deleted.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        alertDlg.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // nothing to do
            }
        });
        alertDlg.create().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        dpd = (DatePickerDialog) getFragmentManager().findFragmentByTag("startDatepickerdialog");
        if (dpd != null) dpd.setOnDateSetListener(this);
        tpd = (TimePickerDialog) getFragmentManager().findFragmentByTag("startTimepickerdialog");
        if (tpd != null) tpd.setOnTimeSetListener(this);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        startYear = year;
        startMonth = monthOfYear;
        startDay = dayOfMonth;
        int monthAddOne = startMonth + 1;
        String date = (startDay < 10 ? "0" + startDay : "" + startDay) + "/" +
                (monthAddOne < 10 ? "0" + monthAddOne : "" + monthAddOne) + "/" +
                startYear;
        EditText task_date = (EditText) findViewById(R.id.task_date);
        task_date.setText(date);
    }

    public void showStartDatePicker(View v) {
        dpd = DatePickerDialog.newInstance(AddTask.this, startYear, startMonth, startDay);
        dpd.setOnDateSetListener(this);
        dpd.show(getFragmentManager(), "startDatepickerdialog");
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        String hourString = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
        String minuteString = minute < 10 ? "0" + minute : "" + minute;
        String time = hourString + ":" + minuteString;
        EditText task_time = (EditText) findViewById(R.id.task_time);
        task_time.setText(time);
    }

    public void showStartTimePicker(View v) {
        tpd = TimePickerDialog.newInstance(AddTask.this, startHour, startMinute, true);
        tpd.setOnTimeSetListener(this);
        tpd.show(getFragmentManager(), "startTimepickerdialog");
    }

}