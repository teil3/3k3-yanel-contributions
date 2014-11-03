if (imageCropW > -1) {
    origWidth = imageCropW;
}
if (imageCropH > -1) {
    origHeight = imageCropH;
}

function oversizeWarn () {
    if (jQuery('#resizewidth').val() <= origWidth  && jQuery('#resizeheight').val() <= origHeight) {
        jQuery('#oversize-warning').css('visibility', 'hidden');
    } else {
        jQuery('#oversize-warning').css('visibility', 'visible');
    }
}      

var nochanges = true;

jQuery(window).load(function(){
    jcropApi = jQuery.Jcrop('#cropimage',{
        onChange: showCoords,
        onSelect: showCoords,
        aspectRatio : 0,
        setSelect:   [ imageCropX, imageCropY, imageCropX + imageCropW, imageCropY + imageCropH ]
    });
            
    jQuery( "#fixaspectratiocrop" ).change(function(e) {
    	jcropApi.setOptions(this.checked?
    			{ aspectRatio: 4/3 }: { aspectRatio: 0 });
    	            jcropApi.focus();
    			});
    
    jQuery("#tabs").tabs();
    
    jQuery("#resizer").resizable({ 
        ghost: true, 
        autoHide: true,
        stop: function(event, ui) { 
            jQuery('#resizewidth').val(Math.round(ui.size.width));
            jQuery('#resizeheight').val(Math.round(ui.size.height));
        	jQuery("#watermarking-holder-resize").css('width', Math.round(ui.size.width));
        	jQuery("#watermarking-holder-resize").css('height', Math.round(ui.size.height));
        	jQuery("#draggable-resize > img").fadeIn();
            oversizeWarn();
        }
    }); 
          
    setResizerImgSize(targetWidth, targetHeight);
    //reset to original size
    jQuery('input#origsize').click(function () {
    	setResizerImgSize(origWidth, origHeight);
    });

    // preserve aspect ratio
    jQuery('input#fixaspectratio').change(function () {
        if(!jQuery(this).hasClass("checked")) {
            //do stuff if the checkbox isn't checked
            jQuery(this).addClass("checked");
            jQuery("#resizer").resizable("destroy");
            jQuery("#resizer").resizable({
                ghost: true, 
                autoHide: true,
                aspectRatio: true,
                stop: function(event, ui) {
                    jQuery('#resizewidth').val(Math.round(ui.size.width));
                    jQuery('#resizeheight').val(Math.round(ui.size.height));
            		jQuery("#watermarking-holder-resize").css('width', Math.round(ui.size.width));
            		jQuery("#watermarking-holder-resize").css('height', Math.round(ui.size.height));   
                    oversizeWarn();
                }
            }); 
            return;
        }

        //do stuff if the checkbox isn't checked
        jQuery(this).removeClass('checked');
        jQuery("#resizer").resizable("destroy");
        jQuery("#resizer").resizable({
            ghost: true,
            autoHide: true,
            stop: function(event, ui) {
                jQuery('#resizewidth').val(Math.round(ui.size.width));
                jQuery('#resizeheight').val(Math.round(ui.size.height));
        		jQuery("#watermarking-holder-resize").css('width', Math.round(ui.size.width));
        		jQuery("#watermarking-holder-resize").css('height', Math.round(ui.size.height));   
                oversizeWarn();
            }
        });
    });
    
    //watermark
    jQuery('input#watermark-path').change(function () {
        path  = jQuery('input#watermark-path').val();
        if (path.length >  0) {
            watermarkImage = new Image(); 
            watermarkImageResize = new Image(); 

            watermarkImage.onload = function(){
                jQuery("#draggable *").remove();
                jQuery("#draggable").append(watermarkImage);
            	jQuery("#draggable > img").css('left', Math.round(jQuery('#dragleft').val()));
            	jQuery("#draggable > img").css('top', Math.round(jQuery('#dragtop').val()));
            	jQuery("#draggable-resize *").remove();
            	jQuery("#draggable-resize").append(watermarkImageResize);
            	jQuery("#draggable-resize > img").css('left', Math.round(jQuery('#dragleft').val()));
            	jQuery("#draggable-resize > img").css('top', Math.round(jQuery('#dragtop').val()));
            	jQuery("#watermarking-holder-resize").css('width', jQuery("#resizer").css('width'));
            	jQuery("#watermarking-holder-resize").css('height', jQuery("#resizer").css('height'));
            	jQuery("#watermarking-holder-resize").css('overflow', 'hidden');
//                jQuery("#draggable-resize > img").hover(function(){
////                    jQuery(this).css('visibility', 'hidden');
//                	jQuery(this).fadeOut();
//                }, function(){
//                	jQuery(this).fadeIn();
////                    jQuery(this).css('visibility', 'visible');
//                });
            	jQuery("#draggable-resize > img").mouseenter(function() {
            		jQuery(this).fadeOut();
            	});
                

            }
//            watermarkImage.onerror = function(){
//                alert("ffferror");
//            }
            watermarkImage.src = back2image+path;
            watermarkImageResize.src = back2image+path;
            
            jQuery( watermarkImage ).draggable({
                stop: function(event, ui) { 
                    jQuery('#dragtop').val(Math.round(ui.position.top));
                    jQuery('#dragleft').val(Math.round(ui.position.left));
                }
            });
        }
        
    });
        
    
        
        
    jQuery('input#watermark-transparency').change(function () {
            $('#draggable img').fadeTo('fast', jQuery('input#watermark-transparency').val());
            $('#draggable-resize img').fadeTo('fast', jQuery('input#watermark-transparency').val());
    });

    jQuery('#dragleft').change(function () {
    	jQuery('#draggable img').css('left',jQuery('#dragleft').val()+'px');
    });
    jQuery('#dragtop').change(function () {
    	jQuery('#draggable img').css('top',jQuery('#dragtop').val()+'px');
    });

    path  = jQuery('input#watermark-path').val();
    if (path.length >  0) {
    	jQuery('input#watermark-path').trigger('change');
    	setTimeout('jQuery(\'input#watermark-transparency\').trigger(\'change\')',1250);
    	setTimeout('jQuery(\'input#watermark-transparency\').trigger(\'change\')',1250);
    }	
    
});



