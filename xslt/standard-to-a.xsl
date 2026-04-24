<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/courses">
        <课程列表>
            <xsl:for-each select="course">
                <课程>
                    <课程编号><xsl:value-of select="id"/></课程编号>
                    <课程名称><xsl:value-of select="name"/></课程名称>
                    <学分><xsl:value-of select="credit"/></学分>
                    <授课教师><xsl:value-of select="teacher"/></授课教师>
                    <上课地点><xsl:value-of select="location"/></上课地点>
                    <是否共享><xsl:value-of select="shared"/></是否共享>
                </课程>
            </xsl:for-each>
        </课程列表>
    </xsl:template>
</xsl:stylesheet>
