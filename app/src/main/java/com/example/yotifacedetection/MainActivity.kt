package com.example.yotifacedetection

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import com.example.yotifacedetection.databinding.ActivityMainBinding
import com.yoti.mobile.android.capture.face.ui.FaceCaptureListener
import com.yoti.mobile.android.capture.face.ui.models.camera.CameraStateListener
import com.yoti.mobile.android.capture.face.ui.models.face.FaceCaptureConfiguration
import com.yoti.mobile.android.capture.face.ui.models.face.FaceCaptureState
import com.yoti.mobile.android.capture.face.ui.models.face.ImageQuality


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            throw IllegalStateException("Please accept camera permissions")
        }
    }

    private val cameraStateListener: CameraStateListener by lazy {
        CameraStateListener {
            Log.d("Yoti", "CameraState :$it")
        }
    }

    private val faceCaptureListener: FaceCaptureListener by lazy {
        FaceCaptureListener { faceCaptureResult ->
            when (val state = faceCaptureResult.state) {
                is FaceCaptureState.InvalidFace -> {
                    Log.d("Yoti", "FaceCaptureListener: INVALID ${state.cause}")
                }
                is FaceCaptureState.ValidFace -> {
                    Log.d("Yoti", "FaceCaptureListener: VALID")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkCameraPermissionAndStartCamera()
    }

    private fun checkCameraPermissionAndStartCamera() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) -> startCamera()
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        binding.root.doOnNextLayout {
            with(binding.faceCapture) {
                startCamera(this@MainActivity, cameraStateListener)
                startAnalysing(createConfiguration(binding.faceCapture), faceCaptureListener)
            }
        }
    }

    private fun createConfiguration(view: View): FaceCaptureConfiguration {
        val scanningRegion = Rect(view.left, view.top, view.right, view.bottom)
        Log.d("Yoti", "scanningRegion : $scanningRegion")
        return FaceCaptureConfiguration(
            scanningRegion = scanningRegion,
            imageQuality = ImageQuality.HIGH,
            requireValidAngle = true,
            requireEyesOpen = true,
            requireBrightEnvironment = true,
            requiredStableFrames = 15
        )
    }
}