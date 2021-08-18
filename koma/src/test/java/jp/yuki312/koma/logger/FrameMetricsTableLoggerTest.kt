package jp.yuki312.koma.logger

import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.yuki312.koma.KomaConfig
import jp.yuki312.koma.FrameMetricsId
import jp.yuki312.koma.aggregate.AggregateResult
import jp.yuki312.koma.aggregate.AggregateResult.AggregateDurations
import jp.yuki312.koma.validate.ValidateResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FrameMetricsTableLoggerTest {

  @Test
  fun print_empty() {
    val expectId = FrameMetricsId.Custom("targetId")
    var resultHighlight: Boolean? = null
    lateinit var resultOutput: StringBuilder
    val target = FrameMetricsTableLogger { highlight, output ->
      resultHighlight = highlight
      resultOutput = output
    }

    target.output(
      id = expectId,
      config = KomaConfig(
        frameRate = 60,
        analyzeMode = false,
        frozenFrameDurationThreshold = 600,
      ),
      aggregateResult = AggregateResult(
        id = expectId,
      ),
      validateResult = ValidateResult(
        id = expectId,
        validatedList = emptyList(),
      )
    )

    val str = resultOutput.toString()

    assertThat(resultHighlight).isFalse
    assertThat(str)
      .isEqualTo(
        """
        --->
        ID: targetId
        
        Metrics Table: <NO DATA>
        
        Validate Table: <NO DATA>
        
         Misc.                           │ value  
        ─────────────────────────────────┼────────
                              Frame rate │ 60 fps 
         Frozen frame duration threshold │ 600 ms 
           Jank frame duration threshold │  16 ms 
        <---
        
        """.trimIndent()
      )
  }

  @Test
  fun print() {
    val expectId = FrameMetricsId.Custom("targetId")
    var resultHighlight: Boolean? = null
    lateinit var resultOutput: StringBuilder
    val target = FrameMetricsTableLogger { highlight, output ->
      resultHighlight = highlight
      resultOutput = output
    }

    target.output(
      id = expectId,
      config = KomaConfig(
        frameRate = 60,
        analyzeMode = false,
        frozenFrameDurationThreshold = 600,
      ),
      aggregateResult = AggregateResult(
        id = expectId,
        total = AggregateDurations.from(
          listOf(1, 2, 3),
          total = listOf(1, 2, 3)
        ),
        anim = AggregateDurations.from(
          listOf(3),
          total = listOf(1, 2, 3)
        ),
      ),
      validateResult = ValidateResult(
        id = expectId,
        validatedList = listOf(
          ValidateResult.MetricsValidation(
            name = "test validation",
            value = 1.0,
            threshold = 1.0,
            isPassed = false,
          )
        ),
      )
    )

    val str = resultOutput.toString()

    assertThat(resultHighlight).isTrue
    assertThat(str)
      .isEqualTo(
        """
        --->
        ID: targetId
        
         Frame metrics │ count │ ratio   │ max  │ min  │ sum  │ avg    │ median │ mode       
        ───────────────┼───────┼─────────┼──────┼──────┼──────┼────────┼────────┼────────────
                 Total │   3 F │ 100.0 % │ 3 ms │ 1 ms │ 6 ms │ 2.0 ms │ 2.0 ms │ 1, 2, 3 ms 
                  Anim │   1 F │  33.3 % │ 3 ms │ 3 ms │ 3 ms │ 3.0 ms │ 3.0 ms │       3 ms 
        
         Validation name │ result │ value │ threshold 
        ─────────────────┼────────┼───────┼───────────
         test validation │     NG │   1.0 │       1.0 
        
         Misc.                           │ value  
        ─────────────────────────────────┼────────
                              Frame rate │ 60 fps 
         Frozen frame duration threshold │ 600 ms 
           Jank frame duration threshold │  16 ms 
        <---
        
        """.trimIndent()
      )
  }
}