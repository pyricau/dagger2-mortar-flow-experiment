package dagger.demo;

import dagger.demo.dagger.ScopeSingleton;
import dagger.demo.mortarflow.WithComponent;
import flow.Flow;
import flow.Layout;
import flow.Path;
import javax.inject.Inject;
import mortar.ViewPresenter;

@Layout(R.layout.view1) @WithComponent(Screen1.Component.class)
public class Screen1 extends Path {

  @dagger.Component(dependencies = MyActivity.Component.class) //
  @ScopeSingleton(Component.class) //
  interface Component {
    void inject(View1 t);
  }

  @ScopeSingleton(Component.class)
  public static class Presenter extends ViewPresenter<View1> {
    @Inject Presenter() {
    }

    public void viewClicked() {
      Flow.get(getView()).goTo(new Screen2());
    }
  }
}
