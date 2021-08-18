package jp.yuki312.koma

sealed class FrameMetricsId(id: String) {

  class Reserved internal constructor(id: String) : FrameMetricsId(id)

  class Custom(id: String) : FrameMetricsId(id) {
    companion object {
      val NO_ID = Custom("<NO_ID>")
    }
  }

  val value: String = id

  init {
    require(id.isNotBlank())
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as FrameMetricsId

    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }
}

internal fun String.toCustomId(): FrameMetricsId.Custom {
  return if (isBlank()) FrameMetricsId.Custom.NO_ID else FrameMetricsId.Custom(this)
}