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

package com.litwan.yanel.impl.resources.imageresizer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.wyona.yanel.core.Resource;
import org.wyona.yanel.core.api.attributes.ViewableV2;
import org.wyona.yanel.core.attributes.viewable.View;
import org.wyona.yanel.core.attributes.viewable.ViewDescriptor;
import org.wyona.yanel.core.source.SourceResolver;
import org.wyona.yanel.core.util.WildcardReplacerHelper;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.util.YarepUtil;

/**
 * @author simon litwan
 *
 */
public class ImageResizerResource extends Resource implements ViewableV2  {

    private static final String CONFIG_PROPERTY_YANEL_PATH = "yanel-path";

    private static final String CONFIG_PROPERTY_YANEL_PATH_MATCHER = "yanel-path-matcher";

    private static final String CONFIG_PROPERTY_YANEL_PATH_TEMPLATE = "yanel-path-template";

    private static final String CONFIG_PROPERTY_PRESERVE_ALPHA = "preserve-alpha";

    private static final String CONFIG_PROPERTY_TARGET_WIDTH = "target-width";
    
    private static final String CONFIG_PROPERTY_TARGET_HEIGHT = "target-height";
    
    private static final String CONFIG_PROPERTY_CROP_X = "crop-x";
    
    private static final String CONFIG_PROPERTY_CROP_Y = "crop-y";
    
    private static final String CONFIG_PROPERTY_CROP_W = "crop-w";
    
    private static final String CONFIG_PROPERTY_CROP_H = "crop-h";

    private static final String CONFIG_PROPERTY_BACKGROUND_COLOR = "background-color";

    private static final String CONFIG_PROPERTY_TARGET_PERCENTAGE = "target-percentage";
    
    private static final String CONFIG_PROPERTY_ALLOW_REQUEST_PARAMETERS = "allow-request-parameters";

    private static final String CONFIG_PROPERTY_PROHIBIT_UP_SCALE = "prohibit-up-scale";

    private static final String PARAMETER_NAME_TARGET_WIDTH = "yanel.resource.imageresizer.width";

    private static final String PARAMETER_NAME_TARGET_HEIGHT = "yanel.resource.imageresizer.height";

    private static final String PARAMETER_NAME_TARGET_PERCENTAGE = "yanel.resource.imageresizer.percentage";
    
    private static final String CONFIG_PROPERTY_DISABLE_CACHE = "disable-cache";

    private static final String CONFIG_PROPERTY_CACHE_ROOT_PATH = "cache-root-path";

    private static final String DEFAULT_CACHE_ROOT_PATH = "/cached-images";    
    
    private static Logger log = Logger.getLogger(ImageResizerResource.class);
    
