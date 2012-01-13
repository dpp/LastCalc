package com.lastcalc.parsers.amounts;

import java.util.*;

import javax.measure.converter.ConversionException;

import com.google.common.collect.*;

import org.jscience.physics.amount.Amount;

import com.lastcalc.TokenList;
import com.lastcalc.parsers.*;


public abstract class AmountMathOp extends Parser {
	private static final long serialVersionUID = -2510157348504286501L;
	private final String description;
	private final TokenList template;
	private final Set<String> subordinateTo;

	public AmountMathOp(final Object operator, final String description, final Set<String> subordinateTo) {
		this.description = description;
		template = TokenList.createD(Amount.class, operator, Amount.class);
		this.subordinateTo = subordinateTo;
	}

	@Override
	public ParseResult parse(final TokenList tokens, final int templatePos) {
		final Amount<?> a = (Amount<?>) tokens.get(templatePos);
		final Amount<?> b = (Amount<?>) tokens.get(templatePos + 2);
		for (final Object token : PreParser.enclosedByStructure(tokens, templatePos)) {
			if (subordinateTo.contains(token))
				return Parser.ParseResult.fail();
		}
		try {
			return Parser.ParseResult.success(tokens.replaceWithTokens(templatePos, templatePos + template.size(),
					operation(a, b)));
		} catch (final ConversionException ce) {
			return Parser.ParseResult.fail();
		}
	}

	@Override
	public String toString() {
		return description;
	}

	protected abstract Amount<?> operation(final Amount<?> a, final Amount<?> b) throws ConversionException;

	@Override
	public TokenList getTemplate() {
		return template;
	}

	public static List<Parser> getOps() {
		final List<Parser> ops = Lists.newLinkedList();

		ops.add(new AmountMathOp("^", "Raise to Power", Collections.<String> emptySet()) {
			private static final long serialVersionUID = 4974989053479508332L;

			@Override
			protected Amount<?> operation(final Amount<?> a, final Amount<?> b) throws ConversionException {
				if (b.getEstimatedValue() != b.getExactValue())
					throw new ConversionException("Can only raise to an integer power");
				return a.pow((int) b.getExactValue());
			}
		});

		ops.add(new AmountMathOp(Lists.newArrayList("*", "for"), "Multiply", Collections.singleton("^")) {

			private static final long serialVersionUID = 9166899968748997536L;

			@Override
			protected Amount<?> operation(final Amount<?> a, final Amount<?> b) throws ConversionException {
				return a.times(b);
			}
		});
		ops.add(new AmountMathOp(Lists.newArrayList("/", "at", "in"), "Divide", Sets.newHashSet("*", "^")) {
			private static final long serialVersionUID = 4974989053479508332L;

			@Override
			protected Amount<?> operation(final Amount<?> a, final Amount<?> b) throws ConversionException {
				return a.divide(b);
			}
		});
		ops.add(new AmountMathOp("+", "Add", Sets.newHashSet("^", "*", "/")) {
			private static final long serialVersionUID = 8999743708212969031L;

			@Override
			protected Amount<?> operation(final Amount<?> a, final Amount<?> b) throws ConversionException {
				return a.plus(b);
			}
		});
		ops.add(new AmountMathOp("-", "Subtract", Sets.newHashSet("^", "*", "/", "+")) {

			private static final long serialVersionUID = 7206664452245347470L;

			@Override
			protected Amount<?> operation(final Amount<?> a, final Amount<?> b) throws ConversionException {
				return a.minus(b);
			}
		});


		ops.add(new RewriteParser("plus", "+"));
		ops.add(new RewriteParser("minus", "-"));
		ops.add(new RewriteParser("multiply", "*"));
		ops.add(new RewriteParser(Lists.newArrayList("multiplied", "by"), "*"));
		ops.add(new RewriteParser("divide", "/"));
		ops.add(new RewriteParser(Lists.newArrayList("divided", "by"), "/"));
		ops.add(new RewriteParser(Lists.newArrayList("to", "the", "power", "of"), "^"));
		ops.add(new UnitStripper());

		return ops;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((template == null) ? 0 : template.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AmountMathOp))
			return false;
		final AmountMathOp other = (AmountMathOp) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (template == null) {
			if (other.template != null)
				return false;
		} else if (!template.equals(other.template))
			return false;
		return true;
	}

}