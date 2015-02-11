package lolawebshop.photouploader;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.cloudinary.Cloudinary;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by Jonathan on 08/02/15.
 *
 * Singleton class para manejar las conexiones a la base de datos y a la API de Cloudinary
 */
public class ConnectionManager extends AsyncTask<String, Void, String> {

    String status = " ";
    FragmentActivity activity;
    String cloudinaryURL;
    String postgresqlURL;
    Cloudinary cloudinary;
    Connection c;
    private static ConnectionManager instance = null;

    protected ConnectionManager() {
        //Existe solo para instanciar
    }

    public static ConnectionManager getInstance() {
        if(instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    @Override
    protected String doInBackground(String... array){

        getConnectionStrings();

        cloudinary = new Cloudinary(cloudinaryURL);

        Log.i("CLOUDINARY", "Conectado");

        initConnection(postgresqlURL);

        Log.i("POSTGRESQL", "Conectado");

        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        Log.i("Background Thread", "task executed");
        Toast.makeText(activity.getApplicationContext(), status, Toast.LENGTH_LONG).show();
    }

    public void initConnection(String databaseURL) {
        try {
            URI dbUri = new URI(databaseURL);
            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() +
                    dbUri.getPath() + "?user=" + username + "&password=" +
                    password + "&ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection(dbUrl);
            status = "Conexiones establecidas";
        } catch (Exception e){
            Log.e("POSTGRESQL", "ERROR! " + e.getMessage(), e);
            status = "Problema en conexiones";
        }
    }

    public void setActivity(FragmentActivity activity) {
        this.activity = activity;
    }

    private void getConnectionStrings(){
        try {
            Bundle bundle = activity.getPackageManager()
                    .getApplicationInfo( activity.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData;
            cloudinaryURL = bundle.getString("CLOUDINARY_URL");
            postgresqlURL = bundle.getString("POSTGRESQL_URL");

        } catch (Exception e) {
            Log.e("PackageManager", "ERROR! " + e.getMessage(), e);
        }

        if (cloudinaryURL == null || postgresqlURL == null) {
            throw new RuntimeException("Couldn't load meta-data from manifest");
        }
    }
}