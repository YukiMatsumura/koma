package jp.yuki312.koma.validate

import jp.yuki312.koma.FrameMetricsId

data class ValidateResult(
  val id: FrameMetricsId,
  val validatedList: List<MetricsValidation<*>>
) {

  data class MetricsValidation<T : Any>(
    val name: String,
    val value: T,
    val threshold: T?,
    val isPassed: Boolean
  )
}