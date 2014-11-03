<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:i18n="http://www.wyona.org/yanel/i18n/1.0" 
  xmlns:xi="http://www.w3.org/2001/XInclude" 
  xmlns:str="http://xsltsl.org/string" 
  xmlns:gl="http://www.3k3.org/gallery/1.0"
  xmlns="http://www.w3.org/1999/xhtml">
  
  <xsl:param name="yanel.back2realm" select="'BACK2REALM_IS_NULL'" />

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="xhtml:head">
    <head>
      <link href="{$yanel.back2realm}css/gallery.css" rel="stylesheet" type="text/css" />
      <xsl:apply-templates/>
    </head>
  </xsl:template>
  
  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
