package cn.onlyloveyd.demo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cn.onlyloveyd.demo.R
import cn.onlyloveyd.demo.databinding.ActivityMatOperationBinding

/**
 * Mat 操作
 * author: yidong
 * 2020/1/8
 */
class MatOperationActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMatOperationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_mat_operation)
    }
}