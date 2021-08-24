package jp.yuki312.koma.command

import androidx.core.app.ComponentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.yuki312.koma.KomaConfig
import jp.yuki312.koma.FrameMetricsId
import jp.yuki312.koma.FrameMetricsProcess
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class CommandInteractorTest {

  private val process: FrameMetricsProcess = mock()
  private val activity: ComponentActivity = mock()

  private lateinit var config: KomaConfig

  @Before fun setUp() {
    config = KomaConfig(
      frameRate = 1,
      analyzeMode = true,
      frozenFrameDurationThreshold = 600,
    )
    doReturn(config).whenever(process).defaultConfig
  }

  @Test
  fun start_withForeground() {
    val target = CommandInteractor { process }
    val id = "testId"

    target.resumedActivity(activity)
    target.start(
      id = id,
      frameRate = 60,
      analyzeMode = false,
    )

    verify(process, times(1)).start(
      eq(FrameMetricsId.Custom(id)),
      eq(activity),
      eq(config.copy(
        frameRate = 60,
        analyzeMode = false
      )),
    )
  }

  @Test
  fun start_withBackground() {
    val target = CommandInteractor { process }
    val id = "testId"

    // not resumed activity
    // target.resumedActivity(activity)
    target.start(
      id = id,
      frameRate = 60,
      analyzeMode = false,
    )

    verifyZeroInteractions(process)
  }

  @Test
  fun stop_transitionalToBackground() {
    val target = CommandInteractor { process }
    val id = "testId"

    target.resumedActivity(activity)
    target.start(
      id = id,
      frameRate = 60,
      analyzeMode = false,
    )
    target.pausedActivity()

    verify(process, times(1)).start(
      eq(FrameMetricsId.Custom(id)),
      eq(activity),
      eq(config.copy(
        frameRate = 60,
        analyzeMode = false
      )),
    )
    verify(process, times(1)).stopAll()
  }
}