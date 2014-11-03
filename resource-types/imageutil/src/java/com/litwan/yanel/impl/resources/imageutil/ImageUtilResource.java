/*
 * Copyright 2010 litwan
 */

package com.litwan.yanel.impl.resources.imageutil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.wyona.yanel.core.ResourceConfiguration;
import org.wyona.yanel.core.attributes.viewable.View;
import org.wyona.yanel.core.util.PathUtil;
import org.wyona.yanel.impl.resources.usecase.ExecutableUsecaseResource;
import org.wyona.yanel.impl.resources.usecase.UsecaseException;
import org.wyona.yanel.servlet.communication.HttpRequest;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.NodeType;
import org.wyona.yarep.util.YarepUtil;

import com.litwan.yanel.impl.resources.image.Crop;
import com.litwan.yanel.impl.resources.image.ImageResource;


/**
 * A simple usecase which is based on ExecutableUsecaseResource
 */
public class ImageUtilResource extends ExecutableUsecaseResource {
    
    protected static final String PARAMETER_PRESERVE_ALPHA = "preserve-alpha";
	protected static final String PARAMETER_BACKGROUND_COLOR = "background-color";
	protected static final String PARAMETER_ALLOW_REQUEST_PARAMETERS = "allow-request-parameters";
	protected static final String PARAMETER_PROHIBIT_UP_SCALE = "prohibit-up-scale";
	protected static final String PARAMETER_DISABLE_CACHE = "disable-cache";
	protected static final String PARAMETER_CACHE_ROOT_PATH = "cache-root-path";
	protected static final String PARAMETER_YANEL_PATH = "yanel-path";
	protected static final String PARAMETER_WATERMARK_TRANSPARENCY = "watermark-transparency";
	protected static final String PARAMETER_DRAGTOP = "dragtop";
	protected static final String PARAMETER_DRAGLEFT = "dragleft";
	protected static final String PARAMETER_DRAGWIDTH = "dragwidth";
	protected static final String PARAMETER_DRAGHEIGHT= "dragheight";
	protected static final String PARAMETER_WATERMARK_PATH = "watermark-path";
	protected static final String PARAMETER_RESIZEHEIGHT = "resizeheight";
	protected static final String PARAMETER_RESIZEWIDTH = "resizewidth";
	protected static final String PARAMETER_Y2 = "y2";
	protected static final String PARAMETER_X2 = "x2";
	protected static final String PARAMETER_Y1 = "y1";
	protected static final String PARAMETER_X1 = "x1";
	protected static final String PARAMETER_ROTATE = "rotate";
	protected static final String PARAMETER_ROTATE_ADD = "addrotate";
    protected static final String PARAMETER_EDIT_PATH = "edit-path";
    protected static final String PARAMETER_SAVE_CROP = "yanel.resource.imageutil.crop.save";
    protected static final String PARAMETER_SAVE_ROTATE = "yanel.resource.imageutil.rotate.save";
    protected static final String PARAMETER_SAVE_RESIZE = "yanel.resource.imageutil.resize.save";
    protected static final String PARAMETER_SAVE_WATERMARK = "yanel.resource.imageutil.watermark.save";
    protected static final String PARAMETER_SAVE_MORE = "yanel.resource.imageutil.more.save";
    protected static final String PARAMETER_SUPRESS_WATERMARK = "yanel.resource.imageutil.watermark.supress";
    protected static final String PARAMETER_CREATE_PATH = "yanel.resource.imageutil.create.path";
    protected static final String PARAMETER_CREATE_FILENAME = "yanel.resource.imageutil.create.filename";
    protected static final String PARAMETER_SAVE_CREATE = "yanel.resource.imageutil.create.save";
    protected static final String PARAMETER_CONTINUE_PATH = "continue-path";
    protected static final String PARAMETER_SHOW_IMAGE = "yanel.resource.imageutil.show.image";
    protected static final String PARAMETER_SHOW_IMAGE_DOWNLOAD = "yanel.resource.imageutil.show.image.download";
    protected static final String CREATE_VIEW_ID = "create";
    protected static final String CONFIG_PROPERTY_MATCHER_EXTENSION = "matcher-extension";    
    protected static final String CONFIG_PROPERTY_CONTINUE_PATH = "continue-path";
    protected static Logger log = Logger.getLogger(ImageUtilResource.class);
    protected String editPath;
    protected ImageResource resToEdit;
    
