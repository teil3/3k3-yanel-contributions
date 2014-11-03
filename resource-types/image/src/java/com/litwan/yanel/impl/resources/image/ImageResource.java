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

package com.litwan.yanel.impl.resources.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
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
public class ImageResource extends Resource implements ViewableV2  {

    public static final String CONFIG_PROPERTY_YANEL_PATH = "yanel-path";
    public static final String CONFIG_PROPERTY_YANEL_PATH_MATCHER = "yanel-path-matcher";
    public static final String CONFIG_PROPERTY_YANEL_PATH_TEMPLATE = "yanel-path-template";
    public static final String CONFIG_PROPERTY_PRESERVE_ALPHA = "preserve-alpha";
    public static final String CONFIG_PROPERTY_TARGET_WIDTH = "target-width";
    public static final String CONFIG_PROPERTY_TARGET_HEIGHT = "target-height";
    public static final String CONFIG_PROPERTY_CROP_X = "crop-x";
    public static final String CONFIG_PROPERTY_CROP_Y = "crop-y";
    public static final String CONFIG_PROPERTY_CROP_W = "crop-w";
    public static final String CONFIG_PROPERTY_CROP_H = "crop-h";
    public static final String CONFIG_PROPERTY_BACKGROUND_COLOR = "background-color";
    public static final String CONFIG_PROPERTY_TARGET_PERCENTAGE = "target-percentage";
    public static final String CONFIG_PROPERTY_ALLOW_REQUEST_PARAMETERS = "allow-request-parameters";
    public static final String CONFIG_PROPERTY_PROHIBIT_UP_SCALE = "prohibit-up-scale";
    public static final String CONFIG_PROPERTY_WATERMARK_PATH = "watermark-path";
    public static final String CONFIG_PROPERTY_WATERMARK_POSITION_X = "watermark-position-x";
    public static final String CONFIG_PROPERTY_WATERMARK_POSITION_Y = "watermark-position-y";
    public static final String CONFIG_PROPERTY_WATERMARK_WIDTH = "watermark-width";
    public static final String CONFIG_PROPERTY_WATERMARK_HEIGHT = "watermark-height";
    public static final String CONFIG_PROPERTY_WATERMARK_TRANSPARENCY = "watermark-transparency";
    public static final String CONFIG_PROPERTY_DISABLE_CACHE = "disable-cache";
    public static final String CONFIG_PROPERTY_CACHE_ROOT_PATH = "cache-root-path";    
    public static final String CONFIG_PROPERTY_ROTATE = "rotate";    
    public static final String PARAMETER_NAME_TARGET_WIDTH = "yanel.resource.image.width";
    public static final String PARAMETER_NAME_TARGET_HEIGHT = "yanel.resource.image.height";
    public static final String PARAMETER_NAME_TARGET_PERCENTAGE = "yanel.resource.image.percentage";
    public static final String PARAMETER_NAME_SHOW_ORIGINAL = "yanel.resource.image.show.original";
    public static final String PARAMETER_NAME_SUPPRESS_CROP = "yanel.resource.image.suppress.crop";
    public static final String PARAMETER_NAME_SUPPRESS_RESIZE = "yanel.resource.image.suppress.resize";
    protected static final String DEFAULT_CACHE_ROOT_PATH = "/cached-images";    

    private static Logger log = Logger.getLogger(ImageResource.class);

    private BufferedImage origImgBuffered;
    private int origHeight = 0;
    private int origWidth = 0;
    private int targetHeight = 0;
    private int targetWidth = 0;
    private String watermarkPath;
    private String watermarkTransparency;
    private int watermarkX = 0;
    private int watermarkY = 0;
    private int watermarkWidth = 0;
    private int watermarkHeight = 0;
    private int rotate = 0;

