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
            <cno><xsl:value-of select="id"/></cno>
            <ctitle><xsl:value-of select="name"/></ctitle>
            <credit_num><xsl:value-of select="credit"/></credit_num>
            <instructor><xsl:value-of select="teacher"/></instructor>
            <classroom><xsl:value-of select="location"/></classroom>
            <share_flag>
                <xsl:choose>
                    <xsl:when test="shared='true' or shared='1'">1</xsl:when>
                    <xsl:otherwise>0</xsl:otherwise>
                </xsl:choose>
            </share_flag>
        </course>
    </xsl:template>
</xsl:stylesheet>
