# PdfCompare  

[![Build Status](https://travis-ci.org/PabloNicolasDiaz/pdfcompare-jdk6.svg?branch=master)](https://travis-ci.org/PabloNicolasDiaz/pdfcompare-jdk6) 
[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.PabloNicolasDiaz/pdfcompare-jdk6.svg)](http://search.maven.org/#search|gav|1|g:"io.github.pablonicolasdiaz"%20AND%20a:"pdfcompare")
[![DepShield Badge](https://depshield.sonatype.org/badges/PabloNicolasDiaz/pdfcompare-jdk6/depshield.svg)](https://depshield.github.io)

A simple Java library to compare two PDF files.
Files are rendered and compared pixel by pixel.
Forked from pdfcompare. Backported to Java 1.6

### Usage with Maven

Just include it as dependency. Please check for the most current version available:

```xml
<dependencies>
  <dependency>
    <groupId>io.github.pablonicolasdiaz</groupId>
    <artifactId>pdfcompare-jdk6</artifactId>
    <version>...</version> <!-- see current version in the maven central tag above -->
  </dependency>
</dependencies>
```

### Simple Usage

There is a simple interactive UI, when you start the Class de.redsix.pdfcompare.Main 
without any additional arguments. Next to the UI you can provide an expected and actual 
file as well as an optional result file by CLI.
```
usage: java -jar pdfcompare-jdk6-x.x.x.jar [EXPECTED] [ACTUAL]
 -h,--help              Displays this text and exit
 -o,--output <output>   Provide an optional output file for the result
```

But the focus of PdfCompare is on embedded usage as a library.

```java
new PdfComparator("expected.pdf", "actual.pdf").compare().writeTo("diffOutput");
```
This will produce an output PDF which may include markings for differences found.
Pixels that are equal are faded a bit. Pixels that differ are marked in red and green.
Green for pixels that were expected, but didn't come.
Red for pixels that are there, but were not expected.
And there are markings at the edge of the paper in magenta to find areas that differ quickly.
Ignored Areas are marked with a yellow background.
Pages that were expected, but did not come are marked with a red border.
Pages that appear, but were not expected are marked with a green border.

The colors used can be changed. To change them, add a file called "application.conf"
to the root of the classpath. In this file you can specify new colors in HTML-Stlye format (without a leading '#'):

- expectedColor=D20000

    The expected color is the color that is used for pixels that were expected, but are not there.
    The first two characters define the red-portion of the color in hexadecimal. The next two characters define the green-portion
    of the color. The last two characters define the blue-portion of the color to use.

- actualColor=00B400

    The actual color is the color that is used for pixels that are there, but were not expected.
    The first two characters define the red-portion of the color in hexadecimal. The next two characters define the green-portion
    of the color. The last two characters define the blue-portion of the color to use.

The compare-method returns a CompareResult, which can be queried:

```java
final CompareResult result = new PdfComparator("expected.pdf", "actual.pdf").compare();
if (result.isNotEqual()) {
    System.out.println("Differences found!");
}
if (result.isEqual()) {
    System.out.println("No Differences found!");
}
if (result.hasDifferenceInExclusion()) {
    System.out.println("Differences in excluded areas found!");
}
result.getDifferences(); // returns page areas, where differences were found
```
For convenience, writeTo also returns the equals status:
```java
boolean isEquals = new PdfComparator("expected.pdf", "actual.pdf").compare().writeTo("diffOutput");
if (!isEquals) {
    System.out.println("Differences found!");
}
```
The compare method can be called with filenames as Strings, Files, Paths or InputStreams.

### Exclusions

It is also possible to define rectangular areas that are ignored during comparison. For that, a file needs to be created, which defines areas to ignore.
The file format is JSON (or actually a superset called [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md)) and has the following form:
```javascript
exclusions: [
    {
        page: 2
        x1: 300 // entries without a unit are in pixels. Pdfs are rendered by default at 300DPI
        y1: 1000
        x2: 550
        y2: 1300
    },
    {
        // page is optional. When not given, the exclusion applies to all pages.
        x1: 130.5mm // entries can also be given in units of cm, mm or pt (DTP-Point defined as 1/72 Inches)
        y1: 3.3cm
        x2: 190mm
        y2: 3.7cm
    },
    {
        page: 7
        // coordinates are optional. When not given, the whole page is excluded.
    }
]
```

When the provided exclusion file is not found, it is ignored and the compare is done without the exclusions.

Exclusions are provided in the code as follows:

```java
new PdfComparator("expected.pdf", "actual.pdf").withIgnore("ignore.conf").compare();
```

Alternatively an Exclusion can be added via the API as follows:

```java
new PdfComparator("expected.pdf", "actual.pdf")
	.withIgnore(new PageArea(1, 230, 350, 450, 420))
	.withIgnore(new PageArea(2))
	.compare();
```
### Encrypted PDF files

When you want to compare password protected PDF files, you can give the password to the Comparator through the withExpectedPassword(String password) or withActualPassword(String password) methods respectively.

```java
new PdfComparator("expected.pdf", "actual.pdf")
    .withExpectedPassword("somePwd")
    .withActualPassword("anotherPwd")
    .compare();
```

### Allow for a difference in percent per page

If for some reason your rendering is a little off or you allow for some error margin, you can configure a percentage of pixels that are ignored during comparison.
That way a difference is only reported, when more than the given percentage of pixels differ. The percentage is calculated per page.

To use this feature, just add a file called "application.conf" to the root of the classpath.
In that file you can add a setting:

- allowedDifferenceInPercentPerPage=0.2

    Percent of pixels that may differ per page. Default is 0.

### Different CompareResult Implementations

There are a few different Implementations of CompareResults with different characteristics.
The can be used to control certain aspects of the system behaviour, in particular memory consumption.

#### Internals about memory consumption

It is good to know a few internals, when using the PdfCompare.
Here is in a nutshell, what PdfCompare does, when it compares two PDFs.

PdfCompare uses the Apache PdfBox Library to read and write Pdfs.

- The Two Pdfs to compare are opened with PdfBox.
- A page from each Pdf is read and rendered into a BufferedImage by default at 300dpi.
- A new empty BufferedImage is created to take the result of the comparison. It has the maximum size of the expected and the actual image.
- When the comparison is finished, the new BufferedImage, which holds the result of the comparison, is kept in memory in a CompareResult object. Holding on to the CompareResult means, that the images are also kept in memory. If memory consumption is a problem, a CompareResultWithPageOverflow or a CompareResultWithMemoryOverflow can be used. Those classes store images to a temporary folder on disk, when certain thresholds are reached.
- After all pages are compared, a new Pdf is created and the images are written page by page into the new Pdf.

So comparing large Pdfs can use up a lot of memory.
I didn't yet find a way to write the difference Pdf page by page incrementally with PdfBox, but there are some workarounds.

#### CompareResults with Overflow

There are currently two different CompareResults, that have different strategies for swapping pages to disk and thereby limiting memory consumption.
- CompareResultWithPageOverflow - stores a bunch of pages into a partial Pdf and merges the resulting Pdfs in the end. The default is to swap every 10 pages, which is a good balance between memory usage and performance.
- CompareResultWithMemoryOverflow - tries to keep as many images in memory as possible and swaps, when a critical amount of memory is consumed by the JVM. As a default, pages are swapped, when 70% of the maximum available heap is filled.

A different CompareResult implementation can be used as follows:

```java
new PdfComparator("expected.pdf", "actual.pdf", new CompareResultWithPageOverflow()).compare();
```

Also there are some internal settings for memory limits, that can be changed.
Just add a file called "application.conf" to the root of the classpath. This file can have some or all of the following settings to overwrite the defaults given here:

- imageCacheSizeCount=30

    How many images are cached by PdfBox
- maxImageSizeInCache=100000

    A rough maximum size of images that are cached, to prevent very big images from being cached
- mergeCacheSizeMB=100

    When Pdfs are partially written and later merged, this is the memory cache that is configured for the PdfBox instance that does the merge.
- swapCacheSizeMB=100

    When Pdfs are partially written, this is the memory cache that is configured for the PdfBox instance that does the partial writes.
- documentCacheSizeMB=200

    This is the cache size configured for the PdfBox instance, that loads the documents that are compared.
- parallelProcessing=true

    When set to false, disables all parallel processing and process everything in a single thread.
- overallTimeoutInMinutes=15

    Set the overall timeout. This is a safety measure to detect possible deadlocks. Complex comparisons might take longer, so this value might have to be increased.

So in this default configuration, PdfBox should use up to 400MB of Ram for it's caches, before swapping to disk.
I have good experience with granting a 2GB heap space to the JVM.

### configuring PdfComparator though an API

All the settings, that can be changed through the application.conf file can also be changed programmatically through the API.
To do so you can use the following code:
```java
new PdfComparator("expected.pdf", "actual.pdf")
	.withEnvironment(new SimpleEnvironment()
        .setActualColor(Color.green)
        .setExpectedColor(Color.blue))
	.compare();
```
The SimpleEnvironment delegates all settings, that were not assigned, to the default Environment.

Through the environment you can configure the memory settings (see above) and the following settings:

- DPI=300

    Sets the DPI that Pdf pages are rendered with. Default is 300.
    
- expectedColor=D20000

    The expected color is the color that is used for pixels that were expected, but are not there.
    The first two characters define the red-portion of the color in hexadecimal. The next two characters define the green-portion
    of the color. The last two characters define the blue-portion of the color to use.

- actualColor=00B400

    The actual color is the color that is used for pixels that are there, but were not expected.
    The first two characters define the red-portion of the color in hexadecimal. The next two characters define the green-portion
    of the color. The last two characters define the blue-portion of the color to use.

- tempDir=System.property("java.io.tmpdir")

    Sets the directory where to write temporary files. Defaults to the java default for java.io.tmpdir, which usually determines a
    system specific default, like /tmp on most unix systems.

- allowedDifferenceInPercentPerPage=0.2

    Percent of pixels that may differ per page. Default is 0.

- parallelProcessing=true

    When set to false, disables all parallel processing and process everything in a single thread.

### Acknowledgements

Big thanks to Chethan Rao <meetchethan@gmail.com> for helping me diagnose out of memory problems and providing
the idea of partial writes and merging of the generated PDFs.
