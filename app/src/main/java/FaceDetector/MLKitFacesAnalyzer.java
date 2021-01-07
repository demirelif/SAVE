package FaceDetector;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.media.MediaPlayer;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.example.saveandroid.R;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.List;

public class MLKitFacesAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "MLKitFacesAnalyzer";
    private FirebaseVisionFaceDetector faceDetector;
    private TextureView tv;
    private ImageView iv;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint dotPaint, linePaint;
    private float widthScaleFactor = 1.0f;
    private float heightScaleFactor = 1.0f;
    private FirebaseVisionImage fbImage;
    private CameraX.LensFacing lens;
    private TextView smilingProbability;
    private int i;
    private float smileProb;
    private float leftEyeOpenProb;
    private float rightEyeOpenProb;

    private int fatigue_frame_counter;
    private int blink_frame_counter;
    private int blink_counter;
    private int yawn_frame_counter;
    private final int FATIGUE_THRESHOLD = 15;
    private final int BLINK_THRESHOLD = 3;
    private final int YAWN_THRESHOLD = 20;

    MLKitFacesAnalyzer(TextureView tv, ImageView iv, CameraX.LensFacing lens, TextView smilingProbability) {
        this.tv = tv;
        this.iv = iv;
        this.lens = lens;
        this.i = 0;
        smileProb = 0;
        leftEyeOpenProb = 0;
        rightEyeOpenProb = 0;
        this.smilingProbability = smilingProbability;
        fatigue_frame_counter = 0;
        blink_frame_counter = 0;
        blink_counter = 0;
        yawn_frame_counter = 0;
    }

    @Override
    public void analyze(ImageProxy image, int rotationDegrees) {
        if (image == null || image.getImage() == null) {
            return;
        }
        int rotation = degreesToFirebaseRotation(rotationDegrees);
        fbImage = FirebaseVisionImage.fromMediaImage(image.getImage(), rotation);
        initDrawingUtils();

        initDetector();
        detectFaces();
    }

    private void initDrawingUtils() {
        bitmap = Bitmap.createBitmap(tv.getWidth(), tv.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        dotPaint = new Paint();
        dotPaint.setColor(Color.RED);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setStrokeWidth(2f);
        dotPaint.setAntiAlias(true);
        linePaint = new Paint();
        linePaint.setColor(Color.GREEN);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2f);
        widthScaleFactor = canvas.getWidth() / (fbImage.getBitmap().getWidth() * 1.0f);
        heightScaleFactor = canvas.getHeight() / (fbImage.getBitmap().getHeight() * 1.0f);
    }

    private void initDetector() {
        FirebaseVisionFaceDetectorOptions detectorOptions = new FirebaseVisionFaceDetectorOptions
                .Builder()
                //.setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS) // CONTOUR AND CLASSIFICATION DOES NOT WORK TOGETHER
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build();
        faceDetector = FirebaseVision
                .getInstance()
                .getVisionFaceDetector(detectorOptions);
    }

    private void detectFaces() {
        faceDetector
                .detectInImage(fbImage)
                .addOnSuccessListener(firebaseVisionFaces -> {
                    if (!firebaseVisionFaces.isEmpty()) {
                        getInfoFromFaces(firebaseVisionFaces);
                        processFaces(firebaseVisionFaces);
                        System.out.println("meow");
                    } else {
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
                    }
                }).addOnFailureListener(e -> Log.i(TAG, e.toString()));
    }


    private void processFaces(List<FirebaseVisionFace> faces) {
        for (FirebaseVisionFace face : faces) {
            //drawContours(face.getContour(FirebaseVisionFaceContour.FACE).getPoints());
            //drawContours(face.getContour(FirebaseVisionFaceContour.LEFT_EYEBROW_BOTTOM).getPoints());
            //drawContours(face.getContour(FirebaseVisionFaceContour.RIGHT_EYEBROW_BOTTOM).getPoints());
            //drawContours(face.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints());
            //drawContours(face.getContour(FirebaseVisionFaceContour.RIGHT_EYE).getPoints());
            //drawContours(face.getContour(FirebaseVisionFaceContour.LEFT_EYEBROW_TOP).getPoints());
            //drawContours(face.getContour(FirebaseVisionFaceContour.RIGHT_EYEBROW_TOP).getPoints());
            //drawContours(face.getContour(FirebaseVisionFaceContour.LOWER_LIP_BOTTOM).getPoints());
            //drawContours(face.getContour(FirebaseVisionFaceContour.LOWER_LIP_TOP).getPoints());
            //drawContours(face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).getPoints());
            //drawContours(face.getContour(FirebaseVisionFaceContour.UPPER_LIP_TOP).getPoints());
            //drawContours(face.getContour(FirebaseVisionFaceContour.NOSE_BRIDGE).getPoints());
            //drawContours(face.getContour(FirebaseVisionFaceContour.NOSE_BOTTOM).getPoints());
            Rect box = new Rect((int) translateX(face.getBoundingBox().left),
                    (int) translateY(face.getBoundingBox().top),
                    (int) translateX(face.getBoundingBox().right),
                    (int) translateY(face.getBoundingBox().bottom));
            canvas.drawText(String.valueOf(face.getTrackingId()),
                    translateX(face.getBoundingBox().centerX()),
                    translateY(face.getBoundingBox().centerY()),
                    linePaint);
            canvas.drawRect(box, linePaint);
        }
        iv.setImageBitmap(bitmap);
    }

    private void drawContours(List<FirebaseVisionPoint> points) {
        int counter = 0;
        for (FirebaseVisionPoint point : points) {
            if (counter != points.size() - 1) {
                canvas.drawLine(translateX(point.getX()),
                        translateY(point.getY()),
                        translateX(points.get(counter + 1).getX()),
                        translateY(points.get(counter + 1).getY()),
                        linePaint);
            } else {
                canvas.drawLine(translateX(point.getX()),
                        translateY(point.getY()),
                        translateX(points.get(0).getX()),
                        translateY(points.get(0).getY()),
                        linePaint);
            }
            counter++;
            canvas.drawCircle(translateX(point.getX()), translateY(point.getY()), 6, dotPaint);
        }
    }

    private float translateY(float y) {
        return y * heightScaleFactor;
    }

    private float translateX(float x) {
        float scaledX = x * widthScaleFactor;
        if (lens == CameraX.LensFacing.FRONT) {
            return canvas.getWidth() - scaledX;
        } else {
            return scaledX;
        }
    }

    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException("Rotation must be 0, 90, 180, or 270.");
        }
    }
    private void getInfoFromFaces(List<FirebaseVisionFace> faces) {
        StringBuilder result = new StringBuilder();
        for (FirebaseVisionFace face : faces) {
            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and nose available):
            smileProb = face.getSmilingProbability();
            leftEyeOpenProb = face.getLeftEyeOpenProbability();
            rightEyeOpenProb = face.getRightEyeOpenProbability();
            result.append("Smile: ");
            if (smileProb > 0.5) {
                result.append("Yes");
            } else {
                result.append("No");
            }
            result.append("\nLeft eye: ");
            if (leftEyeOpenProb > 0.5) {
                result.append("Open");
            } else {
                result.append("Close");
            }
            result.append("\nRight eye: ");
            if (rightEyeOpenProb > 0.5) {
                result.append("Open");
            } else {
                result.append("Close");
            }
            result.append("\n");

            /*	30 frame for fatigue
            	3 frame for blink
            	20 frame for yawn */

            if(leftEyeOpenProb <= 0.5 && rightEyeOpenProb <= 0.5){

                fatigue_frame_counter++;
                blink_frame_counter++;

                if(fatigue_frame_counter >= FATIGUE_THRESHOLD){
                    result.append("FATIGUE ALERT");
                    System.out.println("FATIGUE ALERT");
                    FaceDetectionActivity.getInstanceActivity().playAlarm();
                    //faceDetectionActivity.playAudio();
                }

                if(blink_frame_counter >= BLINK_THRESHOLD){
                    blink_counter++;
                    System.out.println("Number of Blinks =" + blink_counter);
                }
            }
            else{

                fatigue_frame_counter = 0;
                blink_frame_counter = 0;
            }

            smilingProbability.setText(result);
            smilingProbability.invalidate();
            System.out.println("\n" + result);
        }
        //return result.toString();
    }



}

