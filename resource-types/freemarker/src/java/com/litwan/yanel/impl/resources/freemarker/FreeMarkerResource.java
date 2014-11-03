/*
 * Copyright 2011 litwan
 */

package com.litwan.yanel.impl.resources.freemarker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;
import java.util.HashMap;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.wyona.yanel.core.Resource;
import org.wyona.yanel.core.api.attributes.ViewableV2;
import org.wyona.yanel.core.attributes.viewable.View;
import org.wyona.yanel.core.attributes.viewable.ViewDescriptor;
import org.wyona.yanel.core.source.SourceResolver;
import org.wyona.yanel.impl.resources.usecase.UsecaseException;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.Repository;

import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 *
 */
public class FreeMarkerResource extends Resource implements ViewableV2 {
    
    private static Logger log = Logger.getLogger(FreeMarkerResource.class);
    
    protected static String DEFAULT_VIEW_ID = "default";
    protected static String SOURCE_VIEW_ID = "source";
    

    @Override
    public ViewDescriptor[] getViewDescriptors() {
        return null;
    }

    @Override
    public View getView(String viewID) throws Exception {
        if (viewID == null || viewID.length() == 0) {
            viewID = DEFAULT_VIEW_ID;
        }
        try {
            View view = new View();
            String viewTemplateURI = getPath();
            String fmModelPath = getResourceConfigProperty("fm-model-path");
            Repository repo = getRealm().getRepository();
            if (fmModelPath.startsWith("/")) {
                fmModelPath = "yanelrepo:" + fmModelPath;
            } 
            SourceResolver resolver = new SourceResolver(this);
            Source modelSource = resolver.resolve(fmModelPath, null);
            
            /* Create a data-model */
            HashMap fmModel = new HashMap();
            fmModel.put( "doc", freemarker.ext.dom.NodeModel.parse(SAXSource.sourceToInputSource(modelSource)));  
            
            InputStream fmInputStream = getTemplateInputStream(viewID, viewTemplateURI, fmModel);
            view.setInputStream(fmInputStream);
            view.setMimeType(getMimeType(viewID));
            return view;
        } catch (Exception e) {
            String errorMsg = "Error generating view '" + viewID + ": " + e;
            log.error(errorMsg, e);
            throw new UsecaseException(errorMsg, e);
        }
    }

    @Override
    public boolean exists() throws Exception {
        return getRealm().getRepository().existsNode(getPath());
    }

    @Override
    public long getSize() throws Exception {
        return -1;
    }
    
    private InputStream getTemplateInputStream(String viewID, String viewTemplate, HashMap<String, String> freemarkerModel) throws UsecaseException {
        freemarker.template.Configuration cfg = new freemarker.template.Configuration();
        
        try {

            if (log.isDebugEnabled()) log.debug("viewTemplate: "+viewTemplate);
            Repository repo = this.getRealm().getRepository();
            InputStream templateInputStream;
            if (viewTemplate.startsWith("/")) {
                if (log.isDebugEnabled()) log.debug("Accessing view template directly from the repo (no protocol specified). View Template: " + viewTemplate);
                // for backwards compatibility. when not using a protocol
                Node node = repo.getNode(viewTemplate);
                if (viewID.equals(SOURCE_VIEW_ID)) {
                    return node.getInputStream();
                }
                cfg.setTemplateLoader(new YarepNodeTemplateLoader(node));
                
            } else {
                if (log.isDebugEnabled()) log.debug("Accessing view template through the source-resolver (protocol specified). View Template: " + viewTemplate);
                SourceResolver resolver = new SourceResolver(this);
                Source templateSource = resolver.resolve(viewTemplate, null);
                templateInputStream = ((StreamSource)templateSource).getInputStream();
                if (viewID.equals(SOURCE_VIEW_ID)) {
                    return templateInputStream;
                }
                cfg.setTemplateLoader(new InputStreamTemplateLoader(templateInputStream));
            }        
        
            final Template tpl = cfg.getTemplate("");
            final HashMap<String, String> model = freemarkerModel;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(out);
            tpl.process(model, writer);
              

//            PipedInputStream pi = new PipedInputStream();
//            Writer writer = new OutputStreamWriter(new PipedOutputStream(pi));
//            Thread worker = new Thread(new Runnable() {
//                public void run() {
//                        try {
//                            tpl.process(model, writer);
//                        } catch (TemplateException e) {
//                            log.error(e.getMessage(),e);
//                        } catch (IOException e) {
//                            log.error(e.getMessage(),e);
//                        }
//                }
//            });
//            worker.start();
//            return pi;
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new UsecaseException(e.getMessage(), e);
        } 
    }

    /**
    *
    */
   public String getMimeTypeBySuffix(String suffix) {
       // TODO: use MimeTypeUtil
       if (suffix.equals("html")) {
           return "text/html";
       } else if (suffix.equals("htm")) {
           return "text/html";
       } else if (suffix.equals("xhtml")) {
           return "application/xhtml+xml";
       } else if (suffix.equals("xml")) {
           return "application/xml";
       } else if (suffix.equals("xsd")) {
           return "application/xml";
           // TODO: Clarify ...
           //return "application/xsd+xml";
       } else if (suffix.equals("xsl")) {
           return "application/xml";
           // TODO: Clarify ...
           //return "application/xslt+xml";
       } else if (suffix.equals("css")) {
           return "text/css";
       } else if (suffix.equals("js")) {
           return "application/x-javascript";
       } else if (suffix.equals("txt")) {
           return "text/plain";
       } else if (suffix.equals("svg")) {
           return "image/svg+xml";
       } else {
           log.warn("Could not determine mime-type from suffix '" + suffix + "' (path: " + getPath() + "). Return application/octet-stream!");
           return "application/octet-stream";
       }
   }
   
   /**
    * Get mime type
    */
   public String getMimeType(String viewId) throws Exception {
       // TODO: Also check mime type of data repository node

       String mimeType = getResourceConfigProperty("mime-type");

       if (mimeType != null) return mimeType;

       // TODO: Load config mime.types ...
       String suffix = org.wyona.commons.io.PathUtil.getSuffix(getPath());
       if (suffix != null) {
           log.debug("SUFFIX: " + suffix);
           mimeType = getMimeTypeBySuffix(suffix);
       } else {
           log.warn("mime-type will be set to application/octet-stream, because no suffix for " + getPath());
           mimeType = "application/octet-stream";
       }
       return mimeType;
   }   
}
