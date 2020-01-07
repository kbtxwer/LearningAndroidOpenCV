package cn.onlyloveyd.demo.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import cn.onlyloveyd.demo.R
import cn.onlyloveyd.demo.databinding.ActivityReadAndWriteBinding
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File

/**
 * 读写图像
 * author: yidong
 * 2020/1/7
 */
class ReadAndWriteActivity : AppCompatActivity() {
    private val STORAGE_PERMISSION_REQUEST_CODE = 500
    private lateinit var mBinding: ActivityReadAndWriteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_read_and_write)
        supportActionBar?.title = intent.getStringExtra("title")
        loadLena()
        if (checkStoragePermission()) {
            saveGray()
        } else {
            requestStoragePermission()
        }
    }

    private fun loadLena() {
        val bgr = Utils.loadResource(this, R.drawable.lena)
        val source = Mat()
        Imgproc.cvtColor(bgr, source, Imgproc.COLOR_BGR2RGB)
        bgr.release()
        val bitmap = Bitmap.createBitmap(source.width(), source.height(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(source, bitmap)
        mBinding.ivLena.setImageBitmap(bitmap)
    }

    private fun saveGray() {
        val gray = Mat(Size(248.0, 248.0), CvType.CV_8UC3)
        gray.setTo(Scalar(127.0, 127.0, 127.0))
        val grayBitmap = Bitmap.createBitmap(gray.width(), gray.height(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(gray, grayBitmap)
        mBinding.ivGray.setImageBitmap(grayBitmap)

        val file =
            File(Environment.getExternalStorageDirectory().path + File.separator + "gray.jpg")
        if (!file.exists()) {
            file.createNewFile()
        }
        Imgcodecs.imwrite(file.path, gray)
        gray.release()
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveGray()
        } else {
            AlertDialog.Builder(this)
                .setTitle("权限管理")
                .setMessage("请允许读写外部存储权限")
                .setPositiveButton(
                    "确认"
                ) { _, _ -> requestStoragePermission() }
                .setNegativeButton(
                    "取消"
                ) { dialog, _ ->
                    dialog.dismiss()
                    Toast.makeText(this@ReadAndWriteActivity, "无法保存生成的灰色图片", Toast.LENGTH_SHORT)
                        .show()
                }
                .show()
        }
    }
}