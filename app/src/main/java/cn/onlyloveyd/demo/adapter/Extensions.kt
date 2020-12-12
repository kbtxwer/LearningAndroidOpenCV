package cn.onlyloveyd.demo.adapter

import android.app.Activity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

/**
 * 扩张方法
 * author: yidong
 * 2020/12/12
 */
fun Mat.toRgb(): Mat {
    val rgb = Mat()
    Imgproc.cvtColor(this, rgb, Imgproc.COLOR_BGR2RGB)
    return rgb
}

fun Mat.toGray(): Mat {
    val gray = Mat()
    Imgproc.cvtColor(this, gray, Imgproc.COLOR_BGR2GRAY)
    return gray
}

fun Activity.wrapCoroutine(before: () -> Unit, method: () -> Unit, after: () -> Unit) {
    GlobalScope.launch(Dispatchers.Main) {
        before()
        withContext(Dispatchers.IO) {
            method()
        }
        after()
    }
}