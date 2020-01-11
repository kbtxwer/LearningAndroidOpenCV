package cn.onlyloveyd.demo.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import cn.onlyloveyd.demo.R
import cn.onlyloveyd.demo.databinding.ActivityReadAndWriteBinding
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import java.io.File


/**
 * 读写图像
 * author: yidong
 * 2020/1/7
 */
class ReadAndWriteActivity : AppCompatActivity() {
    private val STORAGE_PERMISSION_REQUEST_CODE = 500
    private lateinit var mBinding: ActivityReadAndWriteBinding
    private val mLenaPath =
        Environment.getExternalStorageDirectory().path + File.separator + "lena.png"
    private var currentImreadMode = Imgcodecs.IMREAD_UNCHANGED
        set(value) {
            field = value
            onImreadModeChange()
        }
    private var currentMat = Mat()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_read_and_write)
        supportActionBar?.title = intent.getStringExtra("title")
        if (checkStoragePermission()) {
            onImreadModeChange()
        } else {
            requestStoragePermission()
        }
        mBinding.btSave.setOnClickListener {
            if (checkStoragePermission()) {
                saveMatToStorage(currentMat)
            } else {
                requestStoragePermission()
            }
        }
    }

    private fun loadLenaFromFile() {
        currentMat = Imgcodecs.imread(mLenaPath, currentImreadMode)
        val bitmap =
            Bitmap.createBitmap(currentMat.width(), currentMat.height(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(currentMat, bitmap)
        mBinding.ivLena.setImageBitmap(bitmap)
    }

    private fun onImreadModeChange() {
        loadLenaFromFile()
        mBinding.tvMode.text = getModeName(currentImreadMode)
    }

    private fun saveMatToStorage(source: Mat) {
        val file =
            File(Environment.getExternalStorageDirectory().path + File.separator + "${System.currentTimeMillis()}.jpg")
        if (!file.exists()) {
            file.createNewFile()
        }
        Imgcodecs.imwrite(file.path, source)
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
            loadLenaFromFile()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_imread, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.IMREAD_UNCHANGED -> currentImreadMode = Imgcodecs.IMREAD_UNCHANGED
            R.id.IMREAD_GRAYSCALE -> currentImreadMode = Imgcodecs.IMREAD_GRAYSCALE
            R.id.IMREAD_COLOR -> currentImreadMode = Imgcodecs.IMREAD_COLOR
            R.id.IMREAD_ANYDEPTH -> currentImreadMode = Imgcodecs.IMREAD_ANYDEPTH
            R.id.IMREAD_ANYCOLOR -> currentImreadMode = Imgcodecs.IMREAD_ANYCOLOR
            R.id.IMREAD_LOAD_GDAL -> currentImreadMode = Imgcodecs.IMREAD_LOAD_GDAL
            R.id.IMREAD_REDUCED_GRAYSCALE_2 -> currentImreadMode =
                Imgcodecs.IMREAD_REDUCED_GRAYSCALE_2
            R.id.IMREAD_REDUCED_COLOR_2 -> currentImreadMode = Imgcodecs.IMREAD_REDUCED_COLOR_2
            R.id.IMREAD_REDUCED_GRAYSCALE_4 -> currentImreadMode =
                Imgcodecs.IMREAD_REDUCED_GRAYSCALE_4
            R.id.IMREAD_REDUCED_COLOR_4 -> currentImreadMode = Imgcodecs.IMREAD_REDUCED_COLOR_4
            R.id.IMREAD_REDUCED_GRAYSCALE_8 -> currentImreadMode =
                Imgcodecs.IMREAD_REDUCED_GRAYSCALE_8
            R.id.IMREAD_REDUCED_COLOR_8 -> currentImreadMode = Imgcodecs.IMREAD_REDUCED_COLOR_8
            R.id.IMREAD_IGNORE_ORIENTATION -> currentImreadMode =
                Imgcodecs.IMREAD_IGNORE_ORIENTATION
        }
        return true
    }

    private fun getModeName(mode: Int): String {
        return when (mode) {
            Imgcodecs.IMREAD_UNCHANGED -> "IMREAD_UNCHANGED"
            Imgcodecs.IMREAD_GRAYSCALE -> "IMREAD_GRAYSCALE"
            Imgcodecs.IMREAD_COLOR -> "IMREAD_COLOR"
            Imgcodecs.IMREAD_ANYDEPTH -> "IMREAD_ANYDEPTH"
            Imgcodecs.IMREAD_ANYCOLOR -> "IMREAD_ANYCOLOR"
            Imgcodecs.IMREAD_LOAD_GDAL -> "IMREAD_LOAD_GDAL"
            Imgcodecs.IMREAD_REDUCED_GRAYSCALE_2 -> "IMREAD_REDUCED_GRAYSCALE_2"
            Imgcodecs.IMREAD_REDUCED_COLOR_2 -> "IMREAD_REDUCED_COLOR_2"
            Imgcodecs.IMREAD_REDUCED_GRAYSCALE_4 -> "IMREAD_REDUCED_GRAYSCALE_4"
            Imgcodecs.IMREAD_REDUCED_COLOR_4 -> "IMREAD_REDUCED_COLOR_4"
            Imgcodecs.IMREAD_REDUCED_GRAYSCALE_8 -> "IMREAD_REDUCED_GRAYSCALE_8"
            Imgcodecs.IMREAD_REDUCED_COLOR_8 -> "IMREAD_REDUCED_COLOR_8"
            Imgcodecs.IMREAD_IGNORE_ORIENTATION -> "IMREAD_IGNORE_ORIENTATION"
            else -> "IMREAD_UNCHANGED"
        }
    }
}