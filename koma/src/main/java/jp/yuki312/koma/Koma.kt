package jp.yuki312.koma

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.VisibleForTesting
import jp.yuki312.koma.aggregate.AggregateFunction
import jp.yuki312.koma.aggregate.AggregateResult
import jp.yuki312.koma.command.CommandInteractor
import jp.yuki312.koma.command.CommandReceiver
import jp.yuki312.koma.logger.FrameMetricsTableLogger
import jp.yuki312.koma.track.TrackResult
import jp.yuki312.koma.track.Tracker
import jp.yuki312.koma.uicomponent.UiComponentFrameMetrics
import jp.yuki312.koma.uicomponent.UiComponentInteractor
import jp.yuki312.koma.validate.PercentileValidation
import jp.yuki312.koma.validate.ValidateFunction
import jp.yuki312.koma.validate.ValidateResult
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.atomic.AtomicBoolean

object Koma {

  const val LOGTAG = "koma"

  private val initialized: AtomicBoolean = AtomicBoolean(false)
  private var enable: Boolean = false

  private lateinit var application: Application
  private lateinit var interceptor: KomaInterceptor
  private lateinit var validateFunction: ValidateFunction
  private lateinit var defaultConfig: KomaConfig
  private lateinit var listener: FrameMetricsListener
  private lateinit var activityFrameMetricsFilter: ActivityFrameMetricsFilter
  private lateinit var fragmentFrameMetricsFilter: FragmentFrameMetricsFilter

  internal lateinit var processScope: CoroutineScope

  private val processFactory: ProcessFactory by lazy {
    ProcessFactory {
      FrameMetricsProcess(
        enable = enable,
        tracker = Tracker(),
        aggregator = AggregateFunction(),
        validator = validateFunction,
        interceptor = interceptor,
        defaultConfig = defaultConfig,
        scope = processScope,
        listener = listener,
      )
    }
  }

  private val commandReceiver: CommandReceiver by lazy {
    CommandReceiver(
      interactor = CommandInteractor(processFactory),
      actionScheme = application.packageName
    )
  }

  private val uiComponentFrameMetrics: UiComponentFrameMetrics by lazy {
    UiComponentFrameMetrics(
      UiComponentInteractor(
        activityFilter = activityFrameMetricsFilter,
        fragmentFilter = fragmentFrameMetricsFilter,
        processFactory = processFactory
      )
    )
  }

  fun init(
    app: Application,
    enable: Boolean = true,
    enableCommandReceiver: Boolean = true,
    enableUiComponentMetrics: Boolean = false,
    activityFrameMetricsFilter: ActivityFrameMetricsFilter = ActivityFrameMetricsFilter { true },
    fragmentFrameMetricsFilter: FragmentFrameMetricsFilter = FragmentFrameMetricsFilter { true },
    validateFunction: ValidateFunction = defaultValidateFunction(),
    interceptor: KomaInterceptor? = null,
    frameMetricsListener: FrameMetricsListener = defaultListener(),
    defaultConfig: KomaConfig = defaultConfig()
  ) = init(
    app = app,
    enable = enable,
    enableCommandReceiver = enableCommandReceiver,
    enableUiComponentMetrics = enableUiComponentMetrics,
    activityFrameMetricsFilter = activityFrameMetricsFilter,
    fragmentFrameMetricsFilter = fragmentFrameMetricsFilter,
    validateFunction = validateFunction,
    interceptor = interceptor,
    frameMetricsListener = frameMetricsListener,
    defaultConfig = defaultConfig,
    processScope = CoroutineScope(
      SupervisorJob() + CoroutineName("Koma") + Dispatchers.Default
    )
  )

  @VisibleForTesting
  fun init(
    app: Application,
    enable: Boolean = true,
    enableCommandReceiver: Boolean = true,
    enableUiComponentMetrics: Boolean = false,
    activityFrameMetricsFilter: ActivityFrameMetricsFilter = ActivityFrameMetricsFilter { true },
    fragmentFrameMetricsFilter: FragmentFrameMetricsFilter = FragmentFrameMetricsFilter { true },
    validateFunction: ValidateFunction = defaultValidateFunction(),
    interceptor: KomaInterceptor? = null,
    frameMetricsListener: FrameMetricsListener = defaultListener(),
    defaultConfig: KomaConfig = defaultConfig(),
    processScope: CoroutineScope,
  ) {
    if (!initialized.compareAndSet(false, true)) return // already initialized

    val enable = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
      false
    } else {
      enable
    }

    this.enable = enable
    this.application = app
    this.defaultConfig = defaultConfig
    this.listener = frameMetricsListener
    this.interceptor = interceptor ?: object : KomaInterceptor {
      override fun onTracked(result: TrackResult) = result
      override fun onAggregated(result: AggregateResult) = result
      override fun onValidated(result: ValidateResult) = result
    }
    this.validateFunction = validateFunction
    this.activityFrameMetricsFilter = activityFrameMetricsFilter
    this.fragmentFrameMetricsFilter = fragmentFrameMetricsFilter
    this.processScope = processScope
    if (enable && enableCommandReceiver) commandReceiver.register(app)
    if (enable && enableUiComponentMetrics) uiComponentFrameMetrics.register(app)
  }

  @VisibleForTesting
  fun destroy(app: Application) {
    commandReceiver.unregister(app)
    uiComponentFrameMetrics.unregister(app)
    initialized.set(false)
  }

  fun newProcess(): FrameMetricsProcess {
    return processFactory.create()
  }

  private fun defaultListener(): FrameMetricsListener {
    return object : FrameMetricsListener {
      private val logger = FrameMetricsTableLogger { highlight, output ->
        if (highlight) {
          Log.e(LOGTAG, output.toString())
        } else {
          Log.d(LOGTAG, output.toString())
        }
      }

      override fun onFrameMetricsResult(
        id: FrameMetricsId,
        config: KomaConfig,
        aggregateResult: AggregateResult,
        validateResult: ValidateResult
      ) {
        logger.output(
          id = id,
          config = config,
          aggregateResult = aggregateResult,
          validateResult = validateResult,
        )
      }
    }
  }

  private fun defaultConfig(): KomaConfig {
    return KomaConfig(
      frameRate = 60, /* fps */
      analyzeMode = false,
      frozenFrameDurationThreshold = 600, /* ms */
    )
  }

  private fun defaultValidateFunction(): ValidateFunction {
    return PercentileValidation(90.0, 32)
  }
}
