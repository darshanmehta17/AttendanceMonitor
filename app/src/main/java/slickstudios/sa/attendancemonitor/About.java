package slickstudios.sa.attendancemonitor;

import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

/**
 * Created by Darshan on 24-03-2015.
 */
public class About extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        TextView tvAboutDev=(TextView)findViewById(R.id.tvAboutDeveloper);
        Linkify.addLinks(tvAboutDev,Linkify.ALL);
    }
}