    protected void init() throws UsecaseException {
        log.warn("DEBUG: Init resource ...");
        try {
            String matcherExtension = getResourceConfigProperty(CONFIG_PROPERTY_MATCHER_EXTENSION);
            if (matcherExtension != null && matcherExtension.length() > 0) {
                editPath = getPath().substring(0, getPath().length() - matcherExtension.length());
            } else {
                log.warn("Could not get Resource Configuration Property 'matcher-extension'. Fallback to previous version ...");
                editPath = getParameterAsString(PARAMETER_EDIT_PATH); 
            }
        } catch(Exception e) {
            log.error(e, e);
            throw new UsecaseException(e.getMessage());
        }
        try {
            resToEdit = (ImageResource) getYanel().getResourceManager().getResource(getEnvironment(), getRealm(), editPath);
            resToEdit.init();
        } catch (Exception e) {
            log.debug("Exception: " + e);
        }
        if (resToEdit == null) {
            addError("Could not get Image to edit.");
            return;
        }
    }
    
    protected View processUsecase(String viewID) throws UsecaseException {
        try {
            if (!getRealm().getPolicyManager().authorize(getEditPath(), getEnvironment().getIdentity(), new org.wyona.security.core.api.Usecase("write"))) {
                log.warn("Not authorized: " + getPath() + ", " + getEditPath());
                throw new UsecaseException("Not authorized. Access denied.");
            }
        } catch(Exception e) {
        	log.error(e.getMessage(), e);
        	throw new UsecaseException(e);
        }
    	String saveCreate = getParameterAsString(PARAMETER_SAVE_CREATE);
    	if (resToEdit == null) {
    		if (saveCreate != null) {
    			createImage((HttpRequest)getEnvironment().getRequest());
    		} else {
    			return generateView(CREATE_VIEW_ID);
    		}
    	}
    	String showImage = getParameterAsString(PARAMETER_SHOW_IMAGE);
    	if (showImage != null) {
    	    try {
    	        return resToEdit.getView(VIEW_DEFAULT);
    	    } catch (Exception e) {
    	        log.error("Could not get view of editable image: ", e);
    	    }
    	}
    	String showImageDownload = getParameterAsString(PARAMETER_SHOW_IMAGE_DOWNLOAD);
    	if (showImageDownload != null) {
    	    try {
    	        View resToEditView = resToEdit.getView("download");
    	        return resToEditView;
    	    } catch (Exception e) {
                log.error("Could not get view of editable image to download: ", e);
            }

    	        
    	        
    	}
    	String supressWaterMark = getParameterAsString(PARAMETER_SUPRESS_WATERMARK);
    	if (supressWaterMark != null) {
    		try {
    			ResourceConfiguration rc = new ResourceConfiguration(realm.getRTIRepository().getNode(resToEdit.getPath() + ".yanel-rc"));
    			rc.setProperty(ImageResource.CONFIG_PROPERTY_WATERMARK_PATH, "");
    			rc.setProperty(ImageResource.CONFIG_PROPERTY_WATERMARK_POSITION_X, "");
    			rc.setProperty(ImageResource.CONFIG_PROPERTY_WATERMARK_POSITION_Y, "");
    			rc.setProperty(ImageResource.CONFIG_PROPERTY_WATERMARK_TRANSPARENCY, "");
    			resToEdit = (ImageResource) getYanel().getResourceManager().getResource(getEnvironment(), getRealm(), editPath, rc);//(getEnvironment(), getRealm(), editPath);
    			return resToEdit.getView(VIEW_DEFAULT);
    		} catch (Exception e) {
    			log.error("Could not get view of editable image with supressed water mark: ", e);
    		}
    	}
    	
    	String saveCrop = getParameterAsString(PARAMETER_SAVE_CROP);
    	String saveResize = getParameterAsString(PARAMETER_SAVE_RESIZE);
    	String saveWaterMark = getParameterAsString(PARAMETER_SAVE_WATERMARK);
    	String saveMore = getParameterAsString(PARAMETER_SAVE_MORE);
    	String saveRotate = getParameterAsString(PARAMETER_SAVE_ROTATE);
    	if (getParameter(PARAM_SUBMIT) != null) {
    		if (!checkPreconditions() || hasErrors()) {
    		}
    		if (saveCrop != null) {
    			saveCrop();
    		}
    		if (saveResize != null) {
    			saveResize();
    		}
    		if (saveWaterMark != null) {
    			saveWatermark();
    		}
    		if (saveMore != null) {
    			saveMore();
    		}
    		if (saveRotate != null) {
    			saveRotate();
    		}
        	String continuingPath = getContinuingPath();
			if (continuingPath != null) {
                View view = new View();
                view.setResponse(false); // when redirecting the resource writes the response itself
                if (continuingPath.startsWith("/")) {
                	continuingPath = continuingPath.substring(1);
                }    
                String redirectURL = PathUtil.backToRealm(getPath()) + continuingPath;
                HttpServletResponse response = getEnvironment().getResponse();
                response.setStatus(307);
                response.setHeader("Location", redirectURL);
                return view;
        	}
    		return generateView(VIEW_DEFAULT);
    	} else if (getParameter(PARAM_CANCEL) != null) {
    		cancel();
    		return generateView(VIEW_CANCEL);
    	}
    	return generateView(VIEW_DEFAULT); // this will show the default view if the param is not set
    }    
    
