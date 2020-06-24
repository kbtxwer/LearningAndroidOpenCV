package cn.onlyloveyd.demo.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cn.onlyloveyd.demo.R
import cn.onlyloveyd.demo.databinding.ActivityErosionBinding
import cn.onlyloveyd.demo.ext.showMat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

/**
 * 图像腐蚀
 *
 * @author yidong
 * @date 2020/6/24
 */
class ErosionActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityErosionBinding
    private lateinit var mRgb: Mat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_erosion)

        val bgr = Utils.loadResource(this, R.drawable.number)
        mRgb = Mat()
        Imgproc.cvtColor(bgr, mRgb, Imgproc.COLOR_BGR2RGB)
        mBinding.ivLena.showMat(mRgb)
        bgr.release()

        GlobalScope.launch(Dispatchers.IO) {
            doErode()
        }
    }

    private fun doErode() {
        val kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, Size(24.0, 24.0))
        val result = Mat()
        Imgproc.erode(mRgb, result, kernel)
        GlobalScope.launch(Dispatchers.Main) {
            mBinding.ivResult.showMat(result)
            result.release()
        }
    }


}