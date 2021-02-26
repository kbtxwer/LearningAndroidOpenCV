package cn.onlyloveyd.demo.common

import android.content.Context
import android.view.ViewGroup
import cn.onlyloveyd.demo.databinding.RvItemTextBinding

/**
 * 入口
 * author: yidong
 * 2021/2/12
 */
class EntryAdapter(context: Context) :
    BindingAdapter<Pair<String, () -> Unit>>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        val binding = RvItemTextBinding.inflate(layoutInflater, parent, false)
        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        val binding = holder.binding as RvItemTextBinding
        val pair = getItem(position)
        binding.tvTitle.text = pair.first
        binding.root.setOnClickListener {
            pair.second()
        }
    }
}