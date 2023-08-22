package com.sngtech.signconnect.fragments;

import android.Manifest;
import android.content.Intent;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.camera.camera2.interop.Camera2CameraInfo;
import androidx.camera.camera2.interop.ExperimentalCamera2Interop;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.extensions.ExtensionMode;
import androidx.camera.extensions.ExtensionsManager;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.sngtech.signconnect.HistoryActivity;
import com.sngtech.signconnect.R;
import com.sngtech.signconnect.SignDetailsActivity;
import com.sngtech.signconnect.databinding.FragmentWordsCameraBinding;
import com.sngtech.signconnect.recyclerViews.HistoryItem;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class WordsCameraFragment extends Fragment {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ListenableFuture<ExtensionsManager> extensionsManagerFuture;

    private FragmentWordsCameraBinding binding;

    private VideoCapture<Recorder> videoCapture;

    private Recording recording;

    private boolean isRecording = false;
    private boolean isFinishedDetecting = false;

    // TODO: TEMPORARY
    public static int captureCount = 0;

    private HistoryItem capturedHistoryItem;
    private CameraSelector cameraSelector;

    private static final String[] REQUIRED_PERMISSIONS = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWordsCameraBinding.inflate(inflater, container, false);

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requestPermissions();

        binding.recordingLabel.setVisibility(View.INVISIBLE);
        binding.viewMore.setVisibility(View.INVISIBLE);

        binding.btnRecord.setOnClickListener(ignore -> {
            if(!isRecording) {
                startRecording();
            }
            else {
                stopRecording();
            }
            if(isFinishedDetecting) {
                binding.viewMore.setVisibility(View.VISIBLE);
            }
            isRecording = !isRecording;
        });

        binding.viewMore.setOnClickListener(ignore -> switchToDetailsActivity(capturedHistoryItem));
        binding.btnReverseCamera.setOnClickListener(ignore -> {
            switchCameraFacing();
            Animation reverseBtnAnim = AnimationUtils.loadAnimation(getContext(), R.anim.reverse_camera_btn);
            binding.btnReverseCamera.startAnimation(reverseBtnAnim);
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

        QualitySelector qualitySelector = getBestQuality();

        Recorder recorder = new Recorder.Builder()
                .setExecutor(ContextCompat.getMainExecutor(requireContext()))
                .setQualitySelector(qualitySelector)
                .build();
        videoCapture = VideoCapture.withOutput(recorder);

        Camera camera = cameraProvider.bindToLifecycle(this, selector, preview, videoCapture);
    }

    void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                if(cameraSelector == null)
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

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

    private void startRecording() {
        isFinishedDetecting = false;
        binding.btnRecord.setImageResource(R.drawable.stop_recording_button);
        binding.recordingLabel.setVisibility(View.VISIBLE);
        binding.viewMore.setVisibility(View.INVISIBLE);
        binding.reverseHolder.setVisibility(View.INVISIBLE);

        String[] resultsArr = getResources().getStringArray(R.array.history_results_word_array);
        capturedHistoryItem = new HistoryItem(
                resultsArr[captureCount],
                HistoryItem.SignType.WORD);

        String currentDateTime = LocalDateTime.now().toString().replace(":", "-").replace(".", "_");
        String fileName = capturedHistoryItem.getSignType().getLabel() + "_" + currentDateTime + ".mp4";

        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM);

        try {
            path.mkdirs();
        } catch(Exception e) {
            Log.println(Log.WARN, "warning", "DCIM directory already exists!");
        }

        FileOutputOptions outputFileOptions = new FileOutputOptions.Builder(new File(getContext().getExternalFilesDir(
                Environment.DIRECTORY_DCIM), fileName)).build();

        recording = videoCapture
                .getOutput()
                .prepareRecording(getContext(), outputFileOptions)
                .start(ContextCompat.getMainExecutor(requireContext()), e -> {
                    if(e instanceof VideoRecordEvent.Finalize) {
                        capturedHistoryItem.setCapturedPath(((VideoRecordEvent.Finalize) e).getOutputResults().getOutputUri().getPath());
                        Log.println(Log.INFO, "video_signconnect", "Saved Recording at: " + capturedHistoryItem.getCapturedPath());
                        Log.println(Log.INFO, "video_signconnect", "Recording Stopped");
                    }
                });
        Log.println(Log.INFO, "video_signconnect", "Recording Started");
    }

    private void stopRecording() {
        isFinishedDetecting = true;
        binding.btnRecord.setImageResource(R.drawable.record_button);
        binding.recordingLabel.setVisibility(View.INVISIBLE);
        binding.reverseHolder.setVisibility(View.VISIBLE);
        recording.stop();

        captureCount++;
        if(captureCount >= 2)
            captureCount = 0;
        HistoryActivity.historyItemList.add(capturedHistoryItem);
    }

    @OptIn(markerClass = ExperimentalCamera2Interop.class)
    private QualitySelector getBestQuality() {
        CameraInfo cameraInfo = null;
        int cameraFacing = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA ? 0 : 1;
        try {
            cameraInfo = cameraProviderFuture.get().getAvailableCameraInfos().stream()
                    .filter(cam -> Camera2CameraInfo.from(cam).getCameraCharacteristic(CameraCharacteristics.LENS_FACING) == cameraFacing).findFirst().get();
        } catch(InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        List<Quality> supportedQualities = QualitySelector.getSupportedQualities(cameraInfo);
        return QualitySelector.from(supportedQualities.get(0));
    }

    private void switchCameraFacing() {
        if(cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
        else
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        startCamera();
    }

    private void switchToDetailsActivity(HistoryItem item) {
        Intent newIntent = new Intent(getContext(), SignDetailsActivity.class);
        Bundle detailsBundle = new Bundle();
        detailsBundle.putString("signType", item.getSignType().getLabel());
        detailsBundle.putString("result", item.getResult());
        detailsBundle.putString("datetime", item.getDateTimeLearnt());
        detailsBundle.putString("capturedPath", item.getCapturedPath());
        detailsBundle.putInt("facing", cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA ? 0 : 1);
        newIntent.putExtras(detailsBundle);

        startActivity(newIntent);
    }
}