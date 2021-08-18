package jp.yuki312.koma.validate

import jp.yuki312.koma.aggregate.AggregateResult
import kotlin.math.ceil

class PercentileValidation(
  private val percentileRank: Double,
  private val threshold: Int,
) : ValidateFunction {

  override fun execute(result: AggregateResult): ValidateResult {
    return ValidateResult(
      id = result.id,
      validatedList = listOfNotNull(
        verifyPercentile(
          durations = result.total?.durations,
          rank = percentileRank,
          threshold = threshold,
        )
      )
    )
  }

  private fun verifyPercentile(
    durations: List<Int>?,
    rank: Double,
    threshold: Int,
  ): ValidateResult.MetricsValidation<Int>? {
    val value = durations?.percentile(rank) ?: return null
    return ValidateResult.MetricsValidation(
      name = "Frame duration ${"%.0f".format(rank)} percentile",
      value = value,
      threshold = threshold,
      isPassed = (value < threshold)
    )
  }

  private fun List<Int>.percentile(rank: Double): Int? {
    if (isEmpty()) return null
    return sorted().getOrNull(ceil(rank / 100.0 * size).toInt() - 1)
  }
}