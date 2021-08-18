package jp.yuki312.koma

import androidx.fragment.app.Fragment

fun interface FragmentFrameMetricsFilter {
  fun filter(fragment: Fragment): Boolean
}