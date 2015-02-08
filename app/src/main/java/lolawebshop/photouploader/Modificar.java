package lolawebshop.photouploader;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jonathan on 29/01/15.
 */
public class Modificar extends Fragment {

    ConnectionManager task;

    @Override

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        //TODO: reutilizar las conexiones para los otros Tabs
        Log.i("Background Thread", "init task");

        task = new ConnectionManager();
        task.setActivity(getActivity());
        task.execute();
        Log.i("Background Thread", "executing task");

    }

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,

                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.tab2, container, false);

    }

}