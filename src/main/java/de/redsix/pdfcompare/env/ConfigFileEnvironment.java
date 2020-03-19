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
package de.redsix.pdfcompare.env;

import static org.apache.commons.lang3.Validate.notNull;

import java.awt.*;
import java.io.File;
import java.io.Reader;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;

public class ConfigFileEnvironment implements Environment {

	private static final ConfigParseOptions CONFIG_PARSE_OPTIONS = ConfigParseOptions.defaults();

	private final Config config;

	public ConfigFileEnvironment(File file) {
		notNull(file, "file is null");
		this.config = ConfigFactory.parseFile(file, CONFIG_PARSE_OPTIONS);
	}

	public ConfigFileEnvironment(Reader reader) {
		notNull(reader, "reader is null");
		this.config = ConfigFactory.parseReader(reader, CONFIG_PARSE_OPTIONS);
	}

	public ConfigFileEnvironment(Config config) {
		notNull(config, "config is null");
		this.config = config;
	}

	@Override
	public File getTempDirectory() {
		if (config.hasPath("tempDir")) {
			return new File(config.getString("tempDir"));
		}
		return new File(System.getProperty("java.io.tmpdir"));
	}

	@Override
	public int getNrOfImagesToCache() {
		return config.getInt("imageCacheSizeCount");
	}

	@Override
	public int getMergeCacheSize() {
		return getMB("mergeCacheSizeMB");
	}

	@Override
	public int getSwapCacheSize() {
		return getMB("swapCacheSizeMB");
	}

	@Override
	public int getDocumentCacheSize() {
		return getMB("documentCacheSizeMB") / 2;
	}

	@Override
	public int getMaxImageSize() {
		return config.getInt("maxImageSizeInCache");
	}

	@Override
	public int getOverallTimeout() {
		return config.getInt("overallTimeoutInMinutes");
	}

	@Override
	public boolean useParallelProcessing() {
		return config.getBoolean("parallelProcessing");
	}

	@Override
	public double getAllowedDiffInPercent() {
		if (config.hasPath("allowedDifferenceInPercentPerPage")) {
			return config.getDouble("allowedDifferenceInPercentPerPage");
		}
		return 0;
	}

	@Override
	public Color getExpectedColor() {
		if (config.hasPath("expectedColor")) {
			return Color.decode("#" + config.getString("expectedColor"));
		}
		return new Color(0, 180, 0);
	}

	@Override
	public Color getActualColor() {
		if (config.hasPath("actualColor")) {
			return Color.decode("#" + config.getString("actualColor"));
		}
		return new Color(210, 0, 0);
	}

	@Override
	public int getDPI() {
		if (config.hasPath("DPI")) {
			return config.getInt("DPI");
		}
		return 300;
	}

	private int getMB(final String path) {
		return config.getInt(path) * 1024 * 1024;
	}
}
