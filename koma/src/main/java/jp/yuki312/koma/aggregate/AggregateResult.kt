package jp.yuki312.koma.aggregate

import jp.yuki312.koma.FrameMetricsId

data class AggregateResult(
  val id: FrameMetricsId,
  val frozen: AggregateDurations? = null,
  val jank: AggregateDurations? = null,
  val total: AggregateDurations? = null,
  val input: AggregateDurations? = null,
  val layoutMeasure: AggregateDurations? = null,
  val draw: AggregateDurations? = null,
  val sync: AggregateDurations? = null,
  val command: AggregateDurations? = null,
  val swap: AggregateDurations? = null,
  val delay: AggregateDurations? = null,
  val anim: AggregateDurations? = null,
) {

  data class AggregateDurations(
    val durations: List<Int>,
    val count: Int,
    val ratio: Float,
    val maxDuration: Int?,
    val minDuration: Int?,
    val sumDuration: Int,
    val avgDuration: Double?,
    val medianDuration: Double?,
    val modeDuration: Set<Int>?,
  ) {
    companion object {
      fun from(durations: List<Int>, total: List<Int>): AggregateDurations? {
        if (durations.isNullOrEmpty() || total.isNullOrEmpty()) return null

        val ratio = if (total.isNotEmpty() && durations.isNotEmpty()) {
          durations.count().toFloat() / total.count().toFloat()
        } else {
          0F
        }
        return AggregateDurations(
          durations = durations,
          count = durations.size,
          ratio = ratio,
          maxDuration = durations.maxOrNull(),
          minDuration = durations.minOrNull(),
          sumDuration = durations.sum(),
          avgDuration = durations.average().takeIf { !it.isNaN() },
          medianDuration = durations.median(),
          modeDuration = durations.mode(),
        )
      }

      private fun List<Int>.median(): Double? {
        if (isEmpty()) return null
        return sorted().let {
          if (it.size % 2 == 0) {
            (it[it.size / 2] + it[(it.size - 1) / 2]).toDouble() / 2.0
          } else {
            it[it.size / 2].toDouble()
          }
        }
      }

      private fun List<Int>.mode(): Set<Int>? {
        if (isEmpty()) return null
        return groupingBy { it }
          .eachCount() // grouping e.g.{1=2, 2=1, 3=1, 4=2} (duration=count)
          .let { eachCount ->
            val max = eachCount.maxByOrNull { it.value }?.value
            eachCount.filterValues { count -> count == max } // filter max count
          }
          .keys // map to duration
      }
    }
  }
}