package jp.yuki312.koma

import androidx.annotation.WorkerThread
import jp.yuki312.koma.aggregate.AggregateResult
import jp.yuki312.koma.validate.ValidateResult
import jp.yuki312.koma.track.TrackResult

interface KomaInterceptor {
  @WorkerThread
  fun onTracked(result: TrackResult): TrackResult

  @WorkerThread
  fun onAggregated(result: AggregateResult): AggregateResult

  @WorkerThread
  fun onValidated(result: ValidateResult): ValidateResult
}