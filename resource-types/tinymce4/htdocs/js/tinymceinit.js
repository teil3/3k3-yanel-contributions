tinyMCE.init({
			mode : "textareas",
			theme : "modern",
			plugins : [
					'advlist autolink lists link image charmap print preview hr anchor pagebreak',
					'searchreplace wordcount visualblocks visualchars code fullscreen',
					'insertdatetime media nonbreaking save table contextmenu directionality fullpage',
					'paste textcolor colorpicker textpattern imagetools' ],
			toolbar1 : 'insertfile undo redo | styleselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image',
			toolbar2 : 'print preview media | forecolor backcolor emoticons fullpage',
			image_advtab : true,
			menubar : "edit,insert,view,format,table,tools",
			browser_spellcheck : true,
			convert_urls: false,
			extended_valid_elements : "iframe[src|width|height|name|align],div[*],script[*],xi:include[href]",
			custom_elements : "xi:include",
			entity_encoding : "numeric",
			file_browser_callback : function(field_name, url, type, win) {
				if (window.location.pathname
						.charAt(window.location.pathname.length) != "/") {
					path = window.location.pathname.substring(0,
							window.location.pathname.lastIndexOf("/") + 1)
				}
				var cmsURL = path + BACK2REALM + "usecases/tinymce-lookup.html" // script URL
				if (cmsURL.indexOf("?") < 0) {
					// add the type as the only query parameter
					cmsURL = cmsURL + "?type=" + type;
				} else {
					// add the type as an additional query parameter
					cmsURL = cmsURL + "&type=" + type;
				}
				// INFO: Suppress the toolbar
				cmsURL = cmsURL + "&yanel.target-back2realm=" + BACK2REALM + "&yanel.toolbar=suppress";
				tinymce.activeEditor.windowManager.open({
									title : "My file browser",
									url : cmsURL,
									width : 800,
									height : 600},
								{
									oninsert : function(url) {
										win.document.getElementById(field_name).value = url;
									}
								});
			},
			relative_urls : false
		});
