package dagger.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import javax.inject.Inject;
import mortar.Mortar;

public class View1 extends LinearLayout {

  @Inject Screen1.Presenter presenter;

  public View1(Context context, AttributeSet attrs) {
    super(context, attrs);
    Mortar.getScope(context).<Screen1.Component>getObjectGraph().inject(this);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    findViewById(R.id.button).setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        presenter.viewClicked();
      }
    });
    presenter.takeView(this);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    presenter.dropView(this);
  }
}
