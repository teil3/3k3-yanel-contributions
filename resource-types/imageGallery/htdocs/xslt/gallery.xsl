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
        <xsl:if test="$yanel.toolbar-status = 'on'">
<!--          <link href="{$yanel.back2realm}{$yanel.reservedPrefix}/resource-types/http://www.litwan.com/yanel/resource/1.0::imageGallery/css/admin.css" rel="stylesheet" type="text/css" />-->
          <script type="text/javascript" src="{$yanel.back2realm}{$yanel.reservedPrefix}/resource-types/http://www.litwan.com/yanel/resource/1.0::imageGallery/js/confirmator.js" />
          <script type="text/javascript" src="{$yanel.back2realm}{$yanel.reservedPrefix}/resource-types/http://www.litwan.com/yanel/resource/1.0::imageGallery/js/jquery/jquery-ui-1.8.9.custom.min.js" />
          <script>
            jQuery(document).ready( function() {
                var start;
                jQuery( "#images_items" ).sortable({
                    cancel: '.new_upload',
                    start: function(event, ui) {
                    start = ui.item.prevAll().length;
                  },
                  update: function(event, ui) {
                  var end = ui.item.prevAll().length;
                  //continuing-path=" + (end + 1) + ".html&amp;
                  window.location="?resource.imagegallery.usecase=move&amp;continuing-path=<xsl:value-of select="$yanel.path.parent" />" + (end + 1) + ".html&amp;fromPosition=" + start + "&amp;toPosition=" + end;
                }
              });
            });
  
            function upload(e){
              var target = jQuery.event.fix(e).target;
              jQuery(target).closest("form").submit();
            }
          </script>
  
          <link href="{$yanel.back2realm}{$yanel.reservedPrefix}/resource-types/http://litwan.com/yanel/resource/1.0::swfuploader/uploader.css" rel="stylesheet" type="text/css" />
          <script type="text/javascript" src="{$yanel.back2realm}{$yanel.reservedPrefix}/resource-types/http://litwan.com/yanel/resource/1.0::swfuploader/swfupload.js"></script>
          <script type="text/javascript" src="{$yanel.back2realm}{$yanel.reservedPrefix}/resource-types/http://litwan.com/yanel/resource/1.0::swfuploader/plugins/swfupload.queue.js"></script>
          <script type="text/javascript" src="{$yanel.back2realm}{$yanel.reservedPrefix}/resource-types/http://litwan.com/yanel/resource/1.0::swfuploader/fileprogress.js"></script>
          <script type="text/javascript" src="{$yanel.back2realm}{$yanel.reservedPrefix}/resource-types/http://www.litwan.com/yanel/resource/1.0::imageGallery/js/swfuHandlers.js"></script>
          <script type="text/javascript">
            var swfu;
            window.onload = function() {
              var settings = {
                flash_url : "<xsl:value-of select="$yanel.back2realm" /><xsl:value-of select="$yanel.reservedPrefix" />/resource-types/^http:^2f^2flitwan.com^2fyanel^2fresource^2f1.0::swfuploader/Flash/swfupload.swf",
                upload_url: "<xsl:value-of select="$yanel.path.name" />;jsessionid=<xsl:value-of select="$session.id" />",
                post_params: {"resource.imagegallery.usecase" : "create-image"},
                file_size_limit : "100 MB",
                file_types : "*.jpg;*.JPG",
                file_types_description : "All Files",
                file_upload_limit : 0,
                file_queue_limit : 0,
                custom_settings : {
                progressTarget : "fsUploadProgress",
                cancelButtonId : "btnCancel"
                },
                //debug: true,
      
                // Button settings
                button_image_url: "<xsl:value-of select="$yanel.back2realm" /><xsl:value-of select="$yanel.reservedPrefix" />/resource-types/^http:^2f^2fwww.litwan.com^2fyanel^2fresource^2f1.0::imageGallery/img/image-upload.png",
                button_width: "32",
                button_height: "32",
                button_placeholder_id: "buttonPlaceHolder",
      
                // The event handler functions are defined in handlers.js
                file_queued_handler : fileQueued,
                file_queue_error_handler : fileQueueError,
                file_dialog_complete_handler : fileDialogComplete,
                upload_start_handler : uploadStart,
                upload_progress_handler : uploadProgress,
                upload_error_handler : uploadError,
                upload_success_handler : uploadSuccess,
                upload_complete_handler : uploadComplete,
                queue_complete_handler : queueComplete // Queue plugin event
              };
              swfu = new SWFUpload(settings);
            };
          </script>
        </xsl:if>
        <title>Bilder</title>
        <script type="text/javascript" src="{$yanel.back2realm}{$yanel.reservedPrefix}/resource-types/http://www.litwan.com/yanel/resource/1.0::imageGallery/js/cookie.js" />
        <script type="text/javascript" src="{$yanel.back2realm}{$yanel.reservedPrefix}/resource-types/http://www.litwan.com/yanel/resource/1.0::imageGallery/js/gallery.js" />      
      </head>
      <body>
        <img src="{$yanel.back2realm}{xhtml:html/xhtml:body/xhtml:div[@class='3k3_gallery_current_item']/xhtml:img/@src}" class="current"/>
        <xsl:if test="$yanel.toolbar-status = 'on'">
          <a href="{$yanel.back2realm}{xhtml:html/xhtml:body/xhtml:div[@class='3k3_gallery_current_item']/xhtml:img/@src}.imageutil.html?continue-path={$yanel.path}">
            <img src="{$yanel.back2realm}{$yanel.reservedPrefix}/resource-types/http://www.litwan.com/yanel/resource/1.0::imageGallery/img/edit-imag-icon.png" alt="Bild editieren" title="Bild editieren"/>
          </a>
          <a href="{$yanel.back2realm}{xhtml:html/xhtml:body/xhtml:div[@class='3k3_gallery_current_item']/xhtml:img/@src}.imageutil.html?continue-path={$yanel.path}&amp;rotate=90&amp;addrotate=true&amp;submit=true&amp;yanel.resource.imageutil.rotate.save=true">
            <img src="{$yanel.back2realm}{$yanel.reservedPrefix}/resource-types/http://www.litwan.com/yanel/resource/1.0::imageGallery/img/rotate-right.png" alt="Bild + Legende löschen" title="Bild 90° nach rechts drehen"/>
          </a>          
        </xsl:if>
        <xsl:apply-templates select="xhtml:html/xhtml:body/xhtml:div[@class='3k3_gallery_current_item']"/>
        <div class="3k3_gallery_navi">
          <ul class="3k3_gallery_navi" id="images_items">
            <xsl:for-each select="/xhtml:html/xhtml:body/xhtml:div[@class='3k3_gallery_images']/xhtml:div[@class='3k3_gallery_image']">
