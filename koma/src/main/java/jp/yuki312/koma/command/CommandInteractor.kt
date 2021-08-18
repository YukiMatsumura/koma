package jp.yuki312.koma.command

import android.app.Activity
import android.util.Log
import jp.yuki312.koma.FrameMetricsProcess
import jp.yuki312.koma.ProcessFactory
import jp.yuki312.koma.toCustomId
import java.lang.ref.WeakReference

internal class CommandInteractor(
  processFactory: ProcessFactory,
) {

  private var resumedActivity = WeakReference<Activity>(null)
  private val process: FrameMetricsProcess = processFactory.create()

  fun resumedActivity(activity: Activity) {
    resumedActivity = WeakReference(activity)
  }

  fun pausedActivity() {
    resumedActivity.clear()
    process.stopAll()
  }

  fun start(
    id: String,
    frameRate: Int,
    analyzeMode: Boolean,
  ) {
    val activity = resumedActivity.get()
    if (activity == null) {
      Log.w(
        "FrameMetrics",
        "Your Activity is not resuming. Please use the command while your Activity is resumed."
      )
      return
    }

    process.start(
      id = id.toCustomId(),
      activity = activity,
      config = process.defaultConfig.copy(
        frameRate = frameRate,
        analyzeMode = analyzeMode,
      ),
    )
  }

  fun stop(id: String) {
    process.stop(id.toCustomId())
  }
}