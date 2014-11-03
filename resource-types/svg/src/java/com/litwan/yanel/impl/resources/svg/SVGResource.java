/*
 * Copyright 2010 Litwan
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0 
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.litwan.yanel.impl.resources.svg;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.log4j.Logger;
import org.wyona.yanel.core.api.attributes.CreatableV2;
import org.wyona.yanel.core.api.attributes.ModifiableV2;
import org.wyona.yanel.core.api.attributes.VersionableV2;
import org.wyona.yanel.core.api.attributes.WorkflowableV1;
import org.wyona.yanel.core.attributes.versionable.RevisionInformation;
import org.wyona.yanel.core.attributes.viewable.View;
import org.wyona.yanel.core.workflow.Workflow;
import org.wyona.yanel.core.workflow.WorkflowException;
import org.wyona.yanel.core.workflow.WorkflowHelper;
import org.wyona.yanel.impl.resources.usecase.UsecaseException;
import org.wyona.yanel.impl.resources.usecase.UsecaseResource;
import org.wyona.yanel.impl.resources.xml.ConfigurableViewDescriptor;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.core.Revision;
import org.wyona.yarep.util.YarepUtil;


/**
 * A simple usecase which is based on ExecutableUsecaseResource
 */
public class SVGResource extends UsecaseResource implements ModifiableV2, CreatableV2, VersionableV2, WorkflowableV1 {

    private static Logger log = Logger.getLogger(SVGResource.class);
    
    private static final String VIEW_SVG = "svg";
    
    private static final String VIEW_PNG = "png";
    
    private static final String VIEW_JPEG= "jpg";
    
    private static final String VIEW_HTML = "html";

    private static final String RESOURCE_PROP_JPG_QUALITY = "jpg-quality";

    private static final String RESOURCE_PROP_IMAGE_WIDTH = "image-width";

    private static final String RESOURCE_PROP_DISABLE_CACHE = "disable-cache";

    private static final String RESOURCE_PROP_CACHE_ROOT_PATH = "cache-root-path";

    private static final String DEFAULT_CACHE_ROOT_PATH = "/cached-images";

    private static final String EXTENSION_SVG = "svg";
    
    @Override
    public View getView(String viewID) throws Exception {
        getRepoNode();//simple hack to allow throwing a noSuchNodeException.
        init();
        return processUsecase(viewID);
    }
    
    protected View processUsecase(String viewID) throws UsecaseException {
        if (viewID == null) {
            String path = getPath();
            String extension = getPathExtension(path);
            viewID = getViewIdByExtension(extension);  
        }
        return generateView(viewID);
    }
    
    private String getViewIdByExtension(String extension) {
        return getViewExtensions().get(extension);
    }
    
    private Map<String, String> getViewExtensions() {
        Map<String, String> formatExtensions = new HashMap<String, String>();
        formatExtensions.put("jpg", VIEW_JPEG);
        formatExtensions.put("png", VIEW_PNG);
        formatExtensions.put("html", VIEW_HTML);
        formatExtensions.put("svg", VIEW_SVG);
        return formatExtensions;
    }

    private String getPathExtension(String path){
        String[] pathParts = path.split("\\.");
        return pathParts[pathParts.length - 1];
    }        