    public String getContinuingPath() {
    	String continuingPath = getParameterAsString(PARAMETER_CONTINUE_PATH);
    	if (continuingPath == null) return null;
    	if (continuingPath.startsWith("/")) {
    		continuingPath = continuingPath.substring(1);
    	}
		return continuingPath;
	}
    
    protected void saveCrop() throws UsecaseException {
        try {
            String resToEditPath = resToEdit.getPath();
            ResourceConfiguration rc = new ResourceConfiguration(realm.getRTIRepository().getNode(resToEditPath+ ".yanel-rc"));
            int x1 = Integer.parseInt(getParameterAsString(PARAMETER_X1));
            int y1 = Integer.parseInt(getParameterAsString(PARAMETER_Y1));
            int x2 = Integer.parseInt(getParameterAsString(PARAMETER_X2));
            int y2 = Integer.parseInt(getParameterAsString(PARAMETER_Y2));
			rc.setProperty(ImageResource.CONFIG_PROPERTY_CROP_X, String.valueOf(x1));
			rc.setProperty(ImageResource.CONFIG_PROPERTY_CROP_Y, String.valueOf(y1));
			rc.setProperty(ImageResource.CONFIG_PROPERTY_CROP_W, String.valueOf(x2 - x1));
			rc.setProperty(ImageResource.CONFIG_PROPERTY_CROP_H, String.valueOf(y2 - y1));
            rc.save();
        } catch (Exception e) {
            throw new UsecaseException(e);
        }
        //not sure why this is necessary. but without reinstantiate resToEdit a getResourceConfigProperty(String) still gets the old value 
        try {
            resToEdit = (ImageResource) getYanel().getResourceManager().getResource(getEnvironment(), getRealm(), editPath);
            resToEdit.init();
        } catch (Exception e) {
            log.error("Exception: " + e);
        }        
    }
    
