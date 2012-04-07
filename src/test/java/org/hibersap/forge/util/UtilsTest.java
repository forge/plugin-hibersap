/*
 * Copyright (C) 2012 akquinet AG
 *
 * This file is part of the Forge Hibersap Plugin.
 *
 * The Forge Hibersap Plugin is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * The Forge Hibersap Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with the Forge Hibersap Plugin. If not, see <http://www.gnu.org/licenses/>.
 */

package org.hibersap.forge.util;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 
 * @author Max Schwaab
 *
 */
public class UtilsTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test(expected = IllegalArgumentException.class)
	public void checkPathIncorrect() {
		final String tempPath = this.folder.getRoot().getAbsolutePath();

		Utils.checkPath(tempPath);
	}

	@Test
	public void checkPathCorrect() {
		final String tempPath = this.folder.getRoot().getAbsolutePath() + "\\";

		Utils.checkPath(tempPath);
	}

	@Test
	public void toCamelCaseUpperCase() {
		Assert.assertEquals("ConvertThisText", Utils.toCamelCase("CONVERT_THIS_TEXT", '_'));
	}

	@Test
	public void toCamelCaseUpperSingleCase() {
		Assert.assertEquals("Convert", Utils.toCamelCase("CONVERT", '_'));
	}

	@Test
	public void toCamelCaseLowerCase() {
		Assert.assertEquals("ConvertThisText", Utils.toCamelCase("convert_this_text", '_'));
	}

	@Test
	public void toCamelCaseLowerSingleCase() {
		Assert.assertEquals("Convert", Utils.toCamelCase("convert", '_'));
	}

	@Test
	public void toCamelCaseMixedCase() {
		Assert.assertEquals("ConvertThisText", Utils.toCamelCase("Convert_This_Text", '_'));
	}

	@Test
	public void toCamelCaseMixedSingleCase() {
		Assert.assertEquals("Convert", Utils.toCamelCase("cOnVert", '_'));
	}

	@Test
	public void toCamelCaseEmpty() {
		Assert.assertEquals("", Utils.toCamelCase("", '_'));
	}

	@Test
	public void toCamelCaseNull() {
		Assert.assertEquals(null, Utils.toCamelCase(null, '_'));
	}

	@Test
	public void toCamelCaseSpacer() {
		Assert.assertEquals("ConvertThisText", Utils.toCamelCase("CONVERT/THIS/TEXT", '/'));
	}

}
