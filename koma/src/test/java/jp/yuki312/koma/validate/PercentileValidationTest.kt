package jp.yuki312.koma.validate

import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.yuki312.koma.FrameMetricsId
import jp.yuki312.koma.aggregate.AggregateResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyZeroInteractions

@RunWith(AndroidJUnit4::class)
class PercentileValidationTest {

  @Test
  fun execute_passed() {
    val target = PercentileValidation(90.0, 10)
    val id = FrameMetricsId.Custom("testId")
    val durations = (1..10).toList()
    val mockDuration: AggregateResult.AggregateDurations = mock()
    val data = AggregateResult(
      id = id,
      frozen = mockDuration,
      jank = mockDuration,
      total = AggregateResult.AggregateDurations.from(
        durations = durations,
        total = durations,
      ),
    )
    val result = target.execute(data)

    assertThat(result.id).isEqualTo(id)
    assertThat(result.validatedList).isNotNull
    assertThat(result.validatedList.size).isEqualTo(1)
    assertThat(result.validatedList[0].value).isEqualTo(9)
    assertThat(result.validatedList[0].threshold).isEqualTo(10)
    assertThat(result.validatedList[0].isPassed).isEqualTo(true)
    verifyZeroInteractions(mockDuration)
  }

  @Test
  fun execute_not_passed() {
    val target = PercentileValidation(90.0, 9)
    val id = FrameMetricsId.Custom("testId")
    val durations = (1..10).toList()
    val mockDuration: AggregateResult.AggregateDurations = mock()
    val data = AggregateResult(
      id = id,
      frozen = mockDuration,
      jank = mockDuration,
      total = AggregateResult.AggregateDurations.from(
        durations = durations,
        total = durations,
      ),
    )
    val result = target.execute(data)

    assertThat(result.id).isEqualTo(id)
    assertThat(result.validatedList).isNotNull
    assertThat(result.validatedList.size).isEqualTo(1)
    assertThat(result.validatedList[0].value).isEqualTo(9)
    assertThat(result.validatedList[0].threshold).isEqualTo(9)
    assertThat(result.validatedList[0].isPassed).isEqualTo(false)
    verifyZeroInteractions(mockDuration)
  }
}