// Jcrop simple event handler, called from onChange and onSelect
// event handlers, as per the Jcrop invocation above
function showCoords(c)
{
    jQuery('#x1').val(c.x);
    jQuery('#y1').val(c.y);
    jQuery('#x2').val(c.x2);
    jQuery('#y2').val(c.y2);
    jQuery('#width').val(c.w);
    jQuery('#height').val(c.h);
    jQuery('#resizewidth').val(c.w);
    jQuery('#resizeheight').val(c.h);
};

jQuery(document).ready(function(){
    jQuery("INPUT.spin").spinbox();
	jQuery("#watermark-transparency").spinbox({
	  min: 0.0,    // Set lower limit or null for no limit.
	  max: 1.0,  // Set upper limit or null for no limit.
	  step: 0.1, // Set increment size.
	});
	
	jQuery('#backgroundcolor').css('backgroundColor', '#' + jQuery('#backgroundcolor').val());
	
	jQuery('#backgroundcolor').ColorPicker({
		onChange: function(hsb, hex, rgb, el) {
			alert(el);
			jQuery(el).val(hex);
			jQuery(el).ColorPickerHide();
		},
		onBeforeShow: function () {
			jQuery(this).ColorPickerSetColor(this.value);
		},
		onChange: function (hsb, hex, rgb) {
			jQuery('#backgroundcolor').css('backgroundColor', '#' + hex);
			jQuery('#backgroundcolor').val(hex);
		}		
	});
//	 jQuery('#backgroundcolor').jPicker({
//		 window:{
//			 expandable:true,title:'background-color',alphaSupport:true
//		 },
//		 images:
//		 {
//			 clientPath: imageUtilHtdocsPath + 'js/jpicker-1.1.5/images/'
//		 }, position:
//		    {
//		      x: 'top', // acceptable values "left", "center", "right",
//		                         // "screenCenter", or relative px value
//		      y: 'left', // acceptable values "top", "bottom", "center", or relative px
//		                // value
//		    },
//
//	 },function(color, context)
//     {
//         var all = color.val('all');
//         alert('Color chosen - hex: ' + (all && '#' + all.hex || 'none') + ' - alpha: ' + (all && all.a + '%' || 'none'));
//         jQuery('#backgroundcolor').val(all.hex + all.a.toString(16));
//         $('#Commit').css({ backgroundColor: all && '#' + all.hex || 'transparent' });
//       });	
});
      
function lookupOpener(back2realm, target_input_field_id) {
    lookup_url = back2realm + "usecases/imageutil-lookup.html?yanel.resource.lookup.target-back2realm=" + back2realm + "&type=image&yanel.toolbar=suppress&yanel.resource.lookup.target-input-field=" + target_input_field_id;
    lookup_window_name = "lookup";
    lookup_width = "420";
    lookup_height = "400";
    window.open(lookup_url,lookup_window_name,'width=' + lookup_width + ',height=' + lookup_height);
}

function setResizerImgSize(width, height) {
	jQuery('#resizer').css('width', width +'px');
	jQuery('#resizer').css('height', height +'px');
	jQuery('div .ui-wrapper').css('width', width +'px');
	jQuery('div .ui-wrapper').css('height', height +'px');
	jQuery('#resizewidth').val(width);
	jQuery('#resizeheight').val(height);
	jQuery("#watermarking-holder-resize").css('width', Math.round(width));
	jQuery("#watermarking-holder-resize").css('height', Math.round(height)); 
	oversizeWarn();
}
