package jp.yuki312.koma.uicomponent

import android.app.Activity
import androidx.fragment.app.Fragment
import jp.yuki312.koma.ActivityFrameMetricsFilter
import jp.yuki312.koma.FragmentFrameMetricsFilter
import jp.yuki312.koma.FrameMetricsId
import jp.yuki312.koma.FrameMetricsProcess
import jp.yuki312.koma.ProcessFactory

internal class UiComponentInteractor(
  private val activityFilter: ActivityFrameMetricsFilter,
  private val fragmentFilter: FragmentFrameMetricsFilter,
  processFactory: ProcessFactory,
) {

  private val process: FrameMetricsProcess = processFactory.create()

  fun onActivityResumed(activity: Activity) {
    if (!activityFilter.filter(activity)) return

    process.start(
      id = activity.toFrameMetricsId(),
      activity = activity,
    )
  }

  fun onActivityPaused(activity: Activity) {
    process.stop(
      id = activity.toFrameMetricsId()
    )
  }

  fun onFragmentResumed(fragment: Fragment) {
    if (!fragmentFilter.filter(fragment)) return
    val activity = fragment.activity ?: return

    process.start(
      id = fragment.toFrameMetricsId(),
      activity = activity,
    )
  }

  fun onFragmentPaused(fragment: Fragment) {
    process.stop(
      id = fragment.toFrameMetricsId()
    )
  }

  private fun Any.toFrameMetricsId() : FrameMetricsId {
    return FrameMetricsId.Reserved("${this::class.simpleName ?: "null"}@${this.hashCode()}")
  }
}