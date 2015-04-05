package slickstudios.sa.attendancemonitor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by Darshan on 24-03-2015.
 */
public class SplashActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        Thread timer=new Thread(){
            public void run(){
                try{
                    sleep(1500);

                } catch(InterruptedException e){
                    e.printStackTrace();

                } finally{
                    Intent i=new Intent(SplashActivity.this,MainActivity.class);
                    startActivity(i);
                }
            }
        };
        timer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
