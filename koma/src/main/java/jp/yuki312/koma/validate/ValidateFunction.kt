package jp.yuki312.koma.validate

import jp.yuki312.koma.aggregate.AggregateResult

fun interface ValidateFunction {
  fun execute(result: AggregateResult) : ValidateResult
}