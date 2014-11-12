package dagger.demo.mortarflow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import dagger.demo.R;
import flow.Flow;
import flow.Path;
import flow.PathContainer;
import flow.PathContainerView;

public class PathFrameLayout extends FrameLayout implements PathContainerView {

  private final PathContainer container;
  private boolean disabled;

  @SuppressWarnings("UnusedDeclaration") // Used by layout inflation, of course!
  public PathFrameLayout(Context context, AttributeSet attrs) {
    this(context, attrs,
        new SimplePathContainer.Factory(R.id.screen_switcher_tag, Path.contextFactory()));
  }

  /**
   * Allows subclasses to use custom {@link flow.PathContainer} implementations. Allows the use
   * of more sophisticated transition schemes, and customized context wrappers.
   */
  protected PathFrameLayout(Context context, AttributeSet attrs,
      PathContainer.Factory switcherFactory) {
    super(context, attrs);
    container = switcherFactory.createPathContainer(this);
  }

  @Override public boolean dispatchTouchEvent(MotionEvent ev) {
    return !disabled && super.dispatchTouchEvent(ev);
  }

  @Override public ViewGroup getContainerView() {
    return this;
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
  }

  @Override public void dispatch(Flow.Traversal traversal, final Flow.TraversalCallback callback) {
    disabled = true;
    container.executeTraversal(this, traversal, new Flow.TraversalCallback() {
      @Override public void onTraversalCompleted() {
        callback.onTraversalCompleted();
        disabled = false;
      }
    });
  }

  @Override public ViewGroup getCurrentChild() {
    return (ViewGroup) getContainerView().getChildAt(0);
  }
}