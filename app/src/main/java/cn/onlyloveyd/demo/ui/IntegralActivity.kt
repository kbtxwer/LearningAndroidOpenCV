package cn.onlyloveyd.demo.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import cn.onlyloveyd.demo.R
import cn.onlyloveyd.demo.databinding.ActivityIntegralBinding
import cn.onlyloveyd.demo.ext.getBgrFromResId
import cn.onlyloveyd.demo.ext.showMat
import cn.onlyloveyd.demo.ext.toRgb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.core.Core
import org.opencv.core.Core.NORM_MINMAX
import org.opencv.core.CvType
import org.opencv.core.CvType.CV_8UC1
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


/**
 * 图像积分图
 * author: yidong
 * 2020/12/16
 */
class IntegralActivity : AppCompatActivity() {
    private val mBinding: ActivityIntegralBinding by lazy {
        ActivityIntegralBinding.inflate(layoutInflater)
    }

    private lateinit var rgb: Mat
    private lateinit var sum: Mat
    private lateinit var sqrSum: Mat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        val bgr = getBgrFromResId(R.drawable.lena)
        rgb = Mat()
        rgb = bgr.toRgb()
        mBinding.ivLena.showMat(rgb)

        sum = Mat()
        sqrSum = Mat()
        Imgproc.integral2(rgb, sum, sqrSum, CvType.CV_32S, CvType.CV_32F)
    }

    override fun onDestroy() {
        rgb.release()
        sum.release()
        sqrSum.release()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_integral, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_integral_sum -> {
                doSumNormalize()
            }
            R.id.menu_integral_blur -> {
                doBlur()
            }
        }
        return true
    }

    private fun doSumNormalize() {
        val result = Mat()
        GlobalScope.launch(Dispatchers.Main) {
            mBinding.isLoading = true
            withContext(Dispatchers.IO) {
                Core.normalize(sum, result, 0.0, 255.0, NORM_MINMAX, CV_8UC1)
            }
            mBinding.isLoading = false
            mBinding.ivResult.showMat(result)
        }
    }

    private fun doBlur() {
        GlobalScope.launch(Dispatchers.Main) {
            mBinding.isLoading = true
            val result = Mat.zeros(rgb.size(), rgb.type())
            withContext(Dispatchers.IO) {
                val w = rgb.cols()
                val h = rgb.rows()
                var x2 = 0
                var y2 = 0
                var x1 = 0
                var y1 = 0
                val ksize = 10
                val radius = ksize / 2
                val ch = rgb.channels()
                var cx = 0
                var cy = 0

                for (row in 0 until h + radius) {
                    y2 = if ((row + 1) > h) {
                        h
                    } else {
                        row + 1
                    }
                    y1 = if ((row - ksize) < 0) {
                        0
                    } else {
                        row - ksize
                    }
                    for (col in 0 until w + radius) {
                        x2 = if ((col + 1) > w) w else (col + 1)
                        x1 = if ((col - ksize) < 0) 0 else (col - ksize)
                        cx = if ((col - radius) < 0) 0 else col - radius
                        cy = if ((row - radius) < 0) 0 else row - radius
                        val num = (x2 - x1) * (y2 - y1)
                        val values = ByteArray(ch) { 0 }
                        for (i in 0 until ch) {
                            // 积分图查找和表，计算卷积
                            val s = getBlockSum(sum, x1, y1, x2, y2, i)
                            values[i] = (s / num).toByte()
                        }
                        result.put(cy, cx, values)
                    }
                }
            }
            mBinding.isLoading = false
            mBinding.ivResult.showMat(result)
        }
    }

    private fun getBlockSum(sum: Mat, x1: Int, y1: Int, x2: Int, y2: Int, i: Int): Int {
        // top left
        val tl = sum.get(y1, x1)[i].toInt()
        // top right
        val tr = sum.get(y2, x1)[i].toInt()
        // bottom left
        val bl = sum.get(y1, x2)[i].toInt()
        // bottom right
        val br = sum.get(y2, x2)[i].toInt()

        return (br - bl - tr + tl)
    }
}