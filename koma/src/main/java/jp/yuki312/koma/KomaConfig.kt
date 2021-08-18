package jp.yuki312.koma

import androidx.annotation.IntRange

data class KomaConfig(
  @IntRange(from = 1)
  val frameRate: Int, /* fps */
  val analyzeMode: Boolean,
  val frozenFrameDurationThreshold: Int, /* ms */
  val jankFrameDurationThreshold: Int = 1000 / frameRate, // e.g. 60fps -> 16ms /* ms */
)
