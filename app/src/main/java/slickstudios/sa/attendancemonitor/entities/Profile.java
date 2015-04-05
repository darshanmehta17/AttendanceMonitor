package slickstudios.sa.attendancemonitor.entities;

import com.orm.SugarRecord;

/**
 * Created by Darshan on 24-03-2015.
 */
public class Profile extends SugarRecord<Profile> {
    protected String name,regno;

    public Profile(){}

    public Profile(String name,String regno){
        this.name=name;
        this.regno=regno;
    }

    public String getName() {
        return name;
    }

    public String getRegno() {
        return regno;
    }
}
