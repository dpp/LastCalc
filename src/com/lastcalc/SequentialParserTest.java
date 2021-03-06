/*******************************************************************************
 * LastCalc - The last calculator you'll ever need
 * Copyright (C) 2011, 2012 Uprizer Labs LLC
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU Affero General Public License as published 
 * by the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU Affero General Public License for more 
 * details.
 ******************************************************************************/
package com.lastcalc;

import java.util.*;

import junit.framework.Assert;

import org.jscience.mathematics.number.*;
import org.jscience.mathematics.number.Number;
import org.junit.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class SequentialParserTest {

	@Test
	public void negativesTest() {
		final SequentialParser sp = SequentialParser.create();
		final TokenList res = sp.parseNext("-15+3");
		Assert.assertEquals(LargeInteger.valueOf(-12), res.get(0));
	}

	@Test
	public void parseWithPrevAnswer() {
		final SequentialParser sp = SequentialParser.create();
		sp.parseNext("49+3");
		final TokenList res = sp.parseNext("+1");
		Assert.assertEquals(1, res.size());
		Assert.assertTrue(res.get(0) instanceof LargeInteger);
		Assert.assertEquals(53, ((LargeInteger) res.get(0)).intValue());
	}

	@Test
	public void ifTest() {
		final SequentialParser sp = SequentialParser.create();
		final TokenList v = sp.parseNext("if 7==5 then 1 else 0");
		Assert.assertEquals(1, v.size());
		Assert.assertEquals(0, ((Number<?>) v.get(0)).longValue());
	}

	@Test
	public void incrementTest() {
		final SequentialParser sp = SequentialParser.create();
		sp.parseNext("increment [] = []");
		sp.parseNext("increment [H ... T] = [H+1 ... increment T]");
		// sp.setDumpSteps(true);
		final long startTime = System.currentTimeMillis();
		final TokenList inc = sp.parseNext("increment [1,2,3,4,5,6,7,8]");
		sp.setDumpSteps(false);
		System.out.println("Increment steps required: \t" + sp.getLastParseStepCount() + " \t"
				+ (System.currentTimeMillis() - startTime) + "ms");
		Assert.assertTrue(sp.getLastParseStepCount() <= 44);
		Assert.assertEquals("Expected [2,3,4,5,6,7,8,9] but was " + inc, 1, inc.size());
		Assert.assertTrue(inc.get(0) instanceof List);
		final List<Object> list = (List<Object>) inc.get(0);
		Assert.assertEquals(8, list.size());
		Assert.assertEquals(2, ((LargeInteger) list.get(0)).intValue());
		Assert.assertEquals(3, ((LargeInteger) list.get(1)).intValue());
		Assert.assertEquals(4, ((LargeInteger) list.get(2)).intValue());
		Assert.assertEquals(5, ((LargeInteger) list.get(3)).intValue());
		Assert.assertEquals(6, ((LargeInteger) list.get(4)).intValue());
		Assert.assertEquals(7, ((LargeInteger) list.get(5)).intValue());
		Assert.assertEquals(8, ((LargeInteger) list.get(6)).intValue());
		Assert.assertEquals(9, ((LargeInteger) list.get(7)).intValue());
	}

	@Test
	public void filterTest() {
		final SequentialParser sp = SequentialParser.create();
		sp.parseNext("aboveFive [] = []");
		sp.parseNext("aboveFive [H ... T] = if H > 5 then [H ... aboveFive T] else aboveFive T");
		final long startTime = System.currentTimeMillis();
		final TokenList inc = sp.parseNext("aboveFive [4,5,6,7,8,9,10,11,12,13,14,15, 16, 17, 18, 19, 20]");
		System.out.println("Filter steps required: \t" + sp.getLastParseStepCount() + " \t"
				+ (System.currentTimeMillis() - startTime) + "ms");
		Assert.assertTrue(sp.getLastParseStepCount() <= 222);
		Assert.assertEquals("Expected list but was " + inc, 1, inc.size());
		Assert.assertTrue(inc.get(0) instanceof List);
		final List<Object> list = (List<Object>) inc.get(0);
		Assert.assertEquals(15, list.size());
		Assert.assertEquals(6, ((LargeInteger) list.get(0)).intValue());
		Assert.assertEquals(7, ((LargeInteger) list.get(1)).intValue());
		Assert.assertEquals(8, ((LargeInteger) list.get(2)).intValue());
		Assert.assertEquals(9, ((LargeInteger) list.get(3)).intValue());
		Assert.assertEquals(10, ((LargeInteger) list.get(4)).intValue());
		Assert.assertEquals(11, ((LargeInteger) list.get(5)).intValue());
		Assert.assertEquals(12, ((LargeInteger) list.get(6)).intValue());
		Assert.assertEquals(13, ((LargeInteger) list.get(7)).intValue());
		Assert.assertEquals(14, ((LargeInteger) list.get(8)).intValue());
		Assert.assertEquals(15, ((LargeInteger) list.get(9)).intValue());
		Assert.assertEquals(16, ((LargeInteger) list.get(10)).intValue());
		Assert.assertEquals(17, ((LargeInteger) list.get(11)).intValue());
		Assert.assertEquals(18, ((LargeInteger) list.get(12)).intValue());
		Assert.assertEquals(19, ((LargeInteger) list.get(13)).intValue());
		Assert.assertEquals(20, ((LargeInteger) list.get(14)).intValue());
	}

	@Test
	public void concatTest() {
		final SequentialParser sp = SequentialParser.create();
		sp.parseNext("concat [[]] = []");
		sp.parseNext("concat [[] ... R] = concat R");
		sp.parseNext("concat [[H ... T1] ... T2] = [H ... concat [T1 ... T2]]");
		final long startTime = System.currentTimeMillis();
		final TokenList res = sp.parseNext("concat [[1, 2], [3, 4]]");
		System.out.println("Concat steps required: \t" + sp.getLastParseStepCount() + "\t"
				+ (System.currentTimeMillis() - startTime) + "ms");
		Assert.assertTrue(sp.getLastParseStepCount() <= 36);
		Assert.assertEquals(1, res.size());
		Assert.assertTrue(res.get(0) instanceof List);
		final List<Object> list = (List<Object>) res.get(0);
		Assert.assertEquals(4, list.size());
		Assert.assertEquals(1, ((LargeInteger) list.get(0)).intValue());
		Assert.assertEquals(2, ((LargeInteger) list.get(1)).intValue());
		Assert.assertEquals(3, ((LargeInteger) list.get(2)).intValue());
		Assert.assertEquals(4, ((LargeInteger) list.get(3)).intValue());
	}

	@Test
	public void negativeExpTest() {
		final SequentialParser sp = SequentialParser.create();
		Assert.assertEquals(4, ((org.jscience.mathematics.number.Number) sp.parseNext("0.5^-2").get(0)).intValue());
	}

	@Test
	public void addListsTest() {
		final SequentialParser sp = SequentialParser.create();
		sp.parseNext("[H1 ... T1] + [H2 ... T2] = [H1 + H2 ... (T1 + T2)]");
	}

	@Test
	public void precedenceTest() {
		final SequentialParser sp = SequentialParser.create();
		Assert.assertEquals(13, ((org.jscience.mathematics.number.Number) sp.parseNext("3+5*2").get(0)).intValue());
		Assert.assertEquals(4, ((org.jscience.mathematics.number.Number) sp.parseNext("2*(6/3)").get(0)).intValue());
		Assert.assertEquals(6, ((org.jscience.mathematics.number.Number) sp.parseNext("4-1+3").get(0)).intValue());
		Assert.assertEquals(0, ((org.jscience.mathematics.number.Number) sp.parseNext("4-1-3").get(0)).intValue());
	}

	@Test
	public void backtrackTest() {
		final SequentialParser sp = SequentialParser.create();
		final TokenList result = sp.parseNext("100 lb in kg");
		Assert.assertEquals(1, result.size());
	}

	@Test
	public void radixTest() {
		final SequentialParser sp = SequentialParser.create();
		final TokenList result = sp.parseNext("(0x1b + 0o20 + 0b101) in binary");
		Assert.assertEquals(1, result.size());
		Assert.assertEquals("0b110000", result.get(0).toString());
	}

	@Test
	public void stringFromMapTest() {
		final SequentialParser sp = SequentialParser.create();
		final TokenList result = sp.parseNext("get \"blah\" from {\"blah\": 3, \"oaf\" : 2}");
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(LargeInteger.valueOf(3), result.get(0));
	}

	@Test
	public void powTest() {
		final SequentialParser sp = SequentialParser.create();
		final TokenList result = sp.parseNext("25^(1/2)");
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(5.0,
				((org.jscience.mathematics.number.Number) result.get(0)).doubleValue());
	}

	@Test
	public void stepRangeTest() {
		final SequentialParser sp = SequentialParser.create();
		sp.parseNext("step([]) = []");
		sp.parseNext("step([H...T]) = if T!=[] then [H,step(T)] else [H]");
		sp.parseNext("range(X)(Y) = if X<Y then [X...range(X+1)(Y)] else []");
		sp.parseNext("range(X) = range(0)(X)");
		final TokenList result = sp.parseNext("step(range(2))");
		Assert.assertEquals("[0, [1]]", result.get(0).toString());
	}

	@Test
	public void mathOpTest() {
		final SequentialParser sp = SequentialParser.create();
		Assert.assertTrue(sp.parseNext("sin 3").get(0) instanceof Number);
	}

	@Test
	public void toLowerCaseTest() {
		final SequentialParser sp = SequentialParser.create();
		Assert.assertEquals(sp.parseNext("pi"), sp.parseNext("Pi"));
	}

	@Test
	public void mapMatchTest() {
		final SequentialParser sp = SequentialParser.create();
		sp.parseNext("tst {K:V ... R} {K:V2 ... R2} = R2");
		final TokenList res = sp.parseNext("tst {1:3, 2:3} {1:4}");
		Assert.assertEquals(1, res.size());
		Assert.assertTrue(res.get(0) instanceof Map);
		Assert.assertTrue(((Map<Object, Object>) res.get(0)).isEmpty());
	}

	// @Test
	public void recurseTest() {
		final SequentialParser sp = SequentialParser.create();
		sp.parseNext("double X = X * 2");
		sp.parseNext("F List = apply (X=F X) to List");
		sp.setDumpSteps(true);
		final TokenList res = sp.parseNext("double [1,2,3]");
		Assert.assertEquals(1, res.size());
	}
}
