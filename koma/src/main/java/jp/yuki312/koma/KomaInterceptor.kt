package jp.yuki312.koma

import jp.yuki312.koma.aggregate.AggregateResult
import jp.yuki312.koma.validate.ValidateResult
import jp.yuki312.koma.track.TrackResult

interface KomaInterceptor {
  fun onTracked(result: TrackResult): TrackResult
  fun onAggregated(result: AggregateResult): AggregateResult
  fun onValidated(result: ValidateResult): ValidateResult
}