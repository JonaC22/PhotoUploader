package lolawebshop.photouploader;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.cloudinary.Cloudinary;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.sql.Connection;

public class PhotoUploader extends Activity {

    private static int RESULT_LOAD_IMAGE = 1;

    Cloudinary cloudinary;
    Connection c;
    PreparedStatement query = null;

    private static Connection getConnection(String databaseURL) throws URISyntaxException, SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        URI dbUri = new URI(databaseURL);
        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() +
                dbUri.getPath() + "?user="+ username +"&password=" +
                password + "&ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";

        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(dbUrl);
    }

    private void initConnections(){
        String cloudinaryURL = null;
        String postgresqlURL = null;

        try {
            Bundle bundle = getPackageManager()
                    .getApplicationInfo( getPackageName(), PackageManager.GET_META_DATA)
                    .metaData;
            cloudinaryURL = bundle.getString("CLOUDINARY_URL");
            postgresqlURL = bundle.getString("POSTGRESQL_URL");

        } catch (PackageManager.NameNotFoundException e) {
            // fall-thru
        } catch (NullPointerException e) {
            // fall-thru
        }
        if (cloudinaryURL == null || postgresqlURL == null) {
            throw new RuntimeException("Couldn't load meta-data from manifest");
        }

        cloudinary = new Cloudinary(cloudinaryURL);

        Log.i("CLOUDINARY", "Conectado");

        try {
            c = getConnection(postgresqlURL);
            Log.i("POSTGRESQL", "Conectado");
        } catch (Exception e) {
            Log.e("POSTGRESQL", "ERROR! " + e.getMessage(), e);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);
        initConnections();
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

            Map upload = null;
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            ImageView imageView = (ImageView) findViewById(R.id.imgView);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            try {
                File foto = new File(picturePath);
                upload = cloudinary.uploader().upload(foto, Collections.emptyMap());
                Log.i("CLOUDINARY", "La imagen se subió correctamente " + upload.toString());
                Toast.makeText(getApplicationContext(), "OK, imagen subida", Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Log.e("CLOUDINARY","La aplicacion respondio de una forma inesperada",e);
                Toast.makeText(getApplicationContext(),"ERROR. See logcat", Toast.LENGTH_LONG).show();
            }

            try{
                String titulo= "ejemploTitulo";
                String proveedor = "ejemploProveedor";
                int categoria = 1;
                String imagen = upload.get("public_id").toString();

                query = c.prepareStatement("INSERT INTO lola.productos (titulo, proveedor, categoria, imagen) VALUES (?,?,?,?)");
                query.setString(1,titulo);
                query.setString(2, proveedor);
                query.setInt(3, categoria);
                query.setString(4, imagen);

                int retorno = query.executeUpdate();
                if (retorno>0)
                    Log.i("POSTGRESQL","Insertado correctamente");

            } catch (SQLException sqle){
                Log.e("POSTGRESQL", "SQLState: "
                        + sqle.getSQLState() + " SQLErrorCode: "
                        + sqle.getErrorCode(), sqle);
            } catch (Exception e){
                Log.e("POSTGRESQL", "ERROR ", e);
            } finally {
                if (c != null) {
                    try{
                        query.close();
                    } catch(Exception e){
                        Log.e("POSTGRESQL", "ERROR: Closing query ", e);
                    }
                }
            }
        }
    }
}