package jp.yuki312.koma.validate

import androidx.annotation.WorkerThread
import jp.yuki312.koma.aggregate.AggregateResult

fun interface ValidateFunction {
  @WorkerThread
  fun execute(result: AggregateResult) : ValidateResult
}