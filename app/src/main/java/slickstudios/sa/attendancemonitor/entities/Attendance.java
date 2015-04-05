package slickstudios.sa.attendancemonitor.entities;

import com.orm.SugarRecord;

/**
 * Created by Darshan on 24-03-2015.
 */
public class Attendance extends SugarRecord<Attendance> {

    String date,time;
    Profile profile;

    public Attendance(){}

    public Attendance(Profile profile,String date,String time){
        this.profile=profile;
        this.date=date;
        this.time=time;
    }

    public String getAttDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public Profile getProfile() {
        return profile;
    }
}
