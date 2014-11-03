<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:xi="http://www.w3.org/2001/XInclude"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  exclude-result-prefixes="xhtml dc">

  <xsl:param name="yanel.path.name" select="'NAME_IS_NULL'"/>
  <xsl:param name="yanel.path" select="'PATH_IS_NULL'"/>
  <xsl:param name="yanel.back2context" select="'BACK2CONTEXT_IS_NULL'"/>
  <xsl:param name="yarep.back2realm" select="'BACK2REALM_IS_NULL'"/>
  <xsl:variable name="name-without-suffix" select="substring-before($yanel.path.name, '.')"/>
  <xsl:param name="language" select="'LANGUAGE_IS_NULL'"/>
  <xsl:param name="content-language" select="'CONTENT_LANGUAGE_IS_NULL'"/>
  <xsl:param name="languages" select="'LANGUAGES_IS_NULL'"/>

  <xsl:template match="/">
    <html>
      <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

        <!-- See http://www.w3.org/TR/REC-CSS2/media.html -->
        <link media="screen" type="text/css" href="{$yarep.back2realm}css/screen.css" rel="stylesheet"/>
        <link media="print" type="text/css" href="{$yarep.back2realm}css/print.css" rel="stylesheet"/>

        <!-- The following copy statement is copying for example header stuff from the content source, but also in the case of the TinyMCE or Xinha resource the important javascript and CSS links! -->
        <xsl:copy-of select="/xhtml:html/xhtml:head/*[name(.) != 'title']"/>
        <title>
          <xsl:value-of select="/xhtml:html/xhtml:head/xhtml:title"/>
        </title>
      </head>
      <body>
        <div id="page">


          <div id="header">
           <a href="{$yarep.back2realm}index.html"><img src="{$yarep.back2realm}svg/3k3-yanel-contrib-logo.svg?yanel.resource.viewid=png" alt="yanel logo" id="header-logo"/></a>
          </div>
          <!-- END of header -->

          <xsl:call-template name="navi"/>

          <div id="home">
            <div id="body">
              <xsl:apply-templates select="/xhtml:html/xhtml:body/*"/>
            </div>

            <div id="footer">
              &#169;2010 3k3.org
            </div>
          </div>
        </div>
<script type="text/javascript">

  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', 'UA-579712-3']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();

</script>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template name="navi">
    <div id="left">
      <!-- Use content language instead localization -->
      <xi:include href="yanelresource:/navigation/menu.xml?path={$yanel.path}"/>
    </div>
  </xsl:template>


  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>


</xsl:stylesheet>
