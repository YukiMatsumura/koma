package jp.yuki312.koma.track

import android.app.Activity
import android.util.SparseIntArray
import androidx.annotation.VisibleForTesting
import androidx.core.app.FrameMetricsAggregator
import jp.yuki312.koma.KomaConfig
import jp.yuki312.koma.FrameMetricsId
import jp.yuki312.koma.track.TrackResult.TrackDurations

internal class Tracker @VisibleForTesting constructor(
  private val aggregatorFactory: AggregatorFactory
) {

  fun interface AggregatorFactory {
    fun create(config: KomaConfig): FrameMetricsAggregator
  }

  fun interface FrameTrackingCallback {
    fun invoke(result: TrackResult)
  }

  private data class Tracker(
    val aggregator: FrameMetricsAggregator,
    val config: KomaConfig,
    val callback: FrameTrackingCallback,
  )

  private val trackers = HashMap<FrameMetricsId, Tracker>()

  constructor() : this(
    aggregatorFactory = { config ->
      FrameMetricsAggregator(
        if (config.analyzeMode) {
          FrameMetricsAggregator.EVERY_DURATION
        } else {
          FrameMetricsAggregator.TOTAL_DURATION
        }
      )
    }
  )

  fun startTracking(
    id: FrameMetricsId,
    activity: Activity,
    config: KomaConfig,
    callback: FrameTrackingCallback,
  ): Boolean {
    if (trackers.containsKey(id)) return false // already started

    trackers[id] = Tracker(
      config = config,
      callback = callback,
      aggregator = aggregatorFactory.create(config).apply { add(activity) },
    )

    return true
  }

  fun stopTracking(id: FrameMetricsId): Boolean {
    val t = trackers.remove(id) ?: return false // already stopped or not started yet

    val durations: Array<SparseIntArray?>? = t.aggregator.metrics
    runCatching {
      // Stop monitoring/recording window frame metrics
      t.aggregator.stop()
    }.also {
      /*
       * Resets the metrics data.
       * {@link FrameMetricsAggregator#stop()} will throw exceptions for hardware acceleration disabled activities.
       */
      t.aggregator.reset()
    }

    t.callback.invoke(
      TrackResult(
        id = id,
        durations = TrackDurations.from(durations),
        config = t.config,
      )
    )

    return true
  }

  fun stopAll() {
    trackers.keys.toList().forEach { stopTracking(it) }
  }
}