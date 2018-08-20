package com.me94me.practice_customize_view.ui.foundation.paint.color


import com.me94me.practice_customize_view.R
import com.me94me.practice_customize_view.base.BaseFragment
import com.me94me.practice_customize_view.databinding.FragmentF2PaintBinding

class F1ColorFragment : BaseFragment() {
    companion object {
        var titles = listOf("","")
    }
    var binding:FragmentF2PaintBinding?=null
    var position = 0
    override fun layoutId(): Int {
        return R.layout.fragment_f2_paint
    }

    override fun initView() {
        binding = dataBinding as FragmentF2PaintBinding
    }

    override fun initData() {
        position = arguments!!.getInt("position")
        when(position){
            0->{

            }
        }
    }

}
