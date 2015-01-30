package lolawebshop.photouploader;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import com.cloudinary.Cloudinary;
import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PhotoUploader extends ActionBarActivity {

    private int RESULT_LOAD_IMAGE = 1;
    private boolean SECURE_UPLOAD = true;
    Cloudinary cloudinary;
    String picturePath = null;
    String cloudinaryURL = null;
    String postgresqlURL = null;
    Connection c;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Log.i("Background Thread", "init task");
        ConnectionManager task = new ConnectionManager();
        task.execute();
        Log.i("Background Thread", "executing task");

        //Boton seleccion de imagen 
        Button buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);

        buttonLoadImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        //Boton subir imagen

        Button buttonUpload = (Button) findViewById(R.id.buttonUpload);
        buttonUpload.setEnabled(false);

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (SECURE_UPLOAD) {

                    habilitarSubirImagen(false);

                    UploadManager uploader = new UploadManager();
                    uploader.execute();
                }
            }
        });
    }

    private class ConnectionManager extends AsyncTask<String, Void, String> {

        String status = " ";

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
            Toast.makeText(getApplicationContext(), status, Toast.LENGTH_LONG).show();
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
    }

    private class UploadManager extends AsyncTask<String, Void, String> {
        
        String status = " ";

        @Override
        protected String doInBackground(String... array){
            Map upload = null;
            
            try {
                File foto = new File(picturePath);
                upload = cloudinary.uploader().upload(foto, Collections.emptyMap());
                Log.i("CLOUDINARY", "La imagen se subió correctamente " + upload.toString());
            } catch (Exception e) {
                Log.e("CLOUDINARY", "La aplicacion respondio de una forma inesperada", e);
                Toast.makeText(getApplicationContext(), "ERROR. See logcat", Toast.LENGTH_LONG).show();
            }

            EditText tituloEditText = (EditText) findViewById(R.id.tituloEditText);
            EditText provEditText = (EditText) findViewById(R.id.provEditText);
            Spinner spinnerCategoria = (Spinner) findViewById(R.id.spinnerCategoria);

            String titulo = tituloEditText.getText().toString();
            String proveedor = provEditText.getText().toString();
            int categoria = getCategorias().get(spinnerCategoria.getSelectedItem().toString());

            insertarPostgreSQL(titulo, proveedor, categoria, upload);
            return " ";
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), status, Toast.LENGTH_LONG).show();
        }

        public void insertarPostgreSQL(String titulo, String proveedor, int categoria, Map upload) {

            if (titulo != null && proveedor != null && upload != null) {
                PreparedStatement query = null;
                try {
                    String imagen = upload.get("public_id").toString();
                    query = c.prepareStatement("INSERT INTO lola.productos (titulo, proveedor, categoria, imagen) VALUES (?,?,?,?)");
                    query.setString(1, titulo);
                    query.setString(2, proveedor);
                    query.setInt(3, categoria);
                    query.setString(4, imagen);

                    int retorno = query.executeUpdate();
                    if (retorno > 0) Log.i("POSTGRESQL", "Insertado correctamente");

                }catch(SQLException sqle){
                    Log.e("POSTGRESQL", "SQLState: "
                            + sqle.getSQLState() + " SQLErrorCode: "
                            + sqle.getErrorCode(), sqle);
                }catch(Exception e){
                    Log.e("POSTGRESQL", "ERROR ", e);
                    status = "Error al subir imagen";
                }finally{
                    if (query != null) {
                        try {
                            query.close();
                            status = "La foto se subio correctamente";
                        } catch (Exception e) {
                            Log.e("POSTGRESQL", "ERROR: Closing query ", e);
                        }
                    }
                }
            }
            else {
                Log.e("FORM", "ERROR: Algun atributo es nulo");
            }
        }

        //TODO reemplazarlo por una consulta a la base de datos de las categorias disponibles
        public Map<String, Integer> getCategorias() {
            Map<String, Integer> categorias = new HashMap<String, Integer>();
            categorias.put("aros", 1);
            categorias.put("billeteras", 2);
            categorias.put("cintos", 3);
            categorias.put("chalinas", 4);
            categorias.put("clutchs", 5);
            categorias.put("collares", 6);
            categorias.put("monederos", 7);
            categorias.put("portacelulares", 8);
            categorias.put("pulseras", 9);
            categorias.put("relojes", 10);
            categorias.put("sombreros", 11);
            categorias.put("anillos", 12);
            return categorias;
        }
    }
    
    private void getConnectionStrings(){
        try {
            Bundle bundle = getPackageManager()
                    .getApplicationInfo( getPackageName(), PackageManager.GET_META_DATA)
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

    
    
    private void habilitarSubirImagen(boolean valor){

        SECURE_UPLOAD = valor;
        Button buttonUpload = (Button) findViewById(R.id.buttonUpload);
        buttonUpload.setEnabled(valor);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();
            ImageView imageView = (ImageView) findViewById(R.id.imgView);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            
            habilitarSubirImagen(true);
        }
    }
}