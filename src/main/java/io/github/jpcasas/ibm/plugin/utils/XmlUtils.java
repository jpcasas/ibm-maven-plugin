package io.github.jpcasas.ibm.plugin.utils;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * XML factories hardened against XXE (external entities / DTD loading). Project files
 * such as .project, .policyxml or .mxml never need DTDs.
 */
public final class XmlUtils {

	private XmlUtils() {
	}

	public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
		dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		dbf.setXIncludeAware(false);
		dbf.setExpandEntityReferences(false);
		DocumentBuilder builder = dbf.newDocumentBuilder();
		// report errors through the exception only (the default handler prints to stderr)
		builder.setErrorHandler(new ErrorHandler() {
			@Override
			public void warning(SAXParseException e) {
			}

			@Override
			public void error(SAXParseException e) throws SAXException {
				throw e;
			}

			@Override
			public void fatalError(SAXParseException e) throws SAXException {
				throw e;
			}
		});
		return builder;
	}

	public static TransformerFactory newTransformerFactory() throws TransformerConfigurationException {
		TransformerFactory tf = TransformerFactory.newInstance();
		tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		return tf;
	}
}
