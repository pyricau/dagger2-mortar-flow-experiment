package dagger.demo;

import android.app.Application;
import dagger.demo.dagger.ScopeSingleton;
import dagger.demo.mortarflow.ComponentBuilder;
import mortar.Mortar;
import mortar.MortarScope;

public class MyApplication extends Application {

  @dagger.Component(modules = ApplicationModule.class) //
  @ScopeSingleton(Component.class) interface Component extends HasScreenDeps {
  }

  private MortarScope rootScope;

  @Override
  public void onCreate() {
    super.onCreate();

    Component component = ComponentBuilder.build(Component.class, new ApplicationModule(this));

    rootScope = Mortar.createRootScope(component);
  }

  @Override public Object getSystemService(String name) {
    if (Mortar.isScopeSystemService(name)) {
      return rootScope;
    }
    return super.getSystemService(name);
  }
}
