package jp.yuki312.koma

internal fun interface ProcessFactory {
  fun create(): FrameMetricsProcess
}