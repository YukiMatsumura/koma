package jp.yuki312.koma.logger

import jp.yuki312.koma.KomaConfig
import jp.yuki312.koma.FrameMetricsId
import jp.yuki312.koma.aggregate.AggregateResult
import jp.yuki312.koma.validate.ValidateResult

abstract class FrameMetricsLogger {
  fun output(
    id: FrameMetricsId,
    config: KomaConfig,
    aggregateResult: AggregateResult,
    validateResult: ValidateResult,
  ) {
    printMessage(
      highlight = highlight(
        id = id,
        config = config,
        aggregateResult = aggregateResult,
        validateResult = validateResult,
      ),
      output = StringBuilder().apply {
        buildLoggingMessage(
          id = id,
          config = config,
          aggregateResult = aggregateResult,
          validateResult = validateResult,
          output = this
        )
      }
    )
  }

  protected abstract fun buildLoggingMessage(
    id: FrameMetricsId,
    config: KomaConfig,
    aggregateResult: AggregateResult,
    validateResult: ValidateResult,
    output: StringBuilder
  )

  protected abstract fun highlight(
    id: FrameMetricsId,
    config: KomaConfig,
    aggregateResult: AggregateResult,
    validateResult: ValidateResult,
  ): Boolean

  protected abstract fun printMessage(
    highlight: Boolean,
    output: StringBuilder
  )
}