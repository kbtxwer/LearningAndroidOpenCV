package cn.onlyloveyd.demo.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import cn.onlyloveyd.demo.App
import cn.onlyloveyd.demo.R
import cn.onlyloveyd.demo.databinding.ActivityCornerSubpixBinding
import cn.onlyloveyd.demo.ext.showMat
import cn.onlyloveyd.demo.ext.toGray
import cn.onlyloveyd.demo.ext.toRgb
import cn.onlyloveyd.demo.ext.wrapCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.CvType.CV_32F
import org.opencv.imgproc.Imgproc


/**
 * 亚像素级别角点检测
 * author: yidong
 * 2021/1/31
 */
class CornerSubPixActivity : AppCompatActivity() {
    private val bgr by lazy {
        Utils.loadResource(this, R.drawable.lena)
    }
    private val rgb by lazy { bgr.toRgb() }

    private val mBinding: ActivityCornerSubpixBinding by lazy {
        ActivityCornerSubpixBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        wrapCoroutine({ showLoading() }, { doCornerSubPix() }, { hideLoading() })
    }

    private fun doCornerSubPix() {
        val gray = bgr.toGray()
        val corners = MatOfPoint()
        val maxCorners = 10
        val qualityLevel = 0.01
        val minDistance = 10.0

        Imgproc.goodFeaturesToTrack(
            gray,
            corners,
            maxCorners,
            qualityLevel,
            minDistance,
            Mat(),
            3,
            false,
            0.04
        )

        Log.v(App.TAG, "Number of corners detected: ${corners.rows()}")

        val cornersData = IntArray((corners.total() * corners.channels()).toInt())
        corners.get(0, 0, cornersData)

        for (i in 0 until corners.rows()) {
            Log.v(
                App.TAG,
                "Corner [" + i + "] = (" + cornersData[i * 2] + "," + cornersData[i * 2 + 1] + ")"
            )
        }

        val matCorners = Mat(corners.rows(), 2, CV_32F)
        val matCornersData = FloatArray((matCorners.total() * matCorners.channels()).toInt())
        matCorners.get(0, 0, matCornersData)
        for (i in 0 until corners.rows()) {
            Imgproc.circle(
                rgb, Point(
                    cornersData[i * 2].toDouble(),
                    cornersData[i * 2 + 1].toDouble()
                ), 4,
                Scalar(0.0, 255.0, 0.0), Imgproc.FILLED
            )
            matCornersData[i * 2] = cornersData[i * 2].toFloat()
            matCornersData[i * 2 + 1] = cornersData[i * 2 + 1].toFloat()
        }

        GlobalScope.launch(Dispatchers.Main) {
            mBinding.ivResult.showMat(rgb)
        }
        matCorners.put(0, 0, matCornersData)

        val winSize = Size(5.0, 5.0)
        val zeroSize = Size(-1.0, -1.0)
        val criteria = TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 40, 0.01)
        Imgproc.cornerSubPix(gray, matCorners, winSize, zeroSize, criteria)
        matCorners.get(0, 0, matCornersData)

        for (i in 0 until corners.rows()) {
            Log.v(
                App.TAG,
                "Corner SubPix [" + i + "] = (" + matCornersData[i * 2] + "," + matCornersData[i * 2 + 1] + ")"
            )
        }
    }

    override fun onDestroy() {
        bgr.release()
        rgb.release()
        super.onDestroy()
    }


    private fun showLoading() {
        mBinding.isLoading = true
    }

    private fun hideLoading() {
        mBinding.isLoading = false
    }
}