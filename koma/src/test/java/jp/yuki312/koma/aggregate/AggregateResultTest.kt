package jp.yuki312.koma.aggregate

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AggregateResultTest {

  @Test
  fun from() {
    val result = AggregateResult.AggregateDurations.from(
      durations = listOf(1, 2, 3),
      total = listOf(1, 2, 3)
    )

    if (result == null) {
      fail("Expect result is not null")
      return
    }

    assertThat(result.durations).isEqualTo(listOf(1, 2, 3))
    assertThat(result.count).isEqualTo(3)
    assertThat(result.ratio).isEqualTo(1.0F)
    assertThat(result.avgDuration).isEqualTo(2.0)
    assertThat(result.maxDuration).isEqualTo(3)
    assertThat(result.minDuration).isEqualTo(1)
    assertThat(result.sumDuration).isEqualTo(6)
    assertThat(result.medianDuration).isEqualTo(2.0)
    assertThat(result.modeDuration).isEqualTo(setOf(1, 2, 3))
  }

  @Test
  fun from_zero() {
    val result = AggregateResult.AggregateDurations.from(
      durations = listOf(0, 0, 0),
      total = listOf(0, 0, 0)
    )

    if (result == null) {
      fail("Expect result is not null")
      return
    }

    assertThat(result.durations).isEqualTo(listOf(0, 0, 0))
    assertThat(result.count).isEqualTo(3)
    assertThat(result.ratio).isEqualTo(1.0F)
    assertThat(result.avgDuration).isEqualTo(0.0)
    assertThat(result.maxDuration).isEqualTo(0)
    assertThat(result.minDuration).isEqualTo(0)
    assertThat(result.sumDuration).isEqualTo(0)
    assertThat(result.medianDuration).isEqualTo(0.0)
    assertThat(result.modeDuration).isEqualTo(setOf(0))
  }

  @Test
  fun from_single() {
    val result = AggregateResult.AggregateDurations.from(
      durations = listOf(5),
      total = listOf(1, 2, 3, 4, 5)
    )

    if (result == null) {
      fail("Expect result is not null")
      return
    }

    assertThat(result.durations).isEqualTo(listOf(5))
    assertThat(result.count).isEqualTo(1)
    assertThat(result.ratio).isEqualTo(0.2f)
    assertThat(result.avgDuration).isEqualTo(5.0)
    assertThat(result.maxDuration).isEqualTo(5)
    assertThat(result.minDuration).isEqualTo(5)
    assertThat(result.sumDuration).isEqualTo(5)
    assertThat(result.medianDuration).isEqualTo(5.0)
    assertThat(result.modeDuration).isEqualTo(setOf(5))
  }

  @Test
  fun from_empty() {
    val result = AggregateResult.AggregateDurations.from(
      durations = emptyList(),
      total = listOf(1, 2, 3, 4, 5)
    )

    assertThat(result).isNull()
  }
}