    protected void saveResize() throws UsecaseException {
    	try {
    		String resToEditPath = resToEdit.getPath();
    		ResourceConfiguration rc = new ResourceConfiguration(realm.getRTIRepository().getNode(resToEditPath+ ".yanel-rc"));
    		String resizewidth = getParameterAsString(PARAMETER_RESIZEWIDTH);
    		String resizeheight = getParameterAsString(PARAMETER_RESIZEHEIGHT);
			rc.setProperty(ImageResource.CONFIG_PROPERTY_TARGET_WIDTH, resizewidth);
			rc.setProperty(ImageResource.CONFIG_PROPERTY_TARGET_HEIGHT, resizeheight);
    		rc.save();
    	} catch (Exception e) {
    		throw new UsecaseException(e);
    	}
    	//not sure why this is necessary. but without reinstantiate resToEdit a getResourceConfigProperty(String) still gets the old value 
    	try {
    		resToEdit  = (ImageResource) getYanel().getResourceManager().getResource(getEnvironment(), getRealm(), editPath);
    		resToEdit.init();
    	} catch (Exception e) {
    		log.error("Exception: " + e);
    	}        
    }
    
    protected void saveWatermark() throws UsecaseException {
    	try {
    		String resToEditPath = resToEdit.getPath();
    		ResourceConfiguration rc = new ResourceConfiguration(realm.getRTIRepository().getNode(resToEditPath+ ".yanel-rc"));
    		String watermarkPath = getParameterAsString(PARAMETER_WATERMARK_PATH);
    		String dragLeft = getParameterAsString(PARAMETER_DRAGLEFT);
    		String dragTop = getParameterAsString(PARAMETER_DRAGTOP);
    		String dragWidth = getParameterAsString(PARAMETER_DRAGWIDTH);
    		String dragHeight = getParameterAsString(PARAMETER_DRAGHEIGHT);
    		String watermarkTransparency = getParameterAsString(PARAMETER_WATERMARK_TRANSPARENCY);
    		rc.setProperty(ImageResource.CONFIG_PROPERTY_WATERMARK_PATH, watermarkPath);
    		rc.setProperty(ImageResource.CONFIG_PROPERTY_WATERMARK_POSITION_X, dragLeft);
    		rc.setProperty(ImageResource.CONFIG_PROPERTY_WATERMARK_POSITION_Y, dragTop);
    		rc.setProperty(ImageResource.CONFIG_PROPERTY_WATERMARK_TRANSPARENCY, watermarkTransparency);
    		rc.setProperty(ImageResource.CONFIG_PROPERTY_WATERMARK_WIDTH, dragWidth);
    		rc.setProperty(ImageResource.CONFIG_PROPERTY_WATERMARK_HEIGHT, dragHeight);
    		rc.save();
    	} catch (Exception e) {
    		throw new UsecaseException(e);
    	}
    	//not sure why this is necessary. but without reinstantiate resToEdit a getResourceConfigProperty(String) still gets the old value 
    	try {
    		resToEdit  = (ImageResource) getYanel().getResourceManager().getResource(getEnvironment(), getRealm(), editPath);
    		resToEdit.init();
    	} catch (Exception e) {
    		log.error("Exception: " + e);
    	}        
    }

