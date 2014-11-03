jQuery(function(){
  //Get our elements for faster access and set overlay width
  var div = jQuery('div.sc_menu'),
    ul = jQuery('ul.sc_menu'),
    ulPadding = 15;
  
  //Get menu width
  var divWidth = div.width();

  //Remove scrollbars 
  div.css({overflow: 'hidden'});
  
  //Find last image container
  var lastLi = ul.find('li:last-child');
  
  //When user move mouse over menu
  div.mousemove(function(e){
    //As images are loaded ul width increases,
    //so we recalculate it each time
    var ulWidth = lastLi[0].offsetLeft + lastLi.outerWidth() + ulPadding; 
    var left = (e.pageX - div.offset().left) * (ulWidth-divWidth) / divWidth;
    div.scrollLeft(left);
  });

  div.click(function(e){
	  //As images are loaded ul width increases,
	  //so we recalculate it each time
	  var ulWidth = lastLi[0].offsetLeft + lastLi.outerWidth() + ulPadding; 
	  var left = (e.pageX - div.offset().left) * (ulWidth-divWidth) / divWidth;
  });
  
  jQuery('.texttoggler').bind('click', function() {
		jQuery('.toggled_text').toggle();
	});  
});    

jQuery(document).keydown(function(e){
	url = window.location.href;
	if (url.indexOf("?") != -1) {
		url = url.substring(0, url.lastIndexOf("?"));
	}
	parent = url.substring(0, url.lastIndexOf("/"));
	counter = parseInt(url.substring(url.lastIndexOf("/") + 1,url.lastIndexOf(".")));
	extension = url.substring(url.lastIndexOf("."), url.length);
    if (e.keyCode == 37) { 
    	if (counter < 2 || isNaN(counter)) {
    		return false
    	}
       counter--;
       window.location.href = parent + "/" + counter + extension;
       return false;
    }
    if (e.keyCode == 39) { 
    	if (isNaN(counter)) {
    		counter = 1;
    	}
    	counter++;
    	window.location.href = parent + "/" + counter + extension;
    	return false;
    }
});
