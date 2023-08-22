package com.sngtech.signconnect.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.camera.core.AspectRatio;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.internal.GmsLogger;
import com.google.android.gms.tflite.client.TfLiteInitializationOptions;
import com.google.mlkit.vision.interfaces.Detector;
import com.sngtech.signconnect.databinding.ActivityCaptureBinding;
//import com.sngtech.signconnect.databinding.FragmentCameraBinding;

import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.Rot90Op;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.gms.vision.TfLiteVision;
import org.tensorflow.lite.task.gms.vision.detector.Detection;
import org.tensorflow.lite.task.gms.vision.detector.ObjectDetector;
import org.tensorflow.lite.task.gms.vision.detector.ObjectDetector.ObjectDetectorOptions;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ObjectDetectorHelper {

    public static final String LETTER_MODEL_NAME = "signletters_float32.tflite";
    public static final String WORD_MODEL_NAME = "";

    private Context context;
    private ObjectDetector objectDetector;
    private int numThreads;
    private float threshold;
    private int maxResults;

    public ObjectDetectorHelper(Context context, int numThreads, float threshold, int maxResults) {
        this.context = context;
        this.numThreads = numThreads;
        this.threshold = threshold;
        this.maxResults = maxResults;
    }

    public void setup() {
        Log.println(Log.INFO, "init_gps", "Initialising Google Play Service!");
        TfLiteInitializationOptions tfOptions = TfLiteInitializationOptions.builder()
                .setEnableGpuDelegateSupport(true).build();
        TfLiteVision.initialize(this.context, tfOptions)
                .addOnSuccessListener(unused -> {
                    Log.println(Log.INFO, "vision_success", "Vision Module Loaded Successfully!");
                    setupObjectDetectorOptions();
                })
                .addOnFailureListener(e -> Log.println(Log.ERROR, "vision_fail", "Vision Module Failed to Load: " + e.getMessage()));

        Log.println(Log.INFO, "success_gps", "Successfully Setup Google Play Service!");
    }

    private void setupObjectDetectorOptions() {
        ObjectDetectorOptions.Builder detectorOptionsBuilder = ObjectDetectorOptions.builder()
                .setScoreThreshold(this.threshold)
                .setMaxResults(this.maxResults);

        BaseOptions.Builder baseOptionsBuilder = BaseOptions.builder().setNumThreads(this.numThreads);

        try {
            baseOptionsBuilder.useGpu();
        } catch(Exception e) {
            Log.println(Log.ERROR, "gpu_fail", "GPU is not supported on this Device");
        }

        detectorOptionsBuilder.setBaseOptions(baseOptionsBuilder.build());

        try {
            objectDetector = ObjectDetector.createFromFileAndOptions(this.context, LETTER_MODEL_NAME, detectorOptionsBuilder.build());
        } catch(IOException e) {
            Log.println(Log.ERROR, "object_detector_fail", "Failed to create Object Detector from Model");
        }
    }

//    public ImageAnalysis getAnalyzer(FragmentCameraBinding cameraBinding) {
//        ImageAnalysis imageAnalyzer = new ImageAnalysis.Builder()
//                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                .setTargetRotation((int)cameraBinding.previewView.getRotation())
//                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
//                .build();
//
//        imageAnalyzer.setAnalyzer(
//                ContextCompat.getMainExecutor(cameraBinding.getRoot().getContext()),
//                image -> {
//                    Bitmap bitmap = Bitmap.createBitmap(
//                            image.getWidth(),
//                            image.getHeight(),
//                            Bitmap.Config.ARGB_8888
//                    );
//                    detectHandSigns(image, bitmap);
//                }
//        );
//        return imageAnalyzer;
//    }

    private void detectHandSigns(ImageProxy image, Bitmap bitmap) {
        bitmap.copyPixelsFromBuffer(image.getPlanes()[0].getBuffer());
        int imageRotation = image.getImageInfo().getRotationDegrees();
        runDetection(bitmap, imageRotation);
        Log.println(Log.DEBUG, "results_debugger_info", "test");
    }

    public void runDetection(Bitmap bitmap, int rotation) {
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new Rot90Op(-rotation / 90))
                .build();

        TensorImage tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap));

        List<Detection> results = objectDetector.detect(tensorImage);
        Log.println(Log.DEBUG, "results_debugger_info", results.toString());
    }
}
