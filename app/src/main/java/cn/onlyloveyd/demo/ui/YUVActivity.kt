package cn.onlyloveyd.demo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.onlyloveyd.demo.R
import cn.onlyloveyd.demo.databinding.ActivityYuvBinding
import cn.onlyloveyd.demo.ext.showMat
import cn.onlyloveyd.demo.ext.toRgb
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

/**
 * YUV
 * author: yidong
 * 2021-05-05
 */
class YUVActivity : AppCompatActivity() {
    private val mBinding by lazy {
        ActivityYuvBinding.inflate(layoutInflater)
    }

    private val mBgr by lazy {
        Utils.loadResource(this, R.drawable.lena)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        mBinding.ivLena.showMat(mBgr.toRgb())

        toYuv()
    }

    private fun toYuv() {
        val dst = Mat()
//        Imgproc.cvtColor(mBgr, dst, Imgproc.COLOR_BGR2YUV)
//        Imgproc.cvtColor(mBgr, dst, Imgproc.COLOR_BGR2YUV_YV12)
        Imgproc.cvtColor(mBgr, dst, Imgproc.COLOR_BGR2YUV_I420)
        Imgproc.cvtColor(mBgr, dst, Imgproc.COLOR_BGR2YUV_I420)
//        Imgproc.cvtColor(mBgr, dst, Imgproc.COLOR_BGR2YUV_IYUV)
        mBinding.ivResult.showMat(dst)
    }
}