    protected void saveMore() throws UsecaseException {
    	try {
    		String resToEditPath = resToEdit.getPath();
    		ResourceConfiguration rc = new ResourceConfiguration(realm.getRTIRepository().getNode(resToEditPath + ".yanel-rc"));
    		String preserveAlpha = getParameterAsString(PARAMETER_PRESERVE_ALPHA);
    		String backgroundColor = getParameterAsString(PARAMETER_BACKGROUND_COLOR);
    		String allowRequestParamters = getParameterAsString(PARAMETER_ALLOW_REQUEST_PARAMETERS);
    		String prohibitUpScale = getParameterAsString(PARAMETER_PROHIBIT_UP_SCALE);
    		String disableCache = getParameterAsString(PARAMETER_DISABLE_CACHE);
    		String cacheRootPath = getParameterAsString(PARAMETER_CACHE_ROOT_PATH);
    		String yanelPath = getParameterAsString(PARAMETER_YANEL_PATH);
    		
    		rc.setProperty(ImageResource.CONFIG_PROPERTY_PRESERVE_ALPHA, preserveAlpha);
    		rc.setProperty(ImageResource.CONFIG_PROPERTY_BACKGROUND_COLOR, backgroundColor);
    		rc.setProperty(ImageResource.CONFIG_PROPERTY_ALLOW_REQUEST_PARAMETERS, allowRequestParamters);
    		rc.setProperty(ImageResource.CONFIG_PROPERTY_PROHIBIT_UP_SCALE, prohibitUpScale);
    		rc.setProperty(ImageResource.CONFIG_PROPERTY_DISABLE_CACHE, disableCache);
    		rc.setProperty(ImageResource.CONFIG_PROPERTY_CACHE_ROOT_PATH, cacheRootPath);
    		rc.setProperty(ImageResource.CONFIG_PROPERTY_YANEL_PATH, yanelPath);
    		rc.save();
    	} catch (Exception e) {
    		throw new UsecaseException(e);
    	}
    	//not sure why this is necessary. but without reinstantiate resToEdit a getResourceConfigProperty(String) still gets the old value 
    	try {
    		resToEdit  = (ImageResource) getYanel().getResourceManager().getResource(getEnvironment(), getRealm(), editPath);
    		resToEdit.init();
    	} catch (Exception e) {
    		log.error("Exception: " + e);
    	}        
    }
    
    protected void saveRotate() throws UsecaseException {
    	try {
    		String resToEditPath = resToEdit.getPath();
    		int targetRotate = 0;
    		String rotate = getParameterAsString(PARAMETER_ROTATE);
    		if (rotate == null) return;
    		String addRotate = getParameterAsString(PARAMETER_ROTATE_ADD);
    		int rotateValue = Integer.parseInt(rotate);
    		int origRotate = resToEdit.getRotation();
    		if (addRotate !=null) {
    			targetRotate = origRotate + rotateValue;
    		} else {
    			targetRotate = rotateValue;
    		}
    		while (targetRotate >= 360) {
    			targetRotate = targetRotate - 360;
    		}
    		
    		ResourceConfiguration rc = new ResourceConfiguration(realm.getRTIRepository().getNode(resToEditPath+ ".yanel-rc"));
    		rc.setProperty(ImageResource.CONFIG_PROPERTY_ROTATE, targetRotate+"");
    		rc.save();
    	} catch (Exception e) {
    		throw new UsecaseException(e);
    	}
    	//not sure why this is necessary. but without reinstantiate resToEdit a getResourceConfigProperty(String) still gets the old value 
    	try {
    		resToEdit  = (ImageResource) getYanel().getResourceManager().getResource(getEnvironment(), getRealm(), editPath);
    		resToEdit.init();
    	} catch (Exception e) {
    		log.error("Exception: " + e);
    	}        
    }

    /*
     * This method is executed when submitting the form provided in the default view (probably implemented as a jelly template).
     */
    public void execute() throws UsecaseException {

    }

    /*
     * This method is executed when canceling the form provided in the default view (probably implemented as a jelly template).
     */
    public void cancel() throws UsecaseException {

    }
    
    /*
     * Implement some test which are tested before the usecase will e executed
     */
    public boolean checkPreconditions() throws UsecaseException {
        return true;
    }
    
    public String getDataPath () throws Exception {
        return resToEdit.getDataPath();
    }
    
    public int getImageCropX() throws Exception {
        Crop crop = new Crop(resToEdit);
        return crop.getCropX();
    }
    
