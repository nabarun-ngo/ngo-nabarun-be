package ngo.nabarun.app.infra.misc;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

public class WhereClause {
	private BooleanBuilder delegate;
	
	public static WhereClause builder() {
		return new WhereClause();
	}
	
	public static WhereClause builder(Predicate right) {
		return new WhereClause(right);
	}
	
	public BooleanBuilder build() {
		return delegate;
	}

	private WhereClause() {
		this.delegate = new BooleanBuilder();
	}
	
	private WhereClause(Predicate right) {
		this.delegate = new BooleanBuilder(right);
	}

	public WhereClause and(Predicate right) {
		delegate = delegate.and(right);
		return this;
	}

	public WhereClause optionalAnd(boolean pBoolValue, LazyBooleanExpression pBooleanExpression) {
		if (pBoolValue) {
			delegate = delegate.and(pBooleanExpression.get());
		}
		return this;
	}
	
	public WhereClause or(Predicate right) {
		delegate = delegate.or(right);
		return this;
	}

	public WhereClause optionalOr(boolean pBoolValue, LazyBooleanExpression pBooleanExpression) {
		if (pBoolValue) {
			delegate = delegate.or(pBooleanExpression.get());
		}
		return this;
	}

}
