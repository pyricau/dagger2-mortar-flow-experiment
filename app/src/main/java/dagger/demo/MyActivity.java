package dagger.demo;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import dagger.demo.dagger.ScopeSingleton;
import dagger.demo.mortarflow.ComponentBuilder;
import dagger.demo.mortarflow.FlowBundler;
import flow.Flow;
import flow.HasParent;
import flow.Path;
import flow.PathContainerView;
import java.util.UUID;
import javax.inject.Inject;
import mortar.Blueprint;
import mortar.Mortar;
import mortar.MortarActivityScope;

public class MyActivity extends Activity implements Flow.Dispatcher {

  @ScopeSingleton(Component.class) //
  @dagger.Component(dependencies = MyApplication.Component.class) //
  interface Component extends HasScreenDeps {
    void inject(MyActivity activity);
  }

  @Inject FlowBundler flowBundler;

  private Flow flow;
  private PathContainerView pathContainerView;
  private MortarActivityScope activityScope;
  private String scopeName;
  private boolean configurationChangeIncoming;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Blueprint blueprint = new Blueprint() {
      @Override public String getMortarScopeName() {
        return getBootstrapScopeName();
      }

      @Override public Object createSubgraph(Object o) {
        return ComponentBuilder.build(Component.class, o);
      }
    };

    activityScope = Mortar.requireActivityScope(Mortar.getScope(getApplication()), blueprint);
    activityScope.<Component>getObjectGraph().inject(this);

    flow = flowBundler.onCreate(savedInstanceState);

    activityScope.onCreate(savedInstanceState);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_demo);
    pathContainerView = (PathContainerView) findViewById(R.id.container);
  }

  /**
   * Returns the name of the scope started by this activity's original ancestor. If there is none,
   * it's time to start a new scope with a new universally unique name.
   */
  private String getBootstrapScopeName() {
    if (scopeName == null) scopeName = (String) getLastNonConfigurationInstance();
    if (scopeName == null) {
      scopeName = this.getClass().getName() + "-" + UUID.randomUUID().toString();
    }
    return scopeName;
  }

  @Override public Object getSystemService(String name) {
    if (Mortar.isScopeSystemService(name)) {
      return activityScope;
    }
    if (Flow.isFlowSystemService(name)) {
      return flow;
    }
    return super.getSystemService(name);
  }

  @Override public Object onRetainNonConfigurationInstance() {
    configurationChangeIncoming = true;
    return activityScope == null ? null : activityScope.getName();
  }

  @Override protected void onResume() {
    super.onResume();
    flow.setDispatcher(this);
  }

  @Override protected void onPause() {
    super.onPause();
    flow.removeDispatcher(this);
  }

  @SuppressLint("DefaultLocale") @Override protected void onDestroy() {
    super.onDestroy();
    if (!configurationChangeIncoming) {
      if (activityScope != null) {
        if (!activityScope.isDestroyed()) {
          Mortar.getScope(getApplication()).destroyChild(activityScope);
        }
        activityScope = null;
      }
    }
  }

  @Override public void onBackPressed() {
    boolean handled = Flow.get(pathContainerView.getCurrentChild()).goBack();
    if (!handled) {
      finish();
    }
  }

  @Override public void dispatch(Flow.Traversal traversal,
      Flow.TraversalCallback traversalCallback) {
    Path path = traversal.destination.current();
    pathContainerView.dispatch(traversal, traversalCallback);

    setTitle(path.getClass().getSimpleName());
    ActionBar actionBar = getActionBar();
    boolean hasUp = path instanceof HasParent;
    actionBar.setDisplayHomeAsUpEnabled(hasUp);
    actionBar.setHomeButtonEnabled(hasUp);
  }
}
