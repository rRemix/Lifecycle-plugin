package com.remix.lifecycleplugin;

import android.app.Application;
import timber.log.Timber;

/**
 * created by Remix on 2019-09-08
 */
public class App extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    Timber.plant(new Timber.DebugTree());
  }
}
