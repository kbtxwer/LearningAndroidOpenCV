package cn.onlyloveyd.demo.ui

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import cn.onlyloveyd.demo.App
import cn.onlyloveyd.demo.contrib.wechat.WeChatQRCode
import cn.onlyloveyd.demo.databinding.ActivityCameraPreviewBinding
import cn.onlyloveyd.demo.ext.MMKVKey
import com.tencent.mmkv.MMKV
import org.opencv.android.*
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

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
    private lateinit var mWeChatQRCode: WeChatQRCode

    private lateinit var mRgba: Mat

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.i(App.TAG, "OpenCV loaded successfully")
                    initWeChatQRCode()
                    mOpenCvCameraView.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
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
        mRgba = Mat()
    }

    override fun onCameraViewStopped() {
        mRgba.release()
    }

    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat {
        mRgba = inputFrame.rgba()
        Log.i(App.TAG, "onCameraFrame")
        val rectangles = ArrayList<Mat>()
        val results = mWeChatQRCode.detectAndDecode(inputFrame.gray(), rectangles)
        println(results)
        for ((index, result) in results.withIndex()) {
            val points = rectangles[index]
            val pointArr = FloatArray(8)
            points.get(0, 0, pointArr)
            var pt1 = Point(pointArr[0].toDouble(), pointArr[1].toDouble() - 100)
            for (i in pointArr.indices step 2) {
                val start =
                    Point(pointArr[i % 8].toDouble(), pointArr[(i + 1) % 8].toDouble())
                val end = Point(pointArr[(i + 2) % 8].toDouble(), pointArr[(i + 3) % 8].toDouble())
                Imgproc.line(mRgba, start, end, Scalar(255.0, 0.0, 0.0), 8, Imgproc.LINE_8)
            }
            Imgproc.putText(mRgba, result, pt1, 0, 1.0, Scalar(255.0, 0.0, 0.0), 2)
        }
        return mRgba
    }
}