    /* (non-Javadoc)
     * @see org.wyona.yanel.core.api.attributes.ViewableV2#getView(java.lang.String)
     */
    public View getView(String viewId) throws Exception {
        View defaultView = new View(); 
        String yanelPath = getDataPath();
        if (!exists() && !(yanelPath.startsWith("yanelrepo:") || yanelPath.startsWith("yanelresource:"))) {
            log.warn("No such image file '" + yanelPath + "' in order to resize!");
            throw new org.wyona.yanel.core.ResourceNotFoundException("No such image file '" + yanelPath + "' in order to resize!");
        }
        Repository repo;
        repo = getRealm().getRepository();
        String cachePath = null;
        String pathExtension = getPathExtension(getPath());
        String format = getInformalFormatNameByExtension(pathExtension);
        try {
            format = getInformalFormatNameByExtension(getResourceConfigProperty(CONFIG_PROPERTY_YANEL_PATH_TEMPLATE));
        } catch (Exception e) {
            
        }
        defaultView.setMimeType(getMimeTypeByFormat(format));
        try {
            cachePath = getCachePath();
            Node cacheNode = repo.getNode(cachePath);
            Node contentNode = repo.getNode(yanelPath);
            long contentNodeLastModified = contentNode.getLastModified();
            long cacheNodeLastModified = cacheNode.getLastModified();
            if (isCacheOn() && (contentNodeLastModified < cacheNodeLastModified)) {
                if(log.isDebugEnabled()) log.debug("cache is newer than content, serving cache.");
                defaultView.setInputStream(cacheNode.getInputStream());
                return defaultView;
            }
        } catch (Exception e) {
            if(log.isDebugEnabled()) log.debug(e, e);
        }
        
//        Repository repo = getRealm().getRepository();
        InputStream imgSource = null;
        if (yanelPath.startsWith("yanelrepo:") || yanelPath.startsWith("yanelresource:")) {
            SourceResolver resolver = new SourceResolver(this);
            Source source = resolver.resolve(yanelPath, null);
            imgSource = ((StreamSource) source).getInputStream();
        } else {
            imgSource = repo.getNode(yanelPath).getInputStream();
        }

        BufferedImage imgBuffered = ImageIO.read(imgSource);
        BufferedImage imgResizedBuffered = createResizedCopy(imgBuffered);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytes = byteArrayOutputStream.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try {
            if (isCacheOn() && cachePath != null) {
                org.wyona.yarep.core.Node cacheNode = null;
                if(!getRealm().getRepository().existsNode(cachePath)){
                    cacheNode = YarepUtil.addNodes(getRealm().getRepository(), cachePath, org.wyona.yarep.core.NodeType.RESOURCE);
                } else {
                    cacheNode = getRealm().getRepository().getNode(cachePath);
                }
                
                ImageIO.write(imgResizedBuffered, format, cacheNode.getOutputStream());
                defaultView.setInputStream(cacheNode.getInputStream());
            } else {
                defaultView.setResponse(false);
                OutputStream os = getEnvironment().getResponse().getOutputStream();
                ImageIO.write(imgResizedBuffered, format, os);
//                writeJpegOs(viewId, os);
//                defaultView.setInputStream(bais);
            }
        } catch (Exception e) {
            log.warn("Could not write to cache.", e);
        }
        
        return defaultView;
    }
   
    /* (non-Javadoc)
     * @see org.wyona.yanel.core.api.attributes.ViewableV2#exists()
     */
    public boolean exists() throws Exception {
        String yanelPath = getDataPath();
        Repository repo = getRealm().getRepository();
        if (yanelPath.startsWith("yanelrepo:") || yanelPath.startsWith("yanelresource:")) {
            log.warn("Not implemented yet!");
            return true;
        } 
        return repo.existsNode(yanelPath);
    }   

