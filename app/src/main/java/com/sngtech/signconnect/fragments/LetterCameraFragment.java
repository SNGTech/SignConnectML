package com.sngtech.signconnect.fragments;

import android.Manifest;
import android.content.Intent;
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
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.extensions.ExtensionMode;
import androidx.camera.extensions.ExtensionsManager;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.sngtech.signconnect.HistoryActivity;
import com.sngtech.signconnect.R;
import com.sngtech.signconnect.SignDetailsActivity;
import com.sngtech.signconnect.databinding.FragmentLettersCameraBinding;
import com.sngtech.signconnect.recyclerViews.HistoryItem;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class LetterCameraFragment extends Fragment {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ListenableFuture<ExtensionsManager> extensionsManagerFuture;

    private FragmentLettersCameraBinding binding;

    private ImageCapture imageCapture;

    // TODO: TEMPORARY
    public static int captureCount = 0;

    private static final String[] REQUIRED_PERMISSIONS = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_MEDIA_IMAGES
    };

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
        requestPermissions();

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

    void requestPermissions() {
        requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS);
    }

    void bindUseCases(@NonNull ProcessCameraProvider cameraProvider, @NonNull CameraSelector selector) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());
        binding.previewView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);

        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(binding.previewView.getDisplay().getRotation())
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build();

        cameraProvider.bindToLifecycle(this, selector, preview, imageCapture);
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
        String[] resultsArr = getResources().getStringArray(R.array.history_results_array);
        HistoryItem item = new HistoryItem(
                resultsArr[captureCount],
                HistoryItem.SignType.LETTER);

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
                HistoryActivity.historyItemList.add(item);
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
        newIntent.putExtras(detailsBundle);

        captureCount++;
        if(captureCount >= 3)
            captureCount = 0;

        startActivity(newIntent);
    }
}