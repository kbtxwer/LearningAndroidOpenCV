package cn.onlyloveyd.demo.ext

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.huantansheng.easyphotos.engine.ImageEngine


/**
 * Glide Engine
 * author: yidong
 * 2021-05-09
 */
class GlideEngine private constructor() : ImageEngine {
    /**
     * 加载图片到ImageView
     *
     * @param context   上下文
     * @param uri 图片路径Uri
     * @param imageView 加载到的ImageView
     */
    //安卓10推荐uri，并且path的方式不再可用
    override fun loadPhoto(context: Context, uri: Uri, imageView: ImageView) {
        Glide.with(context).load(uri).transition(withCrossFade()).into(imageView)
    }

    override fun loadGifAsBitmap(context: Context, gifUri: Uri, imageView: ImageView) {
        Glide.with(context).asBitmap().load(gifUri).into(imageView)
    }

    override fun loadGif(context: Context, gifUri: Uri, imageView: ImageView) {
        Glide.with(context).asGif().load(gifUri).transition(withCrossFade()).into(imageView)
    }


    /**
     * 获取图片加载框架中的缓存Bitmap，不用拼图功能可以直接返回null
     *
     * @param context 上下文
     * @param uri    图片路径
     * @param width   图片宽度
     * @param height  图片高度
     * @return Bitmap
     * @throws Exception 异常直接抛出，EasyPhotos内部处理
     */
    //安卓10推荐uri，并且path的方式不再可用
    @Throws(Exception::class)
    override fun getCacheBitmap(context: Context, uri: Uri, width: Int, height: Int): Bitmap {
        return Glide.with(context).asBitmap().load(uri).submit(width, height).get()
    }

    companion object {
        val instance: GlideEngine by lazy {
            GlideEngine()
        }
    }
}