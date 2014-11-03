/*
 * Copyright 2011 Litwan
 */

package com.litwan.yanel.impl.resources.proxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.w3c.tidy.Tidy;
import org.wyona.yanel.core.source.SourceException;
import org.wyona.yanel.core.source.SourceResolver;
import org.wyona.yanel.impl.resources.BasicXMLResource;
import org.wyona.yanel.impl.resources.usecase.UsecaseException;

/**
 * A simple Resource which extends BasicXMLResource
 */
public class ProxyResource extends BasicXMLResource {
    
	public static final String CONFIG_PROPERTY_SOURCE_PATH = "source-path";
	public static final String CONFIG_PROPERTY_INPUT_ENCODING = "input-encoding";
	public static final String CONFIG_PROPERTY_OUTPUT_ENCODING = "output-encoding";
    private static Logger log = Logger.getLogger(ProxyResource.class);
    

    /**
     * getContent from source-path as xml 
     */
    protected InputStream getContentXML(String viewId) throws Exception {
    	InputStream contentStream = getContentFromUrl(getSourcePath());
    	ByteArrayOutputStream out = (ByteArrayOutputStream)tidy(contentStream);
    	return new ByteArrayInputStream(out.toByteArray());
    }
    
    /**
     * Tidy content
     * @return String wellformed by tidy
     * @param String to be cleaned
     * @throws UsecaseException
     */
    private OutputStream tidy(InputStream content) throws UsecaseException {
    	//TODO: tidy should be configured via an external file (e.g. in htdocs)
    	ByteArrayOutputStream os = new ByteArrayOutputStream();
    	Tidy tidy = new Tidy();
    	tidy.setDropEmptyParas(true);
    	tidy.setDropFontTags(true);
    	tidy.setFixComments(true);
    	tidy.setHideEndTags(false);
    	tidy.setIndentAttributes(true);
    	tidy.setMakeClean(true);
    	tidy.setSmartIndent(true);
    	tidy.setQuiet(true);
    	tidy.setXHTML(true);
    	tidy.setXmlOut(true);
    	tidy.setXmlSpace(true);
    	tidy.setXmlPi(true);
    	tidy.setInputEncoding(getConfiguredInputEncoding());
    	tidy.setOutputEncoding(getConfiguredOutputEncoding());
    	tidy.parse(content, os);
//    	log.error(os.toString());
    	return os;
    }    
    
    private String getConfiguredOutputEncoding() {
		String opEncoding = null;
		try {
			opEncoding = getResourceConfigProperty(CONFIG_PROPERTY_OUTPUT_ENCODING);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		if (opEncoding == null || opEncoding.length() < 1) {
			return "utf-8";
		}
		return opEncoding;
	}
    
    private String getConfiguredInputEncoding() {
    	String inEncoding = null;
    	try {
    		inEncoding = getResourceConfigProperty(CONFIG_PROPERTY_INPUT_ENCODING);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
    	if (inEncoding == null || inEncoding.length() < 1) {
    		return "utf-8";
    	}
    	return inEncoding;
    }
    
    private String getSourcePath() throws UsecaseException {
    	String path = null;
    	try {
    		path = getResourceConfigProperty(CONFIG_PROPERTY_SOURCE_PATH);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
    	if (path == null || path.length() < 1) {
    		throw new UsecaseException("Could not get property with name: " + CONFIG_PROPERTY_SOURCE_PATH);
    	}
    	return path;
    }
    
    public InputStream getContentFromUrl(String url) throws UsecaseException{
    	try {
    		SourceResolver resolver = new SourceResolver(this);
    		StreamSource source = (StreamSource)resolver.resolve(url, null);
    		return source.getInputStream();
		} catch (SourceException e) {
			log.error(e.getMessage(), e);
			throw new UsecaseException(e.getMessage());
		}
    }
}
