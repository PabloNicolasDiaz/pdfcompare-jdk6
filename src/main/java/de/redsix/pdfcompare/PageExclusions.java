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

import java.util.ArrayList;
import java.util.Collection;

public class PageExclusions {

	private final Collection<PageArea> exclusions = new ArrayList<PageArea>();
	private final PageExclusions delegate;

	public PageExclusions() {
		delegate = null;
	}

	public PageExclusions(final PageExclusions delegate) {
		this.delegate = delegate;
	}

	public void add(final PageArea exclusion) {
		exclusions.add(exclusion);
	}

	public boolean contains(final int x, final int y) {
		for (PageArea exclusion : exclusions) {
			if (exclusion.contains(x, y)) {
				return true;
			}
		}
		if (delegate != null) {
			return delegate.contains(x, y);
		}
		return false;
	}

}
