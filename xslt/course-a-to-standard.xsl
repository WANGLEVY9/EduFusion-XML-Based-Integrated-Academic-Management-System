<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/课程列表">
        <courses>
            <xsl:apply-templates select="课程"/>
        </courses>
    </xsl:template>

    <xsl:template match="课程">
        <course>
            <id><xsl:value-of select="课程编号"/></id>
            <name><xsl:value-of select="课程名称"/></name>
            <credit><xsl:value-of select="学分"/></credit>
            <teacher><xsl:value-of select="授课教师"/></teacher>
            <location><xsl:value-of select="上课地点"/></location>
            <college>A</college>
            <shared><xsl:value-of select="是否共享"/></shared>
        </course>
    </xsl:template>
</xsl:stylesheet>
