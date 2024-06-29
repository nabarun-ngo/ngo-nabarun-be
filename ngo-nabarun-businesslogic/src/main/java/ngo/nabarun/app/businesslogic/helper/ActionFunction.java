package ngo.nabarun.app.businesslogic.helper;

@FunctionalInterface
public interface ActionFunction<T, U, R> {
	R exec(T t, U u) throws Exception;
}
