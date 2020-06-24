package cn.onlyloveyd.demo.ext

import android.graphics.Bitmap
import android.widget.ImageView
import org.opencv.android.Utils
import org.opencv.core.Mat

/**
 * 扩展方法
 *
 * @author yidong
 * @date 2020/6/24
 */

fun ImageView.showMat(source: Mat) {
    val bitmap = Bitmap.createBitmap(source.width(), source.height(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(source, bitmap)
    setImageBitmap(bitmap)
}
