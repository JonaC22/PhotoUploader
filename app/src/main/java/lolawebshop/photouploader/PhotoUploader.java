package lolawebshop.photouploader;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import java.util.Collections;
import java.util.Map;

public class PhotoUploader extends ActionBarActivity {

    private static int RESULT_LOAD_IMAGE = 1;
    private boolean SECURE_UPLOAD = true;
    Cloudinary cloudinary;
    String picturePath = null;
    Map<String,Integer> categorias = null;
    String cloudinaryURL = null;
    String postgresqlURL = null;

    private void initConnections(){

        getConnectionStrings();
        
        cloudinary = new Cloudinary(cloudinaryURL);

        Log.i("CLOUDINARY", "Conectado");

        DBManager.makeConnection(postgresqlURL);

        Log.i("POSTGRESQL", "Conectado");
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initConnections();
        categorias = DBManager.getCategorias();
        
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
                //TODO agregar un semaforo o monitor para el testAndSet del SECURE_UPLOAD
                if (SECURE_UPLOAD) {
                    
                    SECURE_UPLOAD = false;
                    Button buttonUpload = (Button) findViewById(R.id.buttonUpload);
                    buttonUpload.setEnabled(false);
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
                    int categoria = categorias.get(spinnerCategoria.getSelectedItem().toString());

                    DBManager.insertarPostgreSQL(titulo, proveedor, categoria, upload);
                    Toast.makeText(getApplicationContext(), "La foto se subio correctamente", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
            Button buttonUpload = (Button) findViewById(R.id.buttonUpload);
            buttonUpload.setEnabled(true); 
            SECURE_UPLOAD = true;
        }
    }
}