package jp.yuki312.koma

import android.app.Activity
import android.util.SparseIntArray
import androidx.core.app.FrameMetricsAggregator
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.yuki312.koma.aggregate.AggregateFunction
import jp.yuki312.koma.aggregate.AggregateResult
import jp.yuki312.koma.aggregate.AggregateResult.AggregateDurations
import jp.yuki312.koma.track.TrackResult
import jp.yuki312.koma.track.TrackResult.TrackDurations
import jp.yuki312.koma.track.Tracker
import jp.yuki312.koma.validate.PercentileValidation
import jp.yuki312.koma.validate.ValidateResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class FrameMetricsProcessTest {

  @get:Rule var mockitoRule: MockitoRule = MockitoJUnit.rule()

  private lateinit var tracker: Tracker

  @Mock
  lateinit var frameMetricsAggregator: FrameMetricsAggregator

  @Mock
  lateinit var activity: Activity

  @Before fun setUp() {
    tracker = Tracker { frameMetricsAggregator }
  }

  @Test
  fun start_stop() {
    val expectConfig = KomaConfig(
      frameRate = 60,
      analyzeMode = false,
      frozenFrameDurationThreshold = 600,
    )
    val expectId = FrameMetricsId.Custom("test")
    val interceptor = object : KomaInterceptor {
      lateinit var trackResult: TrackResult
      lateinit var aggregateResult: AggregateResult
      lateinit var validateResult: ValidateResult
      override fun onTracked(result: TrackResult): TrackResult {
        trackResult = result
        return result
      }

      override fun onAggregated(result: AggregateResult): AggregateResult {
        aggregateResult = result
        return result
      }

      override fun onValidated(result: ValidateResult): ValidateResult {
        validateResult = result
        return result
      }
    }
    val metrics = testDataMetrics()
    val expectTrackResult = TrackResult(
      id = expectId,
      config = expectConfig,
      durations = TrackDurations.from(metrics),
    )
    val expectAggregateResult = AggregateResult(
      id = expectId,
      frozen = AggregateDurations.from(
        durations = emptyList(),
        total = expectTrackResult.durations.total
      ),
      jank = AggregateDurations.from(
        durations = emptyList(),
        total = expectTrackResult.durations.total
      ),
      total = AggregateDurations.from(
        durations = emptyList(),
        total = expectTrackResult.durations.total
      ),
    )
    val expectValidateResult = ValidateResult(
      id = expectId,
      validatedList = emptyList()
    )
    lateinit var resultId: FrameMetricsId
    lateinit var resultConfig: KomaConfig
    lateinit var resultAggregateResult: AggregateResult
    lateinit var resultValidateResult: ValidateResult
    val listener = FrameMetricsListener { id, config, aggregateResult, validateResult ->
      resultId = id
      resultConfig = config
      resultAggregateResult = aggregateResult
      resultValidateResult = validateResult
    }
    val target = FrameMetricsProcess(
      enable = true,
      tracker = tracker,
      aggregator = AggregateFunction(),
      validator = PercentileValidation(90.0, 32),
      interceptor = interceptor,
      defaultConfig = expectConfig,
      defaultListener = listener
    )

    doReturn(metrics).whenever(frameMetricsAggregator).metrics

    target.start(
      id = expectId,
      activity = activity,
    )
    target.stop(expectId)

    assertThat(target.defaultConfig).isEqualTo(expectConfig)
    assertThat(interceptor.trackResult).isEqualTo(expectTrackResult)
    assertThat(interceptor.aggregateResult).isEqualTo(expectAggregateResult)
    assertThat(interceptor.validateResult).isEqualTo(expectValidateResult)
    assertThat(resultId).isEqualTo(expectId)
    assertThat(resultConfig).isEqualTo(expectConfig)
    assertThat(resultAggregateResult).isEqualTo(expectAggregateResult)
    assertThat(resultValidateResult).isEqualTo(expectValidateResult)
  }

  @Test
  fun start_stop_intercept() {
    val expectConfig = KomaConfig(
      frameRate = 60,
      analyzeMode = false,
      frozenFrameDurationThreshold = 600,
    )
    val expectId = FrameMetricsId.Custom("test")
    val interceptor = object : KomaInterceptor {
      val expectAggregateResult = AggregateResult(
        id = FrameMetricsId.Custom("overwrite2"),
        frozen = AggregateDurations.from(
          durations = listOf(600, 700, 800),
          total = listOf(600, 700, 800),
        ),
        jank = AggregateDurations.from(
          durations = listOf(16, 17, 18),
          total = listOf(16, 17, 18),
        ),
        total = AggregateDurations.from(
          durations = listOf(1, 2, 3),
          total = listOf(1, 2, 3),
        ),
      )
      val expectValidateResult = ValidateResult(
        id = FrameMetricsId.Custom("overwrite3"),
        validatedList = listOf(
          ValidateResult.MetricsValidation<Int>(
            name = "overwrite name",
            value = -1,
            threshold = 100,
            isPassed = false,
          )
        )
      )

      lateinit var trackResult: TrackResult
      lateinit var aggregateResult: AggregateResult
      lateinit var validateResult: ValidateResult

      override fun onTracked(result: TrackResult): TrackResult {
        trackResult = result
        return TrackResult(
          id = FrameMetricsId.Custom("overwrite1"),
          config = result.config.copy(frameRate = 120),
          durations = TrackDurations(total = listOf(1, 2, 3))
        )
      }

      override fun onAggregated(result: AggregateResult): AggregateResult {
        aggregateResult = result
        return expectAggregateResult
      }

      override fun onValidated(result: ValidateResult): ValidateResult {
        validateResult = result
        return expectValidateResult
      }
    }
    val metrics = testDataMetrics()
    lateinit var resultId: FrameMetricsId
    lateinit var resultConfig: KomaConfig
    lateinit var resultAggregateResult: AggregateResult
    lateinit var resultValidateResult: ValidateResult
    val listener = FrameMetricsListener { id, config, aggregateResult, validateResult ->
      resultId = id
      resultConfig = config
      resultAggregateResult = aggregateResult
      resultValidateResult = validateResult
    }

    val target = FrameMetricsProcess(
      enable = true,
      tracker = tracker,
      aggregator = AggregateFunction(),
      validator = PercentileValidation(90.0, 32),
      interceptor = interceptor,
      defaultConfig = expectConfig,
      defaultListener = listener
    )

    doReturn(metrics).whenever(frameMetricsAggregator).metrics

    target.start(
      id = expectId,
      activity = activity,
    )
    target.stop(expectId)

    assertThat(target.defaultConfig).isEqualTo(expectConfig)
    assertThat(resultId).isEqualTo(expectId)
    assertThat(resultConfig).isEqualTo(expectConfig)
    assertThat(resultAggregateResult).isEqualTo(interceptor.expectAggregateResult)
    assertThat(resultValidateResult).isEqualTo(interceptor.expectValidateResult)
  }

  @Test
  fun disable() {
    val id = FrameMetricsId.Custom("noop")
    val config = KomaConfig(
      frameRate = 60,
      analyzeMode = false,
      frozenFrameDurationThreshold = 600,
    )
    val interceptor = object : KomaInterceptor {
      override fun onTracked(result: TrackResult): TrackResult = result
      override fun onAggregated(result: AggregateResult): AggregateResult = result
      override fun onValidated(result: ValidateResult): ValidateResult = result
    }
    val listener = FrameMetricsListener { _, _, _, _ -> }
    val tracker : Tracker = mock()

    val target = FrameMetricsProcess(
      enable = false,
      tracker = tracker,
      aggregator = AggregateFunction(),
      validator = PercentileValidation(0.0, 0),
      interceptor = interceptor,
      defaultConfig = config,
      defaultListener = listener,
    )

    target.start(
      id = id,
      activity = activity,
    )
    target.stop(id)
    target.stopAll()

    verifyZeroInteractions(tracker)
  }

  private fun testDataMetrics(
    total: List<Pair<Int, Int>> = emptyList(),
    input: List<Pair<Int, Int>> = emptyList(),
    layout: List<Pair<Int, Int>> = emptyList(),
    draw: List<Pair<Int, Int>> = emptyList(),
    sync: List<Pair<Int, Int>> = emptyList(),
    command: List<Pair<Int, Int>> = emptyList(),
    swap: List<Pair<Int, Int>> = emptyList(),
    delay: List<Pair<Int, Int>> = emptyList(),
    anim: List<Pair<Int, Int>> = emptyList(),
  ): Array<SparseIntArray?> {
    fun List<Pair<Int, Int>>.toSparseIntArray(): SparseIntArray? {
      if (isEmpty()) return null
      return SparseIntArray().apply {
        forEach { (key, value) -> append(key, value) }
      }
    }

    return arrayOf(
      total.toSparseIntArray(),
      input.toSparseIntArray(),
      layout.toSparseIntArray(),
      draw.toSparseIntArray(),
      sync.toSparseIntArray(),
      command.toSparseIntArray(),
      swap.toSparseIntArray(),
      delay.toSparseIntArray(),
      anim.toSparseIntArray(),
    )
  }
}