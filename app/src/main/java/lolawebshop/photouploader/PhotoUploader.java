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
import java.util.Collections;
import java.util.Map;

public class PhotoUploader extends Activity {

    private static int RESULT_LOAD_IMAGE = 1;

    Cloudinary cloudinary;

    private void initCloudinary(){
        String apiURL = null;

        try {
            Bundle bundle = getPackageManager()
                    .getApplicationInfo( getPackageName(), PackageManager.GET_META_DATA)
                    .metaData;
            apiURL = bundle.getString("CLOUDINARY_URL");
        } catch (PackageManager.NameNotFoundException e) {
            // fall-thru
        } catch (NullPointerException e) {
            // fall-thru
        }
        if (apiURL == null) {
            throw new RuntimeException("Couldn't load cloudinary meta-data from manifest");
        }

        cloudinary = new Cloudinary(apiURL);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);
        initCloudinary();
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
                Map upload = cloudinary.uploader().upload(foto, Collections.emptyMap());
                Toast.makeText(getApplicationContext(), "OK, imagen subida", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e("Cloudinary Upload","La aplicacion respondio de una forma inesperada",e);
                Toast.makeText(getApplicationContext(),"ERROR. See logcat", Toast.LENGTH_LONG).show();
            }
        }
    }
}