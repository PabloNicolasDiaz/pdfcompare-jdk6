/*
 * Copyright 2016 Malte Finsterwalder
 * Copyright [2018] Pablo Nicolas Diaz Bilotto [https://github.com/PabloNicolasDiaz/]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.redsix.pdfcompare;

import java.util.Collection;

public interface CompareResult {

	/**
	 * Write the result Pdf to a file. Warning: This will remove the diffImages from
	 * memory! Writing can only be done once.
	 *
	 * @param filename without pdf-Extension
	 * @return a boolean indicating, whether the comparison is equal. When true, the
	 * files are equal.
	 */
	boolean writeTo(String filename);

	/**
	 * Returns, whether the compared documents are equal or not. Documents are also
	 * equal, when differences are only in excluded areas.
	 *
	 * @return true, when no differences are found.
	 */
	boolean isEqual();

	/**
	 * The inverse of isEqual()
	 *
	 * @return true, when differences are found.
	 */
	boolean isNotEqual();

	/**
	 * Allows to check for differences in excluded areas.
	 *
	 * @return true, when there where differences in excluded areas.
	 */
	boolean hasDifferenceInExclusion();

	/**
	 * Only true, when there was no actual document at all.
	 *
	 * @return true, when the actual document was missing or not readable.
	 */
	boolean hasOnlyExpected();

	/**
	 * Only true, when there was no expected document at all.
	 *
	 * @return true, when the expected document was missing or not readable.
	 */
	boolean hasOnlyActual();

	/**
	 * Shows, whether one of the two document to comapre was missing or unreadable.
	 *
	 * @return true, when only an ectual or an expected document was found or
	 * readable, but not both.
	 */
	boolean hasOnlyOneDoc();

	/**
	 * Gives a PageArea, that shows the area of a page, where differences where
	 * found. There is only one PageArea per page, which contains all differences of
	 * the page.
	 *
	 * @return a collection of PageAreas, where differences where found.
	 */
	Collection<PageArea> getDifferences();
}