    public void init() {
    	log.debug("Init resource ...");
        try {
            origImgBuffered = getOriginalBufferedImage();
            
            targetHeight = 0;
            targetWidth = 0;

            origHeight = origImgBuffered.getHeight();
            origWidth = origImgBuffered.getWidth();

            
            
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
                targetHeight = origHeight;
                targetWidth = origWidth;
//                throw new Exception("Image is not properly configured.");
            }    
            watermarkPath = getResourceConfigProperty(CONFIG_PROPERTY_WATERMARK_PATH);
            if(watermarkPath != null && watermarkPath.length() < 1) watermarkPath = null;
            watermarkTransparency = getResourceConfigProperty(CONFIG_PROPERTY_WATERMARK_TRANSPARENCY);
            try {
                watermarkX = Integer.parseInt(getResourceConfigProperty(CONFIG_PROPERTY_WATERMARK_POSITION_X));
            } catch (NumberFormatException e) {
                watermarkX = 0;
            }            
            try {
                watermarkY = Integer.parseInt(getResourceConfigProperty(CONFIG_PROPERTY_WATERMARK_POSITION_Y));
            } catch (NumberFormatException e) {
                watermarkY = 0;
            }            
            try {
                watermarkWidth = Integer.parseInt(getResourceConfigProperty(CONFIG_PROPERTY_WATERMARK_WIDTH));
            } catch (NumberFormatException e) {
                watermarkWidth = -1;
            }            
            try {
                watermarkHeight = Integer.parseInt(getResourceConfigProperty(CONFIG_PROPERTY_WATERMARK_HEIGHT));
            } catch (NumberFormatException e) {
                watermarkHeight = -1;
            }            
            try {
            	rotate = Integer.parseInt(getResourceConfigProperty(CONFIG_PROPERTY_ROTATE));
            } catch (NumberFormatException e) {
            	rotate = 0;
            }
            
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }
    /* (non-Javadoc)
     * @see org.wyona.yanel.core.api.attributes.ViewableV2#getView(java.lang.String)
     */
    public View getView(String viewId) throws Exception {
        init();
        Cache cache = new Cache(this);
        View defaultView = new View(); 
        String yanelPath = getDataPath();
        if (!exists() && !(isPathResolvable(yanelPath))) {
            log.warn("No such image file '" + yanelPath + "' in order to resize!");
            throw new org.wyona.yanel.core.ResourceNotFoundException("No such image file '" + yanelPath + "' in order to resize!");
        }
        Repository repo= getRealm().getRepository();
        Repository rcRepo= getRealm().getRTIRepository();
        String cachePath = null;
        String pathExtension = getPathExtension(getPath());
        String format = getInformalFormatNameByExtension(pathExtension);
        try {
            format = getInformalFormatNameByExtension(getResourceConfigProperty(CONFIG_PROPERTY_YANEL_PATH_TEMPLATE));
        } catch (Exception e) {
            
        }
        defaultView.setMimeType(getMimeTypeByFormat(format));
        try {
            cachePath = cache.getCachePath();
            Node cacheNode = repo.getNode(cachePath);
            Node contentNode = repo.getNode(yanelPath);
            Node rcNode = rcRepo.getNode(yanelPath + ".yanel-rc");
            long contentNodeLastModified = contentNode.getLastModified();
            long cacheNodeLastModified = cacheNode.getLastModified();
            long rcNodeLastModified = rcNode.getLastModified();
            if (cache.isCacheOn() && (contentNodeLastModified < cacheNodeLastModified && rcNodeLastModified < cacheNodeLastModified)) {
                if(log.isDebugEnabled()) log.debug("cache is newer than content, serving cache.");
                defaultView.setInputStream(cacheNode.getInputStream());
                return defaultView;
            }
        } catch (Exception e) {
            if(log.isDebugEnabled()) log.debug(e, e);
        }
        
        BufferedImage imgResizedBuffered = createResizedCopy(origImgBuffered);

        //ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //byte[] bytes = byteArrayOutputStream.toByteArray();
        //ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try {
            String parameterShowOriginal = getParameterAsString(PARAMETER_NAME_SHOW_ORIGINAL);
            if(parameterShowOriginal != null){
                Node origNode = getRealm().getRepository().getNode(getDataPath());
                defaultView.setInputStream(origNode.getInputStream());
                
                return defaultView;
            }
            if (cache.isCacheOn() && cachePath != null) {
                org.wyona.yarep.core.Node cacheNode = null;
                if(!getRealm().getRepository().existsNode(cachePath)){
                    cacheNode = YarepUtil.addNodes(getRealm().getRepository(), cachePath, org.wyona.yarep.core.NodeType.RESOURCE);
                } else {
                    cacheNode = getRealm().getRepository().getNode(cachePath);
                }
                
                ImageIO.write(imgResizedBuffered, format, cacheNode.getOutputStream());
                defaultView.setInputStream(cacheNode.getInputStream());
            } else if (viewId != null && viewId.equals("download")){
                defaultView.setResponse(true);
                ByteArrayOutputStream out = new ByteArrayOutputStream();

//                OutputStream os = getEnvironment().getResponse().getOutputStream();
                ImageIO.write(imgResizedBuffered, format, out);
                defaultView.setInputStream(new ByteArrayInputStream(out.toByteArray()));
//                defaultView.setMimeType("application/x-download");
                String fileName = getPath();
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
                defaultView.setHttpHeader("Content-Disposition", "attachment; filename=" + fileName);                
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

    protected BufferedImage getOriginalBufferedImage() throws Exception {
        String yanelPath = getDataPath();
        Repository repo = getRealm().getRepository();
        InputStream imgSource = null;
        if (isPathResolvable(yanelPath)) {
            SourceResolver resolver = new SourceResolver(this);
            Source source = resolver.resolve(yanelPath, null);
            imgSource = ((StreamSource) source).getInputStream();
        } else {
            imgSource = repo.getNode(yanelPath).getInputStream();
        }
        BufferedImage imgBuffered = ImageIO.read(imgSource);
        return imgBuffered;
    }
    
    protected BufferedImage getWaterMarkLogo() throws Exception {
        if (watermarkPath == null) return null;
        Repository repo = getRealm().getRepository();
        InputStream imgSource = null;
        if (isPathResolvable(watermarkPath)) {
            SourceResolver resolver = new SourceResolver(this);
            Source source = resolver.resolve(watermarkPath, null);
            imgSource = ((StreamSource) source).getInputStream();
        } else {
            imgSource = repo.getNode(watermarkPath).getInputStream();
        }
        BufferedImage imgBuffered = ImageIO.read(imgSource);
        return imgBuffered;
    }

    private boolean isPathResolvable(String yanelPath) throws Exception {
        return yanelPath.startsWith("yanelrepo:") || yanelPath.startsWith("yanelresource:") || yanelPath.startsWith("http:");
    }
   
    /* (non-Javadoc)
     * @see org.wyona.yanel.core.api.attributes.ViewableV2#exists()
     */
    public boolean exists() throws Exception {
        String yanelPath = getDataPath();
        Repository repo = getRealm().getRepository();
        if (isPathResolvable(yanelPath)) {
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
        Color color = getBackgroundColor();
        
        boolean preserveAlpha = originalImage.getColorModel().hasAlpha();
        String propertyPreserveAlpha = getResourceConfigProperty(CONFIG_PROPERTY_PRESERVE_ALPHA);
        if (propertyPreserveAlpha != null) {
            preserveAlpha = Boolean.parseBoolean(propertyPreserveAlpha);
        }
        
        String suppressResize = getParameterAsString(PARAMETER_NAME_SUPPRESS_RESIZE);
        //suppress resize
        if (suppressResize != null) {
            targetHeight = origHeight;
            targetWidth = origWidth;
        }

        Crop crop = new Crop(this);
        if (Boolean.parseBoolean(getResourceConfigProperty(CONFIG_PROPERTY_PROHIBIT_UP_SCALE)) && (targetWidth > origWidth || targetHeight > origHeight)) {
//            return originalImage;
            return originalImage.getSubimage(crop.getCropX(), crop.getCropY(), crop.getCropW(), crop.getCropH());
        } else {
            BufferedImage clipedImage = originalImage.getSubimage(crop.getCropX(), crop.getCropY(), crop.getCropW(), crop.getCropH());
            
            BufferedImage scaledImage = getScaledInstance(clipedImage,targetWidth, targetHeight);
            if (rotate != 0) {
            	scaledImage = rotate(scaledImage, rotate);
            }
            
            Graphics2D g = scaledImage.createGraphics();
            try {
            	if (watermarkPath != null) {
            		try {
            			if (watermarkTransparency != null) {
            				float transparency = Float.parseFloat(watermarkTransparency);
            				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
            			}
            		} catch (NumberFormatException e) {
            			log.warn("Could not set Transparency. " + e, e);
            		}
            		BufferedImage waterMarkLogo = getWaterMarkLogo();
            		if (watermarkWidth < 0 ) {
            		    watermarkWidth = waterMarkLogo.getWidth();
            		}
            		if (watermarkHeight < 0 ) {
            		    watermarkHeight = waterMarkLogo.getHeight();
            		}
            		g.drawImage(waterMarkLogo, watermarkX, watermarkY, watermarkWidth, watermarkHeight, color, null);// draw in lower right corner
            	}
            } finally {
            	g.dispose();
            }
            return scaledImage;
            
//            int imageType = preserveAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
//            BufferedImage scaledBI = new BufferedImage(targetWidth, targetHeight, imageType);
//            Graphics2D g = scaledBI.createGraphics();
//            if (preserveAlpha) {
//                g.setComposite(AlphaComposite.Src);
//            }
//            g.drawImage(clipedImage, 0, 0, targetWidth, targetHeight, color, null); 
//            g.dispose();
//            return scaledBI;
        }
    }
    
    public BufferedImage rotate(BufferedImage img, int angle) {  
    	int w = img.getWidth();  
    	int h = img.getHeight();  
//    	int oldW = w;
//    	int oldH = h;
//    	if (angle == 90 || angle == 270) {
//    		w = oldH;
//    		h = oldW;
//    	}
    	BufferedImage dimg = null;
    	
    	if (angle == 0) return img;
    	Graphics2D g = null;
    	if (angle == 90) {
    		dimg = new BufferedImage(h, w, img.getType());  
    		g = dimg.createGraphics();  
    		g.rotate(Math.toRadians(angle), h/2, h/2);  
    	}
    	if (angle == 180) {
    		dimg = new BufferedImage(w, h, img.getType());  
    		g = dimg.createGraphics();  
    		g.rotate(Math.toRadians(angle), w/2, h/2);  
    	}
    	if (angle == 270) {
    		dimg = new BufferedImage(h, w, img.getType());  
    		g = dimg.createGraphics();  
    		g.rotate(Math.toRadians(angle), w/2, w/2);  
    	}
    	
    	
    	g.drawImage(img, null, 0, 0);  
    	return dimg;  
    }  
    
    public String getWatermarkTransparency() {
        if (watermarkTransparency == null) {
            return "1";
        }
        return watermarkTransparency;
    }

    public String getWatermarkPath() {
        return watermarkPath;
    }

    public int getWatermarkY() {
        return watermarkY;
    }

    public int getWatermarkX() {
        return watermarkX;
    }
    
    public int getWatermarkWidth() {
        return watermarkWidth;
    }
    
    public int getWatermarkHeight() {
        return watermarkHeight;
    }

    public int getOrigWidth() {
        return origWidth;
    }

    public int getOrigHeight() {
        return origHeight;
    }
    
    public int getTargetWidth() {
    	if (getRotation() == 90 || getRotation() == 270) {
    		return targetHeight;
    	}
        return targetWidth;
    }
    
    public int getTargetHeight() {
    	if (getRotation() == 90 || getRotation() == 270) {
    		return targetWidth;
    	}
        return targetHeight;
    }
    
    public int getRotation() {
    	return rotate;
    }
    
    

    private Color getBackgroundColor() throws Exception {
        //backgroundColor
        Color color = new Color(255,255,255,255);
        String propertyBackgroundColor = getResourceConfigProperty(CONFIG_PROPERTY_BACKGROUND_COLOR);
        if (propertyBackgroundColor != null && propertyBackgroundColor.length() > 0) {
            String hex = "#" + propertyBackgroundColor;
			color = Color.decode(hex);
			return color;
        }
        return null;
    }

    public String getBackgroundColorAsString() {
    	try {
    		if (getBackgroundColor() != null) return Integer.toHexString((getBackgroundColor().getRGB() & 0xffffff) | 0x1000000).substring(1);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return "";
    }
    
    public boolean isPreserveAlpha() {
    	boolean preserveAlpha = false ;
    	String propertyPreserveAlpha = "";
    	try {
    		preserveAlpha = getOriginalBufferedImage().getColorModel().hasAlpha();
		} catch (Exception e) {
			log.warn("could not found out wheter the original image preserves alpha or not. ", e);
		}
		try {
			propertyPreserveAlpha = getResourceConfigProperty(CONFIG_PROPERTY_PRESERVE_ALPHA);
		} catch (Exception e) {
			log.debug("could not found out wheter the configuration has preserves alpha or not. ", e);
		}
        if (propertyPreserveAlpha != null) {
            preserveAlpha = Boolean.parseBoolean(propertyPreserveAlpha);
        }
        return preserveAlpha;
	}
    /**
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
    

    
    /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    public BufferedImage getScaledInstance(BufferedImage img,
                                           int targetWidth,
                                           int targetHeight) throws Exception
                                           //boolean higherQuality,
                                           //Object hint)
    {
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage)img;
        int w, h;
        w = getOrigWidth();
        h = getOrigHeight();
        
        do {
            if (w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            } else {
                w = targetWidth;
            }

            if (h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            } else {
                h = targetHeight;
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(ret, 0, 0, w, h, getBackgroundColor(), null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }


}
