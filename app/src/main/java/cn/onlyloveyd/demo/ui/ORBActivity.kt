package cn.onlyloveyd.demo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.onlyloveyd.demo.R
import cn.onlyloveyd.demo.databinding.ActivityOrbBinding
import cn.onlyloveyd.demo.ext.showMat
import cn.onlyloveyd.demo.ext.toRgb
import cn.onlyloveyd.demo.ext.wrapCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfKeyPoint
import org.opencv.core.Scalar
import org.opencv.features2d.Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS
import org.opencv.features2d.Features2d.drawKeypoints
import org.opencv.features2d.ORB

/**
 * ORB 特征点检测
 * author: yidong
 * 2021-03-28
 */
class ORBActivity : AppCompatActivity() {

    private val bgr by lazy {
        Utils.loadResource(this, R.drawable.lena)
    }
    private val rgb by lazy { bgr.toRgb() }

    private val mBinding: ActivityOrbBinding by lazy {
        ActivityOrbBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        wrapCoroutine({ showLoading() }, { doORB() }, { hideLoading() })
    }


    private fun showLoading() {
        mBinding.isLoading = true
    }

    private fun hideLoading() {
        mBinding.isLoading = false
    }

    private fun doORB() {
        val orbDetector = ORB.create(
            500,
            1.2f,
            8,
            31,
            0,
            2,
            ORB.HARRIS_SCORE,
            31,
            20
        )
        val points = MatOfKeyPoint()
        orbDetector.detect(rgb, points)
        val description = Mat()
        orbDetector.compute(rgb, points, description)
        val result = Mat()
        rgb.copyTo(result)
        drawKeypoints(rgb, points, result, Scalar.all(255.0), DrawMatchesFlags_DRAW_RICH_KEYPOINTS)
        GlobalScope.launch(Dispatchers.Main) {
            mBinding.ivResult.showMat(result)
        }
    }

    override fun onDestroy() {
        bgr.release()
        rgb.release()
        super.onDestroy()
    }
}
