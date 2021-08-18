package jp.yuki312.koma.logger

data class MetricsTable(
  val total: Row?,
  val frozen: Row?,
  val jank: Row?,
  val input: Row?,
  val layoutMeasure: Row?,
  val draw: Row?,
  val sync: Row?,
  val command: Row?,
  val swap: Row?,
  val delay: Row?,
  val anim: Row?,
) {
  data class Row(
    val count: String,
    val ratio: String,
    val maxDuration: String,
    val minDuration: String,
    val sumDuration: String,
    val avgDuration: String,
    val medianDuration: String,
    val modeDuration: String,
  )
}