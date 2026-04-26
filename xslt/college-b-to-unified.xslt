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
            <id><xsl:value-of select="cno"/></id>
            <name><xsl:value-of select="ctitle"/></name>
            <credit><xsl:value-of select="credit_num"/></credit>
            <teacher><xsl:value-of select="instructor"/></teacher>
            <location><xsl:value-of select="classroom"/></location>
            <college>B</college>
            <shared>
                <xsl:choose>
                    <xsl:when test="share_flag='1' or share_flag='Y' or share_flag='y'">true</xsl:when>
                    <xsl:otherwise>false</xsl:otherwise>
                </xsl:choose>
            </shared>
        </course>
    </xsl:template>
</xsl:stylesheet>
