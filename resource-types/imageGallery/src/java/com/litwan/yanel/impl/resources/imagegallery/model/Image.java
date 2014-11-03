package com.litwan.yanel.impl.resources.imagegallery.model;

import java.util.List;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class Image {

	@Attribute
	private String src;
	
	@Attribute
	private String id;

	@ElementList(inline=true)
	private List<Key> list;

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public List<Key> getKeys() {
		return list;
	}

	public void setKeys(List<Key> keys) {
		this.list = keys;
	}
}
