# koma

"koma" is an Android library that visualizes the UI rendering performance of your app.
It can also output a special log if the UI rendering performance is worse than you expect.

UI rendering performance is measured using the [FrameMetrics](https://developer.android.com/reference/android/view/FrameMetrics) class.
Therefore, this library will be automatically disabled in Android SDKs below 24.

## Measuring UI rendering performance

Performance measurements are logged, along with information such as slow rendering (jank) and screen freezes.

```
 Frame metrics │ count │ ratio   │ max    │ min    │ sum    │ avg      │ median   │ mode
───────────────┼───────┼─────────┼────────┼────────┼────────┼──────────┼──────────┼───────────────
         Total │   7 F │ 100.0 % │ 601 ms │   2 ms │ 758 ms │ 108.3 ms │  17.0 ms │         13 ms
          Jank │   3 F │  17.0 % │  92 ms │  17 ms │ 129 ms │  43.0 ms │  20.0 ms │ 17, 20, 92 ms
        Frozen │   1 F │  79.3 % │ 601 ms │ 601 ms │ 601 ms │ 601.0 ms │ 601.0 ms │        601 ms

 Validation name │ result │ value │ threshold
─────────────────┼────────┼───────┼───────────
 test validation │     NG │   1.0 │       1.0

 Misc.                           │ value
─────────────────────────────────┼────────
                      Frame rate │ 60 fps
 Frozen frame duration threshold │ 600 ms
   Jank frame duration threshold │  16 ms
```

Log format can be customized.

```kotlin
Koma.init(
  frameMetricsListener = { id, config, aggregate, validate ->
    Log.d("koma", "$id total=${aggregate.total?.durations}")
  },
  ...
)
```

Here's how to start UI rendering performance measurement

```kotlin
val process = Koma.newProcess()
val id = FrameMetricsId.Custom("koma id")
process.start(id = id, activity = activity)
...
process.stop(id)
```

If you send Broadcast Intent with the ADB command, you can measure it without adding any code.

```kotlin
Koma.init(
  enableCommandReceiver = true,
  ...
)

// adb shell am broadcast -a jp.yuki312.koma.START
// adb shell am broadcast -a jp.yuki312.koma.STOP
```

You can also automatically measure the performance of all Activities and Fragments.
Exclude those that do not need to be measured.

```kotlin
Koma.init(
  enableUiComponentMetrics = true,
  activityFrameMetricsFilter = ActivityFrameMetricsFilter { activity -> ... },
  fragmentFrameMetricsFilter = FragmentFrameMetricsFilter { false },
  ...
)
```

## Validating UI rendering performance

Use `ValidateFunction` to validate the UI rendering performance.
You can determine that the performance does not meet your expectations.

```kotlin
Koma.init(
  validateFunction = {
    val avg = it.total?.avgDuration ?: 0.0
    ValidateResult.create(
      id = it.id,
      ValidateItem(
        isPassed = avg <= THRESHOLD,
        ...
      )
    )
  },
  frameMetricsListener = { id, config, aggregate, validate ->
    if (!validate.isAllPassed()) Log.d("koma", ";(")
  },
  ...
)
```

By default, `PercentileValidation` is specified, which means that the 90th percentile of the rendering time should be within 32ms.
You can change it to match the level you expect from your app.

```kotlin
Koma.init(
  validateFunction = PercentileValidation(85.0, 16),
  ...
)
```

## Configuration

You can specify the configuration of the measurement with `KomaConfig`.

```kotlin
Koma.init(
  defaultConfig = KomaConfig(
    frameRate = 120, // set for 120fps devices
    analyzeMode = true, // display more detailed measurement results
    frozenFrameDurationThreshold = 600, // Specify the number of milliseconds to consider the screen frozen
  )
  ...
)
```