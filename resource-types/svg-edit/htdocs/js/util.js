// had to add this in a separate js file because of the & within the url which can not be used within jelly
function lookupOpener(back2realm, target_input_field_id) {
    lookup_url = lookup_url = back2realm + "usecases/svg-edit-lookup.html?yanel.resource.lookup.target-back2realm=" + back2realm + "&type=image&yanel.toolbar=suppress&yanel.resource.lookup.target-input-field=" + target_input_field_id;
    lookup_window_name = "lookup";
    lookup_width = "420";
    lookup_height = "400";
    window.open(lookup_url,lookup_window_name,'width=' + lookup_width + ',height=' + lookup_height);
}