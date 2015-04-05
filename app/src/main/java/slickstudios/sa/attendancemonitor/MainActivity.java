package slickstudios.sa.attendancemonitor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.doomonafireball.betterpickers.datepicker.DatePickerBuilder;
import com.doomonafireball.betterpickers.datepicker.DatePickerDialogFragment;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import slickstudios.sa.attendancemonitor.entities.Attendance;
import slickstudios.sa.attendancemonitor.entities.Profile;

/**
 * Created by Darshan on 24-03-2015.
 */

public class MainActivity extends ActionBarActivity implements View.OnClickListener,DatePickerDialogFragment.DatePickerDialogHandler {

    Toolbar toolbar;
    Button bDatePicker;
    ImageButton ibBarcode;
    RecyclerView recyclerView;
    AttendanceListAdapter adapter;
    LinearLayout llEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVars();
    }

    private void initVars() {
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bDatePicker=(Button)findViewById(R.id.bDatePicker);
        bDatePicker.setOnClickListener(this);
        ibBarcode=(ImageButton)findViewById(R.id.fab_barcode);
        ibBarcode.setOnClickListener(this);

        llEmpty=(LinearLayout)findViewById(R.id.llEmpty);
        recyclerView=(RecyclerView)findViewById(R.id.rvAttList);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm=new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        adapter=new AttendanceListAdapter(getTodayList());
        recyclerView.setAdapter(adapter);
    }

    private List<ListItem> getTodayList() {
        String date=getCurrentDate();
        bDatePicker.setText(date);
        List<ListItem> items= getList(date);
        switchView(items.size());
        return items;
    }

    private String getCurrentDate() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        return getDate(mYear,mMonth+1,mDay);
    }

    private void switchView(int size) {
        if(size==0){
            recyclerView.setVisibility(View.GONE);
            llEmpty.setVisibility(View.VISIBLE);
        }
        else{
            recyclerView.setVisibility(View.VISIBLE);
            llEmpty.setVisibility(View.GONE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent intent=new Intent(this,About.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_export) {
            Intent intent=new Intent(this,Export.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bDatePicker:
                DatePickerBuilder dpb = new DatePickerBuilder()
                        .setFragmentManager(getSupportFragmentManager())
                        .setStyleResId(R.style.MyCustomBetterPickerTheme);
                dpb.show();
                break;
            case R.id.fab_barcode:
                //instantiate ZXing integration class
                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                //start scanning
                scanIntegrator.initiateScan();
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
            //retrieve result of scanning - instantiate ZXing object
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            //check we have a valid result
            if (scanningResult != null) {
                //get content from Intent Result
                final String scanContent = scanningResult.getContents();
                //output to UI
                if (scanContent != null) {
                    Profile profileOfPerson=checkData(scanContent);
                    if(profileOfPerson!=null){
                        Attendance attendance=new Attendance(profileOfPerson,getCurrentDate(),getCurrentTime());
                        attendance.save();
                        refreshToToday();
                    }else{
                        final EditText editText=new EditText(this);
                        editText.setHint("Enter the name");
                        editText.setTextColor(getResources().getColor(R.color.text_secondary));
                        AlertDialog.Builder builder=new AlertDialog.Builder(this);
                        builder.setTitle(scanContent).setView(editText);
                        builder.setPositiveButton("Save",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name=editText.getText().toString();
                                if(name!=null){
                                    addToDataBaseAndUpdate(name,scanContent);
                                }else{
                                    Toast.makeText(MainActivity.this,"Invalid name. Scan again!",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        builder.setNegativeButton("Cancel",null);
                        builder.show();
                    }
                }
            } else {
                //invalid scan data or scan canceled
                Toast toast = Toast.makeText(getApplicationContext(),
                        "No scan data received!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }

    private void addToDataBaseAndUpdate(String name, String scanContent) {
        Profile profile=new Profile(name,scanContent);
        profile.save();
        Attendance attendance=new Attendance(profile,getCurrentDate(),getCurrentTime());
        attendance.save();
        refreshToToday();
    }

    private void refreshToToday() {
        //Resets the attendance list to today

        String date=getCurrentDate();
        bDatePicker.setText(date);
        List<ListItem> items=getList(date);
        changeList(items);
        switchView(items.size());
    }

    private String getCurrentTime() {
        //Returns cuurent calendar time

        Calendar mcurrentTime = Calendar.getInstance();
        int mHour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int mMinute = mcurrentTime.get(Calendar.MINUTE);
        return (mHour+":"+mMinute);
    }

    private Profile checkData(String scanContent) {
        // Checks if the person scanned is already in the database

        List<Profile> profiles=Profile.listAll(Profile.class);
        for(Profile p:profiles){
            if(p.getRegno().contentEquals(scanContent))
                return p;
        }
        return null;
    }

    @Override
    public void onDialogDateSet(int i, int year, int month, int day) {
        String date=getDate(year,month-i,day);
        bDatePicker.setText(date);
        List<ListItem> items=getList(date);
        changeList(items);
        switchView(items.size());
    }

    private List<ListItem> getList(String date) {
        // Returns a list of attendance on a given date

        List<Attendance> attendances=Attendance.find(Attendance.class,"date = ?",date);
        List<ListItem> items=new ArrayList<>();
        for(Attendance i:attendances){
            items.add(new ListItem(i.getProfile().getName(),i.getTime()));
        }
        return items;
    }

    private String getDate(int year, int month, int day) {
        return (day+"/"+month+"/"+year);
    }

    private void changeList(List<ListItem> items){
        adapter=new AttendanceListAdapter(items);
        recyclerView.setAdapter(adapter);
    }

}
