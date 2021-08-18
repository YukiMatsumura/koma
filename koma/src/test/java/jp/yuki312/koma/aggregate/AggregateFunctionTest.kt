package jp.yuki312.koma.aggregate

import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.yuki312.koma.KomaConfig
import jp.yuki312.koma.FrameMetricsId
import jp.yuki312.koma.track.TrackResult
import jp.yuki312.koma.track.TrackResult.TrackDurations
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AggregateFunctionTest {

  private lateinit var target: AggregateFunction

  @Before fun setUp() {
    target = AggregateFunction()
  }

  @Test
  fun execute_total() {
    val config = KomaConfig(
      frameRate = 60,
      analyzeMode = false,
      frozenFrameDurationThreshold = 600,
      jankFrameDurationThreshold = 16
    )
    val id = FrameMetricsId.Custom("targetId")
    val arg = TrackResult(
      id = id,
      config = config,
      durations = TrackDurations(
        total = listOf(0, 15, 16, 599, 600)
      )
    )

    val result = target.execute(arg)

    assertThat(result.total!!.durations.size).isEqualTo(5)
    assertThat(result.total!!.count).isEqualTo(5)
    assertThat(result.jank!!.count).isEqualTo(2)
    assertThat(result.frozen!!.count).isEqualTo(1)
    assertThat(result.input).isNull()
    assertThat(result.layoutMeasure).isNull()
    assertThat(result.draw).isNull()
    assertThat(result.sync).isNull()
    assertThat(result.command).isNull()
    assertThat(result.swap).isNull()
    assertThat(result.delay).isNull()
    assertThat(result.anim).isNull()
  }

  @Test
  fun execute_total_null() {
    val config = KomaConfig(
      frameRate = 60,
      analyzeMode = false,
      frozenFrameDurationThreshold = 600,
      jankFrameDurationThreshold = 16
    )
    val id = FrameMetricsId.Custom("targetId")
    val arg = TrackResult(
      id = id,
      config = config,
      durations = TrackDurations(/* empty result */)
    )

    val result = target.execute(arg)

    assertThat(result.total).isNull()
    assertThat(result.jank).isNull()
    assertThat(result.frozen).isNull()
  }

  @Test
  fun execute_120fps() {
    val config = KomaConfig(
      frameRate = 120,
      analyzeMode = false,
      frozenFrameDurationThreshold = 600,
      // jankFrameDurationThreshold = 1000 / 120 = 8.3
    )
    val id = FrameMetricsId.Custom("targetId")
    val arg = TrackResult(
      id = id,
      config = config,
      durations = TrackDurations(
        total = listOf(0, 15, 16, 599, 600)
      )
    )

    val result = target.execute(arg)

    assertThat(config.jankFrameDurationThreshold).isEqualTo(8)
    assertThat(result.total!!.durations.size).isEqualTo(5)
    assertThat(result.total!!.count).isEqualTo(5)
    assertThat(result.jank!!.count).isEqualTo(3)
    assertThat(result.frozen!!.count).isEqualTo(1)
  }

  @Test
  fun execute_analyze() {
    val config = KomaConfig(
      frameRate = 60,
      analyzeMode = true,
      frozenFrameDurationThreshold = 600,
    )
    val id = FrameMetricsId.Custom("targetId")
    val arg = TrackResult(
      id = id,
      config = config,
      durations = TrackDurations(
        total = listOf(0, 15, 16, 599, 600),
        input = listOf(1, 2, 3),
        layoutMeasure = listOf(4, 5, 6),
        draw = listOf(7, 8, 9),
        sync = listOf(10, 11, 12),
        command = listOf(13, 13, 13),
        swap = listOf(14, 13, 12),
        delay = listOf(0, 0, 0),
        anim = emptyList(),
      )
    )

    val result = target.execute(arg)

    assertThat(result.total!!.durations.size).isEqualTo(5)
    assertThat(result.total!!.count).isEqualTo(5)
    assertThat(result.jank!!.count).isEqualTo(2)
    assertThat(result.frozen!!.count).isEqualTo(1)
    assertThat(result.input!!.count).isEqualTo(3)
    assertThat(result.input!!.modeDuration).isEqualTo(setOf(1, 2, 3))
    assertThat(result.layoutMeasure!!.medianDuration).isEqualTo(5.0)
    assertThat(result.draw!!.minDuration).isEqualTo(7)
    assertThat(result.sync!!.maxDuration).isEqualTo(12)
    assertThat(result.command!!.avgDuration).isEqualTo(13.0)
    assertThat(result.swap!!.ratio).isEqualTo(0.031707317f)
    assertThat(result.delay!!.sumDuration).isEqualTo(0)
    assertThat(result.anim).isNull()
  }

  @Test
  fun execute_null() {
    val config = KomaConfig(
      frameRate = 60,
      analyzeMode = true,
      frozenFrameDurationThreshold = 600,
    )
    val id = FrameMetricsId.Custom("targetId")
    val arg = TrackResult(
      id = id,
      config = config,
      durations = TrackDurations( /* all empty */)
    )

    val result = target.execute(arg)

    assertThat(result.total).isNull()
    assertThat(result.jank).isNull()
    assertThat(result.frozen).isNull()
    assertThat(result.input).isNull()
    assertThat(result.layoutMeasure).isNull()
    assertThat(result.draw).isNull()
    assertThat(result.sync).isNull()
    assertThat(result.command).isNull()
    assertThat(result.swap).isNull()
    assertThat(result.delay).isNull()
    assertThat(result.anim).isNull()
  }
}