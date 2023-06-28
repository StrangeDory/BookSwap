package com.example.bookswap.utils

import android.app.Activity
import android.app.AlertDialog
import com.example.bookswap.R

class WaitingDialog(val mActivity: Activity) {

    private lateinit var dialog: AlertDialog

    fun startLoading() {
        val inflater = mActivity.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_wait, null)
        val builder = AlertDialog.Builder(mActivity)
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialog = builder.create()
        dialog.show()
    }

    fun hideLoading(){
        dialog.dismiss()
    }
}