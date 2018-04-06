package com.bikcrum.facedetectioncrop;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "facedetect";
    private static final int RESULT_LOAD_IMG = 12;
    private ImageView imageView;
    private FaceDetectionCrop faceDetectionCrop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image_view);
    }

    public void choosePhoto(View view) {

        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                if (imageUri == null) {
                    Toast.makeText(this, "Something went wrong. Upload again", Toast.LENGTH_LONG).show();
                    return;
                }
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                faceDetectionCrop = FaceDetectionCrop.initialize(MainActivity.this, selectedImage);

                //get bitmap which has guidelines showing faces and crop regions
                Bitmap bitmap = faceDetectionCrop.getDetectionGuideLines();

                imageView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));

                findViewById(R.id.cropImageBtn).setVisibility(View.VISIBLE);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong. Upload again", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }

    public void cropPhoto(View view) {
        if (faceDetectionCrop == null) {
            Toast.makeText(this, "Something went wrong. Upload again", Toast.LENGTH_LONG).show();
            return;
        }

        //get cropped bitmap in square form containing face (if exist)
        Bitmap bitmap = faceDetectionCrop.getFaceCroppedBitmap();

        imageView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
    }
}
