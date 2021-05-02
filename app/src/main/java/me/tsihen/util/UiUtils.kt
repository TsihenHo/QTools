package me.tsihen.util

import android.content.Context

fun dip2px(ctx: Context, dp: Float): Int = (dp * ctx.resources.displayMetrics.density + 0.5f).toInt()
fun dip2sp(ctx: Context, sp: Float): Int = (sp * ctx.resources.displayMetrics.scaledDensity + 0.5f).toInt()
