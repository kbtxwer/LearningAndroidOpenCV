package cn.onlyloveyd.demo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.onlyloveyd.demo.R
import cn.onlyloveyd.demo.adapter.ImageTextAdapter
import cn.onlyloveyd.demo.adapter.ImageTextObject
import cn.onlyloveyd.demo.databinding.ActivityResizeBinding
import cn.onlyloveyd.demo.ext.getBgrFromResId
import cn.onlyloveyd.demo.ext.toRgb
import cn.onlyloveyd.demo.ext.wrapCoroutine
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

/**
 * 图像插值
 * author: yidong
 * 2020/12/27
 */
class ResizeActivity : AppCompatActivity() {

    private val mList = mutableListOf<ImageTextObject>()
    private val mAdapter by lazy { ImageTextAdapter(this, mList) }
    private val mBinding: ActivityResizeBinding by lazy {
        ActivityResizeBinding.inflate(layoutInflater)
    }
    private val mFlags = mapOf(
        Imgproc.INTER_NEAREST to "INTER_NEAREST",
        Imgproc.INTER_LINEAR to "INTER_LINEAR",
        Imgproc.INTER_CUBIC to "INTER_CUBIC",
        Imgproc.INTER_AREA to "INTER_AREA",
        Imgproc.INTER_LANCZOS4 to "INTER_LANCZOS4",
    )

    private val rgb: Mat by lazy {
        val bgr = getBgrFromResId(R.drawable.tiny_lena)
        bgr.toRgb()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        mBinding.container.adapter = mAdapter
        wrapCoroutine({ before() }, { doResize() }, { after() })
    }

    override fun onDestroy() {
        super.onDestroy()
        rgb.release()
    }

    private fun doResize() {
        val w = rgb.rows()
        val h = rgb.cols()

        for (i in mFlags) {
            val dst = Mat()
            Imgproc.resize(
                rgb,
                dst,
                Size((w * 100).toDouble(), (h * 100).toDouble()),
                0.0,
                0.0,
                i.key
            )
            mList.add(ImageTextObject(dst, mFlags.getOrElse(i.key) { "" }))
        }
    }

    private fun before() {
        mBinding.isLoading = true
    }

    private fun after() {
        mBinding.isLoading = false
        mAdapter.notifyDataSetChanged()
    }
}