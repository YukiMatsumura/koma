package jp.yuki312.koma.track

import android.app.Activity
import android.util.SparseIntArray
import androidx.core.app.FrameMetricsAggregator
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.yuki312.koma.KomaConfig
import jp.yuki312.koma.FrameMetricsId
import jp.yuki312.koma.track.TrackResult.TrackDurations
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class TrackerTest {

  @get:Rule var mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock
  lateinit var frameMetricsAggregator: FrameMetricsAggregator

  @Mock
  lateinit var activity: Activity

  private lateinit var target: Tracker
  private val config = KomaConfig(
    frameRate = 60,
    analyzeMode = false,
    frozenFrameDurationThreshold = 600,
  )

  @Before fun setUp() {
    target = Tracker { frameMetricsAggregator }
  }

  @Test
  fun start_stop() {
    var result: TrackResult? = null
    val targetId = FrameMetricsId.Custom("TargetID")
    val expectMetrics = testDataMetrics(1 to 1)

    doReturn(expectMetrics).whenever(frameMetricsAggregator).metrics

    target.startTracking(
      id = targetId,
      activity = activity,
      config = config,
    ) {
      result = it
    }
    target.stopTracking(FrameMetricsId.Custom("*NOT* TargetId"))

    // If the IDs are different, the tracker will not stop.
    assertThat(result).isNull()

    target.stopTracking(targetId)

    // If the IDs are same, the tracker will stop.
    assertThat(result!!.id).isEqualTo(targetId)
    assertThat(result!!.durations).isEqualTo(TrackDurations.from(expectMetrics))
    assertThat(result!!.config).isEqualTo(config)
  }

  @Test
  fun throwException_stop() {
    var result: TrackResult? = null
    val targetId = FrameMetricsId.Custom("TargetID")
    val expectMetrics = testDataMetrics(1 to 1)

    // FrameMetricsAggregator#stop() will throw exceptions for hardware acceleration disabled activities.
    // This test scenario will validate that case.
    doThrow(RuntimeException("expected error")).whenever(frameMetricsAggregator).stop()
    doReturn(expectMetrics).whenever(frameMetricsAggregator).metrics

    target.startTracking(
      id = targetId,
      activity = activity,
      config = config,
    ) {
      result = it
    }
    target.stopTracking(targetId)

    verify(frameMetricsAggregator, times(1)).stop()
    assertThat(result!!.durations).isEqualTo(TrackDurations.from(expectMetrics))
  }

  @Test
  fun startMultiple_stopAll() {
    var result1: TrackResult? = null
    var result2: TrackResult? = null
    val targetId1 = FrameMetricsId.Custom("TargetID1")
    val targetId2 = FrameMetricsId.Custom("TargetID2")
    val expectMetrics1 = testDataMetrics(1 to 1)
    val expectMetrics2 = testDataMetrics(2 to 2)

    doReturn(expectMetrics1, expectMetrics2).whenever(frameMetricsAggregator).metrics

    // target 1
    target.startTracking(
      id = targetId1,
      activity = activity,
      config = config,
    ) {
      result1 = it
    }

    // target2
    target.startTracking(
      id = targetId2,
      activity = activity,
      config = config,
    ) {
      result2 = it
    }

    target.stopAll()

    // If the IDs are same, the tracker will stop.
    assertThat(result1!!.id).isEqualTo(targetId1)
    assertThat(result1!!.durations).isEqualTo(TrackDurations.from(expectMetrics1))
    assertThat(result2!!.id).isEqualTo(targetId2)
    assertThat(result2!!.durations).isEqualTo(TrackDurations.from(expectMetrics2))
  }

  private fun testDataMetrics(vararg metrics: Pair<Int, Int>): Array<SparseIntArray?> {
    return arrayOf(
      SparseIntArray().apply {
        metrics.forEach { (key, value) -> append(key, value) }
      }
    )
  }
}