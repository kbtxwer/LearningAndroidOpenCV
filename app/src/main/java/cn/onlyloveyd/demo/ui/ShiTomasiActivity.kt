package cn.onlyloveyd.demo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.onlyloveyd.demo.R
import cn.onlyloveyd.demo.databinding.ActivityShiTomasBinding
import cn.onlyloveyd.demo.ext.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

/**
 * Shi-Tomas角点检测
 * author: yidong
 * 2021/1/7
 */
class ShiTomasiActivity : AppCompatActivity() {

    private val gray by lazy {
        getBgrFromResId(R.drawable.lena).toGray()
    }
    private val rgb by lazy {
        getBgrFromResId(R.drawable.lena).toRgb()
    }
    private val mBinding: ActivityShiTomasBinding by lazy {
        ActivityShiTomasBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        mBinding.ivLena.showMat(gray)
        wrapCoroutine({ showLoading() }, { doShiTomas() }, { hideLoading() })
    }

    private fun doShiTomas() {
        val corners = MatOfPoint()
        val maxCorners = 100
        val qualityLevel = 0.1
        val minDistance = 0.04

        Imgproc.goodFeaturesToTrack(
            gray,
            corners,
            maxCorners,
            qualityLevel,
            minDistance,
            Mat(),
            3,
            false
        )
        val points = corners.toList()
        val result = rgb.clone()
        GlobalScope.launch(Dispatchers.Main) {
            for (point in points) {
                Imgproc.circle(result, point, 10, Scalar(0.0, 255.0, 0.0), 2, Imgproc.LINE_8)
            }
            mBinding.ivResult.showMat(result)
        }
    }

    private fun showLoading() {
        mBinding.isLoading = true
    }

    private fun hideLoading() {
        mBinding.isLoading = false
    }

    override fun onDestroy() {
        super.onDestroy()
        gray.release()
        rgb.release()
    }
}

