package com.me94me.practice_customize_view.ui.foundation.drawtext

import androidx.viewpager.widget.ViewPager
import com.me94me.practice_customize_view.R
import com.me94me.practice_customize_view.adapter.viewpager.DrawTextPagerAdapter
import com.me94me.practice_customize_view.base.BaseActivity
import com.me94me.practice_customize_view.databinding.ActivityDrawTextBinding

class DrawTextActivity : BaseActivity() {
    var binding:ActivityDrawTextBinding?=null
    override fun layoutId(): Int {
        return R.layout.activity_draw_text
    }

    override fun initView() {
        binding = dataBinding as ActivityDrawTextBinding
        binding!!.viewPager.adapter = DrawTextPagerAdapter(supportFragmentManager)

        supportActionBar!!.title = DrawTextFragment.titles[0]
    }

    override fun initClick() {
        binding!!.viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                supportActionBar!!.title = DrawTextFragment.titles[position]
            }
        })
    }

}
