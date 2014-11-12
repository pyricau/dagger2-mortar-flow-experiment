package dagger.demo.mortarflow;

import java.lang.reflect.Method;

public class ComponentBuilder {

  /**
   * Magic method that creates a component with its dependencies set, by reflection. Relies on
   * Dagger naming conventions. This code could also be generated at compile time.
   */
  public static <T> T build(Class<T> componentClass, Object... dependencies) {
    String fqn = componentClass.getName();

    String packageName = componentClass.getPackage().getName();
    // Accounts for inner classes, ie MyApplication$Component
    String simpleName = fqn.substring(packageName.length() + 1);

    try {
      Class<?> generatedClass = Class.forName(packageName + ".Dagger_" + simpleName);
      Object builder = generatedClass.getMethod("builder").invoke(null);

      for (Method method : builder.getClass().getMethods()) {
        Class<?>[] params = method.getParameterTypes();
        if (params.length == 1) {
          Class<?> dependencyClass = params[0];
          for (Object dependency : dependencies) {
            if (dependencyClass.isAssignableFrom(dependency.getClass())) {
              method.invoke(builder, dependency);
              break;
            }
          }
        }
      }
      //noinspection unchecked
      return (T) builder.getClass().getMethod("build").invoke(builder);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