    public int getImageCropY() throws Exception {
        Crop crop = new Crop(resToEdit);
        return crop.getCropY();
    }
    
    public int getImageCropW() throws Exception {
        Crop crop = new Crop(resToEdit);
        return crop.getCropW();
    }
    
    public int getImageCropH() throws Exception {
        Crop crop = new Crop(resToEdit);
        return crop.getCropH();
    }
    
    public int getImageTargetHeight() throws Exception {
        return resToEdit.getTargetHeight();
    }
    
    public int getImageTargetWidth() throws Exception {
        return resToEdit.getTargetWidth();
    }
    
    public int getImageOrigHeight() throws Exception {
        return resToEdit.getOrigHeight();
    }
    
    public int getImageOrigWidth() throws Exception {
        return resToEdit.getOrigWidth();
    }
    
    public String getWatermarkPath() throws Exception {
        return resToEdit.getWatermarkPath();
    }
    
    public String getWatermarkTransparency() throws Exception {
        return resToEdit.getWatermarkTransparency();
    }
    
    public int getWatermarkX() throws Exception {
        return resToEdit.getWatermarkX();
    }
    
    public int getWatermarkY() throws Exception {
        return resToEdit.getWatermarkY();
    }
    
    public int getWatermarkWidth() throws Exception {
        return resToEdit.getWatermarkWidth();
    }
    
    public int getWatermarkHeight() throws Exception {
        return resToEdit.getWatermarkHeight();
    }
    
    public boolean getPreserveAlpha() {
		return resToEdit.isPreserveAlpha();
	}
    
    public String getBackgroundColor() {
    	return resToEdit.getBackgroundColorAsString();
	}
    
