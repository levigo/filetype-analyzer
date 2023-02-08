package org.jadice.filetype.matchers.xml;

import java.io.IOException;

import org.jadice.filetype.Context;
import org.jadice.filetype.matchers.XMLMatcher;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.Locator2;
import org.xml.sax.helpers.DefaultHandler;

public final class SAXAnalysisHandler extends DefaultHandler {

    private final XMLMatcher xmlMatcher;
    private final Context context;

    boolean hitRoot = false;

    private String rootElementName;

    private String namespaceURI;

    private Locator2 locator;

    private String xmlVersion;

    private String encoding;

    public SAXAnalysisHandler(XMLMatcher xmlMatcher, final Context context) {
        this.xmlMatcher = xmlMatcher;
        this.context = context;
    }

    @Override
    public void setDocumentLocator(final Locator locator) {
        if (locator instanceof Locator2) {
            this.locator = (Locator2) locator;
        }
    }

    @Override
    public InputSource resolveEntity(final String publicId, final String systemId) throws IOException, SAXException {
        // Don't resolve any entities
        return null;
    }

    @Override
    public void startDocument() throws SAXException {
        if (locator != null) {
            this.xmlVersion = locator.getXMLVersion();
            this.encoding = locator.getEncoding();
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName,
                             final Attributes attributes) {
        if (!hitRoot) {
            hitRoot = true;

            if (uri != null && !uri.isEmpty()) {
                namespaceURI = uri;
                context.info(xmlMatcher, "Found namespace URI: '" + uri + "'");
            }

            rootElementName = localName;
            context.info(xmlMatcher, "Found root element: '" + localName + "'");
        }
    }

    public String getRootElementName() {
        return rootElementName;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public String getXmlVersion() {
        return xmlVersion;
    }

    public String getEncoding() {
        return encoding;
    }
}