<!--            <xsl:value-of select="position()" />-->
              <li class="zahl" id="{position()}">
                <xsl:choose>
                  <xsl:when test="position() = $current.position">
                    <a id="edit-{position()}" class="current">
                      <span class="3k3_gallery_item_position">
                        <xsl:value-of select="position()" />
                      </span>
                      <img src="{$yanel.back2realm}{xhtml:img/@src}.min.jpg" class="current" title="{position()}"/>
                    </a>
                  </xsl:when>
                  <xsl:otherwise>
                    <a href="{position()}.html" id="edit-{position()}">
                      <span class="3k3_gallery_item_position">
                        <xsl:value-of select="position()"/>
                      </span>
                      <img src="{$yanel.back2realm}{xhtml:img/@src}.min.jpg" title="{position()}"/>
                    </a>
                  </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="$yanel.toolbar-status = 'on'">
                  <span href="{$yanel.path.name}?resource.imagegallery.usecase=delete&amp;id={xhtml:img/@id}&amp;continue-path=" id="{position()}" class="confirm_link" name="Bild {position()} wirklich löschen?">
                    <img onclick="" src="{$yanel.back2realm}{$yanel.reservedPrefix}/resource-types/http://www.litwan.com/yanel/resource/1.0::imageGallery/img/delete.png" alt="Bild + Legende löschen" title="Bild + Legende löschen"/>
                  </span>
                </xsl:if>
              </li>
            </xsl:for-each>
          </ul>
        </div>
          <div class="fieldset flash" id="fsUploadProgress">
            &#160;
          </div>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="xhtml:html/xhtml:body/xhtml:div[@class='3k3_gallery_current_item']">
    <p class="3k3_gallery_key">
      <xsl:choose>
        <xsl:when test="$yanel.toolbar-status = 'on'">
          <div id="buttonPlaceHolder">&#160;</div><br/>
          <form  action="" class="toggled_text" style="display:none;">
           <textarea id="text" name="text" rows="1" cols="80">
             <xsl:value-of select="xhtml:div[@class='3k3_gallery_current_text']"/>
           </textarea>
          <input type="hidden" name="continuing-path" value="{$yanel.path}" /> 
           <input type="hidden" name="lang" value="{$content-language}"/>
           <input type="hidden" name="id" value="{@id}"/>
           <input type="hidden" name="resource.imagegallery.usecase" value="textedit"/>
           <input type="submit" name="Name" value="ändern"/>
           <input type="button" value="cancel" class="texttoggler"/>
         </form>
         <span  class="texttoggler toggled_text">
          <img src="{$yanel.back2realm}{$yanel.reservedPrefix}/resource-types/http://www.litwan.com/yanel/resource/1.0::imageGallery/img/edit-icon.png" class="edit-icon" alt="Legende editieren" title="Legende editieren"/>
          <xsl:value-of select="xhtml:div[@class='3k3_gallery_current_text']"/>
         </span>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="xhtml:div[@class='3k3_gallery_current_text']"/>&#160;
        </xsl:otherwise>
      </xsl:choose>
    </p>
  </xsl:template>
  
</xsl:stylesheet>
