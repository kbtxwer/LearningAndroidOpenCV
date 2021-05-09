package cn.onlyloveyd.demo.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import cn.onlyloveyd.demo.App
import cn.onlyloveyd.demo.R
import cn.onlyloveyd.demo.databinding.ActivityOrbFlannBinding
import cn.onlyloveyd.demo.ext.showMat
import cn.onlyloveyd.demo.ext.toGray
import cn.onlyloveyd.demo.ext.wrapCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfDMatch
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.Features2d.drawMatches
import org.opencv.features2d.FlannBasedMatcher
import org.opencv.features2d.ORB

/**
 * ORB FLANN 匹配
 * author: yidong
 * 2021-03-28
 */
class ORBFLANNMatchActivity : AppCompatActivity() {

    private val firstBgr by lazy {
        Utils.loadResource(this, R.drawable.lena)
    }
    private val firstGray by lazy { firstBgr.toGray() }

    private val secondBgr by lazy {
        Utils.loadResource(this, R.drawable.lena_250)
    }
    private val secondGray by lazy { secondBgr.toGray() }

    private val mBinding: ActivityOrbFlannBinding by lazy {
        ActivityOrbFlannBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        wrapCoroutine({ showLoading() }, { doORBFlannMatch() }, { hideLoading() })
    }

    private fun showLoading() {
        mBinding.isLoading = true
    }

    private fun hideLoading() {
        mBinding.isLoading = false
    }

    private fun doORBFlannMatch() {
        val start = System.currentTimeMillis()
        val firstKeyPoints = MatOfKeyPoint()
        val secondKeyPoints = MatOfKeyPoint()

        val firstDescriptor = Mat()
        val secondDescriptor = Mat()

        orbFeatures(firstGray, firstKeyPoints, firstDescriptor)
        orbFeatures(secondGray, secondKeyPoints, secondDescriptor)
        if (firstDescriptor.type() != CvType.CV_32F && secondDescriptor.type() != CvType.CV_32F) {
            firstDescriptor.convertTo(firstDescriptor, CvType.CV_32F)
            secondDescriptor.convertTo(secondDescriptor, CvType.CV_32F)
        }

        val matches = MatOfDMatch()
        val matcher = FlannBasedMatcher.create()
//        val matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED)
//        val matcher = DescriptorMatcher.create("FlannBased")
        matcher.match(firstDescriptor, secondDescriptor, matches)
        Log.e(App.TAG, " matchers size = ${matches.size()}")
        Log.e(App.TAG, " Flann Waster Time = ${System.currentTimeMillis() - start}")

        val list = matches.toList()
        list.sortBy { it.distance }
        Log.e(App.TAG, "Min = ${list.first().distance}")
        val min = list.first().distance
        val max = list.last().distance

        val goodMatchers = list.filter {
            it.distance < max.times(0.4)
        }

        Log.e(App.TAG, " good matchers size = ${goodMatchers.size}")

        val result = Mat()
        val matOfDMatch = MatOfDMatch()
        matOfDMatch.fromList(goodMatchers)
        drawMatches(firstGray, firstKeyPoints, secondGray, secondKeyPoints, matOfDMatch, result)

        GlobalScope.launch(Dispatchers.Main) {
            mBinding.ivResult.showMat(result)
        }
    }

    private fun orbFeatures(source: Mat, keyPoints: MatOfKeyPoint, descriptor: Mat) {
        val orbDetector = ORB.create(
            1000,
            1.2f
        )
        orbDetector.detect(source, keyPoints)
        orbDetector.compute(source, keyPoints, descriptor)
        Log.e(App.TAG, "count = ${keyPoints.size()}")
    }

    override fun onDestroy() {
        firstBgr.release()
        secondBgr.release()
        firstGray.release()
        secondGray.release()
        super.onDestroy()
    }
}
