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
            <cid><xsl:value-of select="id"/></cid>
            <cname><xsl:value-of select="name"/></cname>
            <credit><xsl:value-of select="credit"/></credit>
            <teacher><xsl:value-of select="teacher"/></teacher>
            <room><xsl:value-of select="location"/></room>
            <is_shared>
                <xsl:choose>
                    <xsl:when test="shared='true' or shared='1'">1</xsl:when>
                    <xsl:otherwise>0</xsl:otherwise>
                </xsl:choose>
            </is_shared>
        </course>
    </xsl:template>
</xsl:stylesheet>
