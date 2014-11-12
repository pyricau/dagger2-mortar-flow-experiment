package dagger.demo;

import dagger.demo.dagger.ScopeSingleton;
import dagger.demo.mortarflow.WithComponent;
import flow.Layout;
import flow.Path;
import javax.inject.Inject;
import mortar.ViewPresenter;

@Layout(R.layout.view2) @WithComponent(Screen2.Component.class)
public class Screen2 extends Path {

  @dagger.Component(dependencies = MyActivity.Component.class) //
  @ScopeSingleton(Component.class) //
  interface Component {
    void inject(View2 t);
  }

  @ScopeSingleton(Component.class)
  public static class Presenter extends ViewPresenter<View2> {
    private final Toaster toaster;

    @Inject Presenter(Toaster toaster) {
      this.toaster = toaster;
    }

    public void viewClicked() {
      toaster.toastYo();
    }
  }
}
