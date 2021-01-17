package cn.onlyloveyd.demo.ui

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import cn.onlyloveyd.demo.App
import cn.onlyloveyd.demo.databinding.ActivityCameraPreviewBinding
import org.opencv.android.*
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Camera Preview
 * author: yidong
 * 2021/1/17
 */
class CameraPreviewActivity : CameraActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private val mBinding: ActivityCameraPreviewBinding by lazy {
        ActivityCameraPreviewBinding.inflate(layoutInflater)
    }

    private var mRgba: Mat? = null
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
//        mOpenCvCameraView.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView.setCvCameraViewListener(this)
        mBinding.capture.setOnClickListener(View.OnClickListener {
            if (mRgba != null) {
                if (!mRgba!!.empty()) {
                    val inter = Mat(mRgba!!.width(), mRgba!!.height(), CvType.CV_8UC4)
                    //将四通道的RGBA转为三通道的BGR，重要！！
                    Imgproc.cvtColor(mRgba, inter, Imgproc.COLOR_RGBA2BGR)
                    var sdDir: File? = null
                    //判断是否存在机身内存
                    val sdCardExist =
                        Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
                    if (sdCardExist) {
                        //获得机身储存根目录
                        sdDir = Environment.getExternalStorageDirectory()
                    }
                    //将拍摄准确时间作为文件名
                    val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
                    val filename = sdf.format(Date())
                    val filePath = sdDir.toString() + "/Pictures/OpenCV/" + filename + ".png"
                    //将转化后的BGR矩阵内容写入到文件中
                    Imgcodecs.imwrite(filePath, inter)
                    Toast.makeText(this, "图片保存到: $filePath", Toast.LENGTH_SHORT).show()
                }
            }
        })
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
        mRgba = Mat(height, width, CvType.CV_8UC4)
    }

    override fun onCameraViewStopped() {
        mRgba?.release()
    }

    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat? {
        mRgba = inputFrame.rgba()
        return mRgba
    }
}