    /**
     * @param originalImage
     * @return
     * @throws Exception
     */
    BufferedImage createResizedCopy(BufferedImage originalImage) throws Exception {
        int origHeight = 0;
        int origWidth = 0;
        int targetHeight = 0;
        int targetWidth = 0;
        int cropX = 0;
        int cropY = 0;
        origHeight = originalImage.getHeight();
        origWidth = originalImage.getWidth();
        int cropH = origHeight;
        int cropW = origWidth;
        Color color = new Color(0xFFFFFF);
        
        
        boolean preserveAlpha = originalImage.getColorModel().hasAlpha();
        String propertyPreserveAlpha = getResourceConfigProperty(CONFIG_PROPERTY_PRESERVE_ALPHA);
        if (propertyPreserveAlpha != null) {
            preserveAlpha = Boolean.parseBoolean(propertyPreserveAlpha);
        }
        
        String propertyTargetPercentage = getResourceConfigProperty(CONFIG_PROPERTY_TARGET_PERCENTAGE);
        String propertyTargetHeight= getResourceConfigProperty(CONFIG_PROPERTY_TARGET_HEIGHT);
        String propertyTargetWidth = getResourceConfigProperty(CONFIG_PROPERTY_TARGET_WIDTH);

        
        if(Boolean.parseBoolean(getResourceConfigProperty(CONFIG_PROPERTY_ALLOW_REQUEST_PARAMETERS))) {
            String parameterPercentage = getParameterAsString(PARAMETER_NAME_TARGET_PERCENTAGE);
            String parameterHeight = getParameterAsString(PARAMETER_NAME_TARGET_HEIGHT);
            String parameterWidth = getParameterAsString(PARAMETER_NAME_TARGET_WIDTH);
            if (parameterPercentage != null) {
                propertyTargetPercentage = parameterPercentage;
            }
            if (parameterHeight != null) {
                propertyTargetHeight= parameterHeight;
                if(parameterWidth == null){
                    //reset propertyTargetWidth because request parameter are stronger
                    propertyTargetWidth = null;
                }
            }
            if (parameterWidth != null) {
                propertyTargetWidth = parameterWidth;
                if(parameterHeight == null){
                    //reset propertyTargetHeight because request parameter are stronger
                    propertyTargetHeight = null;
                }
            }
        }
        
        if(propertyTargetPercentage != null && !propertyTargetPercentage.equals("")){
            float propertyTargetPercentageInt = Float.parseFloat(propertyTargetPercentage);
            targetHeight = Math.round(((float)origHeight / 100) * propertyTargetPercentageInt);
            targetWidth = Math.round(((float)origWidth / 100) * propertyTargetPercentageInt);
        } else if (propertyTargetHeight != null && !propertyTargetHeight.equals("") && propertyTargetWidth != null && !propertyTargetWidth.equals("")) {
            targetHeight = Integer.parseInt(propertyTargetHeight);
            targetWidth = Integer.parseInt(propertyTargetWidth);
        } else if ((propertyTargetHeight == null || propertyTargetWidth == null ) && !(propertyTargetHeight == null && propertyTargetWidth == null)) {
            if (propertyTargetHeight == null) {
                targetWidth = Integer.parseInt(propertyTargetWidth);
                float factor = (float)origWidth / (float)targetWidth;
                targetHeight = Math.round((float)origHeight/factor);
            } else if (propertyTargetWidth == null) {
                targetHeight = Integer.parseInt(propertyTargetHeight);
                float factor = (float)origHeight / (float)targetHeight;
                targetWidth = Math.round((float)origWidth/factor);
            }
        } else {
            throw new Exception("Image resizer is not properly configured.");
        }
        //croping
        String propertyCropX = getResourceConfigProperty(CONFIG_PROPERTY_CROP_X);
        String propertyCropY = getResourceConfigProperty(CONFIG_PROPERTY_CROP_Y);
        String propertyCropW = getResourceConfigProperty(CONFIG_PROPERTY_CROP_W);
        String propertyCropH = getResourceConfigProperty(CONFIG_PROPERTY_CROP_H);
        if (propertyCropX != null) {
            try {
                cropX = Integer.parseInt(propertyCropX);
            } catch (Exception e) {
                log.warn("resource-property '" + CONFIG_PROPERTY_CROP_X + "' seems not to be an int. ", e);
            }
        }
        if (propertyCropY != null) {
            try {
                cropY = Integer.parseInt(propertyCropY);
            } catch (Exception e) {
                log.warn("resource-property '" + CONFIG_PROPERTY_CROP_Y + "' seems not to be an int. ", e);
            }
        }
        if (propertyCropW != null) {
            try {
                cropW = Integer.parseInt(propertyCropW);
            } catch (Exception e) {
                log.warn("resource-property '" + CONFIG_PROPERTY_CROP_W + "' seems not to be an int. ", e);
            }
        }
        if (propertyCropH != null) {
            try {
                cropH = Integer.parseInt(propertyCropH);
            } catch (Exception e) {
                log.warn("resource-property '" + CONFIG_PROPERTY_CROP_H + "' seems not to be an int. ", e);
            }
        }
        // prevent outside of Raster Exceptions
        if (cropX + cropW > origWidth) {
            cropW = origWidth - cropX;
        }
        // prevent outside of Raster Exceptions
        if (cropY + cropH > origHeight) {
            cropH = origHeight - cropY;
        }
        //backgroundColor
        String propertyBackgroundColor = getResourceConfigProperty(CONFIG_PROPERTY_BACKGROUND_COLOR);
        if (propertyBackgroundColor != null) {
            color = Color.decode(propertyBackgroundColor);
        }
        if (Boolean.parseBoolean(getResourceConfigProperty(CONFIG_PROPERTY_PROHIBIT_UP_SCALE)) && (targetWidth > origWidth || targetHeight > origHeight)) {
            return originalImage;
        } else {
            BufferedImage clipedImage = originalImage.getSubimage(cropX, cropY, cropW, cropH);
            int imageType = preserveAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            BufferedImage scaledBI = new BufferedImage(targetWidth, targetHeight, imageType);
            Graphics2D g = scaledBI.createGraphics();
            if (preserveAlpha) {
                g.setComposite(AlphaComposite.Src);
            }
            g.drawImage(clipedImage, 0, 0, targetWidth, targetHeight, color, null); 
            g.dispose();
            return scaledBI;
        }
    }
    
