package dagger.demo.mortarflow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import flow.Flow;
import flow.Path;
import flow.PathContainer;
import flow.PathContainerView;
import flow.PathContext;
import flow.PathContextFactory;
import mortar.Blueprint;
import mortar.Mortar;
import mortar.MortarScope;

import static flow.Flow.Direction.REPLACE;

/**
 * Provides basic right-to-left transitions. Saves and restores view state.
 * Uses {@link flow.PathContext} to allow customized sub-containers.
 */
public class SimplePathContainer extends PathContainer {
  public static final class Factory extends PathContainer.Factory {
    public Factory(int tagKey, PathContextFactory contextFactory) {
      super(tagKey, contextFactory);
    }

    @Override public PathContainer createPathContainer(PathContainerView view) {
      return new SimplePathContainer(view, tagKey, contextFactory);
    }
  }

  private final PathContainerView pathContainerView;
  private final PathContextFactory contextFactory;

  SimplePathContainer(PathContainerView pathContainerView, int tagKey, PathContextFactory contextFactory) {
    super(pathContainerView, tagKey);
    this.pathContainerView = pathContainerView;
    this.contextFactory = contextFactory;
  }

  @Override protected void performTraversal(final ViewGroup containerView,
      final TraversalState traversalState, final Flow.Direction direction,
      final Flow.TraversalCallback callback) {

    final PathContext context;
    final PathContext oldPath;
    if (containerView.getChildCount() > 0) {
      oldPath = PathContext.get(containerView.getChildAt(0).getContext());
    } else {
      oldPath = PathContext.root(containerView.getContext());
    }

    final Path to = traversalState.toPath();

    View newView;
    context = PathContext.create(oldPath, to, contextFactory);
    int layout = getLayout(to);

    MortarScope parentScope = Mortar.getScope(pathContainerView.getContext());

    Blueprint blueprint = getBlueprint(to);

    //noinspection unchecked
    Context childContext = parentScope.requireChild(blueprint).createContext(context);

    newView = LayoutInflater.from(childContext)
        .inflate(layout, containerView, false);

    View fromView = null;
    if (traversalState.fromPath() != null) {
      fromView = containerView.getChildAt(0);
      traversalState.saveViewState(fromView);
    }
    traversalState.restoreViewState(newView);

    if (fromView == null || direction == REPLACE) {
      containerView.removeAllViews();
      containerView.addView(newView);
      oldPath.destroyNotIn(context, contextFactory);
      callback.onTraversalCompleted();
    } else {
      containerView.addView(newView);
      final View finalFromView = fromView;
      waitForMeasure(newView, new OnMeasuredCallback() {
        @Override public void onMeasured(View view, int width, int height) {
          runAnimation(containerView, finalFromView, view, direction, new Flow.TraversalCallback() {
            @Override public void onTraversalCompleted() {
              containerView.removeView(finalFromView);
              oldPath.destroyNotIn(context, contextFactory);
              callback.onTraversalCompleted();
            }
          });
        }
      });
    }
  }

  private Blueprint getBlueprint(final Path to) {
    final WithComponent withComponent = to.getClass().getAnnotation(WithComponent.class);
    return new Blueprint() {
      @Override public String getMortarScopeName() {
        return to.getClass().getName();
      }
      @Override public Object createSubgraph(Object o) {
        return ComponentBuilder.build(withComponent.value(), o);
      }
    };
  }

  private void runAnimation(final ViewGroup container, final View from, final View to,
      Flow.Direction direction, final Flow.TraversalCallback callback) {
    Animator animator = createSegue(from, to, direction);
    animator.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        container.removeView(from);
        callback.onTraversalCompleted();
      }
    });
    animator.start();
  }

  public interface OnMeasuredCallback {
    void onMeasured(View view, int width, int height);
  }

  public static void waitForMeasure(final View view, final OnMeasuredCallback callback) {
    int width = view.getWidth();
    int height = view.getHeight();

    if (width > 0 && height > 0) {
      callback.onMeasured(view, width, height);
      return;
    }

    view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
      @Override public boolean onPreDraw() {
        final ViewTreeObserver observer = view.getViewTreeObserver();
        if (observer.isAlive()) {
          observer.removeOnPreDrawListener(this);
        }

        callback.onMeasured(view, view.getWidth(), view.getHeight());

        return true;
      }
    });
  }

  private Animator createSegue(View from, View to, Flow.Direction direction) {
    boolean backward = direction == Flow.Direction.BACKWARD;
    int fromTranslation = backward ? from.getWidth() : -from.getWidth();
    int toTranslation = backward ? -to.getWidth() : to.getWidth();

    AnimatorSet set = new AnimatorSet();

    set.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, fromTranslation));
    set.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, toTranslation, 0));

    return set;
  }
}