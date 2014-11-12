# Dagger 2 + Mortar + Flow

This is a totally experimental hack to see how we could get Dagger 2, Mortar and Flow to work together. Please do not use this. Seriously.

## Goal

The goal is to see how the screen / view / presenter code would feel.

## Mortar

I modified Mortar to decouple it from Dagger 1. Clone [Mortar](https://github.com/square/mortar), checkout `py/dagger2`, and run `mvn clean install -DskipTests -pl :mortar -am`

## Flow

This project uses the latest Flow snapshot. You can clone [Flow](https://github.com/square/flow) and run `mvn clean install` on master.

## Principle

* With Dagger 2, ObjectGraph is replaced by components. Those define the scopes and hold the references to scoped singletons.
* Instead of being associated to a module, our screens are now associated to a component (which may include 0 or n modules)
* That component has a dependency on the parent component (the parent graph / scope), e.g. MyActivity.Component for a screen
* For each component we define a custom scoping annotation. Bindings marked with that annotation are singletons in that scope.

## What to look at

All the interesting files are [here](https://github.com/pyricau/dagger2-mortar-flow-experiment/tree/master/app/src/main/java/dagger/demo).

### HasScreenDeps

Bindings that belong to a higher level component are not exposed in the child component. To make them available, one needs to declare them on the parent component:

```java
@dagger.Component(modules = ApplicationModule.class) //
interface Component {
  Toaster toaster();
}
```

If there are several layers of components, you need to pass it down at each level. You can use a shared interface to make this process easier:

```java
interface HasScreenDeps {
  Toaster toaster();
  FlowBundler flowBundler();
}
@dagger.Component(modules = ApplicationModule.class) //
interface Component extends HasScreenDeps {
}
```

### Scoping annotations

`@Singleton` is still around but is now just another scoping annotation.

We now can associate a scoping annotation with a component, and then use that annotation on the binding.

```java
@Scope
public @interface MyComponentScope {
}

@dagger.Component
@MyComponentScope
interface Component {
}

@MyComponentScope
public static class Presenter {
  @Inject Presenter() {
  }
}
```

You don't need to create a new annotation for every component though, Dagger2 relies on compile time uniqueness of the annotation value. Therefore, we defined the following:

```java
@Scope
public @interface ScopeSingleton {
  Class<?> value();
}
```

And we can now use it for all components:

```java
@dagger.Component(dependencies = MyActivity.Component.class) //
@ScopeSingleton(Component.class) //
interface Component {
  void inject(View2 t);
}

@ScopeSingleton(Component.class)
public static class Presenter {
  @Inject Presenter() {
  }
}
```

Ideally, `@Singleton` would either be removed from Dagger 2, or updated to have an optional `Class<?>` field. However it's defined in JSR 330 so it can't be done unless Dagger 2 drops the spec.

### ComponentBuilder

In Dagger 2, components must be created manually using generated code, e.g.: `Screen2.Component childComponent = Dagger_Screen2$Component.builder().component(parentComponent).build();`

ComponentBuilder does that for you. Two advantages: less boilerplate in the screen code, and you don't have compilation errors when you move stuff around or if you haven't built yet.

### @WithComponent

We need a way to associate a screen with a component. `@WithComponent` does that:

```java
@Layout(R.layout.view1) @WithComponent(Screen1.Component.class)
public class Screen1 extends Path {

  @dagger.Component(dependencies = MyActivity.Component.class) //
  interface Component {
    void inject(View1 t);
  }
}
```

### Screen / View / Presenter

It's all [in here](https://github.com/pyricau/dagger2-mortar-flow-experiment/tree/master/app/src/main/java/dagger/demo).
