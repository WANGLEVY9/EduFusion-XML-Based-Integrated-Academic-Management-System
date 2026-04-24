<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/courseCollection">
        <courses>
            <xsl:for-each select="courseItem">
                <course>
                    <id><xsl:value-of select="Cno"/></id>
                    <name><xsl:value-of select="Cname"/></name>
                    <credit><xsl:value-of select="Credit"/></credit>
                    <teacher><xsl:value-of select="Lecturer"/></teacher>
                    <location><xsl:value-of select="Room"/></location>
                    <college>C</college>
                    <shared><xsl:value-of select="Share"/></shared>
                </course>
            </xsl:for-each>
        </courses>
    </xsl:template>
</xsl:stylesheet>
