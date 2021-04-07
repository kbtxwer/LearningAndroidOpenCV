package cn.onlyloveyd.demo.ext

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class AlphaAndScalePageTransformer : ViewPager2.PageTransformer {
    private val SCALEMAX = 0.8f
    private val ALPHAMAX = 0.5f

    override fun transformPage(page: View, position: Float) {
        val scale =
            if (position < 0) (1 - SCALEMAX) * position + 1 else (SCALEMAX - 1) * position + 1
        val alpha =
            if (position < 0) (1 - ALPHAMAX) * position + 1 else (ALPHAMAX - 1) * position + 1
        if (position < 0) {
            page.pivotX = page.width.toFloat()
            page.pivotY = (page.height / 2).toFloat()
        } else {
            page.pivotX = 0F
            page.pivotY = (page.height / 2).toFloat()
        }
        page.scaleX = scale
        page.scaleY = scale
        page.alpha = abs(alpha)
    }
}
