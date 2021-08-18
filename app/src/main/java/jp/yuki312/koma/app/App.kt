package jp.yuki312.koma.app

import android.app.Application
import jp.yuki312.koma.Koma

class App : Application() {
  override fun onCreate() {
    super.onCreate()
    Koma.init(app = this)
  }
}