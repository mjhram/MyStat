package mjhram.com.mystat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    Spinner dateSelectionSpinner;
    static TextView selectedDateView, prevDateTextView;
    static ImageButton prevDateImgBtn;
    //static CheckBox saveCheckBox;
    static Button btnCalc;
    static long prevDate;
    EditText prevReadTextEdit;
    EditText notesEditText;
    //int unitsPerMonthX2016 = 1000;
    //int unitsPerMonth = 500;
    SharedPreferences sharedPref;
    databaseHandler dbHandler;
    ArrayAdapter<user_info> spinnerArrayAdapter;
    Spinner usersSpinner;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        storeSettings();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_perday:
                showReadingsPerDay();
                return true;
            case R.id.menu_history:
                onHistoryClicked(null);
                return true;
            case R.id.menu_export:
                exportDB();
                return true;
            case R.id.menu_import:
                importDbFromFile();
                return true;
            case R.id.menu_graph:
                showGraph(SortType.PerDay);
                return true;
            case R.id.menu_graph2:
                showGraph(SortType.None);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    ArrayList<user_info> usersList;
    public static int currentUserIdx=-1;
    //int prevUserId=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getPreferences(Context.MODE_PRIVATE);
        dbHandler = new databaseHandler(this);

        usersSpinner = (Spinner) findViewById(R.id.spinner_user);
        usersList = dbHandler.getUsers();
        spinnerArrayAdapter = new ArrayAdapter<user_info>(
                this,android.R.layout.simple_spinner_item, usersList);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        usersSpinner.setAdapter(spinnerArrayAdapter);
        usersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String tmp = parent.getItemAtPosition(position).toString();
                Log.v("item", tmp);
                user_info usr=null;
                int idx;
                for(idx = 0; idx<usersList.size(); idx++) {
                    usr = usersList.get(idx);
                    if(usr.name == tmp) {
                        break;
                    }
                }
                if(currentUserIdx == idx){
                    return;
                }
                currentUserIdx = idx;
                if(currentUserIdx>=0 && currentUserIdx<usersList.size()) {
                    prevReadTextEdit.setText(String.format("%2.2f", usr.storedReading));
                    notesEditText.setText(usr.storedNote);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        dateSelectionSpinner = (Spinner) findViewById(R.id.spinner_date);
        // Create an ArrayAdapter using the string array and a default spinner
        ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter
                .createFromResource(this, R.array.date_array,
                        android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        staticAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        dateSelectionSpinner.setAdapter(staticAdapter);
        dateSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String tmp = (String) parent.getItemAtPosition(position);
                Log.v("item", tmp);
                if(tmp.equals("Now") || tmp.equals("Yesterday")) {
                    prevDateTextView.setVisibility(View.GONE);
                    prevDateImgBtn.setVisibility(View.GONE);
                } else {
                    prevDateTextView.setVisibility(View.VISIBLE);
                    prevDateImgBtn.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        btnCalc = (Button) findViewById(R.id.buttonCalculate);
        prevDateTextView = (TextView) findViewById(R.id.textViewPrevDate);
        prevDateImgBtn = findViewById(R.id.imageButtonPrevDate);
        prevReadTextEdit = (EditText) findViewById(R.id.editTextPrevReading);
        notesEditText = (EditText) findViewById(R.id.editTextNotes);
        dateTimePicker_init();
        loadSettings();
    }

    public void onPrevDateClicked(View v) {
        selectedDateView = prevDateTextView;
        dateTimePicker_show();
        //DialogFragment newFragment = new DatePickerFragment();
        //newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void onCalculateClicked(View v) {
        storeSettings();
        //if(saveCheckBox.isChecked()) {
            saveValues();
        //}
    }

    void loadSettings() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long timeInMilliSec = c.getTimeInMillis();
        prevDate = timeInMilliSec;//sharedPref.getLong("PREV_DATE", timeInMilliSec);
        showDate(prevDateTextView, prevDate);

        currentUserIdx = sharedPref.getInt("CURRENT_USER_IDX", 0);
        //float tmp = sharedPref.getFloat("PREV_READING", 0.0f);
        //prevReadTextEdit.setText(String.format("%2.2f", tmp));
        //boolean checked = sharedPref.getBoolean("SAVE_VALUES", true);

        if(currentUserIdx<0 || currentUserIdx>=usersList.size()) {
            currentUserIdx =0;
        }
        usersSpinner.setSelection(currentUserIdx);
        user_info usr = usersList.get(currentUserIdx);
        //usr.storedReading = Float.parseFloat(prevReadTextEdit.getText().toString());
        user_info usr2 = dbHandler.loadSettings(usr.name);
        prevReadTextEdit.setText(String.format("%2.2f", usr2.storedReading));
        notesEditText.setText(usr2.storedNote);
    }

    void storeSettings() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("CURRENT_USER_IDX", currentUserIdx);
        //editor.putLong("PREV_DATE", prevDate);
        //editor.putFloat("PREV_READING", Float.parseFloat(prevReadTextEdit.getText().toString()));
        editor.apply();
        user_info usr = usersList.get(currentUserIdx);
        usr.storedReading = Float.parseFloat(prevReadTextEdit.getText().toString());
        usr.storedNote = notesEditText.getText().toString();
        dbHandler.storeSettings(usr);
    }

    void saveValues(){
        elec_info eInfo = new elec_info();
        String tmp = dateSelectionSpinner.getSelectedItem().toString();
        if(tmp.equals("Now")) {
            eInfo.prevDateInMilliSec = getNow();
        }else if(tmp.equals("Yesterday")) {
            eInfo.prevDateInMilliSec = getNow() - 86400000L;
        }else {
            eInfo.prevDateInMilliSec = prevDate;
        }
        //eInfo.nextDateInMilliSec = nextDate;
        eInfo.prevReading = Float.parseFloat(prevReadTextEdit.getText().toString());
        eInfo.note = notesEditText.getText().toString();
        //eInfo.nextReading = Long.parseLong(nextReadTextEdit.getText().toString());
        //eInfo.price = String.format("%.0f",price);
        //eInfo.calculationString = calculationStr;
        dbHandler.addRecord(eInfo, currentUserIdx);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent theIntent)
    {
        super.onActivityResult(requestCode, resultCode, theIntent);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==2 && theIntent != null)
        {
            elec_info eInfo = theIntent.getParcelableExtra("eInfo");
            prevDate = eInfo.prevDateInMilliSec;
            //nextDate = eInfo.nextDateInMilliSec;
            showDate(prevDateTextView, prevDate);
            //showDate(nextDateTextView, nextDate);

            prevReadTextEdit.setText(String.format("%2.2f", eInfo.prevReading));
            notesEditText.setText(eInfo.note);

        }
    }

    public void onHistoryClicked(View aa) {
        if(dbHandler.getRecordsCount(currentUserIdx) !=0) {
            //local history instead of site history
            Intent myIntent = new Intent(MainActivity.this, InfoListActivity.class);
            MainActivity.this.startActivityForResult(myIntent,2);
        } else {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("History")
                    .setMessage("There are no History records.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    void showGraph(SortType sortType) {
        if(dbHandler.getRecordsCount(currentUserIdx) !=0) {
            //local history instead of site history
            Intent myIntent = new Intent(MainActivity.this, GraphActivity.class);
            Bundle b = new Bundle();
            b.putSerializable("sortType", sortType); //Your id
            myIntent.putExtras(b);
            MainActivity.this.startActivity(myIntent);
        } else {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Graph")
                    .setMessage("There are no records to show.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    void showReadingsPerDay() {
        if(dbHandler.getRecordsCount(currentUserIdx) !=0) {
            //local history instead of site history
            Intent myIntent = new Intent(MainActivity.this, PerDayListActivity.class);
            MainActivity.this.startActivity(myIntent);
        } else {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("History")
                    .setMessage("There are no History records.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    static long get2016Jan1inMSec() {
        final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.set(2016, 0, 1);
        long msec = c.getTimeInMillis();
        return msec;
    }

    public long getNow() {
        long msec = System.currentTimeMillis();
        return msec;
    }

    private void addUser(String name){
        long userId = dbHandler.addUser(name);
        if(userId ==-1){
            //error
            return;
        }
        user_info usr = new user_info();
        usr.name = name;
        usr.id = userId;
        usersList.add(usr);
        spinnerArrayAdapter.notifyDataSetChanged();
        currentUserIdx=usersList.size()-1;
        usersSpinner.setSelection(currentUserIdx);
        user_info usr2 = dbHandler.loadSettings(usr.name);
        prevReadTextEdit.setText(String.format("%2.2f", usr2.storedReading));
        notesEditText.setText(usr2.storedNote);
    }

    public void onAddUserClicked(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add User");
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text = input.getText().toString();
                addUser(m_Text);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private SwitchDateTimeDialogFragment dateTimeFragment;
    private static final String TAG_DATETIME_FRAGMENT = "TAG_DATETIME_FRAGMENT";
    void dateTimePicker_show () {
        final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        if(selectedDateView == prevDateTextView) {
            c.setTimeInMillis(prevDate);
        }
        // Re-init each time
        dateTimeFragment.startAtCalendarView();
        dateTimeFragment.setDefaultDateTime(c.getTime());
        dateTimeFragment.show(getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);
    }
    private void dateTimePicker_init() {
        // Construct SwitchDateTimePicker
        dateTimeFragment = (SwitchDateTimeDialogFragment) getSupportFragmentManager().findFragmentByTag(TAG_DATETIME_FRAGMENT);
        if (dateTimeFragment == null) {
            dateTimeFragment = SwitchDateTimeDialogFragment.newInstance(
                    getString(R.string.label_datetime_dialog),
                    getString(android.R.string.ok),
                    getString(android.R.string.cancel)
                    //,getString(R.string.clean),// Optional
                    //"en"
            );
        }

        // Optionally define a timezone
        dateTimeFragment.setTimeZone(TimeZone.getTimeZone("GMT"));

        // Init format
        final SimpleDateFormat myDateFormat = new SimpleDateFormat("d MMM yyyy HH:mm", java.util.Locale.getDefault());
        // Assign unmodifiable values
        dateTimeFragment.set24HoursMode(true);
        dateTimeFragment.setHighlightAMPMSelection(false);
        dateTimeFragment.setMinimumDateTime(new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime());
        dateTimeFragment.setMaximumDateTime(new GregorianCalendar(2025, Calendar.DECEMBER, 31).getTime());

        // Define new day and month format
        try {
            dateTimeFragment.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("MMMM dd", Locale.getDefault()));
        } catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
            Log.e("DTSwitch", e.getMessage());
        }

        // Set listener for date
        // Or use dateTimeFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
        dateTimeFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonWithNeutralClickListener() {
            @Override
            public void onPositiveButtonClick(Date date) {
                //textView.setText(myDateFormat.format(date));
                // Do something with the time chosen by the user
                long msec = date.getTime();
                if(selectedDateView == prevDateTextView) {
                    prevDate = msec;
                }
                showDate(selectedDateView, msec);
            }

            @Override
            public void onNegativeButtonClick(Date date) {
                // Do nothing
            }

            @Override
            public void onNeutralButtonClick(Date date) {
                // Optional if neutral button does'nt exists
                //textView.setText("");
            }
        });
    }
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            if(selectedDateView == prevDateTextView) {
                c.setTimeInMillis(prevDate);
            }
            int day = c.get(Calendar.DAY_OF_MONTH);
            int month = c.get(Calendar.MONTH);
            int year = c.get(Calendar.YEAR);

            // Create a new instance of TimePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int y, int m, int d) {
            // Do something with the time chosen by the user
            final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            c.set(y, m, d, 0, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
            long msec = c.getTimeInMillis();
            if(selectedDateView == prevDateTextView) {
                prevDate = msec;
            } /*else {
                nextDate = msec;
            }*/
            showDate(selectedDateView, msec);
        }
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            c.set(arg1, arg2, arg3);
            long msec = c.getTimeInMillis();
            showDate(selectedDateView, msec);
        }
    };

    static void showDate(TextView dateView, long msec) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTimeInMillis(msec);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        month++;
        dateView.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }
    private String _path;
    public void importDbFromFile() {

        new ChooserDialog().with(this)
                .withStartFile(_path)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        importDB(path);
                        //Toast.makeText(MainActivity.this, "FILE: " + path, Toast.LENGTH_SHORT).show();
                        _path = path;

                    }
                })
                .build()
                .show();
    }

    public static String getTime() {
        String timezone="GMT+3";

        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        Date date = c.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd");
        String strDate = df.format(date);
        return strDate;
    }

    public static String getFileNameWithoutExtension(String _path, String _pathSeperator, String _extensionSeperator) {
        try {
            int dot = _path.lastIndexOf(_extensionSeperator);
            int sep = _path.lastIndexOf(_pathSeperator);
            return _path.substring(sep + 1, dot);
        } catch (Exception ex) {
            return "Unknown";
        }
    }

    final private int MY_PERMISSIONS_REQUEST = 100;
    private int fn; //1=import, 2=export

    private void importDB(String backupDB){
        fn = 1;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST);
        }

        //File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source=null;
        FileChannel destination=null;
        String tmp = this.getPackageName();
        String currentDBPath = "/data/"+ tmp +"/databases/"+databaseHandler.DATABASE_NAME;
        //String backupDBPath = databaseHandler.DATABASE_NAME;
        File currentDB = new File(data, currentDBPath);
        //File backupDB = new File(sd, backupDBPath);
        try {
            source = new FileInputStream(backupDB).getChannel();
            destination = new FileOutputStream(currentDB).getChannel();
            long s = source.size();
            destination.transferFrom(source, 0, s);
            source.close();
            destination.close();
            Toast.makeText(this, "DB Imported!", Toast.LENGTH_LONG).show();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void exportDB(){
        fn = 2;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST);
            return;
        }
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source=null;
        FileChannel destination=null;
        String tmp = this.getPackageName();
        String currentDBPath = "/data/"+ tmp +"/databases/"+databaseHandler.DATABASE_NAME;
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        String formatted = format1.format(currentTime.getTime());

        String backupDBPath = getFileNameWithoutExtension(databaseHandler.DATABASE_NAME,"\\", ".");
        backupDBPath = backupDBPath + "_"+getTime();
        String extension = databaseHandler.DATABASE_NAME.substring(databaseHandler.DATABASE_NAME.lastIndexOf("."));
        backupDBPath = backupDBPath + extension;
                File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Toast.makeText(this, "DB Exported!"+backupDB.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if(fn == 1) importDbFromFile();
                    else if(fn==2) exportDB();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    fn=0;
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
            default:
                fn = 0;
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
