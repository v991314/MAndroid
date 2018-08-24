package com.me94me.mrecyclerview

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = object :BaseQuickAdapter<Int,BaseViewHolder>(R.layout.adapter){
            override fun convert(helper: BaseViewHolder?, item: Int?) {
                helper!!.getView<TextView>(R.id.text).text = "测试".plus(helper.adapterPosition)
            }
        }
        recyclerView.adapter = adapter
        adapter.setNewData(listOf(1,1,1,1,1,1,1))

    }
}
