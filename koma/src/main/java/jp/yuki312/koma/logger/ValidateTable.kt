package jp.yuki312.koma.logger

data class ValidateTable(
  val validateList: List<Row>
) {
  data class Row(
    val label: String,
    val passed: String,
    val value: String,
    val threshold: String,
  )
}