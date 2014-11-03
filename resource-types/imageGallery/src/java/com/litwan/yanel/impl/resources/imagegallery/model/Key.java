package com.litwan.yanel.impl.resources.imagegallery.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Attribute;

@Root
public class Key {
	
	@Attribute
	public String lang;
	@Element (required=false)
	public String text;
	
	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
}
