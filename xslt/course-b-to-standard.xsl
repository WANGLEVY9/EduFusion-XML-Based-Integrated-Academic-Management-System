<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/B_Courses">
        <courses>
            <xsl:for-each select="B_Course">
                <course>
                    <id><xsl:value-of select="编号"/></id>
                    <name><xsl:value-of select="名称"/></name>
                    <credit><xsl:value-of select="学分值"/></credit>
                    <teacher><xsl:value-of select="教师"/></teacher>
                    <location><xsl:value-of select="教室"/></location>
                    <college>B</college>
                    <shared><xsl:value-of select="共享标记"/></shared>
                </course>
            </xsl:for-each>
        </courses>
    </xsl:template>
</xsl:stylesheet>
