package lolawebshop.photouploader;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Subir extends Fragment {

    private int RESULT_LOAD_IMAGE = 1;
    private boolean SECURE_UPLOAD = true;
    String picturePath = null;
    ConnectionManager task;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        
        super.onActivityCreated(savedInstanceState);

        task = ConnectionManager.getInstance();
        
        //Boton seleccion de imagen
        Button buttonLoadImage = (Button) getView().findViewById(R.id.buttonLoadPicture);

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

        Button buttonUpload = (Button) getActivity().findViewById(R.id.buttonUpload);
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

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,

                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.main, container, false);
    }

    private class UploadManager extends AsyncTask<String, Void, String> {
        
        String status = " ";

        @Override
        protected String doInBackground(String... array){
            Map upload = null;
            
            try {
                File foto = new File(picturePath);
                upload = task.cloudinary.uploader().upload(foto, Collections.emptyMap());
                Log.i("CLOUDINARY", "La imagen se subió correctamente " + upload.toString());
            } catch (Exception e) {
                Log.e("CLOUDINARY", "La aplicacion respondio de una forma inesperada", e);
                Toast.makeText(getActivity().getApplicationContext(), "ERROR. See logcat", Toast.LENGTH_LONG).show();
            }

            EditText tituloEditText = (EditText) getActivity().findViewById(R.id.tituloEditText);
            EditText provEditText = (EditText) getActivity().findViewById(R.id.provEditText);
            Spinner spinnerCategoria = (Spinner) getActivity().findViewById(R.id.spinnerCategoria);

            String titulo = tituloEditText.getText().toString();
            String proveedor = provEditText.getText().toString();
            int categoria = getCategorias().get(spinnerCategoria.getSelectedItem().toString());

            insertarPostgreSQL(titulo, proveedor, categoria, upload);
            return " ";
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getActivity().getApplicationContext(), status, Toast.LENGTH_LONG).show();
        }

        public void insertarPostgreSQL(String titulo, String proveedor, int categoria, Map upload) {

            if (titulo != null && proveedor != null && upload != null) {
                PreparedStatement query = null;
                try {
                    String imagen = upload.get("public_id").toString();
                    query = task.c.prepareStatement("INSERT INTO lola.productos (titulo, proveedor, categoria, imagen) VALUES (?,?,?,?)");
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
    
    private void habilitarSubirImagen(boolean valor){

        SECURE_UPLOAD = valor;
        Button buttonUpload = (Button) getActivity().findViewById(R.id.buttonUpload);
        buttonUpload.setEnabled(valor);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        getActivity();
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor;
            cursor = getActivity().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();
            ImageView imageView = (ImageView) getActivity().findViewById(R.id.imgView);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            
            habilitarSubirImagen(true);
        }
    }
}