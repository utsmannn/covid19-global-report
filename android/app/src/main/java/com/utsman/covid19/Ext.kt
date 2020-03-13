package com.utsman.covid19

import android.content.Context
import android.util.Log
import android.widget.Toast

fun logi(msg: String?) = Log.d("COVID-19", msg)
fun loge(msg: String?) = Log.e("COVID-19", msg)
fun Context.toast(msg: String?) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()