package edu.fusion.common.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.dom4j.Document;
import org.dom4j.Element;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Dom4jXmlServiceTest {

    @Test
    void createDocumentShouldHaveRootElement() {
        Document doc = Dom4jXmlService.createDocument("request");
        assertNotNull(doc);
        assertEquals("request", doc.getRootElement().getName());
    }

    @Test
    void parseAndExtractChildText() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<request><type>queryCourses</type><college>A</college></request>";
        Document doc = Dom4jXmlService.parse(xml);
        assertEquals("request", doc.getRootElement().getName());
        assertEquals("queryCourses", Dom4jXmlService.childText(doc.getRootElement(), "type"));
        assertEquals("A", Dom4jXmlService.childText(doc.getRootElement(), "college"));
    }

    @Test
    void childTextShouldReturnEmptyForMissingTag() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><response></response>";
        Document doc = Dom4jXmlService.parse(xml);
        String value = Dom4jXmlService.childText(doc.getRootElement(), "missingTag");
        assertTrue(value.isEmpty());
    }

    @Test
    void addTextElementShouldAppendChild() {
        Document doc = Dom4jXmlService.createDocument("response");
        Element root = doc.getRootElement();
        Dom4jXmlService.addTextElement(root, "success", "true");
        Dom4jXmlService.addTextElement(root, "message", "OK");

        assertEquals("true", Dom4jXmlService.childText(root, "success"));
        assertEquals("OK", Dom4jXmlService.childText(root, "message"));
    }

    @Test
    void toStringShouldProduceValidXml() {
        Document doc = Dom4jXmlService.createDocument("test");
        Element root = doc.getRootElement();
        Dom4jXmlService.addTextElement(root, "key", "value");

        String xml = Dom4jXmlService.toString(doc);
        assertTrue(xml.contains("<test>"));
        assertTrue(xml.contains("<key>value</key>"));
        assertTrue(xml.contains("</test>"));
    }

    @Test
    void toCompactStringShouldProduceSingleLineXml() {
        Document doc = Dom4jXmlService.createDocument("root");
        Dom4jXmlService.addTextElement(doc.getRootElement(), "a", "1");

        String compact = Dom4jXmlService.toCompactString(doc);
        assertFalse(compact.contains("\n  "));
        assertTrue(compact.contains("<root>"));
        assertTrue(compact.contains("</root>"));
    }

    @Test
    void validateAgainstXsdShouldPassForValidXml(@TempDir Path tempDir) throws Exception {
        Path xsdPath = tempDir.resolve("test-schema.xsd");
        writeString(xsdPath,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">"
                + "  <xs:element name=\"data\">"
                + "    <xs:complexType>"
                + "      <xs:sequence>"
                + "        <xs:element name=\"value\" type=\"xs:string\"/>"
                + "      </xs:sequence>"
                + "    </xs:complexType>"
                + "  </xs:element>"
                + "</xs:schema>");

        String validXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><data><value>hello</value></data>";
        assertTrue(Dom4jXmlService.validateAgainstXsd(validXml, xsdPath));
    }

    @Test
    void validateAgainstXsdShouldFailForInvalidXml(@TempDir Path tempDir) throws Exception {
        Path xsdPath = tempDir.resolve("test-schema.xsd");
        writeString(xsdPath,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">"
                + "  <xs:element name=\"data\">"
                + "    <xs:complexType>"
                + "      <xs:sequence>"
                + "        <xs:element name=\"value\" type=\"xs:string\"/>"
                + "      </xs:sequence>"
                + "    </xs:complexType>"
                + "  </xs:element>"
                + "</xs:schema>");

        String invalidXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><invalid><value>hello</value></invalid>";
        assertFalse(Dom4jXmlService.validateAgainstXsd(invalidXml, xsdPath));
    }

    @Test
    void validateAgainstXsdWithPathShouldWork(@TempDir Path tempDir) throws Exception {
        Path xsdPath = tempDir.resolve("test-schema.xsd");
        writeString(xsdPath,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">"
                + "  <xs:element name=\"data\">"
                + "    <xs:complexType>"
                + "      <xs:sequence>"
                + "        <xs:element name=\"value\" type=\"xs:string\"/>"
                + "      </xs:sequence>"
                + "    </xs:complexType>"
                + "  </xs:element>"
                + "</xs:schema>");

        Path xmlPath = tempDir.resolve("valid-data.xml");
        writeString(xmlPath,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><data><value>hello</value></data>");

        assertTrue(Dom4jXmlService.validateAgainstXsd(xmlPath, xsdPath));
    }

    @Test
    void transformShouldApplyXslt(@TempDir Path tempDir) throws Exception {
        Path xsltPath = tempDir.resolve("test-transform.xslt");
        writeString(xsltPath,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
                + "  <xsl:template match=\"/\">"
                + "    <result><xsl:value-of select=\"data/value\"/></result>"
                + "  </xsl:template>"
                + "</xsl:stylesheet>");

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><data><value>transformed</value></data>";
        String result = Dom4jXmlService.transform(xml, xsltPath);
        assertTrue(result.contains("<result>transformed</result>"));
    }

    @Test
    void transformWithPathShouldWork(@TempDir Path tempDir) throws Exception {
        Path xsltPath = tempDir.resolve("identity.xslt");
        writeString(xsltPath,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
                + "  <xsl:template match=\"@*|node()\">"
                + "    <xsl:copy><xsl:apply-templates select=\"@*|node()\"/></xsl:copy>"
                + "  </xsl:template>"
                + "</xsl:stylesheet>");

        Path xmlPath = tempDir.resolve("input.xml");
        Path targetPath = tempDir.resolve("output.xml");
        writeString(xmlPath,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><item>data</item></root>");

        Dom4jXmlService.transform(xmlPath, xsltPath, targetPath);
        assertTrue(Files.exists(targetPath));
        String content = readString(targetPath);
        assertTrue(content.contains("<item>data</item>"));
    }

    @Test
    void integrationXmlLifecycle() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<request><type>statistics</type></request>";
        Document doc = Dom4jXmlService.parse(xml);
        assertEquals("statistics", Dom4jXmlService.childText(doc.getRootElement(), "type"));

        Document response = Dom4jXmlService.createDocument("response");
        Element root = response.getRootElement();
        Dom4jXmlService.addTextElement(root, "success", "true");
        Dom4jXmlService.addTextElement(root, "message", "Statistics generated");

        String responseXml = Dom4jXmlService.toString(response);
        assertTrue(responseXml.contains("<success>true</success>"));
        assertTrue(responseXml.contains("Statistics generated"));
    }

    private static void writeString(Path path, String content) throws Exception {
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    private static String readString(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
