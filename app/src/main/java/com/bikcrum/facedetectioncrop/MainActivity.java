package com.bikcrum.facedetectioncrop;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTarget;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "facedetect";
    private static final int RESULT_LOAD_IMG = 12;
    private ImageView imageView;
    private LinearLayout progress;
    private FaceDetectionCrop faceDetectionCrop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image_view);

        progress = findViewById(R.id.progress);
        progress.setVisibility(View.GONE);
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

            final Uri imageUri = data.getData();
            if (imageUri == null) {
                Toast.makeText(this, "Something went wrong. Upload again", Toast.LENGTH_SHORT).show();
                return;
            }
            Glide.with(this).load(imageUri).asBitmap().into(new ImageViewTarget<Bitmap>(imageView) {
                @Override
                protected void setResource(Bitmap resource) {
                    //simply set what user picked up
                    imageView.setImageBitmap(resource);

                    //now send bitmap for processing for face detection and crop
                    processImage(resource);
                }
            });
        } else {
            Toast.makeText(this, "You didn't pick Image", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * It is recommended to process image asynchronously to prevent ui lock
     *
     * @param selectedImage bitmap that user picked
     */
    private void processImage(final Bitmap selectedImage) {
        //initialize face detection and crop in asynctask
        new ProcessImage().execute(selectedImage);
    }


    private class ProcessImage extends AsyncTask<Bitmap, Void, Void> {
        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Bitmap... bitmaps) {

            //initialize the bitmap
            faceDetectionCrop = FaceDetectionCrop.initialize(MainActivity.this, bitmaps[0]);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //get bitmap which has guidelines showing faces and crop regions
            Bitmap bitmap = faceDetectionCrop.getDetectionGuideLines();
            imageView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));

            //once image is loaded stop progress and show ability to crop
            findViewById(R.id.cropImageBtn).setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            super.onPostExecute(aVoid);
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
