package cn.onlyloveyd.demo.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import cn.onlyloveyd.demo.R
import cn.onlyloveyd.demo.adapter.ImageTextAdapter
import cn.onlyloveyd.demo.adapter.ImageTextObject
import cn.onlyloveyd.demo.databinding.ActivityPoissonImageEditBinding
import cn.onlyloveyd.demo.ext.toRgb
import cn.onlyloveyd.demo.ext.wrapCoroutine
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.photo.Photo

/**
 * 泊松图像编辑
 * author: yidong
 * 2020/12/11
 */
class PoissonImageEditActivity : AppCompatActivity() {

    private val mList = mutableListOf<ImageTextObject>()
    private lateinit var mAdapter: ImageTextAdapter

    private val mBinding: ActivityPoissonImageEditBinding by lazy {
        ActivityPoissonImageEditBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        mAdapter = ImageTextAdapter(this, mList)
        mBinding.container.adapter = mAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_cloning, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        title = item.title
        when (item.itemId) {
            R.id.normal_cloning -> {
                this.wrapCoroutine({ showLoading() }, { doNormalCloning() }, { hideLoading() })
            }
            R.id.mixed_cloning -> {
                this.wrapCoroutine({ showLoading() }, { doMixedCloning() }, { hideLoading() })
            }
            R.id.monochrome_transfer -> {
                this.wrapCoroutine({ showLoading() }, { doMonochromeTransfer() }, { hideLoading() })
            }
            R.id.local_color_change -> {
                this.wrapCoroutine({ showLoading() }, { doColorChange() }, { hideLoading() })
            }
            R.id.local_illumination_change -> {
                this.wrapCoroutine({ showLoading() }, { doIlluminationChange() }, { hideLoading() })
            }
            R.id.texture_flattening -> {
                this.wrapCoroutine({ showLoading() }, { doTextureFlattening() }, { hideLoading() })
            }
        }
        return true
    }

    private fun doNormalCloning() {
        val source = Utils.loadResource(this, R.drawable.normal_cloning_source)
        val mask = Utils.loadResource(this, R.drawable.normal_cloning_mask)
        val destination = Utils.loadResource(this, R.drawable.normal_cloning_destination)
        val result = Mat()
        val center = Point(400.toDouble(), 100.toDouble())
        Photo.seamlessClone(source, destination, mask, center, result, Photo.NORMAL_CLONE)

        mList.clear()
        mList.add(ImageTextObject(source.toRgb(), "source"))
        mList.add(ImageTextObject(mask, "mask"))
        mList.add(ImageTextObject(destination.toRgb(), "destination"))
        mList.add(ImageTextObject(result.toRgb(), "result"))
    }

    private fun doMixedCloning() {
        val source = Utils.loadResource(this, R.drawable.mixed_cloning_source)
        val mask = Utils.loadResource(this, R.drawable.mixed_cloning_mask)
        val destination = Utils.loadResource(this, R.drawable.mixed_cloning_destination)
        val result = Mat()
        val center = Point(destination.size().width / 2.0, destination.size().height / 2.0)
        Photo.seamlessClone(source, destination, mask, center, result, Photo.MIXED_CLONE)

        mList.clear()
        mList.add(ImageTextObject(source.toRgb(), "source"))
        mList.add(ImageTextObject(mask, "mask"))
        mList.add(ImageTextObject(destination.toRgb(), "destination"))
        mList.add(ImageTextObject(result.toRgb(), "result"))
    }

    private fun doMonochromeTransfer() {
        val source = Utils.loadResource(this, R.drawable.monochrome_transfer_source)
        val mask = Utils.loadResource(this, R.drawable.monochrome_transfer_mask)
        val destination = Utils.loadResource(this, R.drawable.monochrome_transfer_destination)
        val result = Mat()
        val center = Point(destination.size().width / 2.0, destination.size().height / 2.0)
        Photo.seamlessClone(source, destination, mask, center, result, Photo.MONOCHROME_TRANSFER)

        mList.clear()
        mList.add(ImageTextObject(source.toRgb(), "source"))
        mList.add(ImageTextObject(mask, "mask"))
        mList.add(ImageTextObject(destination.toRgb(), "destination"))
        mList.add(ImageTextObject(result.toRgb(), "result"))
    }

    private fun doColorChange() {
        val source = Utils.loadResource(this, R.drawable.color_change_source)
        val mask = Utils.loadResource(this, R.drawable.color_change_mask)
        val result = Mat()
        Photo.colorChange(source, mask, result, 1.5F, .5F, .5F)

        mList.clear()
        mList.add(ImageTextObject(source.toRgb(), "source"))
        mList.add(ImageTextObject(mask, "mask"))
        mList.add(ImageTextObject(result.toRgb(), "result"))
    }

    private fun doIlluminationChange() {
        val source = Utils.loadResource(this, R.drawable.illumination_change_source)
        val mask = Utils.loadResource(this, R.drawable.illumination_change_mask)
        val result = Mat()
        Photo.illuminationChange(source, mask, result, 0.2f, 0.4f)

        mList.clear()
        mList.add(ImageTextObject(source.toRgb(), "source"))
        mList.add(ImageTextObject(mask, "mask"))
        mList.add(ImageTextObject(result.toRgb(), "result"))
    }

    private fun doTextureFlattening() {
        val source = Utils.loadResource(this, R.drawable.texture_flattening_source)
        val mask = Utils.loadResource(this, R.drawable.texture_flattening_mask)
        val result = Mat()
        Photo.textureFlattening(source, mask, result, 30F, 45F, 3)

        mList.clear()
        mList.add(ImageTextObject(source.toRgb(), "source"))
        mList.add(ImageTextObject(mask, "mask"))
        mList.add(ImageTextObject(result.toRgb(), "result"))
    }

    private fun showLoading() {
        mBinding.isLoading = true
    }

    private fun hideLoading() {
        mAdapter.notifyDataSetChanged()
        mBinding.isLoading = false
    }
}