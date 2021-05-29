package cn.onlyloveyd.demo.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.onlyloveyd.demo.App
import cn.onlyloveyd.demo.BuildConfig
import cn.onlyloveyd.demo.R
import cn.onlyloveyd.demo.databinding.ActivityRectifyBinding
import cn.onlyloveyd.demo.ext.GlideEngine
import cn.onlyloveyd.demo.ext.showMat
import cn.onlyloveyd.demo.ext.toRgb
import com.huantansheng.easyphotos.EasyPhotos
import com.huantansheng.easyphotos.callback.SelectCallback
import com.huantansheng.easyphotos.models.album.entity.Photo
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.utils.Converters
import java.io.File
import kotlin.concurrent.thread

/**
 * Z轴倾斜矫正
 * author: yidong
 * 2021-05-14
 */
class ZRectifyActivity : AppCompatActivity() {

    private val mBinding: ActivityRectifyBinding by lazy {
        ActivityRectifyBinding.inflate(layoutInflater)
    }

    private val srcImage by lazy {
        Utils.loadResource(this, R.drawable.image_z_rectify_sample)
    }

    private var isProcessing = false

    private lateinit var pickerImage: Mat
    private var dstImage: Mat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        doProcess(srcImage)
    }

    fun stepOne(inputImage: Mat): Mat {
        // 1. 均值漂移滤波
        val blurred = Mat()
        Imgproc.pyrMeanShiftFiltering(inputImage, blurred, 25.0, 10.0)
        return blurred
    }

    /**
     * 图像处理过程
     */
    private fun doProcess(inputImage: Mat) {
        isProcessing = true
        mBinding.ivSrc.showMat(inputImage.toRgb())
        mBinding.progressBar.visibility = View.VISIBLE

        thread {
            // 1. 均值漂移滤波
            val blurred = Mat()
            Imgproc.pyrMeanShiftFiltering(inputImage, blurred, 25.0, 10.0)
            Imgcodecs.imwrite(
                "${cacheDir.absolutePath + File.separator}1.png",
                blurred
            )

            // 2. 灰度化、二值化
            val binary = processGrayAndBinary(blurred)
            Imgcodecs.imwrite(
                "${cacheDir.absolutePath + File.separator}2.png",
                binary
            )
            // 3. 边缘检测
            Imgproc.Canny(binary, binary, 100.0, 200.0, 3)
            Imgcodecs.imwrite(
                "${cacheDir.absolutePath + File.separator}3.png",
                binary
            )
            // 4. 轮廓发现
            val contours = mutableListOf<MatOfPoint>()
            val hierarchy = Mat()
            Imgproc.findContours(
                binary,
                contours,
                hierarchy,
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE
            )

            contours.sortByDescending {
                Imgproc.contourArea(it)
            }

            // 边缘绘制
            val result = inputImage.clone()
            Imgproc.drawContours(
                result,
                contours,
                -1,
                Scalar(255.0, 255.0, 0.0),
                4,
                Imgproc.LINE_AA
            )
            Imgcodecs.imwrite(
                "${cacheDir.absolutePath + File.separator}4.png",
                result
            )

            //  获取最外层轮廓（有点理想化）
            val externalContour2f = MatOfPoint2f()
            contours.first().convertTo(externalContour2f, CvType.CV_32F)
            val peri = Imgproc.arcLength(externalContour2f, true)
            val approxCurve = MatOfPoint2f()
            // 5. 多边形逼近
            Imgproc.approxPolyDP(externalContour2f, approxCurve, 0.02 * peri, true)
            val count = approxCurve.size().height
            if (count != 4.0) {
                runOnUiThread {
                    Toast.makeText(this, "无法找到矩形顶点", Toast.LENGTH_SHORT).show()
                    mBinding.progressBar.visibility = View.INVISIBLE
                    isProcessing = false
                }
                return@thread
            } else {
                val dstPoints = mutableListOf(
                    Point(inputImage.width().toDouble(), 0.0),
                    Point(0.0, 0.0),
                    Point(0.0, inputImage.height().toDouble()),
                    Point(inputImage.width().toDouble(), inputImage.height().toDouble()),
                )
                val dstMat = Converters.vector_Point2f_to_Mat(dstPoints)
                // 6. 透视变换矩阵
                val transform = Imgproc.getPerspectiveTransform(approxCurve, dstMat)
                val dst = Mat()
                // 7. 透视变换
                Imgproc.warpPerspective(
                    inputImage,
                    dst,
                    transform,
                    inputImage.size(),
                    Imgproc.INTER_NEAREST
                )
                Imgcodecs.imwrite(
                    "${cacheDir.absolutePath + File.separator}5.png",
                    dst
                )
                runOnUiThread {
                    // 显示结果
                    mBinding.ivResult.showMat(dst)
                    mBinding.progressBar.visibility = View.INVISIBLE
                    isProcessing = false
                }
            }
        }
    }

    /**
     * 源图像灰度化二值化
     */
    private fun processGrayAndBinary(src: Mat): Mat {
        if (src.cols() > 1000 || src.rows() > 800) {//图片过大，进行降采样
            Imgproc.pyrDown(src, src)
            Imgproc.pyrDown(src, src)
        }
        var grayImage = Mat()
        if (src.type() == CvType.CV_8UC1) {
            grayImage = src.clone()
        } else if (src.type() == CvType.CV_8UC3) {
            Imgproc.cvtColor(src, grayImage, Imgproc.COLOR_BGR2GRAY)//转化灰度图
        }

        val binaryImage = Mat()
        Imgproc.adaptiveThreshold(
            grayImage,
            binaryImage,
            125.0,
            Imgproc.ADAPTIVE_THRESH_MEAN_C,
            Imgproc.THRESH_BINARY,
            7,
            0.0
        )//自适应滤波
        return binaryImage
    }

    fun pickImage(view: View) {
        if (!isProcessing) {
            EasyPhotos.createAlbum(
                this, true, false,
                GlideEngine.instance
            )
                .setFileProviderAuthority(BuildConfig.APPLICATION_ID)
                .setCount(1)
                .start(object : SelectCallback() {
                    override fun onResult(photos: ArrayList<Photo>, isOriginal: Boolean) {
                        Log.d(App.TAG, photos.toString())
                        if (Imgcodecs.haveImageReader(photos.first().path)) {
                            pickerImage = Imgcodecs.imread(photos.first().path)
                            doProcess(pickerImage)
                        }
                    }

                    override fun onCancel() {}
                })
        } else {
            Toast.makeText(this, "图像处理中……", Toast.LENGTH_SHORT).show()
        }
    }

}