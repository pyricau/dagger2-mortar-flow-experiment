package dagger.demo;

import android.app.Application;
import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dagger.Module;
import dagger.Provides;
import dagger.demo.dagger.ScopeSingleton;
import dagger.demo.mortarflow.FlowBundler;
import dagger.demo.mortarflow.GsonParceler;
import flow.Backstack;
import flow.Parceler;

@Module
final class ApplicationModule {
  private final Application application;

  ApplicationModule(Application application) {
    this.application = application;
  }

  @Provides Application application() {
    return application;
  }

  @Provides Context context() {
    return application;
  }

  @Provides @ScopeSingleton(MyApplication.Component.class) Gson provideGson() {
    return new GsonBuilder().create();
  }

  @ScopeSingleton(MyApplication.Component.class) @Provides Parceler provideParcer(Gson gson) {
    return new GsonParceler(gson);
  }

  @ScopeSingleton(MyApplication.Component.class) @Provides FlowBundler provideFlowBundler(
      Parceler parceler) {
    return new FlowBundler(parceler) {
      @Override protected Backstack getColdStartBackstack(Backstack restoredBackstack) {
        return restoredBackstack == null ? Backstack.single(new Screen1()) : restoredBackstack;
      }
    };
  }
}
