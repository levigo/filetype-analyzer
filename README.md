# Overview

A library to identify file formats and to extract meta-data from those files.

## Features

- recognize common formats based on an XML-based matching description
- extract meta-data from certain formats using specialized matchers and extractors
- allow adding new recognized formats

## Usage

### Maven dependency

    <dependency>
        <groupId>org.jadice.filetype</groupId>
        <artifactId>analyzer</artifactId>
        <version>2.8.2</version>
    </dependency>

### Minimal usage

    Map<String, Object> results =
        Analyzer.getInstance("/magic.xml")
            .analyze(new File("my-example-file.pdf"));

    System.out.println("Extension: " + results.get(ExtensionAction.KEY));
    System.out.println("Mime-Type: " + results.get(MimeTypeAction.KEY));

    // some types have specialized matchers providing extra info
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