    /**
     *  convenient method to allow easy overriding of how the dataPath is generated
     *  @return String datapath
     */
    public String getDataPath() throws Exception {
        String yanelPath = null;  
        try {
            yanelPath = getResourceConfigProperty(CONFIG_PROPERTY_YANEL_PATH);
        } catch (Exception e) {
            log.debug("Could not get resource-type proeprty yanel-path.", e);
        }
        if(yanelPath != null && yanelPath.length() > 0) {
            return yanelPath;
        }
        WildcardReplacerHelper dataPath = new WildcardReplacerHelper(getResourceConfigProperty(CONFIG_PROPERTY_YANEL_PATH_TEMPLATE), getResourceConfigProperty(CONFIG_PROPERTY_YANEL_PATH_MATCHER));
        return dataPath.getReplacedString(getPath());
    }

    /* (non-Javadoc)
     * @see org.wyona.yanel.core.api.attributes.ViewableV2#getSize()
     */
    public long getSize() throws Exception {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.wyona.yanel.core.api.attributes.ViewableV2#getViewDescriptors()
     */
    public ViewDescriptor[] getViewDescriptors() {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * @param String extension
     * @return String formatExtesion
     * @throws Exception
     */
    private String getInformalFormatNameByExtension(String extension) throws Exception {
        Map<String, String> formatExtensions = new HashMap<String, String>();
        formatExtensions.put("jpg", "jpg");
        formatExtensions.put("jpeg", "jpg");
        formatExtensions.put("jpe", "jpg");
        formatExtensions.put("png", "png");
        formatExtensions.put("gif", "gif");
        String formatExtesion = formatExtensions.get(extension);
        if (formatExtesion == null) {
            throw new Exception("Image format not supported. " + extension);
        }
        return formatExtesion;
    }

    /**
     * @param String format
     * @return String mimetype
     * @throws Exception
     */
    private String getMimeTypeByFormat(String format) throws Exception {
        Map<String, String> mimeTypes = new HashMap<String, String>();
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("png", "image/png");
        mimeTypes.put("gif", "image/gif");
        return mimeTypes.get(format);
    }
    
    /**
     * @param String path
     * @return String extension
     */
    private String getPathExtension(String path){
        String[] pathParts = path.split("\\.");
        return pathParts[pathParts.length - 1];
    }
    
    // Caching
    
    private String getCachePathParent() throws Exception {
        String cacheRootPath = getCacheRootPath();
        String dataPath = getDataPath();
        String dataPathElements[] = dataPath.split(":");
        if (dataPathElements.length > 0) {
            return cacheRootPath + "/" + dataPathElements[0] + dataPathElements[1] + "/";
        }
        return cacheRootPath + dataPathElements[0] + "/";
    }
    
    private String getCachePath() throws Exception {
        String path = getPath();
        String name = path.substring(path.lastIndexOf("/") + 1);
        String cachePath = getCachePathParent() + name;

        String queryString = getEnvironment().getRequest().getQueryString();
        if(queryString != null) {
            cachePath += queryString;
        }
        
        
        try {
            String yanelPath =  getResourceConfigProperty(CONFIG_PROPERTY_YANEL_PATH);
            if (yanelPath!= null && yanelPath.length() > 0) {
                cachePath += yanelPath;
            }
        } catch (Exception e) {
            log.debug(e,e);
        }

        
        try {
            String yanelPath =  getResourceConfigProperty(CONFIG_PROPERTY_PRESERVE_ALPHA);
            if (yanelPath!= null && yanelPath.length() > 0) {
                cachePath += yanelPath;
            }
        } catch (Exception e) {
            log.debug(e,e);
        }

        try {
            String targetWidth =  getResourceConfigProperty(CONFIG_PROPERTY_TARGET_WIDTH);
            if (targetWidth!= null && targetWidth.length() > 0) {
                cachePath += targetWidth;
            }
        } catch (Exception e) {
            log.debug(e,e);
        }

        
        try {
            String targetHeight =  getResourceConfigProperty(CONFIG_PROPERTY_TARGET_HEIGHT);
            if (targetHeight!= null && targetHeight.length() > 0) {
                cachePath += targetHeight;
            }
        } catch (Exception e) {
            log.debug(e,e);
        }

        try {
            String targetPercentage =  getResourceConfigProperty(CONFIG_PROPERTY_TARGET_PERCENTAGE);
            if (targetPercentage!= null && targetPercentage.length() > 0) {
                cachePath += targetPercentage;
            }
        } catch (Exception e) {
            log.debug(e,e);
        }

        try {
            String targetCropX =  getResourceConfigProperty(CONFIG_PROPERTY_CROP_X);
            if (targetCropX!= null && targetCropX.length() > 0) {
                cachePath += targetCropX;
            }
        } catch (Exception e) {
            log.debug(e,e);
        }
        try {
            String targetCropY =  getResourceConfigProperty(CONFIG_PROPERTY_CROP_Y);
            if (targetCropY!= null && targetCropY.length() > 0) {
                cachePath += targetCropY;
            }
        } catch (Exception e) {
            log.debug(e,e);
        }        
        try {
            String targetCropW =  getResourceConfigProperty(CONFIG_PROPERTY_CROP_W);
            if (targetCropW!= null && targetCropW.length() > 0) {
                cachePath += targetCropW;
            }
        } catch (Exception e) {
            log.debug(e,e);
        }        
        try {
            String targetCropH =  getResourceConfigProperty(CONFIG_PROPERTY_CROP_H);
            if (targetCropH!= null && targetCropH.length() > 0) {
                cachePath += targetCropH;
            }
        } catch (Exception e) {
            log.debug(e,e);
        }        
        return cachePath;
    }

    private String getCacheRootPath() {
        String cacheRootPath = DEFAULT_CACHE_ROOT_PATH;
        try {
            String cacheRootPathVal = getResourceConfigProperty(CONFIG_PROPERTY_CACHE_ROOT_PATH);
            if (cacheRootPathVal != null){
                cacheRootPath = cacheRootPathVal; 
            }
        } catch (Exception e) {
            if(log.isDebugEnabled())  log.debug("No cache root path configured within resource configuration. Use default '" + cacheRootPath + "'!");
        }
        return cacheRootPath;
    }
    
    public boolean isCacheOn(){
        try {
            if (Boolean.parseBoolean(getResourceConfigProperty(CONFIG_PROPERTY_DISABLE_CACHE))){
                return false;
            }
        } catch (Exception e) {
            if(log.isDebugEnabled()) log.debug("Could not read resource config property" + CONFIG_PROPERTY_DISABLE_CACHE, e);
        }
        return true;
    }    
}
