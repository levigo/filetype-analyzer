# Overview

A library to identify file formats and to extract meta-data from those files.

## Modules

This project is split into two modules:

- **analyzer-core** -- Core framework for file format identification with lightweight dependencies. Provides base
  matchers for common formats (ZIP, XML, text, images, OOXML, ODF, MODCA, etc.).
- **analyzer** -- Enhanced matchers for PDF (via PDFBox), MS Office (via POI), and RTF, including PDF signature
  validation. Contains more details about the detected format.

Use `analyzer-core` if you only need basic format detection. Use `analyzer` if you need deep extraction of PDF metadata,
Office document properties, or RTF analysis.

## Features

- recognize common formats based on an XML-based matching description
- extract meta-data from certain formats using specialized matchers and extractors
- allow adding new recognized formats

## Usage

### Maven dependency

For full functionality (includes core transitively):

    <dependency>
        <groupId>org.jadice.filetype</groupId>
        <artifactId>analyzer</artifactId>
        <version>2.9.5</version>
    </dependency>

For lightweight format detection only:

    <dependency>
        <groupId>org.jadice.filetype</groupId>
        <artifactId>analyzer-core</artifactId>
        <version>2.9.5</version>
    </dependency>

### Minimal usage

    Map<String, Object> results =
        Analyzer.getInstance("/magic.xml")
            .analyze(new File("my-example-file.pdf"));

    System.out.println("Extension: " + results.get(ExtensionAction.KEY));
    System.out.println("Mime-Type: " + results.get(MimeTypeAction.KEY));

    // some types have specialized matchers providing extra info (analyzer module)
    System.out.println("Details: " + results.get(PDFMatcher.DETAILS_KEY));

### Recognized formats

	application/ms-tnef
	application/msexcel
	application/msvisio
	application/msword
	application/octet-stream
	application/pdf
	application/postscript
	application/x-7z-compressed
	application/x-bash
	application/x-bzip2
	application/x-csh
	application/x-gzip
	application/x-ksh
	application/x-miff
	application/x-rar-compressed
	application/x-sh
	application/x-shockwave-flash
	application/x-tar
	application/zip
	application/zip-spanned
	application/zip;protection=encrypted
	image/g3fax
	image/gif
	image/heic
	image/heif
	image/jpeg
	image/png
	image/tiff
	image/vnd.djvu
	image/x-emf
	image/x-ms-bmp
	image/x-portable-bitmap
	image/x-wmf
	message/rfc822
	text/html
	text/plain
	text/rtf
	text/sgml
	text/x-c
	text/x-java
	text/x-perl
	video/mpeg
	video/quicktime
