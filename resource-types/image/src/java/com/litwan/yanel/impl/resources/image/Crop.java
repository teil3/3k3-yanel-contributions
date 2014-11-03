package com.litwan.yanel.impl.resources.image;

import org.apache.log4j.Logger;

import com.litwan.yanel.impl.resources.image.ImageResource;

public class Crop {

    int cropX = 0;
    int cropY = 0;
    int cropH = 0;
    int cropW = 0;
    
    private static Logger log = Logger.getLogger(Crop.class);
    
    public Crop(ImageResource image) throws Exception {
        
        int origHeight = image.getOriginalBufferedImage().getHeight();
        int origWidth = image.getOriginalBufferedImage().getWidth();
        cropH = origHeight;
        cropW = origWidth;
        
        String suppressCrop = image.getParameterAsString(ImageResource.PARAMETER_NAME_SUPPRESS_CROP);
        String propertyCropX = image.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_CROP_X);
        String propertyCropY = image.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_CROP_Y);
        String propertyCropW = image.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_CROP_W);
        String propertyCropH = image.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_CROP_H);
        
        if (propertyCropX != null) {
            try {
                cropX = Integer.parseInt(propertyCropX);
            } catch (Exception e) {
                log.warn("resource-property '" + ImageResource.CONFIG_PROPERTY_CROP_X + "=" + propertyCropX + "' seems not to be an int. ", e);
                
            }
        }
        if (propertyCropY != null) {
            try {
                cropY = Integer.parseInt(propertyCropY);
            } catch (Exception e) {
                log.warn("resource-property '" + ImageResource.CONFIG_PROPERTY_CROP_Y + "=" + propertyCropY + "' seems not to be an int. ", e);
            }
        }
        if (propertyCropW != null) {
            try {
                cropW = Integer.parseInt(propertyCropW);
            } catch (Exception e) {
                log.warn("resource-property '" + ImageResource.CONFIG_PROPERTY_CROP_W + "=" + propertyCropW + "' seems not to be an int. ", e);
            }
        }
        if (propertyCropH != null) {
            try {
                cropH = Integer.parseInt(propertyCropH);
            } catch (Exception e) {
                log.warn("resource-property '" + ImageResource.CONFIG_PROPERTY_CROP_H + "=" + propertyCropH + "' seems not to be an int. ", e);
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
        //suppress crop
        if (suppressCrop != null) {
            cropX = 0;
            cropY = 0;
            cropW = origWidth;
            cropH = origHeight;
        }
    }

    public int getCropX() {
        return cropX;
    }

    public int getCropY() {
        return cropY;
    }

    public int getCropH() {
        return cropH;
    }

    public int getCropW() {
        return cropW;
    }
}
