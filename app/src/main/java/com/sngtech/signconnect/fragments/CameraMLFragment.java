package com.sngtech.signconnect.fragments;

import android.Manifest;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.extensions.ExtensionMode;
import androidx.camera.extensions.ExtensionsManager;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.View;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.sngtech.signconnect.utils.ObjectDetectorHelper;
import com.sngtech.signconnect.R;
//import com.sngtech.signconnect.databinding.FragmentCameraBinding;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class CameraMLFragment extends Fragment {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ListenableFuture<ExtensionsManager> extensionsManagerFuture;

    private ObjectDetectorHelper detectorHelper;

    //private FragmentCameraBinding binding;

    private static final String[] REQUIRED_PERMISSIONS = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    public CameraMLFragment() {
        super(R.layout.fragment_ml_camera);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        binding = FragmentCameraBinding.inflate(inflater, container, false);
//
//        // Inflate the layout for this fragment
//        return binding.getRoot();
//    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        detectorHelper = new ObjectDetectorHelper(this.getContext(), 2, 0.5f, 3);
        detectorHelper.setup();
        requestPermissions();
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
        //preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());
        //binding.previewView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);

        // Model Image Analyser
        //ImageAnalysis imageAnalyzer = detectorHelper.getAnalyzer(binding);

        //cameraProvider.bindToLifecycle(this, selector, preview, imageAnalyzer);
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
}