    /**
     * Generate bitmaps (jpg or png) from svg
     */ 
    protected View renderCustomView(ConfigurableViewDescriptor viewDescriptor) throws UsecaseException {
        String viewId = viewDescriptor.getId();
        Repository repo;
        View view = new View();
        
        try {
            view.setMimeType(getMimeType(viewId));
        } catch (Exception e) {
            log.warn(e, e);
        }
        
        try {
            repo = getRealm().getRepository();
        } catch (Exception e) {
            throw new UsecaseException(e.getMessage(),e);
        }
        
        String cachePath = getCachePath();
        try {
            Node cacheNode = repo.getNode(cachePath);
            Node contentNode = repo.getNode(getContentPath());
            long contentNodeLastModified = contentNode.getLastModified();
            long cacheNodeLastModified = cacheNode.getLastModified();
            if (isCacheOn() && (contentNodeLastModified < cacheNodeLastModified)) {
                if(log.isDebugEnabled()) log.debug("cache is newer than content, serving cache.");
                view.setInputStream(cacheNode.getInputStream());
                return view;
            }
        } catch (Exception e) {
            if(log.isDebugEnabled()) log.debug(e, e);
        }
        if (viewId.equals(VIEW_JPEG)) {
            try {
                if (isCacheOn()) {
                    org.wyona.yarep.core.Node cacheNode = null;
                    if(!getRealm().getRepository().existsNode(cachePath)){
                        cacheNode = YarepUtil.addNodes(getRealm().getRepository(), cachePath, org.wyona.yarep.core.NodeType.RESOURCE);
                    } else {
                        cacheNode = getRealm().getRepository().getNode(cachePath);
                    }
                    writeJpegOs(viewId, cacheNode.getOutputStream());
                    view.setInputStream(cacheNode.getInputStream());
                } else {
                    view.setResponse(false);
                    OutputStream os = getEnvironment().getResponse().getOutputStream();
                    writeJpegOs(viewId, os);
                }
            } catch (Exception e) {
                log.warn("Could not write to cache.", e);
            }
        } else if (viewId.equals(VIEW_PNG)) {
            try {
                if (isCacheOn()) {
                    org.wyona.yarep.core.Node cacheNode = null;
                    if(!getRealm().getRepository().existsNode(cachePath)){
                        cacheNode = YarepUtil.addNodes(getRealm().getRepository(), cachePath, org.wyona.yarep.core.NodeType.RESOURCE);
                    } else {
                        cacheNode = getRealm().getRepository().getNode(cachePath);
                    }
                    writePngOs(viewId, cacheNode.getOutputStream());
                    view.setInputStream(cacheNode.getInputStream());
                } else {
                    view.setResponse(false);
                    OutputStream os = getEnvironment().getResponse().getOutputStream();
                    writePngOs(viewId, os);
                }
            } catch (Exception e) {
                log.warn("Could not write to cache.", e);
            }
        } else {
            try {
                view = getXMLView(viewId, getContentXML(viewId));
            } catch (Exception e) {
                throw new UsecaseException(e.getMessage(),e);
            }
        }
        return view;
    }

    private String getCachePathParent() {
        String cacheRootPath = getCacheRootPath();
        return cacheRootPath + getContentPath() + "/";
    }
    
    private String getCachePath() {
        String path = getPath();
        String name = path.substring(path.lastIndexOf("/") + 1);
        String cachePath = getCachePathParent() + name;

        String queryString = getEnvironment().getRequest().getQueryString();
        if(queryString != null) {
            cachePath += queryString;
        }
        
        //TODO: this should actually only be considered for a jpeg
        cachePath += getJpegQualityProperty();
        
        String widthProperty = getWidthProperty();
        if (widthProperty != null) {
            cachePath += widthProperty;
        }
        
        return cachePath;
    }

    private String getCacheRootPath() {
        String cacheRootPath = DEFAULT_CACHE_ROOT_PATH;
        try {
            String cacheRootPathVal = getResourceConfigProperty(RESOURCE_PROP_CACHE_ROOT_PATH);
            if (cacheRootPathVal != null){
                cacheRootPath = cacheRootPathVal; 
            }
        } catch (Exception e) {
            if(log.isDebugEnabled())  log.debug("No cache root path configured within resource configuration. Use default '" + cacheRootPath + "'!");
        }
        return cacheRootPath;
    }

    private void writePngOs(String viewId, OutputStream ostream) throws UsecaseException {
        // Create a PNG transcoder
        PNGTranscoder t = new PNGTranscoder();
        
        String widthProperty = getWidthProperty();
        if(widthProperty != null){
            t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, Float.parseFloat(widthProperty));
        }
        
