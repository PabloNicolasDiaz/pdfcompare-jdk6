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

import static de.redsix.pdfcompare.PdfComparator.DPI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.Charsets;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;

import lombok.Cleanup;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * Exclusions collect rectangular areas of the document, that shall be ignored
 * during comparison. Each area is specified through a {@link PageArea} object.
 *
 * Exclusions can be read from a file in JSON format (or actually a superset
 * called
 * <a href="https://github.com/lightbend/config/blob/master/HOCON.md">HOCON</a>)
 * which has the following form:
 * 
 * <pre>
 * exclusions: [
 *     {
 *         page: 2
 *         x1: 300 // entries without a unit are in pixels, when Pdf is rendered at 300DPI
 *         y1: 1000
 *         x2: 550
 *         y2: 1300
 *     },
 *     {
 *         // page is optional. When not given, the exclusion applies to all pages.
 *         x1: 130.5mm // entries can also be given in units of cm, mm or pt (DTP-Point defined as 1/72 Inches)
 *         y1: 3.3cm
 *         x2: 190mm
 *         y2: 3.7cm
 *     },
 *     {
 *         page: 7
 *         // coordinates are optional. When not given, the whole page is excluded.
 *     }
 * ]
 * </pre>
 *
 */
@Slf4j
public class Exclusions {

	private static final float CM_TO_PIXEL = 1 / 2.54f * DPI;
	private static final float MM_TO_PIXEL = CM_TO_PIXEL / 10;
	private static final float PT_TO_PIXEL = 300f / 72f;
	private static final Pattern NUMBER = Pattern.compile("([0-9.]+)(cm|mm|pt)");
	private static final ConfigParseOptions configParseOptions = ConfigParseOptions.defaults()
			.setSyntax(ConfigSyntax.CONF).setAllowMissing(true);
	private final Map<Integer, PageExclusions> exclusionsPerPage = new HashMap<Integer, PageExclusions>();
	private final PageExclusions exclusionsForAllPages = new PageExclusions();

	public Exclusions add(final PageArea exclusion) {
		val page = exclusion.getPage();
		if (page < 0) {
			exclusionsForAllPages.add(exclusion);
		} else {
			if (exclusionsPerPage.get(page) == null) {
				val x = new PageExclusions(exclusionsForAllPages);
				x.add(exclusion);
				exclusionsPerPage.put(page, x);
			}
		}
		return this;
	}

	public PageExclusions forPage(final int page) {
		val p = Integer.valueOf(page);
		if (exclusionsPerPage.containsKey(p))
			return exclusionsPerPage.get(p);
		return exclusionsForAllPages;
	}

	public void readExclusions(final String filename) {
		if (filename != null) {
			readExclusions(new File(filename));
		}
	}

	public void readExclusions(final File file) {
		requireNonNull(file, "file must not be null");
		if (file.exists()) {
			val config = ConfigFactory.parseFile(file, configParseOptions);
			readFromConfig(config);
		} else {
			log.info("Ignore-file at '{}' not found. Continuing without ignores.", file);
		}
	}

	private static <T> T requireNonNull(T t, String string) {
		if (t == null)
			throw new NullPointerException(string);
		return t;
	}

	@SuppressWarnings("deprecation")
	public void readExclusions(InputStream inputStream) {
		if (inputStream != null) {
			try {
				@Cleanup
				val inputStreamReader = new InputStreamReader(inputStream, Charsets.UTF_8);
				readExclusions(inputStreamReader);
			} catch (IOException e) {
				log.warn("Could not read ignores from InputStream. Continuing without ignores.", e);
			}
		}
	}

	public void readExclusions(Reader reader) {
		if (reader != null) {
			final Config config = ConfigFactory.parseReader(reader, configParseOptions);
			readFromConfig(config);
		}
	}

	private void readFromConfig(final Config load) {
		final List<? extends ConfigObject> exclusions = load.getObjectList("exclusions");
		for (val co : exclusions) {
			val c = co.toConfig();
			PageArea pa;
			if (!c.hasPath("x1") && !c.hasPath("y1") && !c.hasPath("x2") && !c.hasPath("y2")) {
				pa = new PageArea(c.getInt("page"));
			} else if (c.hasPath("page")) {
				pa = new PageArea(c.getInt("page"), toPix(c, "x1"), toPix(c, "y1"), toPix(c, "x2"), toPix(c, "y2"));
			} else {
				pa = new PageArea(toPix(c, "x1"), toPix(c, "y1"), toPix(c, "x2"), toPix(c, "y2"));
			}
			this.add(pa);
		}
	}

	private int toPix(final Config c, final String key) {
		try {
			return c.getInt(key);
		} catch (ConfigException.WrongType e) {
			final String valueStr = c.getString(key);
			final Matcher matcher = NUMBER.matcher(valueStr);
			if (matcher.matches()) {
				float factor = 0;
				if ("mm".equals(matcher.group(2))) {
					factor = MM_TO_PIXEL;
				} else if ("cm".equals(matcher.group(2))) {
					factor = CM_TO_PIXEL;
				} else if ("pt".equals(matcher.group(2))) {
					factor = PT_TO_PIXEL;
				}
				return Math.round(factor * Float.parseFloat(matcher.group(1)));
			} else {
				throw new RuntimeException("Exclusion can't be read. String not parsable to a number: " + valueStr);
			}
		}
	}
}
