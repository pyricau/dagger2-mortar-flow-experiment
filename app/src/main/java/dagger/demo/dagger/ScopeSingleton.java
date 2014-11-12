package dagger.demo.dagger;

import javax.inject.Scope;

@Scope
public @interface ScopeSingleton {
  Class<?> value();
}