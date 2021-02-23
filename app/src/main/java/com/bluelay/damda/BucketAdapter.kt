package com.bluelay.damda

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import java.text.SimpleDateFormat

class BucketAdapter (val context : Context, val bucketList : ArrayList<Bucket>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view : View = LayoutInflater.from(context).inflate(R.layout.adapter_view_bucket, null)

        val cbBucket = view.findViewById<CheckBox>(R.id.cbBucket)
        val etBucket = view.findViewById<EditText>(R.id.etBucket)
        val tvBucket = view.findViewById<TextView>(R.id.tvBucketDate)

        val bucket = bucketList[position]
        cbBucket.isChecked = bucket.checked == 1
        if(cbBucket.isChecked == true) {
            etBucket.setTextColor(Color.parseColor("#969191"))
        }
        else {
            etBucket.setTextColor(Color.parseColor("#000000"))
        }
        etBucket.setText(bucket.content)
        tvBucket.setText(bucket.date)

        etBucket.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                bucketList[position].content = s.toString()
            }
        })

        val formatter = SimpleDateFormat("yyyy.MM.dd")

        cbBucket.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                bucket.checked = 1
                bucket.date = formatter.format(System.currentTimeMillis())
                etBucket.setTextColor(Color.parseColor("#969191"))
            }
            else {
                bucket.checked = 0
                bucket.date = ""
                etBucket.setTextColor(Color.parseColor("#000000"))
            }
            tvBucket.setText(bucket.date)
        }

        return view
    }
    override fun getCount(): Int {
        return bucketList.size
    }

    override fun getItem(position: Int): Any {
        return bucketList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }
}