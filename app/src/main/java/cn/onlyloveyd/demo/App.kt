package cn.onlyloveyd.demo

import android.app.Application
import cn.onlyloveyd.demo.ext.MMKVKey
import cn.onlyloveyd.demo.ext.copyFromAssets
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 主程序
 * author: yidong
 * 2020/1/7
 */
class App : Application() {
    companion object {
        const val TAG = "LearningAndroidOpenCV"
    }

    init {
        System.loadLibrary("opencv_java4")
        System.loadLibrary("native-lib")
    }

    override fun onCreate() {
        super.onCreate()
        val rootDir: String = MMKV.initialize(this)
        println("mmkv root: $rootDir")
        initWeChatModelFile()
    }

    /**
     * 加载模型
     */
    private fun initWeChatModelFile() {
        GlobalScope.launch(Dispatchers.IO) {
            MMKV.defaultMMKV()?.encode(
                MMKVKey.WeChatQRCodeDetectProtoTxt,
                copyFromAssets(R.raw.detect_prototxt, "wechat_qrcode", "detect.prototxt")
            )
            MMKV.defaultMMKV()?.encode(
                MMKVKey.WeChatQRCodeDetectCaffeModel,
                copyFromAssets(R.raw.detect_caffemodel, "wechat_qrcode", "detect.caffemodel")
            )
            MMKV.defaultMMKV()?.encode(
                MMKVKey.WeChatQRCodeSrProtoTxt,
                copyFromAssets(R.raw.sr_prototxt, "wechat_qrcode", "sr.prototxt")
            )
            MMKV.defaultMMKV()?.encode(
                MMKVKey.WeChatQRCodeSrCaffeModel,
                copyFromAssets(R.raw.sr_caffemodel, "wechat_qrcode", "sr.caffemodel")
            )
        }
    }
}
