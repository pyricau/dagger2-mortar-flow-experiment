package dagger.demo;

import android.content.Context;
import android.widget.Toast;
import dagger.demo.dagger.ScopeSingleton;
import javax.inject.Inject;

@ScopeSingleton(MyApplication.Component.class) public class Toaster {

  private Context context;

  @Inject public Toaster(Context context) {
    this.context = context;
  }

  public void toastYo() {
    Toast.makeText(context, "Yo", Toast.LENGTH_SHORT).show();
  }
}
