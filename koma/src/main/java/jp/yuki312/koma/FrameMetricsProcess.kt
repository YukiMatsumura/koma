package jp.yuki312.koma

import android.app.Activity
import jp.yuki312.koma.aggregate.AggregateFunction
import jp.yuki312.koma.track.Tracker
import jp.yuki312.koma.validate.ValidateFunction

class FrameMetricsProcess internal constructor(
  private val enable: Boolean,
  private val tracker: Tracker,
  private val aggregator: AggregateFunction,
  private val validator: ValidateFunction,
  private val interceptor: KomaInterceptor?,
  val defaultConfig: KomaConfig,
  private val defaultListener: FrameMetricsListener,
) {

  fun start(
    id: FrameMetricsId,
    activity: Activity,
    config: KomaConfig = defaultConfig,
    listener: FrameMetricsListener = defaultListener,
  ) {
    if (!enable) return

    tracker.startTracking(
      id = id,
      activity = activity,
      config = config,
    ) { tracked ->
      val trackResult = interceptor?.onTracked(tracked) ?: tracked

      val aggregateResult = aggregator.execute(trackResult)
      val aggregate = interceptor?.onAggregated(aggregateResult) ?: aggregateResult

      val validateResult = validator.execute(aggregateResult)
      val validate = interceptor?.onValidated(validateResult) ?: validateResult

      listener.onFrameMetricsResult(
        id = id,
        config = config,
        aggregateResult = aggregate,
        validateResult = validate,
      )
    }
  }

  fun stop(id: FrameMetricsId) {
    if (!enable) return

    tracker.stopTracking(id)
  }

  fun stopAll() {
    if (!enable) return

    tracker.stopAll()
  }
}