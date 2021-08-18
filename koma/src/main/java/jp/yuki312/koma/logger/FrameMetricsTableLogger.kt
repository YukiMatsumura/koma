package jp.yuki312.koma.logger

import com.jakewharton.picnic.BorderStyle
import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import jp.yuki312.koma.KomaConfig
import jp.yuki312.koma.FrameMetricsId
import jp.yuki312.koma.aggregate.AggregateResult
import jp.yuki312.koma.validate.ValidateResult

class FrameMetricsTableLogger(
  private val output: (highlight: Boolean, output: StringBuilder) -> Unit
) : FrameMetricsLogger() {

  override fun buildLoggingMessage(
    id: FrameMetricsId,
    config: KomaConfig,
    aggregateResult: AggregateResult,
    validateResult: ValidateResult,
    output: StringBuilder
  ) {
    output.apply {
      appendLine("--->")
      appendLine("ID: ${id.value}")
      appendLine()
      appendMetricsTable(TableMapper.mapToMetricsTable(aggregateResult))
      appendLine()
      appendValidateTable(TableMapper.mapToValidateTable(validateResult))
      appendLine()
      appendMiscTable(TableMapper.mapToMiscTable(config))
      appendLine("<---")
    }
  }

  override fun highlight(
    id: FrameMetricsId,
    config: KomaConfig,
    aggregateResult: AggregateResult,
    validateResult: ValidateResult
  ): Boolean {
    return validateResult.validatedList.any { !it.isPassed }
  }

  override fun printMessage(highlight: Boolean, output: StringBuilder) {
    output(highlight, output)
  }

  private fun StringBuilder.appendMetricsTable(table: MetricsTable?) {
    if (table == null) {
      appendLine("Metrics Table: <NO DATA>")
      return
    }

    val header = arrayOf(
      "Frame metrics",
      "count",
      "ratio",
      "max",
      "min",
      "sum",
      "avg",
      "median",
      "mode",
    )

    fun columns(label: String, row: MetricsTable.Row) : Array<String> =
      arrayOf(
        label,
        row.count,
        row.ratio,
        row.maxDuration,
        row.minDuration,
        row.sumDuration,
        row.avgDuration,
        row.medianDuration,
        row.modeDuration,
      )


    val body: Array<Array<String>> = listOf(
      "Total" to table.total,
      "Jank" to table.jank,
      "Frozen" to table.frozen,
      "Input" to table.input,
      "LayoutMeasure" to table.layoutMeasure,
      "Draw" to table.draw,
      "Sync" to table.sync,
      "Command" to table.command,
      "Swap" to table.swap,
      "Delay" to table.delay,
      "Anim" to table.anim,
    )
      .mapNotNull {
        val row = it.second ?: return@mapNotNull null
        columns(it.first, row)
      }
      .toTypedArray()

    if (body.isNullOrEmpty()) {
      appendLine("Metrics Table: <NO DATA>")
      return
    }

    appendLine(table(header, body))
  }

  private fun StringBuilder.appendValidateTable(table: ValidateTable?) {
    if (table == null || table.validateList.isNullOrEmpty()) {
      appendLine("Validate Table: <NO DATA>")
      return
    }
    val header = arrayOf(
      "Validation name",
      "result",
      "value",
      "threshold",
    )

    fun columns(row: ValidateTable.Row) =
      arrayOf(
        row.label,
        row.passed,
        row.value,
        row.threshold,
      )

    val body = table.validateList.map { columns(it) }.toTypedArray()

    appendLine(table(header, body))
  }

  private fun StringBuilder.appendMiscTable(table: MiscTable?) {
    table ?: return
    val header = arrayOf(
      "Misc.",
      "value",
    )

    fun columns(label: String, value: String) = arrayOf(label, value)

    val body = arrayOf(
      columns("Frame rate", table.frameRate),
      columns("Frozen frame duration threshold", table.frozenFrameDurationThreshold),
      columns("Jank frame duration threshold", table.jankFrameDurationThreshold),
    )

    appendLine(table(header, body))
  }

  private fun table(
    header: Array<String>,
    body: Array<Array<String>>,
  ) = table {
    style {
      borderStyle = BorderStyle.Hidden
    }
    cellStyle {
      alignment = TextAlignment.MiddleRight
      paddingLeft = 1
      paddingRight = 1
      borderLeft = true
      borderRight = true
    }
    header {
      cellStyle {
        border = true
        alignment = TextAlignment.BottomLeft
      }
      row(*header)
    }
    body {
      body.forEach { row(*it) }
    }
  }
}