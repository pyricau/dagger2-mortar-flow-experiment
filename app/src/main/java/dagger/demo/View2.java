package dagger.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import javax.inject.Inject;
import mortar.Mortar;

public class View2 extends LinearLayout {

  @Inject Screen2.Presenter presenter;

  public View2(Context context, AttributeSet attrs) {
    super(context, attrs);
    Mortar.getScope(context).<Screen2.Component>getObjectGraph().inject(this);
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
