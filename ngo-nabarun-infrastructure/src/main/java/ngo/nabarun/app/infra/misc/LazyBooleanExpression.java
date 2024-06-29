package ngo.nabarun.app.infra.misc;

import com.querydsl.core.types.dsl.BooleanExpression;

@FunctionalInterface
public interface LazyBooleanExpression {
	BooleanExpression get();
}