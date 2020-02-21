package cn.onlyloveyd.demo

import android.R
import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.SimpleAdapter
import cn.onlyloveyd.demo.ui.*

class MainActivity : ListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listAdapter = SimpleAdapter(
            this, getData(),
            R.layout.simple_list_item_1, arrayOf("title"),
            intArrayOf(R.id.text1)
        )
    }

    private fun getData(): List<Map<String, Any>> {
        val myData = mutableListOf<Map<String, Any>>()

        myData.add(
            mapOf(
                "title" to "读取和保存图像",
                "intent" to activityToIntent(ReadAndWriteActivity::class.java.name)
            )
        )
        myData.add(
            mapOf(
                "title" to "Mat操作",
                "intent" to activityToIntent(MatOperationActivity::class.java.name)
            )
        )
        myData.add(
            mapOf(
                "title" to "图像像素值统计",
                "intent" to activityToIntent(PixelStatisticsActivity::class.java.name)
            )
        )
        myData.add(
            mapOf(
                "title" to "绘制几何图形",
                "intent" to activityToIntent(ShapeRenderActivity::class.java.name)
            )
        )

        myData.add(
            mapOf(
                "title" to "调整对比度和亮度",
                "intent" to activityToIntent(ContrastBrightnessActivity::class.java.name)
            )
        )
        myData.add(
            mapOf(
                "title" to "颜色模型装换",
                "intent" to activityToIntent(ColorTransferActivity::class.java.name)
            )
        )

        myData.add(
            mapOf(
                "title" to "通道分离合并",
                "intent" to activityToIntent(ChannelSplitMergeActivity::class.java.name)
            )
        )

        myData.add(
            mapOf(
                "title" to "图像二值化",
                "intent" to activityToIntent(ImageBinaryzationActivity::class.java.name)
            )
        )

        myData.add(
            mapOf(
                "title" to "LUT查找表",
                "intent" to activityToIntent(LutActivity::class.java.name)
            )
        )
        return myData
    }

    private fun activityToIntent(activity: String): Intent =
        Intent(Intent.ACTION_VIEW).setClassName(this.packageName, activity)

    override fun onListItemClick(listView: ListView, view: View, position: Int, id: Long) {
        val map = listView.getItemAtPosition(position) as Map<*, *>

        val intent = Intent(map["intent"] as Intent)
        intent.putExtra("title", map["title"] as String)
        intent.addCategory(Intent.CATEGORY_SAMPLE_CODE)
        startActivity(intent)
    }
}
