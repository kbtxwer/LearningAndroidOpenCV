package cn.onlyloveyd.demo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.onlyloveyd.demo.R
import cn.onlyloveyd.demo.databinding.ActivityHarrisBinding
import cn.onlyloveyd.demo.ext.getBgrFromResId
import cn.onlyloveyd.demo.ext.showMat
import cn.onlyloveyd.demo.ext.toGray
import cn.onlyloveyd.demo.ext.wrapCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

/**
 * Harris角点检测
 * author: yidong
 * 2020/12/30
 */
class HarrisActivity : AppCompatActivity() {
    private val mBinding: ActivityHarrisBinding by lazy {
        ActivityHarrisBinding.inflate(layoutInflater)
    }

    private val gray by lazy {
        this.getBgrFromResId(R.drawable.lena).toGray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        mBinding.ivLena.showMat(gray)
        wrapCoroutine({ showLoading() }, { doCornerHarris() }, { hideLoading() })
    }

    private fun doCornerHarris() {
        val dst = Mat()
        val dstNorm = Mat()
        val dstNormal8U = Mat()
        Imgproc.cornerHarris(gray, dst, 2, 3, 0.04)
        Core.normalize(dst, dstNorm, 0.0, 255.0, Core.NORM_MINMAX)
        Core.convertScaleAbs(dstNorm, dstNormal8U)
        Imgproc.threshold(dstNormal8U, dstNormal8U, 120.0, 255.0, Imgproc.THRESH_BINARY)
        GlobalScope.launch(Dispatchers.Main) {
            mBinding.ivResult.showMat(dstNormal8U)
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
    }
}