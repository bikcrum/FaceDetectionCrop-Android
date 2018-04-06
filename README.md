# FaceDetectionCrop-Android
An android library for detecting face in the image, and identify the crop area.

#### Step 1: Add it in your root build.gradle at the end of repositories:
```
allprojects {
  repositories {
    ...
	  maven { url 'https://jitpack.io' }
  }
}
```
  
#### Step 2. Add the dependency
```
dependencies {
  compile 'com.github.bikcrum:FaceDetectionCrop-Android:1.0'
}
```

#### Step 3. Initialize FaceDetectionCrop Object
```
FaceDetectionCropfaceDetectionCrop = FaceDetectionCrop.initialize(MainActivity.this, selectedImage);
```

#### Step 4. Get guidelines showing face regions, frames and area to crop in square form
```
Bitmap bitmap = faceDetectionCrop.getDetectionGuideLines();
```

#### Step 5. Get guidelines showing face regions, frames and area to crop in square form
```
Bitmap bitmap = faceDetectionCrop.getDetectionGuideLines();

// see how guideline bitmap looks by loading it in imageview
imageView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
```
#### Step 6: Get cropped bitmap in square form containing face
```
Bitmap bitmap = faceDetectionCrop.getFaceCroppedBitmap();

// see how cropped image looks by loading it in imageview
imageView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
```