    public boolean getAllowRequestParameters() {
    	boolean allow = false;
    	try {
    		allow =  Boolean.parseBoolean(resToEdit.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_ALLOW_REQUEST_PARAMETERS));
		} catch (Exception e) {
			log.debug("Could not read resource type configuration protperty ImageResource.CONFIG_PROPERTY_ALLOW_REQUEST_PARAMETERS (" + ImageResource.CONFIG_PROPERTY_ALLOW_REQUEST_PARAMETERS + ") falling back to false." , e);
		}
    	return allow;
	}
    
    public boolean getProhibitUpScale() {
    	boolean prohibit = true;
    	try {
    		prohibit =  Boolean.parseBoolean(resToEdit.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_PROHIBIT_UP_SCALE));
    	} catch (Exception e) {
    		log.debug("Could not read resource type configuration protperty ImageResource.CONFIG_PROPERTY_PROHIBIT_UP_SCALE (" + ImageResource.CONFIG_PROPERTY_PROHIBIT_UP_SCALE + ") falling back to false." , e);
    	}
    	return prohibit;
	}
    
    public boolean getDisableCache() {
    	boolean disable = false;
    	try {
    		disable =  Boolean.parseBoolean(resToEdit.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_DISABLE_CACHE));
    	} catch (Exception e) {
    		log.debug("Could not read resource type configuration protperty ImageResource.CONFIG_PROPERTY_DISABLE_CACHE (" + ImageResource.CONFIG_PROPERTY_DISABLE_CACHE + ") falling back to false." , e);
    	}
    	return disable;
	}
    
    public String getCacheRootPath() {
    	String cacheRootPath = "";
    	try {
    		cacheRootPath =  resToEdit.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_CACHE_ROOT_PATH);
    	} catch (Exception e) {
    		log.debug("Could not read resource type configuration protperty ImageResource.CONFIG_PROPERTY_CACHE_ROOT_PATH (" + ImageResource.CONFIG_PROPERTY_CACHE_ROOT_PATH + ") falling back to false." , e);
    	}
    	return cacheRootPath;
	}
    
    public String getYanelPath() {
    	String yanelPath = "";
    	try {
    		yanelPath =  resToEdit.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_YANEL_PATH);
    	} catch (Exception e) {
    		log.debug("Could not read resource type configuration protperty ImageResource.CONFIG_PROPERTY_YANEL_PATH (" + ImageResource.CONFIG_PROPERTY_YANEL_PATH + ") falling back to false." , e);
    	}
    	return yanelPath;
	}

    
    public String getRotation() {
    	String rotate = "0";
    	try {
    		rotate =  resToEdit.getResourceConfigProperty(ImageResource.CONFIG_PROPERTY_ROTATE);
    	} catch (Exception e) {
    		log.debug("Could not read resource type configuration protperty CONFIG_PROPERTY_ROTATE (" + ImageResource.CONFIG_PROPERTY_ROTATE + ") falling back to 0." , e);
    	}
    	if (rotate == null) return "0";
    	return rotate;
	}
    
    public String getEditPath() {
        return editPath;	
    }

    public String getFilename(String path) {
    	return path.substring(path.lastIndexOf("/") + 1, path.length());	
    }
    
    public String getPath(String path) {
    	return path.substring(0, path.lastIndexOf("/") + 1);	
    }
    
    protected void createImage(HttpRequest request) {
    	if (getEnvironment().getRequest() instanceof HttpRequest) {
            if (request.isMultipartRequest()) {
            	try {
            		String parameterCreatePath = getParameterAsString(PARAMETER_CREATE_PATH);
            		String parameterCreateFilename = getParameterAsString(PARAMETER_CREATE_FILENAME);
            		
            		Enumeration<?> parameters = request.getFileNames();
            		if (parameters.hasMoreElements()) {
            			String name = (String) parameters.nextElement();
            			String fileName = request.getFilesystemName(name).replaceAll("'", "");
            			if (log.isInfoEnabled()) log.info("uploading file: " + fileName);
            			
            			// do a rough security validation on the file name before creating the asset
            			if (fileName == null || fileName.length() <= 0 || fileName.length() > 100) {
            				log.warn("ImageUtilResource, not uploading file: " + fileName);
            			} else {
            				if (log.isDebugEnabled()) {
            					log.debug("uploading: " + fileName);
            				}
            				InputStream is = request.getInputStream(name);
            				String uploadMimeType = request.getContentType(name);
            				if ( parameterCreateFilename != null && parameterCreateFilename.length() > 1) {
            					fileName = parameterCreateFilename;
            				}
            				Node node = YarepUtil.addNodes(getRealm().getRepository(), parameterCreatePath + fileName, NodeType.RESOURCE);
            				node.setMimeType(uploadMimeType);
            				OutputStream os = node.getOutputStream();
            				IOUtils.copy(is, os);
            				os.close();
            				createResourceConfiguration(parameterCreatePath + fileName);
            			}
            		}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
            }
        }
    }
    
    /**
     * Create resource configuration (yanel-rc)
     */
    protected void createResourceConfiguration(String path) throws Exception {
        // Create resource config XML
        StringBuilder rcContent = new StringBuilder("<?xml version=\"1.0\"?>\n\n");
        rcContent.append("<yanel:resource-config xmlns:yanel=\"http://www.wyona.org/yanel/rti/1.0\">\n");
        rcContent.append("<yanel:rti name=\"image\" namespace=\"http://www.litwan.com/yanel/resource/1.0\"/>\n\n");
        rcContent.append("</yanel:resource-config>");


        // Save resource config
        org.wyona.yarep.core.Repository rcRepo = realm.getRTIRepository();
        String newRCPath = PathUtil.getRCPath(path);
        if (log.isDebugEnabled()) log.debug(newRCPath);
        YarepUtil.addNodes(rcRepo, newRCPath, org.wyona.yarep.core.NodeType.RESOURCE);

        java.io.Writer writer = new java.io.OutputStreamWriter(rcRepo.getNode(newRCPath.toString()).getOutputStream());
        writer.write(rcContent.toString());
        writer.close();
    }    
}
