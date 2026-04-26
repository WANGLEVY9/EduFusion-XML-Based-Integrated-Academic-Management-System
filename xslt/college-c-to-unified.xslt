<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

    <xsl:template match="/">
        <courses>
            <xsl:apply-templates select="courses/course"/>
        </courses>
    </xsl:template>

    <xsl:template match="course">
        <course>
            <id><xsl:value-of select="cid"/></id>
            <name><xsl:value-of select="cname"/></name>
            <credit><xsl:value-of select="credit"/></credit>
            <teacher><xsl:value-of select="teacher"/></teacher>
            <location><xsl:value-of select="room"/></location>
            <college>C</college>
            <shared>
                <xsl:choose>
                    <xsl:when test="is_shared='1' or is_shared='Y' or is_shared='y'">true</xsl:when>
                    <xsl:otherwise>false</xsl:otherwise>
                </xsl:choose>
            </shared>
        </course>
    </xsl:template>
</xsl:stylesheet>
