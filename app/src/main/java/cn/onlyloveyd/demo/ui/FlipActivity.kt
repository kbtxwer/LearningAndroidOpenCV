package cn.onlyloveyd.demo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.onlyloveyd.demo.R
import cn.onlyloveyd.demo.adapter.ImageTextAdapter
import cn.onlyloveyd.demo.adapter.ImageTextObject
import cn.onlyloveyd.demo.databinding.ActivityFlipBinding
import cn.onlyloveyd.demo.ext.getBgrFromResId
import cn.onlyloveyd.demo.ext.toRgb
import cn.onlyloveyd.demo.ext.wrapCoroutine
import org.opencv.core.Core
import org.opencv.core.Mat

/**
 * 图像翻转
 * author: yidong
 * 2020/12/20
 */
class FlipActivity : AppCompatActivity() {
    private val mList = mutableListOf<ImageTextObject>()
    private val mAdapter by lazy { ImageTextAdapter(this, mList) }
    private val mBinding: ActivityFlipBinding by lazy {
        ActivityFlipBinding.inflate(layoutInflater)
    }

    private val rgb: Mat by lazy {
        val bgr = getBgrFromResId(R.drawable.lena)
        bgr.toRgb()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        mBinding.container.adapter = mAdapter
        wrapCoroutine({ showLoading() }, { doFlip() }, { hideLoading() })
    }

    override fun onDestroy() {
        rgb.release()
        super.onDestroy()
    }

    private fun doFlip() {
        val resultX = Mat()
        val resultY = Mat()
        val resultXY = Mat()
        val resultTranspose = Mat()
        val resultRepeat = Mat()
        Core.flip(rgb, resultX, 0)
        Core.flip(rgb, resultY, 1)
        Core.flip(rgb, resultXY, -1)
        Core.transpose(rgb, resultTranspose)
        Core.repeat(rgb, 2, 2, resultRepeat)

        val resultSymmetryLowerToUpper = rgb.clone()
        Core.completeSymm(resultSymmetryLowerToUpper, true)
        val resultSymmetryUpperToLower = rgb.clone()
        Core.completeSymm(resultSymmetryUpperToLower, false)
        mList.add(ImageTextObject(rgb, "原图"))
        mList.add(ImageTextObject(resultX, "X轴翻转"))
        mList.add(ImageTextObject(resultY, "Y轴翻转"))
        mList.add(ImageTextObject(resultXY, "XY轴翻转"))
        mList.add(ImageTextObject(resultTranspose, "转置"))
        mList.add(ImageTextObject(resultRepeat, "repeat"))
        mList.add(ImageTextObject(resultSymmetryLowerToUpper, "Symmetry(下到上)"))
        mList.add(ImageTextObject(resultSymmetryUpperToLower, "Symmetry(上到下)"))
    }

    private fun showLoading() {
        mBinding.isLoading = true
    }

    private fun hideLoading() {
        mBinding.isLoading = false
        mAdapter.notifyDataSetChanged()
    }
}