package jp.yuki312.koma

import android.app.Activity

fun interface ActivityFrameMetricsFilter {
  fun filter(activity: Activity): Boolean
}