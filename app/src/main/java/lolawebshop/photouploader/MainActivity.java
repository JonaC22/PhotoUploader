package lolawebshop.photouploader;

/**
 * Created by jonathan on 29/01/15.
 */
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
        FragmentTabHost tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
            tabHost.setup(this,
                    getSupportFragmentManager(), android.R.id.tabcontent);
            tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("Lengüeta 1"),
                    PhotoUploader.class, null);
            tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("Lengüeta 2"),
                    Tab2.class, null);
            tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("Lengüeta 3"),
                    Tab3.class, null);
    }
}