package lolawebshop.photouploader;

/**
 * Created by jonathan on 29/01/15.
 */
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initConnectionManager();

        FragmentTabHost tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
            tabHost.setup(this,
                    getSupportFragmentManager(), android.R.id.tabcontent);
            tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("Subir"),
                    Subir.class, null);
            tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("Modificar"),
                    Modificar.class, null);
            tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("Eliminar"),
                    Eliminar.class, null);
    }

    private void initConnectionManager() {
        Log.i("Background Thread", "init task");
        ConnectionManager task = ConnectionManager.getInstance();
        task.setActivity(this);
        task.execute();
        Log.i("Background Thread", "executing task");
    }
}