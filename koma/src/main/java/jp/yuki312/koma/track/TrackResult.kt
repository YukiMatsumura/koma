package jp.yuki312.koma.track

import android.util.SparseIntArray
import androidx.core.app.FrameMetricsAggregator
import androidx.core.util.forEach
import jp.yuki312.koma.KomaConfig
import jp.yuki312.koma.FrameMetricsId

data class TrackResult(
  val id: FrameMetricsId,
  val config: KomaConfig,
  val durations: TrackDurations,
) {

  data class TrackDurations(
    val total: List<Int> = emptyList(),
    val input: List<Int> = emptyList(),
    val layoutMeasure: List<Int> = emptyList(),
    val draw: List<Int> = emptyList(),
    val sync: List<Int> = emptyList(),
    val command: List<Int> = emptyList(),
    val swap: List<Int> = emptyList(),
    val delay: List<Int> = emptyList(),
    val anim: List<Int> = emptyList(),
  ) {

    companion object {
      fun from(metrics: Array<SparseIntArray?>?): TrackDurations {
        return TrackDurations(
          total = metrics?.getOrNull(FrameMetricsAggregator.TOTAL_INDEX).toList(),
          input = metrics?.getOrNull(FrameMetricsAggregator.INPUT_INDEX).toList(),
          layoutMeasure = metrics?.getOrNull(FrameMetricsAggregator.LAYOUT_MEASURE_INDEX).toList(),
          draw = metrics?.getOrNull(FrameMetricsAggregator.DRAW_INDEX).toList(),
          sync = metrics?.getOrNull(FrameMetricsAggregator.SYNC_INDEX).toList(),
          command = metrics?.getOrNull(FrameMetricsAggregator.COMMAND_INDEX).toList(),
          swap = metrics?.getOrNull(FrameMetricsAggregator.SWAP_INDEX).toList(),
          delay = metrics?.getOrNull(FrameMetricsAggregator.DELAY_INDEX).toList(),
          anim = metrics?.getOrNull(FrameMetricsAggregator.ANIMATION_INDEX).toList()
        )
      }

      private fun SparseIntArray?.toList(): List<Int> {
        this ?: return emptyList()
        val result = mutableListOf<Int>()
        this.forEach { duration, count -> repeat(count) { result.add(duration) } }
        return result
      }
    }
  }
}