package cn.onlyloveyd.demo.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import cn.onlyloveyd.demo.App
import cn.onlyloveyd.demo.R
import cn.onlyloveyd.demo.contrib.wechat.WeChatQRCode
import cn.onlyloveyd.demo.databinding.ActivityCameraxBinding
import cn.onlyloveyd.demo.ext.MMKVKey
import com.bumptech.glide.load.ImageHeaderParser.UNKNOWN_ORIENTATION
import com.tencent.mmkv.MMKV
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * CameraX
 * author: yidong
 * 2021-05-04
 */
class CameraXPreviewActivity : AppCompatActivity() {

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    private var displayId: Int = -1
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private var isAnalyzerActive = true

    private val displayManager by lazy {
        getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }


    private val orientationEventListener by lazy {
        object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == UNKNOWN_ORIENTATION) {
                    return
                }

                val rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                imageAnalyzer?.targetRotation = rotation
                imageCapture?.targetRotation = rotation
            }
        }
    }

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) {
            if (mBinding.viewFinder.display.displayId == displayId) {
                val rotation = mBinding.viewFinder.display.rotation
                imageAnalyzer?.targetRotation = rotation
                imageCapture?.targetRotation = rotation
            }
        }
    }

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var mBinding: ActivityCameraxBinding
    private lateinit var mWeChatQRCode: WeChatQRCode


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_camerax)

        initWeChatQRCode()

        // Set up the listener for take photo button
        mBinding.cameraCaptureButton.setOnClickListener { takePhoto() }

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Every time the orientation of device changes, update rotation for use cases
        displayManager.registerDisplayListener(displayListener, null)
        orientationEventListener.enable()
        // Wait for the views to be properly laid out
        if (allPermissionsGranted()) {
            mBinding.viewFinder.post {

                // Keep track of the display in which this view is attached
                displayId = mBinding.viewFinder.display.displayId

                // Set up the camera and its use cases
                setUpCamera()
            }
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

    }

    /** Initialize CameraX, and prepare to bind the camera use cases  */
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // CameraProvider
            cameraProvider = cameraProviderFuture.get()
            // Build and bind the camera use cases
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }


    private fun initWeChatQRCode() {
        mWeChatQRCode = WeChatQRCode(
            MMKV.defaultMMKV()?.decodeString(MMKVKey.WeChatQRCodeDetectProtoTxt) ?: "",
            MMKV.defaultMMKV()?.decodeString(MMKVKey.WeChatQRCodeDetectCaffeModel) ?: "",
            MMKV.defaultMMKV()?.decodeString(MMKVKey.WeChatQRCodeSrProtoTxt) ?: "",
            MMKV.defaultMMKV()?.decodeString(MMKVKey.WeChatQRCodeSrCaffeModel) ?: "",
        )
        Log.d(App.TAG, mWeChatQRCode.toString())
        Log.d(App.TAG, "Finish Init")
    }

    /** Declare and bind preview, capture and analysis use cases */
    private fun bindCameraUseCases() {
        // Get screen metrics used to setup camera for full screen resolution
        val metrics = DisplayMetrics().also { mBinding.viewFinder.display.getRealMetrics(it) }
        Log.d(App.TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        Log.d(App.TAG, "Preview aspect ratio: $screenAspectRatio")

        val rotation = mBinding.viewFinder.display.rotation

        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview
        preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        // ImageAnalysis
        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
            // The analyzer can then be assigned to the instance
            .also {
                it.setAnalyzer(
                    cameraExecutor,
                    WeChatAnalyzer(
                        mWeChatQRCode,
                    ) { results ->
                        runOnUiThread {
                            if (results.isNotEmpty()) {
                                if (isAnalyzerActive) {
                                    Toast.makeText(this, results.toString(), Toast.LENGTH_SHORT)
                                        .show()
                                    isAnalyzerActive = false
                                    finish()
                                }
                            }
                        }
                    })
            }

// Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()
        try {
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, imageAnalyzer
            )
            preview?.setSurfaceProvider(mBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(App.TAG, "Use case binding failed", exc)
        }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(App.TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(App.TAG, msg)
                }
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                mBinding.viewFinder.post {
                    // Keep track of the display in which this view is attached
                    displayId = mBinding.viewFinder.display.displayId
                    // Set up the camera and its use cases
                    setUpCamera()
                }
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private class WeChatAnalyzer(
        private val weChatQRCode: WeChatQRCode,
        private val listener: (results: List<String>) -> Unit
    ) : ImageAnalysis.Analyzer {
        override fun analyze(image: ImageProxy) {
            Log.d(App.TAG, "size = ${image.width} * ${image.height}")
            Log.d(App.TAG, "rotationDegrees = ${image.imageInfo.rotationDegrees}")
            val rectangles = ArrayList<Mat>()
            val results = weChatQRCode.detectAndDecode(gray(image), rectangles)
            listener(results)
            image.close()
        }

        fun gray(image: ImageProxy): Mat {
            val planeProxy = image.planes
            val width = image.width
            val height = image.height
            val yPlane = planeProxy[0].buffer
            val yPlaneStep = planeProxy[0].rowStride
            return Mat(height, width, CvType.CV_8UC1, yPlane, yPlaneStep.toLong())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Shut down our background executor
        cameraExecutor.shutdown()
        orientationEventListener.disable()
        displayManager.unregisterDisplayListener(displayListener)
    }

}