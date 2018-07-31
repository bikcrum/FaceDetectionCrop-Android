/*
 * Copyright 2018 Bikram Pandit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bikcrum.facedetectioncrop;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v4.math.MathUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class FaceDetectionCrop {
    private Context context;
    private Bitmap bitmap;
    private SparseArray<Face> faces;
    private RectF boundary;
    private RectF cropArea;



    /**
     * Constructor to initialize facedetector
     *
     * @param context
     * @param bitmap
     */
    private FaceDetectionCrop(Context context, Bitmap bitmap) {
        this.bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        this.context = context;
        initialize();
    }

    /**
     * Initialize function that builds facedetector with bitmap provided. Create frames for each faces, boundary covering all faces and square crop area.
     */
    private void initialize() {
        //bitmap should not null
        if (bitmap == null) {
            throw new RuntimeException("Bitmap should not be null");
        }

        //context should not be null
        if (context == null) {
            throw new RuntimeException("Context should not be null");
        }

        //init facedetector using google vision api
        FaceDetector faceDetector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .build();

        //creating frame for bitmap
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();

        //detect all faces in the frame
        faces = faceDetector.detect(frame);

        //release facedetector
        faceDetector.release();

        //init boundary that will cover all faces in it
        boundary = new RectF();

        //for each faces do this
        for (int i = 0; i < faces.size(); i++) {
            //get face at index i
            Face thisFace = faces.valueAt(i);

            //get face boundary
            float x1 = thisFace.getPosition().x;
            float y1 = thisFace.getPosition().y;
            float x2 = x1 + thisFace.getWidth();
            float y2 = y1 + thisFace.getHeight();

            //for first loop init boundary with same as first face boundary
            if (i == 0) {
                boundary.left = x1;
                boundary.top = y1;
                boundary.right = x2;
                boundary.bottom = y2;
            } else {
                //for rest of the loops change boundary according to face boundary so it covers all faces
                boundary.left = Math.min(boundary.left, x1);
                boundary.top = Math.min(boundary.top, y1);
                boundary.right = Math.max(boundary.right, x2);
                boundary.bottom = Math.max(boundary.bottom, y2);
            }

        }

        //get size as minimum of width and height of bitmap because we can square image
        float bitmapSize = Math.min(this.bitmap.getWidth(), bitmap.getHeight());

        //crop area is square frame equal to result bitmap size
        cropArea = new RectF(0, 0, bitmapSize, bitmapSize);

        //startX and startY is offset to change cropArea such that result cropArea will contain face(s)
        float startX = MathUtils.clamp(
                boundary.centerX() - bitmapSize / 2.0f,
                0,
                bitmap.getWidth() - bitmapSize
        );

        float startY = MathUtils.clamp(
                boundary.centerY() - bitmapSize / 2.0f,
                0,
                bitmap.getHeight() - bitmapSize
        );

        //change offset to calculated startX and startY
        cropArea.offsetTo(startX, startY);
    }

    /**
     * You must call this function before any methods of FaceDetectionCrop object.
     *
     * @param context The context
     * @param bitmap  Pass the bitmap that is to be processed for detection
     * @return Get process bitmap that is ready to be face detected and cropped
     */
    public static synchronized FaceDetectionCrop initialize(Context context, Bitmap bitmap) {
        return new FaceDetectionCrop(context, bitmap);
    }

    /**
     * If bitmap is required to changed after initialization
     *
     * @param bitmap Passing the new bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        //bitmap can't be null
        if (bitmap == null) {
            throw new RuntimeException("Bitmap should not be null");
        }
        this.bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        initialize();
    }

    /**
     * Check if bitmap has face in it
     *
     * @return true or false
     */
    public boolean hasFace() {
        if (faces == null) {
            throw new RuntimeException("Initialize FaceDetectionCrop using FaceDetectionCrop.initialize() before using it.");
        }
        return faces.size() != 0;
    }

    /**
     * Get number of faces in bitmap
     *
     * @return count
     */
    public int getFaceCount() {
        if (faces == null) {
            throw new RuntimeException("Initialize FaceDetectionCrop using FaceDetectionCrop.initialize() before using it.");
        }
        return faces.size();
    }

    /**
     * Get guidelines for each of the face detected in bitmap, frame that covers all faces and square crop area
     *
     * @return result bitmap
     */
    public Bitmap getDetectionGuideLines() {
        //all of the following should not be null to detect guidelines
        if (bitmap == null || boundary == null || cropArea == null || faces == null) {
            throw new RuntimeException("Initialize FaceDetectionCrop using FaceDetectionCrop.initialize() before using it.");
        }

        //create copy of bitmap so changes will not reflect original bitmap
        Bitmap bitmap1 = Bitmap.createBitmap(bitmap);

        //create canvas
        Canvas c = new Canvas(bitmap1);

        //create paint object
        Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setAntiAlias(true);
        p.setFilterBitmap(true);
        p.setDither(true);


        //draw blue rectangle that contain each face
        p.setColor(Color.BLUE);
        p.setStrokeWidth(dpTopx(8));

        //for each face do following
        for (int i = 0; i < faces.size(); i++) {
            Face thisFace = faces.valueAt(i);

            float x1 = thisFace.getPosition().x;
            float y1 = thisFace.getPosition().y;
            float x2 = x1 + thisFace.getWidth();
            float y2 = y1 + thisFace.getHeight();

            //draw blue color rectangle in each face
            c.drawRect(x1, y1, x2, y2, p);
        }

        //draw red rectangle that contain all faces
        p.setColor(Color.RED);
        p.setStrokeWidth(dpTopx(6));
        c.drawRect(boundary.left, boundary.top, boundary.right, boundary.bottom, p);

        //draw green rectangle that is maximum square rectangle containing all faces
        p.setStrokeWidth(dpTopx(6));
        p.setColor(Color.GREEN);
        c.drawRect(cropArea.left, cropArea.top, cropArea.right, cropArea.bottom, p);

        //return result bitmap
        return bitmap1;
    }

    /**
     * Get cropped bitmap that contains face
     *
     * @return result bitmap
     */
    public Bitmap getFaceCroppedBitmap() {
        //we can't get cropped bitmap is bitmap is null or cropArea is null
        if (bitmap == null || cropArea == null) {
            throw new RuntimeException("Initialize FaceDetectionCrop using FaceDetectionCrop.initialize() before using it.");
        }
        //crop bitmap with calculated cropArea
        return Bitmap.createBitmap(
                bitmap,
                (int) cropArea.left,
                (int) cropArea.top,
                (int) (cropArea.width()),
                (int) (cropArea.height())
        );
    }

    private float dpTopx(int dp) {
        Resources r = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }
}
