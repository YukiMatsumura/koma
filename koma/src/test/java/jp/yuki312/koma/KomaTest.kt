package jp.yuki312.koma

import android.os.Build
import android.view.Window
import androidx.core.app.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class KomaTest {

  @After
  fun teardown() {
    runCatching { Koma.destroy(ApplicationProvider.getApplicationContext()) }
  }

  @Config(sdk = [Build.VERSION_CODES.N])
  @Test
  fun newProcess_enable() = runBlockingTest {
    val id = FrameMetricsId.Custom("targetId")
    val listener: FrameMetricsListener = mock()
    val activity: ComponentActivity = mock()
    val window: Window = mock()
    doReturn(window).whenever(activity).window

    Koma.init(
      app = ApplicationProvider.getApplicationContext(),
      enable = true,
      frameMetricsListener = listener,
      processScope = this,
    )
    val enableProcess = Koma.newProcess()

    enableProcess.start(
      id = id,
      activity = activity,
    )
    enableProcess.stopAll()

    verify(listener).onFrameMetricsResult(
      id = eq(id),
      config = any(),
      aggregateResult = any(),
      validateResult = any(),
    )
  }

  @Config(sdk = [Build.VERSION_CODES.M])
  @Test
  fun newProcess_enable_preN() = runBlockingTest {
    val id = FrameMetricsId.Custom("targetId")
    val listener: FrameMetricsListener = mock()
    val activity: ComponentActivity = mock()

    Koma.init(
      app = ApplicationProvider.getApplicationContext(),
      enable = true,
      frameMetricsListener = listener,
      processScope = this
    )
    val disableProcess = Koma.newProcess()

    disableProcess.start(
      id = id,
      activity = activity,
    )
    disableProcess.stopAll()

    verifyZeroInteractions(listener)
  }

  @Test
  fun newProcess_disable() = runBlockingTest {
    val listener: FrameMetricsListener = mock()
    val activity: ComponentActivity = mock()
    Koma.init(
      app = ApplicationProvider.getApplicationContext(),
      enable = false,
      frameMetricsListener = listener,
    )
    val disableProcess = Koma.newProcess()
    disableProcess.start(
      id = FrameMetricsId.Custom("noop"),
      activity = activity,
    )
    disableProcess.stopAll()
    verifyZeroInteractions(listener)
  }
}