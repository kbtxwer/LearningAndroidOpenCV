package cn.onlyloveyd.demo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.onlyloveyd.demo.R
import cn.onlyloveyd.demo.databinding.ActivityMeanShiftBinding
import cn.onlyloveyd.demo.ext.showMat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

/**
 * Description
 *
 * @author yidong
 * @date 11/25/20
 */
class MeanShiftActivity : AppCompatActivity() {

    private val mBinding: ActivityMeanShiftBinding by lazy {
        ActivityMeanShiftBinding.inflate(layoutInflater)
    }
    private lateinit var mRgb: Mat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        mRgb = Mat()
        val bgr = Utils.loadResource(this, R.drawable.wedding)
        Imgproc.cvtColor(bgr, mRgb, Imgproc.COLOR_BGR2RGB)
        mBinding.ivLena.showMat(mRgb)
        GlobalScope.launch(Dispatchers.IO) {
            doMeanShift()
        }
    }

    private fun doMeanShift() {
        val dst = Mat()
        Imgproc.pyrMeanShiftFiltering(mRgb, dst, 100.0, 100.0)
        GlobalScope.launch(Dispatchers.Main) {
            mBinding.ivResult.showMat(dst)
        }
    }
}