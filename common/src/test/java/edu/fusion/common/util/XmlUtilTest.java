package edu.fusion.common.util;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlUtilTest {

    @Test
    void createAndParseXmlStringShouldWork() {
        Document document = XmlUtil.createDocument("request");
        Element root = document.getDocumentElement();
        root.appendChild(element(document, "type", "queryCourses"));
        root.appendChild(element(document, "college", "A"));

        String xml = XmlUtil.toString(document);
        Document parsed = XmlUtil.parse(xml);

        assertEquals("request", parsed.getDocumentElement().getTagName());
        assertEquals("queryCourses", XmlUtil.childText(parsed.getDocumentElement(), "type"));
        assertEquals("A", XmlUtil.childText(parsed.getDocumentElement(), "college"));
    }

    @Test
    void childTextShouldReturnEmptyWhenMissing() {
        Document document = XmlUtil.createDocument("response");
        String value = XmlUtil.childText(document.getDocumentElement(), "missingTag");
        assertTrue(value.isEmpty());
    }

    private Element element(Document document, String name, String value) {
        Element element = document.createElement(name);
        element.setTextContent(value);
        return element;
    }
}
