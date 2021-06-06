package cn.onlyloveyd.demo.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.onlyloveyd.demo.App
import cn.onlyloveyd.demo.BuildConfig
import cn.onlyloveyd.demo.databinding.ActivitySmartCropBinding
import cn.onlyloveyd.demo.ext.GlideEngine
import com.huantansheng.easyphotos.EasyPhotos
import com.huantansheng.easyphotos.callback.SelectCallback
import com.huantansheng.easyphotos.models.album.entity.Photo

/**
 * 智能裁剪
 * author: yidong
 * 2021-06-04
 */
class SmartCropActivity : AppCompatActivity() {


    private val mBinding: ActivitySmartCropBinding by lazy {
        ActivitySmartCropBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
    }

    fun pickImage(view: View) {
        hideCropButton()
        hideCroppedImage()
        EasyPhotos.createAlbum(
            this, true, false,
            GlideEngine.instance
        )
            .setFileProviderAuthority(BuildConfig.APPLICATION_ID)
            .setCount(1)
            .start(object : SelectCallback() {
                override fun onResult(photos: ArrayList<Photo>, isOriginal: Boolean) {
                    Log.d(App.TAG, photos.toString())
                    if (photos.isNotEmpty()) {
                        val path = photos.first().path
                        val options = BitmapFactory.Options()
                        options.inJustDecodeBounds = true
                        BitmapFactory.decodeFile(path, options)
                        options.inJustDecodeBounds = false
                        options.inSampleSize = calculateSampleSize(options)
                        val selectedBitmap = BitmapFactory.decodeFile(path, options)
                        if (selectedBitmap != null) {
                            mBinding.ivCrop.setImageToCrop(selectedBitmap)
                        }
                        showCropButton()
                    }
                }

                override fun onCancel() {}
            })
    }

    fun cropImage(view: View) {
        showCroppedImage()
        hideCropButton()
        if (mBinding.ivCrop.bitmap != null) {
            val cropBitmap = mBinding.ivCrop.crop()
            mBinding.ivShow.setImageBitmap(cropBitmap)
        }
    }

    private fun calculateSampleSize(options: BitmapFactory.Options): Int {
        val outHeight = options.outHeight
        val outWidth = options.outWidth
        var sampleSize = 1
        val destHeight = 1000
        val destWidth = 1000
        if (outHeight > destHeight || outWidth > destHeight) {
            sampleSize = if (outHeight > outWidth) {
                outHeight / destHeight
            } else {
                outWidth / destWidth
            }
        }
        if (sampleSize < 1) {
            sampleSize = 1
        }
        return sampleSize
    }

    fun showCropButton() {
        mBinding.btCrop.visibility = View.VISIBLE
    }

    private fun hideCropButton() {
        mBinding.btCrop.visibility = View.GONE
    }

    fun showCroppedImage() {
        mBinding.ivShow.visibility = View.VISIBLE
        mBinding.ivCrop.visibility = View.GONE
    }

    private fun hideCroppedImage() {
        mBinding.ivShow.visibility = View.GONE
        mBinding.ivCrop.visibility = View.VISIBLE
    }
}