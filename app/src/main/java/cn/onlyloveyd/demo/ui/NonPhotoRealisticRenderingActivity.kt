package cn.onlyloveyd.demo.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import cn.onlyloveyd.demo.R
import cn.onlyloveyd.demo.databinding.ActivityNonPhotorealisticRenderingBinding
import cn.onlyloveyd.demo.ext.showMat
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.photo.Photo

/**
 * Photo模块
 *
 * @author yidong
 * @date 11/30/20
 */
class NonPhotoRealisticRenderingActivity : AppCompatActivity() {

    private val mBinding: ActivityNonPhotorealisticRenderingBinding by lazy {
        ActivityNonPhotorealisticRenderingBinding.inflate(layoutInflater)
    }

    private lateinit var mRgb: Mat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        mRgb = Mat()
        val bgr = Utils.loadResource(this, R.drawable.lena)
        Imgproc.cvtColor(bgr, mRgb, Imgproc.COLOR_BGR2RGB)
        mBinding.ivLena.showMat(mRgb)
        doPencilSketch()
    }


    private fun doEdgePreservingFilter(flag: Int) {
        val dst = Mat()
        Photo.edgePreservingFilter(mRgb, dst, flag)
        mBinding.ivResult.showMat(dst)
    }

    private fun doDetailEnhance() {
        val dst = Mat()
        Photo.detailEnhance(mRgb, dst)
        mBinding.ivResult.showMat(dst)
    }


    private fun doPencilSketch() {
        val dst1 = Mat()
        val dst2 = Mat()
        Photo.pencilSketch(mRgb, dst1, dst2, 10.0f, 0.1f, 0.03f)
        mBinding.ivResult.showMat(dst2)
    }

    private fun doStylization() {
        val dst = Mat()
        Photo.stylization(mRgb, dst)
        mBinding.ivResult.showMat(dst)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_non_photorealistic_rendering, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.photo_edge_preserving_normconv_filter
            -> {
                title = "NORMCONV_FILTER"
                doEdgePreservingFilter(Photo.NORMCONV_FILTER)
            }
            R.id.photo_edge_preserving_recurs_filter
            -> {
                title = "RECURS_FILTER"
                doEdgePreservingFilter(Photo.RECURS_FILTER)
            }
            R.id.photo_detail_enhance
            -> {
                title = "Detail Enhance"
                doDetailEnhance()
            }
            R.id.photo_pencil_sketch
            -> {
                title = "Pencil Sketch"
                doPencilSketch()
            }
            R.id.photo_stylization
            -> {
                title = "Stylization"
                doStylization()
            }
        }
        return true
    }
}