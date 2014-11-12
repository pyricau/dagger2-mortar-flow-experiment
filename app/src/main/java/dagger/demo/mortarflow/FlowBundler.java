package dagger.demo.mortarflow;

import android.os.Bundle;
import flow.Backstack;
import flow.Flow;
import flow.Parceler;

public abstract class FlowBundler {
  private static final String FLOW_KEY = "flow_key";

  private final Parceler parceler;

  private Flow flow;

  protected FlowBundler(Parceler parceler) {
    this.parceler = parceler;
  }

  public Flow onCreate(Bundle savedInstanceState) {
    if (flow != null) return flow;

    Backstack restoredBackstack = null;
    if (savedInstanceState != null && savedInstanceState.containsKey(FLOW_KEY)) {
      restoredBackstack = Backstack.from(savedInstanceState.getParcelable(FLOW_KEY), parceler);
    }
    flow = new Flow(getColdStartBackstack(restoredBackstack));
    return flow;
  }

  public void onSaveInstanceState(Bundle outState) {
    Backstack backstack = getBackstackToSave(flow.getBackstack());
    if (backstack == null) return;
    outState.putParcelable(FLOW_KEY, backstack.getParcelable(parceler));
  }

  /**
   * Returns the backstack that should be archived by {@link #onSaveInstanceState}. Overriding
   * allows subclasses to handle cases where the current configuration is not one that should
   * survive process death.  The default implementation returns a BackStackToSave that specifies
   * that view state should be persisted.
   *
   * @return the stack to archive, or null to archive nothing
   */
  protected Backstack getBackstackToSave(Backstack backstack) {
    return backstack;
  }

  /**
   * Returns the backstack to initialize the new flow.
   *
   * @param restoredBackstack the backstack recovered from the bundle passed to {@link #onCreate},
   * or null if there was no bundle or no backstack was found
   */
  protected abstract Backstack getColdStartBackstack(Backstack restoredBackstack);
}