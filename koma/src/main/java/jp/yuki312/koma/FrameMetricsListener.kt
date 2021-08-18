package jp.yuki312.koma

import jp.yuki312.koma.aggregate.AggregateResult
import jp.yuki312.koma.validate.ValidateResult

fun interface FrameMetricsListener {
  fun onFrameMetricsResult(
    id: FrameMetricsId,
    config: KomaConfig,
    aggregateResult: AggregateResult,
    validateResult: ValidateResult,
  )
}