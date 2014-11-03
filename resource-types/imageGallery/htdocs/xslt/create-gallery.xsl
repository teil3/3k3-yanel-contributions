<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:i18n="http://www.wyona.org/yanel/i18n/1.0" 
  xmlns:xi="http://www.w3.org/2001/XInclude" 
  xmlns:str="http://xsltsl.org/string" 
  xmlns:gl="http://www.3k3.org/gallery/1.0"
  xmlns="http://www.w3.org/1999/xhtml">

  <xsl:output method="xhtml" encoding="UTF-8" />

  <xsl:param name="yanel.toolbar-status" select="'TOOLBAR-STATUS_IS_NULL'" />
  <xsl:param name="yanel.back2realm" select="'BACK2REALM_IS_NULL'" />
  <xsl:param name="yanel.path.parent" select="'PARENTPATH_IS_NULL'" />
  <xsl:param name="yanel.path.name" select="'PATHNAME_IS_NULL'" />
  <xsl:param name="yanel.path" select="'PATH_IS_NULL'" />
  <xsl:param name="yanel.reservedPrefix" select="'RESERVED_PREFIX_IS_NULL'" />
  <xsl:param name="content-language" select="'CONTENT_LANG_IS_NULL'" />
  <xsl:param name="session.id" select="'RESERVED_PREFIX_IS_NULL'" />
  <xsl:param name="current.position" select="'CURRENT_POSITION_IS_NULL'" />

  <xsl:template match="/">
    <html>
      <head>
      </head>
      <body>
        <p>do you want to creat a gallery?</p>
        <p>
          <form>
             <label for="name">name:</label>
             <input type="text" name="name" id="name"/>
             <input type="hidden" name="resource.imagegallery.usecase" value="create-gallery"/>
             <input type="submit" value="create" name="create" id="create"/>
          </form>
        </p>
      </body>
    </html>
  </xsl:template>

</xsl:stylesheet>
