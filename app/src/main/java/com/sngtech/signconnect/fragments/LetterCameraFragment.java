package com.sngtech.signconnect.fragments;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.extensions.ExtensionMode;
import androidx.camera.extensions.ExtensionsManager;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sngtech.signconnect.SignDetailsActivity;
import com.sngtech.signconnect.databinding.FragmentLettersCameraBinding;
import com.sngtech.signconnect.models.HistoryItem;
import com.sngtech.signconnect.models.HistoryModel;
import com.sngtech.signconnect.models.User;
import com.sngtech.signconnect.models.UserModel;
import com.sngtech.signconnect.models.UserQueryListener;
import com.sngtech.signconnect.utils.ObjectDetectorHelper;

import org.tensorflow.lite.task.gms.vision.detector.Detection;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class LetterCameraFragment extends Fragment implements ObjectDetectorHelper.ObjectDetectorListener, UserQueryListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final String[] REQUIRED_PERMISSIONS = new String[] {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
    };

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ListenableFuture<ExtensionsManager> extensionsManagerFuture;

    private FragmentLettersCameraBinding binding;

    private ImageCapture imageCapture;

    private ObjectDetectorHelper objectDetectorHelper;
    private Bitmap bitmap;

    private ExecutorService cameraExecutor;

    Detection result;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLettersCameraBinding.inflate(inflater, container, false);

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        UserModel.queryUser(FirebaseAuth.getInstance().getCurrentUser(), db, this);

        objectDetectorHelper = new ObjectDetectorHelper(HistoryItem.SignType.LETTER, this.getContext(), 4, 0.4f, 3, this);

        objectDetectorHelper.setup();
        requestPermissionsAndStart();
        binding.boxDetectionView.clear();

        binding.btnDetect.setOnClickListener(ignore -> {
            onCapture();
        });
    }

    // Handle Permissions and Start Camera
    final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean isGranted = true;
                for(Map.Entry<String, Boolean> permission : permissions.entrySet()) {
                    if(!permission.getValue()) {
                        isGranted = false;
                    }
                }

                if(!isGranted) {
                    Toast.makeText(getContext(), "Permission Requests Denied", Toast.LENGTH_SHORT).show();
                } else {
                    startCamera();
                }
            });

    void requestPermissionsAndStart() {
        requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS);
    }

    void bindUseCases(@NonNull ProcessCameraProvider cameraProvider, @NonNull CameraSelector selector) {
        Preview preview = new Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9).build();
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());
        binding.previewView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);
        binding.previewView.setScaleType(PreviewView.ScaleType.FILL_START);

        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(binding.previewView.getDisplay().getRotation())
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build();

        ImageAnalysis imageAnalyzer = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(binding.previewView.getDisplay().getRotation())
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageAnalyzer.setAnalyzer(
                ContextCompat.getMainExecutor(requireContext()),
                image -> {
                    bitmap = Bitmap.createBitmap(
                            image.getWidth(),
                            image.getHeight(),
                            Bitmap.Config.ARGB_8888
                    );
                    detectHandSigns(image, bitmap);
                    image.close();
                }
        );

        cameraProvider.bindToLifecycle(this, selector, imageAnalyzer, preview, imageCapture);
    }

    private void detectHandSigns(ImageProxy image, Bitmap bitmap) {
        bitmap.copyPixelsFromBuffer(image.getPlanes()[0].getBuffer());
        int imageRotation = image.getImageInfo().getRotationDegrees();
        objectDetectorHelper.runDetection(bitmap, imageRotation);
    }

    void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                extensionsManagerFuture = ExtensionsManager.getInstanceAsync(requireContext(), cameraProvider);
                extensionsManagerFuture.addListener(() -> {
                    cameraProvider.unbindAll();
                    try {
                        ExtensionsManager extensionsManager = extensionsManagerFuture.get();
                        if(extensionsManager.isExtensionAvailable(
                                cameraSelector,
                                ExtensionMode.AUTO)) {

                            CameraSelector newSelector = extensionsManager.getExtensionEnabledCameraSelector(cameraSelector, ExtensionMode.AUTO);

                            bindUseCases(cameraProvider, newSelector);
                        } else {
                            bindUseCases(cameraProvider, cameraSelector);
                        }
                    } catch (ExecutionException | InterruptedException ignored) {}
                }, ContextCompat.getMainExecutor(requireContext()));
            } catch(ExecutionException | InterruptedException ignored) {}
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void onCapture() {
        HistoryItem item = new HistoryItem(result.getCategories().get(0).getLabel(), HistoryItem.SignType.LETTER);
        item.setFacing(1);

        String currentDateTime = LocalDateTime.now().toString().replace(":", "-").replace(".", "_");
        String fileName = item.getSignType().getLabel() + "_" + currentDateTime;

        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);

        try {
            path.mkdirs();
        } catch(Exception e) {
            Log.println(Log.WARN, "warning", "Pictures directory already exists!");
        }

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(new File(getContext().getExternalFilesDir(
                Environment.DIRECTORY_PICTURES), fileName)).build();
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(requireContext()), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                item.setCapturedPath(outputFileResults.getSavedUri().getPath());
                HistoryModel.addItemtoHistory(FirebaseAuth.getInstance().getCurrentUser(), db, item);

                switchToDetailsActivity(item);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(getContext(), "Unknown Error: Unable to capture image for detection!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void switchToDetailsActivity(HistoryItem item) {
        Intent newIntent = new Intent(getContext(), SignDetailsActivity.class);
        Bundle detailsBundle = new Bundle();
        detailsBundle.putString("signType", item.getSignType().getLabel());
        detailsBundle.putString("result", item.getResult());
        detailsBundle.putString("datetime", item.getDateTimeLearnt());
        detailsBundle.putString("capturedPath", item.getCapturedPath());
        detailsBundle.putInt("facing", 1);
        newIntent.putExtras(detailsBundle);

        startActivity(newIntent);
    }

    @Override
    public void onResult(List<Detection> results, int imgWidth, int imgHeight) {
        Log.println(Log.INFO, "results_debugger_info", results.toString());
        getActivity().runOnUiThread(() -> {
            if (results.isEmpty())
                binding.btnDetect.setVisibility(View.INVISIBLE);
            else
                binding.btnDetect.setVisibility(View.VISIBLE);

            if(results.size() > 0)
                result = results.get(0);
            binding.boxDetectionView.setResults(results, imgWidth, imgHeight);
            binding.boxDetectionView.invalidate();
        });
    }

    @Override
    public void onQuerySuccess(User user) {
        boolean detBoxEnabled = user.isDetBoxEnabled();

        if(detBoxEnabled)
            binding.boxDetectionView.setVisibility(View.VISIBLE);
        else
            binding.boxDetectionView.setVisibility(View.INVISIBLE);
    }
}