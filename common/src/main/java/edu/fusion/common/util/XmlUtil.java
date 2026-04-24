package edu.fusion.common.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

public final class XmlUtil {

    private XmlUtil() {
    }

    public static Document createDocument(String rootName) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            document.appendChild(document.createElement(rootName));
            return document;
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Failed to create XML document", e);
        }
    }

    public static Document parse(Path xmlFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(xmlFile.toFile());
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new IllegalStateException("Failed to parse XML: " + xmlFile, e);
        }
    }

    public static Document parse(String xmlContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            try (InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes("UTF-8"))) {
                return builder.parse(inputStream);
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new IllegalStateException("Failed to parse XML content", e);
        }
    }

    public static void write(Document document, Path targetFile) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(document), new StreamResult(targetFile.toFile()));
        } catch (TransformerException e) {
            throw new IllegalStateException("Failed to write XML: " + targetFile, e);
        }
    }

    public static String toString(Document document) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(document), new StreamResult(outputStream));
            return outputStream.toString("UTF-8");
        } catch (TransformerException | IOException e) {
            throw new IllegalStateException("Failed to convert XML document to string", e);
        }
    }

    public static boolean validateAgainstXsd(Path xmlPath, Path xsdPath) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(xsdPath.toFile());
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xmlPath.toFile()));
            return true;
        } catch (SAXException | IOException e) {
            return false;
        }
    }

    public static void transform(Path xmlPath, Path xsltPath, Path targetPath) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Source xslt = new StreamSource(xsltPath.toFile());
            Transformer transformer = transformerFactory.newTransformer(xslt);
            transformer.transform(new StreamSource(xmlPath.toFile()), new StreamResult(targetPath.toFile()));
        } catch (TransformerException e) {
            throw new IllegalStateException("Failed to transform XML by XSLT", e);
        }
    }

    public static String childText(Element parent, String childTag) {
        if (parent.getElementsByTagName(childTag).getLength() == 0) {
            return "";
        }
        return parent.getElementsByTagName(childTag).item(0).getTextContent();
    }

    public static File toFile(Path path) {
        return path.toFile();
    }
}
