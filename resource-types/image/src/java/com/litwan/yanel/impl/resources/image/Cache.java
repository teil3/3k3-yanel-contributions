package com.litwan.yanel.impl.resources.image;

import org.apache.log4j.Logger;

import com.litwan.yanel.impl.resources.image.ImageResource;

public class Cache {

    private ImageResource image;
    
    private static Logger log = Logger.getLogger(Crop.class);
    
    public Cache(ImageResource image) throws Exception {
        this.image = image;
        
    }
    
    public String getCachePath() throws Exception {
        String path = image.getPath();
        String name = path.substring(path.lastIndexOf("/") + 1);
        String cachePath = getCachePathParent() + name;

        String queryString = image.getEnvironment().getRequest().getQueryString();
        if(queryString != null) {
            cachePath += queryString;
        }
        
        
        try {
            String yanelPath =  image.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_YANEL_PATH);
            if (yanelPath!= null && yanelPath.length() > 0) {
                cachePath += yanelPath;
            }
        } catch (Exception e) {
            log.debug(e,e);
        }

        
        try {
            String yanelPath =  image.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_PRESERVE_ALPHA);
            if (yanelPath!= null && yanelPath.length() > 0) {
                cachePath += yanelPath;
            }
        } catch (Exception e) {
            log.debug(e,e);
        }

        try {
            String targetWidth =  image.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_TARGET_WIDTH);
            if (targetWidth!= null && targetWidth.length() > 0) {
                cachePath += targetWidth;
            }
        } catch (Exception e) {
            log.debug(e,e);
        }

        
        try {
            String targetHeight =  image.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_TARGET_HEIGHT);
            if (targetHeight!= null && targetHeight.length() > 0) {
                cachePath += targetHeight;
            }
        } catch (Exception e) {
            log.debug(e,e);
        }

        try {
            String targetPercentage =  image.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_TARGET_PERCENTAGE);
            if (targetPercentage!= null && targetPercentage.length() > 0) {
                cachePath += targetPercentage;
            }
        } catch (Exception e) {
            log.debug(e,e);
        }

        try {
            String targetCropX =  image.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_CROP_X);
            if (targetCropX!= null && targetCropX.length() > 0) {
                cachePath += targetCropX;
            }
        } catch (Exception e) {
            log.debug(e,e);
        }
        try {
            String targetCropY =  image.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_CROP_Y);
            if (targetCropY!= null && targetCropY.length() > 0) {
                cachePath += targetCropY;
            }
        } catch (Exception e) {
            log.debug(e,e);
        }        
        try {
            String targetCropW =  image.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_CROP_W);
            if (targetCropW!= null && targetCropW.length() > 0) {
                cachePath += targetCropW;
            }
        } catch (Exception e) {
            log.debug(e,e);
        }        
        try {
            String targetCropH =  image.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_CROP_H);
            if (targetCropH!= null && targetCropH.length() > 0) {
                cachePath += targetCropH;
            }
        } catch (Exception e) {
            log.debug(e,e);
        }        
        try {
            String watermarkPath =  image.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_WATERMARK_PATH);
            if (watermarkPath!= null && watermarkPath.length() > 0) {
                cachePath += watermarkPath.replaceAll("/", "");
            }
        } catch (Exception e) {
            log.debug(e,e);
        }        
        try {
            String watermarkTransparency =  image.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_WATERMARK_TRANSPARENCY);
            if (watermarkTransparency!= null && watermarkTransparency.length() > 0) {
                cachePath += watermarkTransparency;
            }
        } catch (Exception e) {
            log.debug(e,e);
        }        
        return cachePath;
    }
    
    public boolean isCacheOn(){
        try {
            if (Boolean.parseBoolean(image.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_DISABLE_CACHE))){
                return false;
            }
        } catch (Exception e) {
            if(log.isDebugEnabled()) log.debug("Could not read resource config property" + ImageResource.CONFIG_PROPERTY_DISABLE_CACHE, e);
        }
        return true;
    }    

    private String getCacheRootPath() {
        String cacheRootPath = ImageResource.DEFAULT_CACHE_ROOT_PATH;
        try {
            String cacheRootPathVal = image.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_CACHE_ROOT_PATH);
            if (cacheRootPathVal != null){
                cacheRootPath = cacheRootPathVal; 
            }
        } catch (Exception e) {
            if(log.isDebugEnabled())  log.debug("No cache root path configured within resource configuration. Use default '" + cacheRootPath + "'!");
        }
        return cacheRootPath;
    }

    private String getCachePathParent() throws Exception {
        String cacheRootPath = getCacheRootPath();
        String dataPath = image.getDataPath();
        String dataPathElements[] = dataPath.split(":");
        if (dataPathElements.length > 1) {
            return cacheRootPath + "/" + dataPathElements[0] + dataPathElements[1] + "/";
        }
        return cacheRootPath + dataPathElements[0] + "/";
    }
}
