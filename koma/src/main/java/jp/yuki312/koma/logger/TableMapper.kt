package jp.yuki312.koma.logger

import jp.yuki312.koma.KomaConfig
import jp.yuki312.koma.aggregate.AggregateResult
import jp.yuki312.koma.aggregate.AggregateResult.AggregateDurations
import jp.yuki312.koma.validate.ValidateResult
import jp.yuki312.koma.validate.ValidateResult.MetricsValidation

object TableMapper {

  fun mapToMetricsTable(result: AggregateResult): MetricsTable {
    return MetricsTable(
      total = result.total.mapToRow(),
      frozen = result.frozen.mapToRow(),
      jank = result.jank.mapToRow(),
      input = result.input.mapToRow(),
      layoutMeasure = result.layoutMeasure.mapToRow(),
      draw = result.draw.mapToRow(),
      sync = result.sync.mapToRow(),
      command = result.command.mapToRow(),
      swap = result.swap.mapToRow(),
      delay = result.delay.mapToRow(),
      anim = result.anim.mapToRow(),
    )
  }

  fun mapToValidateTable(result: ValidateResult): ValidateTable {
    return ValidateTable(
      validateList = result.validatedList.map { it.mapToRow() }
    )
  }

  fun mapToMiscTable(config: KomaConfig): MiscTable {
    return MiscTable(
      frameRate = "%d fps".formatOrHyphen(config.frameRate),
      frozenFrameDurationThreshold = "%d ms".formatOrHyphen(config.frozenFrameDurationThreshold),
      jankFrameDurationThreshold = "%d ms".formatOrHyphen(config.jankFrameDurationThreshold),
    )
  }

  private fun AggregateDurations?.mapToRow(): MetricsTable.Row? {
    this ?: return null
    return MetricsTable.Row(
      count = "%d F".formatOrHyphen(this.count),
      ratio = "%.1f %%".formatOrHyphen(this.ratio * 100f),
      maxDuration = "%d ms".formatOrHyphen(this.maxDuration),
      minDuration = "%d ms".formatOrHyphen(this.minDuration),
      sumDuration = "%d ms".formatOrHyphen(this.sumDuration),
      avgDuration = "%.1f ms".formatOrHyphen(this.avgDuration),
      medianDuration = "%.1f ms".formatOrHyphen(this.medianDuration),
      modeDuration = this.modeDuration?.joinToString(", ")
        ?.let { "$it ms" }
        ?: "-", // 100, 200 ms
    )
  }

  private fun MetricsValidation<*>.mapToRow(): ValidateTable.Row {
    return ValidateTable.Row(
      label = name,
      passed = if (isPassed) "OK" else "NG",
      value = formatValue(value),
      threshold = formatValue(threshold),
    )
  }

  private fun formatValue(value: Any?): String {
    return when (value) {
      null -> "-"
      is Short, Int, Long -> "%d".format(value)
      is Float, Double -> "%.1f".format(value)
      is String -> value
      else -> value.toString()
    }
  }

  private fun String.formatOrHyphen(arg: Any?): String {
    return if (arg == null) "-" else format(arg)
  }
}