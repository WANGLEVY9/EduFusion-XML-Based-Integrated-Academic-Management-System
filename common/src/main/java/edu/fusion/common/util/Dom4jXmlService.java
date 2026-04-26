package edu.fusion.common.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class Dom4jXmlService {

    private static final SAXReader reader = new SAXReader();
    private static final OutputFormat prettyFormat = OutputFormat.createPrettyPrint();

    static {
        prettyFormat.setEncoding("UTF-8");
        prettyFormat.setIndent(true);
        prettyFormat.setIndentSize(2);
        try {
            reader.setFeature("http://apache.org/xml/features/validation/schema", true);
            reader.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
        } catch (SAXException e) {
            throw new IllegalStateException("Failed to configure SAXReader features", e);
        }
    }

    private Dom4jXmlService() {
    }

    public static Document createDocument(String rootName) {
        return DocumentHelper.createDocument();
    }

    public static Element addRootElement(Document document, String rootName) {
        return document.addElement(rootName);
    }

    public static Document parse(String xmlContent) {
        try {
            return reader.read(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to parse XML content", e);
        }
    }

    public static Document parse(Path xmlFile) {
        try {
            return reader.read(xmlFile.toFile());
        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to parse XML file: " + xmlFile, e);
        }
    }

    public static String toString(Document document) {
        try {
            Writer writer = new StringWriter();
            XMLWriter xmlWriter = new XMLWriter(writer, prettyFormat);
            xmlWriter.write(document);
            xmlWriter.close();
            return writer.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize XML document", e);
        }
    }

    public static String toCompactString(Document document) {
        try {
            Writer writer = new StringWriter();
            OutputFormat compact = OutputFormat.createCompactFormat();
            compact.setEncoding("UTF-8");
            XMLWriter xmlWriter = new XMLWriter(writer, compact);
            xmlWriter.write(document);
            xmlWriter.close();
            return writer.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize XML document", e);
        }
    }

    public static String childText(Element parent, String childTag) {
        Element child = parent.element(childTag);
        return child == null ? "" : child.getText();
    }

    public static Element childElement(Element parent, String childTag) {
        Element child = parent.element(childTag);
        if (child == null) {
            throw new IllegalArgumentException("Child element not found: " + childTag);
        }
        return child;
    }

    public static Element addTextElement(Element parent, String name, String value) {
        Element element = parent.addElement(name);
        element.setText(value == null ? "" : value);
        return element;
    }

    public static boolean validateAgainstXsd(String xmlContent, Path xsdPath) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(xsdPath.toFile());
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8))));
            return true;
        } catch (SAXException | IOException e) {
            return false;
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

    public static void validateOrThrow(String xmlContent, Path xsdPath) {
        if (!validateAgainstXsd(xmlContent, xsdPath)) {
            throw new IllegalArgumentException("XML content failed XSD validation: " + xsdPath);
        }
    }

    public static void validateOrThrow(Path xmlPath, Path xsdPath) {
        if (!validateAgainstXsd(xmlPath, xsdPath)) {
            throw new IllegalArgumentException("XML file failed XSD validation: " + xmlPath + " against " + xsdPath);
        }
    }

    public static String transform(String xmlContent, Path xsltPath) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Source xslt = new StreamSource(xsltPath.toFile());
            Transformer transformer = transformerFactory.newTransformer(xslt);
            Writer writer = new StringWriter();
            transformer.transform(
                    new StreamSource(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8))),
                    new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException e) {
            throw new IllegalStateException("Failed to transform XML by XSLT: " + xsltPath, e);
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
}
