package com.utsman.covid19

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory


fun logi(msg: String?) = Log.d("COVID-19", msg)
fun loge(msg: String?) = Log.e("COVID-19", msg)
fun Context.toast(msg: String?) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun bitmapFromVector(context: Context, @DrawableRes icon: Int, text: String): BitmapDescriptor {

    val background = ContextCompat.getDrawable(context, icon)
    background!!.setBounds(0, 0, background.intrinsicWidth, background.intrinsicHeight)

    val bitmap = Bitmap.createBitmap(background.intrinsicWidth, background.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    background.draw(canvas)

    val color = Paint()
    color.textSize = 30f
    color.color = Color.WHITE
    //canvas.drawText(text,background.intrinsicWidth/2.5f, background.intrinsicHeight/1.8f, color)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

fun createDrawableFromView(context: Context, view: View): Bitmap? {
    val displayMetrics = DisplayMetrics()
    (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
    view.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
    view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
    view.buildDrawingCache()
    val bitmap = Bitmap.createBitmap(
        view.measuredWidth,
        view.measuredHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return bitmap
}