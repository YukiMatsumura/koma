package jp.yuki312.koma

import androidx.annotation.WorkerThread
import jp.yuki312.koma.aggregate.AggregateResult
import jp.yuki312.koma.validate.ValidateResult

fun interface FrameMetricsListener {
  @WorkerThread
  fun onFrameMetricsResult(
    id: FrameMetricsId,
    config: KomaConfig,
    aggregateResult: AggregateResult,
    validateResult: ValidateResult,
  )
}