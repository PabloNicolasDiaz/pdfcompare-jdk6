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

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.DefaultResourceCache;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.redsix.pdfcompare.env.Environment;

public class ResourceCacheWithLimitedImages extends DefaultResourceCache {

	private static final Logger LOG = LoggerFactory.getLogger(ResourceCacheWithLimitedImages.class);
	private final Environment environment;
	private final Map<COSObject, SoftReference<PDXObject>> xobjects = new LinkedHashMap<COSObject, SoftReference<PDXObject>>() {

		/**
		 *
		 */
		private static final long serialVersionUID = 3895067084718344201L;

		@Override
		protected boolean removeEldestEntry(final Entry<COSObject, SoftReference<PDXObject>> eldest) {
			return size() > environment.getNrOfImagesToCache();
		}
	};

	public ResourceCacheWithLimitedImages(Environment environment) {
		this.environment = environment;
	}

	@Override
	public PDXObject getXObject(COSObject indirect) throws IOException {
		SoftReference<PDXObject> xobject = this.xobjects.get(indirect);
		if (xobject != null) {
			return xobject.get();
		}
		return null;
	}

	@Override
	public void put(COSObject indirect, PDXObject xobject) throws IOException {
		final int length = xobject.getStream().getLength();
		if (length > environment.getMaxImageSize()) {
			LOG.trace("Not caching image with Size: {}", length);
			return;
		}
		if (xobject instanceof PDImageXObject) {
			PDImageXObject imageObj = (PDImageXObject) xobject;
			if (imageObj.getWidth() * imageObj.getHeight() > environment.getMaxImageSize()) {
				return;
			}
		}
		this.xobjects.put(indirect, new SoftReference<PDXObject>(xobject));
	}
}
