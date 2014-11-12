package dagger.demo;

import dagger.demo.mortarflow.FlowBundler;

public interface HasScreenDeps {
  // We're exposing these bindings to components that depend directly on this component.
  Toaster toaster();
  FlowBundler flowBundler();
}
