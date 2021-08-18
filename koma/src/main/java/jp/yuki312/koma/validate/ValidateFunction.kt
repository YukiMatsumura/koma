package jp.yuki312.koma.validate

import jp.yuki312.koma.aggregate.AggregateResult

interface ValidateFunction {
  fun execute(result: AggregateResult) : ValidateResult
}