        // Create the transcoder input.
        try {
            TranscoderInput input = new TranscoderInput(getContentXML(viewId));
            //there seems to be no way to use yanels URIResolver
            //this way relative URLs get resolved via the requested server URL
            input.setURI(getRequestedUrl());
            TranscoderOutput output = new TranscoderOutput(ostream);
            
            // transcode the image.
            t.transcode(input, output);
            
            // Flush and close the stream.
            ostream.flush();
            ostream.close();
        } catch (Exception e) {
            throw new UsecaseException(e.getMessage(),e);
        }
    }

    private void writeJpegOs(String viewId, OutputStream ostream) throws UsecaseException {
        // Create a JPEG transcoder
        JPEGTranscoder t = new JPEGTranscoder();

        // Set the transcoding hints.

        t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, getJpegQualityProperty());
        String widthProperty = getWidthProperty();
        if (widthProperty != null) {
            t.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, Float.parseFloat(widthProperty));
        }

        // Create the transcoder input.
        try {
            TranscoderInput input = new TranscoderInput(getContentXML(viewId));
            //there seems to be no way to use yanels URIResolver
            //this way relative URLs get resolved via the requested server URL
            input.setURI(getRequestedUrl());
            TranscoderOutput output = new TranscoderOutput(ostream);

            // transcode the image.
            t.transcode(input, output);

            // Flush and close the stream.
            ostream.flush();
            ostream.close();
        } catch (Exception e) {
            throw new UsecaseException(e.getMessage(),e);
        }
    }

    private String getWidthProperty() {
        try {
            String width = getResourceConfigProperty(RESOURCE_PROP_IMAGE_WIDTH);
            if (width != null) {
                return width;
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        return null;
    }

    private float getJpegQualityProperty() {
        //use jpg quality 1 (no lossy) by default, otherwise it looks ugly. if someone likes it ugly he can configure a higher RESOURCE_PROP_JPG_QUALITY
        // http://xmlgraphics.apache.org/batik/javadoc/org/apache/batik/transcoder/image/JPEGTranscoder.html#KEY_QUALITY tells 1 (no lossy) is default, but it doesn't seem so
        float jpgQuality = 1;
        try {
            String jpgQualityPropertyValue = getResourceConfigProperty(RESOURCE_PROP_JPG_QUALITY);
            if (jpgQualityPropertyValue != null && !jpgQualityPropertyValue.equals("")) {
                jpgQuality = Float.parseFloat(jpgQualityPropertyValue);
            } else {
                log.warn("No jpeg quality specified, hence will use 1 (no lossy) as default");
            }
            if (!(jpgQuality > 0 && jpgQuality <= 1)) {
                jpgQuality = 1;
                log.warn("jpeg quality must be > 0 and <= 1. falling back to default 1 (no lossy)");
            }
        } catch (Exception e) {
            log.error(e, e); 
        }
        return jpgQuality;
    }
    
    /**
     * Gets the XML content which will be fed into the processing pipeline.
     * @return xml stream
     * @throws Exception
     */
    protected InputStream getContentXML(String viewId) throws Exception {
        return getRepoNode().getInputStream();
    }
    
    public String getContentPath() {
        String path = getPath();
        path = path.replaceAll(".png$", "");
        path = path.replaceAll(".jpg$", "");
        path = path.replaceAll(".html$", "");
        path = path.replaceAll(".svg$", "");
        return path + "." +  EXTENSION_SVG;
    }    

    public boolean isCacheOn(){
        try {
            if (Boolean.parseBoolean(getResourceConfigProperty(RESOURCE_PROP_DISABLE_CACHE))){
                return false;
            }
        } catch (Exception e) {
            if(log.isDebugEnabled()) log.debug("Could not read resource config property" + RESOURCE_PROP_DISABLE_CACHE, e);
        }
        return true;
    }
    
    @Override
    public boolean delete() throws Exception {
        getRepoNode().delete();
        return true;
    }

    @Override
   public InputStream getInputStream() throws Exception {
       return getRepoNode().getInputStream();
   }

    @Override
   public long getLastModified() throws Exception {
       long lastModified;
       Node node = getRepoNode();
       if (node.isResource()) {
           lastModified = node.getLastModified();
       } else {
           lastModified = 0;
       }

       return lastModified;
    }

    @Override
    public OutputStream getOutputStream() throws Exception {
        return getRepoNode().getOutputStream();
    }

    @Override
    public Reader getReader() throws Exception {
        return new InputStreamReader(getInputStream(), "UTF-8");
    }

    @Override
    public Writer getWriter() throws Exception {
        log.warn("Not implemented yet!");
        return null;
    }

    @Override
    public void write(InputStream in) throws Exception {
        log.warn("Not implemented yet!");
    }    
    
    /**
    *
    */
   public void create(HttpServletRequest request) {
       try {
           // TODO: SVG template should not be hardcoded!
           Repository repo = getRealm().getRepository();

//           String title = request.getParameter("rp.title");
//           if (title == null || title.length() == 0) {
//               log.warn("No title has been specified!");
//               title = "No title has been specified!";
//           }

           Node newNode = org.wyona.yanel.core.util.YarepUtil.addNodes(repo, getPath().toString(), org.wyona.yarep.core.NodeType.RESOURCE);
           Writer writer = new java.io.OutputStreamWriter(newNode.getOutputStream());
//           writer.write("<?xml version=\"1.0\"?>");
//           writer.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
//           writer.write("<head>");
//           writer.write("  <title>" + title + "</title>");
//           writer.write("  <link rel=\"neutron-introspection\" type=\"application/neutron+xml\" href=\"?yanel.resource.usecase=introspection\"/>");
//           writer.write("</head>");
//           writer.write("<body>");
//           writer.write("  <h1>" + title + "</h1>");
//           writer.write("  <p>Edit this text with <a href=\"http://www.yulup.org\">Yulup</a>!</p>");
//           writer.write("</body>");
//           writer.write("</html>");
           writer.write("<svg xmlns=\"http://www.w3.org/2000/svg\" id=\"svgroot\" height=\"480\" width=\"640\">");
           writer.write("</svg>");
           writer.close();
       } catch (Exception e) {
           log.error(e.getMessage(), e);
       }
   }

   /**
    *
    */
   public java.util.HashMap createRTIProperties(HttpServletRequest request) {
//       java.util.HashMap map = new java.util.HashMap();
//       map.put("xslt", request.getParameter("rp.xslt"));
//       map.put("mime-type", request.getParameter("rp.mime-type"));
//       map.put("source-view-mime-type", request.getParameter("rp.source-view-mime-type"));
//       map.put("workflow-schema", request.getParameter("rp.workflow-schema"));
//       map.put("yanel-path", request.getParameter("rp.yanel-path"));
//
//       // TODO: get all parameters, e.g. source-view-mime-type (Security!)
//       return map;
       return null;
   }

   public String getCreateName(String suggestedName) {
       return suggestedName;
   }

   /**
    *
    */
   public String getPropertyType(String name) {
       log.warn("Not implemented yet!");
       return null;
   }

   /**
    *
    */
   public Object getProperty(String name) {
       log.warn("Not implemented yet!");
       return null;
   }

   /**
    *
    */
   public String[] getPropertyNames() {
       String[] propertyNames = new String[1];
       propertyNames[0] = "title";
       return propertyNames;
   }

   /**
    *
    */
   public void setProperty(String name, Object value) {
       log.warn("Not implemented yet!");
   }
   
   // VersionableV2

   @Override
   public View getView(String viewId, String revisionName) throws Exception {
       String mimeType = getMimeType(VIEW_SVG);
       View view = new View();
       view.setMimeType(mimeType);
       view.setInputStream(getRepoNode().getRevision(revisionName).getInputStream());
       return view;
   }

   /**
    * @see org.wyona.yanel.core.api.attributes.VersionableV2#getRevisions()
    */
   public RevisionInformation[] getRevisions() throws Exception {
       Revision[] revisions = getRepoNode().getRevisions();

       // TODO: Use utility method/class
       RevisionInformation[] revisionInfos = new RevisionInformation[revisions.length];
       for (int i = 0; i < revisions.length; i++) {
           revisionInfos[i] = new RevisionInformation(revisions[i]);
       }
       return revisionInfos;
   }

   /**
    * @see org.wyona.yanel.core.api.attributes.VersionableV2#checkin()
    */
   public void checkin(String comment) throws Exception {
       Node node = getRepoNode();
       Revision revision = node.checkin(comment);
       // set initial workflow state and date:
       Workflow workflow = WorkflowHelper.getWorkflow(this);
       if (workflow != null) {
           setWorkflowState(workflow.getInitialState(), revision.getRevisionName());
       }
       /*
       if (node.isCheckedOut()) {
           String checkoutUserID = node.getCheckoutUserID();
           if (checkoutUserID.equals(userID)) {
               node.checkin();
           } else {
               throw new Exception("Resource is checked out by another user: " + checkoutUserID);
           }
       } else {
           throw new Exception("Resource is not checked out.");
       }
       */
   }

   public void checkout(String userID) throws Exception {
       Node node = getRepoNode();
       node.checkout(userID);
       /*
       if (node.isCheckedOut()) {
           String checkoutUserID = node.getCheckoutUserID();
           if (checkoutUserID.equals(userID)) {
               log.warn("Resource " + getPath() + " is already checked out by this user: " + checkoutUserID);
           } else {
               throw new Exception("Resource is already checked out by another user: " + checkoutUserID);
           }
       } else {
           node.checkout(userID);
       }
       */
   }

   public void cancelCheckout() throws Exception {
       Node node = getRepoNode();
       node.cancelCheckout();
   }

   /**
    * Roll back to previous revision
    */
   public void restore(String revisionName) throws Exception {
       getRepoNode().restore(revisionName);
       // rebuild cache with the rolled back version
       //TODO: but it would be nicer to add the revision number to the cachePath
       String cachePath = getCachePathParent();
       if(getRealm().getRepository().existsNode(cachePath)){
           getRealm().getRepository().getNode(cachePath).delete();
       }
   }

   public Date getCheckoutDate() throws Exception {
       log.warn("Get checkout date not implemented!");
       //Node node = getRepoNode();
       //return node.getCheckoutDate();
       return null;
   }

   public String getCheckoutUserID() throws Exception {
       Node node = getRepoNode();
       return node.getCheckoutUserID();
   }

   public boolean isCheckedOut() throws Exception {
       Node node = getRepoNode();
       return node.isCheckedOut();
   }


   /************************************************
    * Workflow                                     *
    ************************************************/

   public void doTransition(String transitionID, String revision) throws WorkflowException {
       WorkflowHelper.doTransition(this, transitionID, revision);
   }

   public View getLiveView(String viewid) throws Exception {
       return WorkflowHelper.getLiveView(this, viewid);
   }

   public boolean isLive() throws WorkflowException {
       return WorkflowHelper.isLive(this);
   }

   public String getWorkflowVariable(String name) throws WorkflowException {
       try {
       return WorkflowHelper.getWorkflowVariable(getRepoNode(), name);
       } catch (Exception e) {
           log.error(e, e);
           throw new WorkflowException(e.getMessage(), e);
       }
   }

   public void setWorkflowVariable(String name, String value) throws WorkflowException {
       try {
           WorkflowHelper.setWorkflowVariable(getRepoNode(), name, value);
       } catch (Exception e) {
           log.error(e, e);
           throw new WorkflowException(e.getMessage(), e);
       }
   }

   public void removeWorkflowVariable(String name) throws WorkflowException {
       try {
           WorkflowHelper.removeWorkflowVariable(getRepoNode(), name);
       } catch (Exception e) {
           log.error(e, e);
           throw new WorkflowException(e.getMessage(), e);
       }
   }

   public String getWorkflowState(String revision) throws WorkflowException {
       try {
           return WorkflowHelper.getWorkflowState(getRepoNode(), revision);
       } catch (Exception e) {
           log.error(e, e);
           throw new WorkflowException(e.getMessage(), e);
       }
   }

   public void setWorkflowState(String state, String revision) throws WorkflowException {
       try {
           WorkflowHelper.setWorkflowState(getRepoNode(), state, revision);
       } catch (Exception e) {
           log.error(e, e);
           throw new WorkflowException(e.getMessage(), e);
       }
   }


   public Date getWorkflowDate(String revision) throws WorkflowException {
       try {
           return WorkflowHelper.getWorkflowDate(getRepoNode(), revision);
       } catch (Exception e) {
           log.error(e, e);
           throw new WorkflowException(e.getMessage(), e);
       }
   }

   public String getWorkflowIntrospection() throws WorkflowException {
       return WorkflowHelper.getWorkflowIntrospection(this);
   }
    
   private Node getRepoNode() throws Exception {
       return getRealm().getRepository().getNode(getContentPath());
   }

   private String getRequestedUrl() {
       HttpServletRequest req = getEnvironment().getRequest();
       return req.getRequestURL().toString(); 
   } 
}
