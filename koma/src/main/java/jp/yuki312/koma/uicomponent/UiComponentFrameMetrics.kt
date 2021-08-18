package jp.yuki312.koma.uicomponent

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks

internal class UiComponentFrameMetrics(
  private val interactor: UiComponentInteractor
) {

  private val activityObserver = object : ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) {
      if (activity !is FragmentActivity) return
      activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
        fragmentObserver,
        true
      )
    }

    override fun onActivityResumed(activity: Activity) {
      interactor.onActivityResumed(activity)
    }

    override fun onActivityPaused(activity: Activity) {
      interactor.onActivityPaused(activity)
    }

    override fun onActivityStopped(activity: Activity) {
      if (activity !is FragmentActivity) return
      activity.supportFragmentManager.unregisterFragmentLifecycleCallbacks(
        fragmentObserver
      )
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
  }

  private val fragmentObserver = object : FragmentLifecycleCallbacks() {
    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
      interactor.onFragmentResumed(f)
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
      interactor.onFragmentPaused(f)
    }
  }

  fun register(app: Application) {
    app.registerActivityLifecycleCallbacks(activityObserver)
  }

  fun unregister(app: Application) {
    app.unregisterActivityLifecycleCallbacks(activityObserver)
  }
}