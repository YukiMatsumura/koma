package jp.yuki312.koma.command

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.core.app.ComponentActivity

internal class CommandReceiver(
  private val interactor: CommandInteractor,
  actionScheme: String,
) : BroadcastReceiver() {

  private val resumedActivityObserver = object : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) {
      if (activity !is ComponentActivity) return
      interactor.resumedActivity(activity)
    }

    override fun onActivityPaused(activity: Activity) {
      if (activity !is ComponentActivity) return
      interactor.pausedActivity()
    }

    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
  }

  private val intentActionStart = "$actionScheme.$INTENT_ACTION_START_SUFFIX"
  private val intentActionStop = "$actionScheme.$INTENT_ACTION_STOP_SUFFIX"

  override fun onReceive(context: Context?, intent: Intent?) {
    if (intent == null) return

    when {
      intent.isStartAction() -> interactor.start(
        id = intent.extras.id,
        frameRate = intent.extras.frameRate,
        analyzeMode = intent.extras.isAnalyze,
      )
      intent.isStopAction() -> interactor.stop(
        id = intent.extras.id
      )
      else -> {
      }
    }
  }

  fun register(app: Application) {
    app.registerActivityLifecycleCallbacks(resumedActivityObserver)
    val intentFilter = IntentFilter().apply {
      addAction(intentActionStart)
      addAction(intentActionStop)
    }
    app.registerReceiver(this, intentFilter)
  }

  fun unregister(app: Application) {
    app.unregisterReceiver(this)
    app.unregisterActivityLifecycleCallbacks(resumedActivityObserver)
  }

  private fun Intent.isStartAction(): Boolean {
    return action == intentActionStart
  }

  private fun Intent.isStopAction(): Boolean {
    return action == intentActionStop
  }

  private val Bundle?.frameRate: Int
    get() = this?.get(INTENT_EXTRA_FRAME_RATE) as? Int ?: 60

  private val Bundle?.isAnalyze: Boolean
    get() = this?.get(INTENT_EXTRA_ANALYZE_MODE) as? Boolean ?: false

  private val Bundle?.id: String
    get() = this?.get(INTENT_EXTRA_FRAME_METRICS_ID) as? String ?: ""

  companion object {
    const val INTENT_ACTION_START_SUFFIX = "START"
    const val INTENT_ACTION_STOP_SUFFIX = "STOP"

    const val INTENT_EXTRA_FRAME_METRICS_ID = "id"
    const val INTENT_EXTRA_FRAME_RATE = "framerate"
    const val INTENT_EXTRA_ANALYZE_MODE = "analyze"
  }
}