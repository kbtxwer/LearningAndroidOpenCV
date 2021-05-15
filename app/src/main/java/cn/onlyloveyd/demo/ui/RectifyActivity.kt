package cn.onlyloveyd.demo.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import kotlin.concurrent.thread

/**
 * 平面倾斜矫正
 * author: yidong
 * 2021-05-14
 */
class RectifyActivity : AppCompatActivity() {

    private val mBinding: ActivityRectifyBinding by lazy {
        ActivityRectifyBinding.inflate(layoutInflater)
    }

    private val srcImage by lazy {
        Utils.loadResource(this, R.drawable.image_rectify_sample)
    }

    private var isProcessing = false

    private lateinit var pickerImage: Mat
    private var dstImage: Mat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        doProcess(srcImage)
    }

    /**
     * 图像处理过程
     */
    private fun doProcess(inputImage: Mat) {
        isProcessing = true
        mBinding.ivSrc.showMat(inputImage.toRgb())
        mBinding.progressBar.visibility = View.VISIBLE
        thread {
            val secondStepResult = processRectify(inputImage)
            val cropMat = cropRectifyResult(secondStepResult.first, secondStepResult.second)
            dstImage = Mat()
            cropMat.copyTo(dstImage)
            runOnUiThread {
                mBinding.ivResult.showMat(dstImage?.toRgb()!!)
                mBinding.progressBar.visibility = View.INVISIBLE
                isProcessing = false
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
            255.0,
            Imgproc.ADAPTIVE_THRESH_MEAN_C,
            Imgproc.THRESH_BINARY,
            7,
            0.0
        )//自适应滤波
        return binaryImage
    }

    /**
     * 原图矫正
     * src : 8UC1
     */
    private fun processRectify(source: Mat): Pair<Rect, Mat> {
        val binary = processGrayAndBinary(source)
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(
            binary,
            contours,
            hierarchy,
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_NONE
        )

        contours.sortByDescending {
            Imgproc.boundingRect(it).area()
        }
        // 获取最大轮廓
        val externalContour = contours.first()

        // 获取最大轮廓下的最小旋转矩形
        val externalContour2f = MatOfPoint2f()
        externalContour.convertTo(externalContour2f, CvType.CV_32F)
        val rect = Imgproc.minAreaRect(externalContour2f)//获取对应的最小矩形框，这个长方形是倾斜的
        val rectPoint = arrayOfNulls<Point>(4)
        rect.points(rectPoint)
        val angle = rect.angle
        val center = rect.center
        Log.d(App.TAG, "center =  ${center}, angle = $angle")

        // 获取矫正仿射矩阵
        val matrix = Imgproc.getRotationMatrix2D(center, angle, 1.0)//得到旋转矩阵算子，0.8缩放因子

        Imgproc.warpAffine(binary, binary, matrix, binary.size(), 1, 0, Scalar.all(0.0))
        Imgproc.warpAffine(source, source, matrix, binary.size(), 1, 0, Scalar.all(0.0))

        return Pair(getMaxContourRect(binary), source)
    }

    /**
     * 裁剪结果图片
     */
    private fun cropRectifyResult(roiRect: Rect, waitToSub: Mat): Mat {
        return waitToSub.submat(roiRect)
    }

    /**
     * 获取最大矩形
     */
    private fun getMaxContourRect(src: Mat): Rect {
        val binary = processGrayAndBinary(src)
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(
            binary,
            contours,
            hierarchy,
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_NONE
        )
        //获得矩形包围框,之所以先用boundRect，是为了使用它的area方法求面积，而RotatedRect类不具备该方法
        contours.sortByDescending {
            Imgproc.boundingRect(it).area()
        }
        return Imgproc.boundingRect(contours.first())//找出最大的那个矩形框（即最大轮廓）
    }

    /**
     * 顺时针90
     */
    private fun rotateClockWise(src: Mat): Mat {
        val dst = Mat()
        Core.rotate(src, dst, Core.ROTATE_90_CLOCKWISE)
        return dst
    }

    /**
     * 逆时针90
     */
    private fun rotateCounterClockWise(src: Mat): Mat {
        val dst = Mat()
        Core.rotate(src, dst, Core.ROTATE_90_COUNTERCLOCKWISE)
        return dst
    }

    fun pickImage(view: View) {
        if (!isProcessing) {
            EasyPhotos.createAlbum(
                this, false, false,
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


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_rectify, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.positive_rotate_90 -> {
                if (dstImage != null) {
                    dstImage = rotateClockWise(dstImage!!)
                    mBinding.ivResult.showMat(dstImage?.toRgb()!!)
                }
            }

            R.id.negative_rotate_90 -> {
                if (dstImage != null) {
                    dstImage = rotateCounterClockWise(dstImage!!)
                    mBinding.ivResult.showMat(dstImage?.toRgb()!!)
                }
            }
        }
        return true
    }
}