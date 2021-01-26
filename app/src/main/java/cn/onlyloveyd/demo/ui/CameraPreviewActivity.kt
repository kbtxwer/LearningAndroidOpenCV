package cn.onlyloveyd.demo.ui

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import cn.onlyloveyd.demo.App
import cn.onlyloveyd.demo.databinding.ActivityCameraPreviewBinding
import org.opencv.android.*
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.core.Mat

/**
 * Camera Preview
 * author: yidong
 * 2021/1/17
 */
class CameraPreviewActivity : CameraActivity(), CameraBridgeViewBase.CvCameraViewListener2 {


    private val mBinding: ActivityCameraPreviewBinding by lazy {
        ActivityCameraPreviewBinding.inflate(layoutInflater)
    }

    private lateinit var mOpenCvCameraView: CameraBridgeViewBase

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.i(App.TAG, "OpenCV loaded successfully")
                    mOpenCvCameraView.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    /** Called when the activity is first created.  */
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(App.TAG, "called onCreate")
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(mBinding.root)
        mOpenCvCameraView = mBinding.preview
        mOpenCvCameraView.setCvCameraViewListener(this)
    }

    override fun onPause() {
        super.onPause()
        mOpenCvCameraView.disableView()
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(
                App.TAG,
                "Internal OpenCV library not found. Using OpenCV Manager for initialization"
            )
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d(App.TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun getCameraViewList(): List<CameraBridgeViewBase?> {
        return listOf(mOpenCvCameraView)
    }

    override fun onDestroy() {
        super.onDestroy()
        mOpenCvCameraView.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
    }

    override fun onCameraViewStopped() {
    }

    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat? {
        return inputFrame.gray()
    }
}
