package cn.onlyloveyd.demo.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cn.onlyloveyd.demo.R
import cn.onlyloveyd.demo.databinding.ActivityShapeRenderBinding
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

/**
 * 绘制几何图形
 * author: yidong
 * 2020/1/25
 */
class ShapeRenderActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityShapeRenderBinding
    private lateinit var mSourceMat: Mat

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, ShapeRenderActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_shape_render)

        mSourceMat = Mat()
        val bgr = Utils.loadResource(this, R.drawable.lena)
        Imgproc.cvtColor(bgr, mSourceMat, Imgproc.COLOR_BGR2RGB)
        showMat(mSourceMat.clone())
        bgr.release()
    }

    override fun onDestroy() {
        mSourceMat.release()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_shape_render, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.line -> renderLine(mSourceMat.clone())
            R.id.rectangle -> renderRectangle(mSourceMat.clone())
            R.id.circle -> renderCircle(mSourceMat.clone())
            R.id.ellipse -> renderEllipse(mSourceMat.clone())
            R.id.text -> renderText(mSourceMat.clone())
        }
        return true
    }

    private fun renderLine(source: Mat) {
        val start = Point(0.0, source.height().toDouble())
        val end = Point(source.width().toDouble(), 0.0)
        Imgproc.line(source, start, end, Scalar(255.0, 0.0, 0.0), 4)
        showMat(source)
    }

    private fun renderRectangle(source: Mat) {
        val leftTop = Point(10.0, 10.0)
        val rightBottom = Point(60.0, 60.0)
        Imgproc.rectangle(source, leftTop, rightBottom, Scalar(255.0, 0.0, 0.0), -1)
        showMat(source)
    }

    private fun renderCircle(source: Mat) {
        val center = Point(source.width() / 2.0, source.height() / 2.0)
        val radius = 60
        Imgproc.circle(source, center, radius, Scalar(255.0, 0.0, 0.0), 2, Imgproc.LINE_AA)
        showMat(source)
    }

    private fun renderText(source: Mat) {
        val center = Point(source.width() / 2.0, source.height() / 2.0)
        Imgproc.putText(
            source,
            "Hello",
            center,
            Imgproc.FONT_HERSHEY_TRIPLEX,
            1.2,
            Scalar(255.0, 0.0, 0.0),
            2
        )
        // 中文乱码
        showMat(source)
    }

    private fun renderEllipse(source: Mat) {
        val size = Size(100.0, 50.0)
        val center = Point(source.width() - 100.0, source.height() - 50.0)
        Imgproc.ellipse(
            source,
            center,
            size,
            360.0,
            0.0,
            360.0,
            Scalar(255.0, 0.0, 0.0),
            2,
            Imgproc.LINE_8,
            0
        )
        showMat(source)
    }

    private fun showMat(source: Mat) {
        val bitmap = Bitmap.createBitmap(source.width(), source.height(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(source, bitmap)
        mBinding.ivSource.setImageBitmap(bitmap)
        source.release()
    }

}