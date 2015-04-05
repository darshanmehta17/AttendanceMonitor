package slickstudios.sa.attendancemonitor;

import android.app.AlertDialog;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.doomonafireball.betterpickers.datepicker.DatePickerBuilder;
import com.doomonafireball.betterpickers.datepicker.DatePickerDialogFragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import slickstudios.sa.attendancemonitor.entities.Attendance;
import slickstudios.sa.attendancemonitor.entities.Profile;

/**
 * Created by Darshan on 24-03-2015.
 */

public class Export extends ActionBarActivity implements View.OnClickListener,DatePickerDialogFragment.DatePickerDialogHandler {

    Toolbar toolbar;
    Button bFromDate,bToDate,bGenerate;
    Calendar fromDate,toDate;
    final String FILENAME = "Attendance.xls", ABSENT = "Absent", PRESENT = "Present";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        initVars();
    }

    private void initVars() {
        toolbar=(Toolbar)findViewById(R.id.toolbar_export);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bFromDate=(Button)findViewById(R.id.bDatePickerFrom);
        bToDate=(Button)findViewById(R.id.bDatePickerTo);
        bGenerate=(Button)findViewById(R.id.bGenerate);
        bFromDate.setOnClickListener(this);
        bToDate.setOnClickListener(this);
        bGenerate.setOnClickListener(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_export, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bDatePickerFrom:
                DatePickerBuilder dpbfrom = new DatePickerBuilder()
                        .setFragmentManager(getSupportFragmentManager())
                        .setReference(R.id.bDatePickerFrom)
                        .setStyleResId(R.style.MyCustomBetterPickerTheme);
                dpbfrom.show();
                break;
            case R.id.bDatePickerTo:
                DatePickerBuilder dpbto = new DatePickerBuilder()
                        .setFragmentManager(getSupportFragmentManager())
                        .addDatePickerDialogHandler(Export.this)
                        .setReference(R.id.bDatePickerTo)
                        .setStyleResId(R.style.MyCustomBetterPickerTheme);
                dpbto.show();
                break;
            case R.id.bGenerate:
                generateExcel();
                break;
        }
    }

    private void generateExcel() {
        if(fromDate!=null && toDate!=null && toDate.after(fromDate)) {


            ProgressBar progressBar=new ProgressBar(this);
            progressBar.setIndeterminate(true);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(progressBar)
                    .setCancelable(false)
                    .setTitle("Generating...");
            AlertDialog dialog=builder.show();


            File file = getFile();
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("en", "EN"));
            WritableWorkbook workbook;
            try{
                workbook = Workbook.createWorkbook(file, wbSettings);
                //Excel sheet name. 0 represents first sheet
                WritableSheet sheet = workbook.createSheet("AttendanceList", 0);
                try{

                    sheet.addCell(new Label(0, 0, "Registration number")); // column and row
                    Calendar calendarFrom=fromDate,calendarTo=toDate;
                    calendarTo.add(Calendar.DAY_OF_MONTH,1);
                    int columnNumber=1,columnCount=0,rowNumber=1,rowCount=0;
                    List<String> dates = new ArrayList<>();


                    while(!(calendarFrom.compareTo(calendarTo)==0)) {  //adds dates
                        String date = getDateFromCalendar(calendarFrom);
                        dates.add(date);
                        sheet.addCell(new Label(columnNumber, 0, date));
                        columnNumber++;
                        columnCount++;
                        calendarFrom.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    List<Profile> profiles = Profile.listAll(Profile.class);
                    for (Profile p : profiles) {    //adds registration numbers
                        sheet.addCell(new Label(0, rowNumber, p.getRegno()));
                        rowNumber++;
                        rowCount++;
                    }

                    for(columnNumber=1;columnNumber<=columnCount;columnNumber++){   //adds attendance

                        List<String> students=new ArrayList<>();
                        List<Attendance> attendances = getAttendance(dates.get(columnNumber-1));

                        for (Attendance attendance : attendances) {
                            students.add(attendance.getProfile().getRegno());
                        }

                        for (rowNumber=1;rowNumber<=rowCount;rowNumber++){
                            boolean flag = false;
                            for (String student : students) {
                                if (student.contentEquals(profiles.get(rowNumber-1).getRegno())) {
                                    sheet.addCell(new Label(columnNumber, rowNumber, PRESENT));
                                    flag = true;
                                    break;
                                }
                            }
                            if (!flag)
                                sheet.addCell(new Label(columnNumber, rowNumber, ABSENT));
                        }
                    }

                } catch (RowsExceededException e) {
                    e.printStackTrace();
                } catch (WriteException e) {
                    e.printStackTrace();
                }
                workbook.write();
                try {
                    workbook.close();
                } catch (WriteException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(dialog.isShowing()){
                dialog.dismiss();
                Toast.makeText(this,"Excel file named Attendance.xls exported to AttendanceMonitor folder in SDcard ",Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(this,"Choose appropriate limits for the dates",Toast.LENGTH_LONG).show();
        }
    }

    private List<Attendance> getAttendance(String date) {
        return Attendance.find(Attendance.class,"date = ?",date);
    }

    private File getFile() {
        //Saving file in external storage
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/AttendanceMonitor");

        //create directory if not exist
        if(!directory.isDirectory()){
            directory.mkdirs();
        }

        //file path
        return new File(directory, FILENAME);

    }


    private String getDate(int year, int month, int day) {
        return (day+"/"+month+"/"+year);
    }

    private Calendar getCalendar(int year, int month, int day) {
        Calendar calendar=Calendar.getInstance();
        calendar.clear();
        calendar.set(year,month,day);
        return calendar;
    }

    @Override
    public void onDialogDateSet(int reference, int year, int month, int day) {
        switch (reference){
            case R.id.bDatePickerFrom:
                bFromDate.setText(getDate(year,month+1,day));
                fromDate=getCalendar(year,month,day);
                break;
            case R.id.bDatePickerTo:
                bToDate.setText(getDate(year,month+1,day));
                toDate=getCalendar(year, month, day);
                break;
        }

    }

    private String getDateFromCalendar(Calendar calendar){
        return calendar.get(Calendar.DAY_OF_MONTH)+"/"+(calendar.get(Calendar.MONTH)+1)+"/"+calendar.get(Calendar.YEAR);
    }

}
