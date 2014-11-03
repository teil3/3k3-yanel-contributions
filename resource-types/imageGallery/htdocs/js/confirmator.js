jQuery(document).ready(function() {
		jQuery('.confirm_link').each(function(index) {
			link = jQuery(this);
			href = link.attr('href');
			hint = link.attr('name');
			link.attr('onclick', 'confirmDelete(\''+ href +'\', \''+ hint +'\')');
			link.attr('href', '');
		});
});

function confirmDelete(link, hint) {
	if (confirm(hint)) {
		window.location = link;
		return true;
	} else {
		return false;
	}
}
