package com.litwan.yanel.impl.resources.imagegallery.model;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

@Root
@Namespace(reference="http://www.3k3.org/gallery/1.0", prefix="")
public class Gallery {
    
	@ElementList(inline=true)
	private List<Image> list;

	public List<Image> getImages() {
		return list;
	}

	public void setImages(List<Image> list) {
		this.list =  list;
	}
}
