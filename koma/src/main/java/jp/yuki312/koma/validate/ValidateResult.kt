package jp.yuki312.koma.validate

import jp.yuki312.koma.FrameMetricsId

data class ValidateResult(
  val id: FrameMetricsId,
  val validatedList: List<ValidateItem<*>>
) {

  data class ValidateItem<T : Any>(
    val name: String,
    val value: T,
    val threshold: T?,
    val isPassed: Boolean
  )

  fun isPassedAll() : Boolean {
    return validatedList.all { it.isPassed }
  }
}