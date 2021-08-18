package jp.yuki312.koma.aggregate

import jp.yuki312.koma.aggregate.AggregateResult.AggregateDurations
import jp.yuki312.koma.track.TrackResult

internal class AggregateFunction {

  fun execute(trackResult: TrackResult): AggregateResult {
    val config = trackResult.config
    val durations = trackResult.durations
    val total = durations.total
    val frozen = mutableListOf<Int>()
    val jank = mutableListOf<Int>()

    total.forEach {
      if (it >= config.frozenFrameDurationThreshold) frozen.add(it)
      else if (it >= config.jankFrameDurationThreshold) jank.add(it)
    }

    return if (!config.analyzeMode) {
      AggregateResult(
        id = trackResult.id,
        total = AggregateDurations.from(total, total),
        frozen = AggregateDurations.from(frozen, total),
        jank = AggregateDurations.from(jank, total),
      )
    } else {
      AggregateResult(
        id = trackResult.id,
        total = AggregateDurations.from(total, total),
        frozen = AggregateDurations.from(frozen, total),
        jank = AggregateDurations.from(jank, total),
        input = AggregateDurations.from(durations.input, total),
        layoutMeasure = AggregateDurations.from(durations.layoutMeasure, total),
        draw = AggregateDurations.from(durations.draw, total),
        sync = AggregateDurations.from(durations.sync, total),
        command = AggregateDurations.from(durations.command, total),
        swap = AggregateDurations.from(durations.swap, total),
        delay = AggregateDurations.from(durations.delay, total),
        anim = AggregateDurations.from(durations.anim, total),
      )
    }
  }
}
