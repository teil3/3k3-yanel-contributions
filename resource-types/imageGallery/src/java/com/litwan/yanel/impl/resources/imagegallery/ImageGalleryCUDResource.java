/*
 * Copyright 2011 litwan
 */

package com.litwan.yanel.impl.resources.imagegallery;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.id.uuid.UUID;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.wyona.yanel.core.attributes.viewable.View;
import org.wyona.yanel.core.source.SourceException;
import org.wyona.yanel.core.source.SourceResolver;
import org.wyona.yanel.core.util.PathUtil;
import org.wyona.yanel.impl.resources.usecase.ExecutableUsecaseResource;
import org.wyona.yanel.impl.resources.usecase.UsecaseException;
import org.wyona.yanel.impl.resources.usecase.UsecaseResource;
import org.wyona.yanel.impl.resources.xml.ConfigurableViewDescriptor;
import org.wyona.yanel.servlet.communication.HttpRequest;
import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.NodeType;
import org.wyona.yarep.core.Repository;
import org.wyona.yarep.util.YarepUtil;

import com.litwan.yanel.impl.resources.imagegallery.model.Gallery;
import com.litwan.yanel.impl.resources.imagegallery.model.Image;
import com.litwan.yanel.impl.resources.imagegallery.model.Key;


/**
 * create, update, delete usecase for visualKeyResource which is based on ExecutableUsecaseResource
 */
public class ImageGalleryCUDResource extends UsecaseResource {
    
    protected static final String USECASE_ADD_KEY = "add-key";
    protected static final String USECASE_CREATE_IMAGE = "create-image";
    protected static final String USECASE_CREATE_GALLERY = "create-gallery";
    protected static final String USECASE_NEW_GALLERY = "new-gallery";
    protected static final String USECASE_TEXTEDIT = "textedit";
    protected static final String USECASE_MOVE = "move";
    protected static final String USECASE_SWAP = "swap";
    protected static final String USECASE_DOWN = "down";
    protected static final String USECASE_UP = "up";
    protected static final String USECASE_DELETE = "delete";
    protected static final String REQUEST_PARAMTER_USECASE = "resource.imagegallery.usecase";
    protected static final String REQUEST_PARAMTER_RC_TEMPLATE_PATH = "rc-tmplate-path";
    protected static final String RC_PROPERTY_YANEL_PATH = "yanel-path";
    protected static final String RC_PROPERTY_XML_FILENAME = "xml-filename";
    protected static final String RC_PROPERTY_IMAGES_BASE_PATH = "images-base-path";
    protected static final String DEFAULT_XML_FILENAME = "gallery.xml";
//    protected static final String DEFAULT_IMAGES_BASE_PATH = "/images/gallery/";
    protected static final String CONTINUING_PATH = "continuing-path";
	protected static final String VIEW_ID_CREATE_GALLERY = "create-gallery";
    protected List<Image> images = new ArrayList<Image>();
    protected Gallery gallery = new Gallery();
    private static Logger log = Logger.getLogger(ImageGalleryCUDResource.class);
    
    
    protected void init() throws UsecaseException {
        try {
        	gallery.setImages(images);
            Serializer serializer = new Persister();
            Repository repo = getRealm().getRepository();
            gallery = serializer.read(Gallery.class, repo.getNode(getModelXmlPath()).getInputStream());
            images = gallery.getImages();
        } catch (Exception e) {
            log.warn(e, e);
        }
    }
    
    public List<Image> getImages() {
        return images;
    }
    
    protected View processUsecase(String viewID) throws UsecaseException {
        String uscaseParameter = getParameterAsString(REQUEST_PARAMTER_USECASE);
        if (uscaseParameter != null) {
            if (uscaseParameter.equals(USECASE_DELETE)) {
                deleteImage(getParameterAsString("id"));
            }
            if (uscaseParameter.equals(USECASE_UP)) {
                moveUpImage(getParameterAsString("id"));
            }
            if (uscaseParameter.equals(USECASE_DOWN)) {
                moveDownImage(getParameterAsString("id"));
            }
            if (uscaseParameter.equals(USECASE_SWAP)) {
                swapImage(Integer.parseInt(getParameterAsString("position1")), Integer.parseInt(getParameterAsString("position2")));
            }
            if (uscaseParameter.equals(USECASE_MOVE)) {
                moveImage(Integer.parseInt(getParameterAsString("fromPosition")), Integer.parseInt(getParameterAsString("toPosition")));
            }
            if (uscaseParameter.equals(USECASE_TEXTEDIT)) {
                replaceText(getParameterAsString("id"), getParameterAsString("lang"), getParameterAsString("text"));
            }
            if (uscaseParameter.equals(USECASE_NEW_GALLERY)) {
    			if (!modelXmlExists()) {
    				return generateView(VIEW_ID_CREATE_GALLERY);
    			}
            }
            if (uscaseParameter.equals(USECASE_CREATE_GALLERY)) {
            	try {
            		creatGallery(getParameterAsString("name"));
				} catch (Exception e) {
					return super.processUsecase(VIEW_ID_CREATE_GALLERY);
				}
            }
            if (uscaseParameter.equals(USECASE_CREATE_IMAGE)) {
                createImage((HttpRequest)getEnvironment().getRequest());
                try {
                    return super.processUsecase(viewID);
                } catch (Exception e) {
                    throw new UsecaseException(e.getMessage());
                }
            }
            if (uscaseParameter.equals(USECASE_ADD_KEY)) {
                addKey(getParameterAsString("id"), getParameterAsString("lang"));
            }
        }
        String continuingPath = getContinuingPath();
        if (continuingPath != null) {
            View view = new View();
            view.setResponse(false); // this resource writes the response itself
            if (continuingPath.startsWith("/")) {
                continuingPath = continuingPath.substring(1);
            }            
            if (!continuingPath.startsWith("http://")) {
                
                continuingPath = PathUtil.backToRealm(getPath()) + continuingPath;
            }
            HttpServletResponse response = getEnvironment().getResponse();
            response.setStatus(307);
            response.setHeader("Location", continuingPath);
            return view;
        }
        
        return super.processUsecase(viewID);
    }

    private void creatGallery(String name) throws Exception {
        String galleryBaseNodePath = getResourceConfigProperty("gallery-base-node");
        Repository repo = getRealm().getRepository();
        String newPath = galleryBaseNodePath + name;
        if (repo.existsNode(newPath)) {
            return;
        }
        Node node = YarepUtil.addNodes(getRealm().getRepository(), newPath + "/" + getModelXmlName(), NodeType.RESOURCE);
    }

    public String getContinuingPath() {
        String continuingPath = getParameterAsString(CONTINUING_PATH);
        if (continuingPath == null) {
            continuingPath = request.getHeader("referer");
        }
        if (continuingPath == null) {
            return null;
        }
        if (continuingPath.startsWith("/")) {
            continuingPath = continuingPath.substring(1);
        }
        return continuingPath;
    }
    
    private void saveXml() {
        Serializer serializer = new Persister();
        Repository repo;
        try {
            repo = getRealm().getRepository();
            serializer.write(gallery, repo.getNode(getModelXmlPath()).getOutputStream());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }    
    
    private void deleteImage(String id) {
        String imagePath = null;
        Repository repo = null;
        Image delImage = null;
        //remove the image from the xml
        Iterator<Image> it = images.iterator();
        while (it.hasNext()) {
            Image image = (Image) it.next();
            if (image.getId().equals(id)) {
                imagePath = image.getSrc();
                delImage = image;
            }
        }
        images.remove(delImage);
        saveXml();
        
        //remove the image node
        try {
            repo = getRealm().getRepository();
            repo.getNode(imagePath).delete();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        // delete resource config
        try {
            org.wyona.yarep.core.Repository rcRepo = realm.getRTIRepository();
            String newRCPath = PathUtil.getRCPath(imagePath);
            if (log.isDebugEnabled()) log.debug(newRCPath);
            rcRepo.getNode(imagePath).delete();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    
    private void moveUpImage(String id) {
        int position = 0; 
        //move the image up
        Iterator<Image> it = images.iterator();
        while (it.hasNext()) {
            Image image = (Image) it.next();
            if (image.getId().equals(id)) {
                break;
            }
            position++;
        }
        Collections.swap(images, position ,position -1 );
        saveXml();
    }
    
    private void moveDownImage(String id) {
        int position = 0; 
        //move the image up
        Iterator<Image> it = images.iterator();
        while (it.hasNext()) {
            Image image = (Image) it.next();
            if (image.getId().equals(id)) {
                break;
            }
            position++;
        }
        Collections.swap(images, position,position + 1);
        saveXml();
    }
    
    private void moveImage(int fromPosition, int toPosition) {
        Image image = images.get(fromPosition);
        images.remove(fromPosition);
        images.add(toPosition, image);
        saveXml();
    }
    
    private void swapImage(int position1, int position2) {
        Collections.swap(images, position1,position2);
        saveXml();
    }
    
    private void replaceText(String id, String lang, String text) {
        Iterator<Image> it = images.iterator();
        while (it.hasNext()) {
            Image image = (Image) it.next();
            if (image.getId().equals(id)) {
                Iterator<Key> keyIt = image.getKeys().iterator();
                boolean notExistst = true;
                while (keyIt.hasNext()) {
                    Key key = (Key) keyIt.next();
                    if (key.getLang().equals(lang)) {
                        key.setText(text);
                        notExistst = false;
                    }
                }
                if (notExistst) {
                    Key key = new Key();
                    key.setLang(lang);
                    key.setText(text);
                    List<Key> keys = image.getKeys();
                    keys.add(key);
                }
            }
        }
        saveXml();
    }
    
    public String getImagePath(String positionString) {
        int position = Integer.parseInt(positionString);
        return getImagePath(position);
    }
    
    public String getImagePath(int position) {
        try {
            Image image = images.get(position);
            return image.getSrc();
        } catch (Exception e) {
            log.warn(e,e);
            return null;
        }
    }
    
    private void createImage(HttpRequest request) {
        if (getEnvironment().getRequest() instanceof HttpRequest) {
            if (request.isMultipartRequest()) {
                try {
                    Enumeration<?> parameters = request.getFileNames();
                    if (parameters.hasMoreElements()) {
                        String name = (String) parameters.nextElement();
                        String fileName = request.getFilesystemName(name).replaceAll("'", "");
                        String parameterCreatePath = getImageBasePath();
                        String parameterCreateFilename = generateUUID() + fileName.substring(fileName.lastIndexOf("."), fileName.length()).toLowerCase();
                        if (log.isInfoEnabled()) log.info("uploading file: " + fileName);
                        
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
                        addImage2XML(fileName, parameterCreatePath);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private String getImageBasePath() {
        String imageBasePath = null;
        try {
            imageBasePath = getResourceConfigProperty(RC_PROPERTY_IMAGES_BASE_PATH);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        if (imageBasePath != null && imageBasePath.length() > 0) {
            return imageBasePath;
        }
        return PathUtil.getParent(getPath());
    }
    

    private String getModelXmlPath() throws Exception {
        String modelXmlPath = "";
        String rcPropXmlFilename = null;
        String rcPropYanelPath = null;
        try {
            rcPropXmlFilename = getModelXmlName();
            rcPropYanelPath = getResourceConfigProperty(RC_PROPERTY_YANEL_PATH);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        if (rcPropYanelPath != null && rcPropYanelPath.length() > 0 ) {
            modelXmlPath = rcPropYanelPath;
        } else {
        	modelXmlPath = PathUtil.getParent(getPath());
        }
        modelXmlPath = modelXmlPath + rcPropXmlFilename;
        return modelXmlPath;
    }    
    
    
    private String getModelXmlName() throws Exception {
        String rcPropXmlFilename = null;
        try {
            rcPropXmlFilename = getResourceConfigProperty(RC_PROPERTY_XML_FILENAME);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        if (rcPropXmlFilename != null && rcPropXmlFilename.length() > 0 ) {
            return rcPropXmlFilename;
        }
        return DEFAULT_XML_FILENAME;
    }    
    
    private void addImage2XML(String fileName, String createPath) {
        Key key = new Key();
        key.setLang("de");
        List<Key> keys = new ArrayList<Key>();
        keys.add(key);
        Image image = new Image();
        image.setId(generateUUID());
        image.setSrc(createPath + fileName);
        image.setKeys(keys);
        images.add(image);
        saveXml();
    }    
    
    /**
     * Create resource configuration (yanel-rc)
     */
    private void createResourceConfiguration(String path) throws Exception {
        String templatePath = null;
        String rcTemplateParamter = getParameterAsString(REQUEST_PARAMTER_RC_TEMPLATE_PATH);
        InputStream rcTemplateIS = null;
        if (rcTemplateParamter != null) {
            templatePath = rcTemplateParamter;
            if (templatePath.startsWith("/")) {
                templatePath = "yanelrepo:" + templatePath;
            }
        }
        templatePath = "rthtdocs:/img-template.yanel-rc";
        try {
            SourceResolver resolver = new SourceResolver(this);
            StreamSource source = (StreamSource)resolver.resolve(templatePath, null);
            rcTemplateIS =  source.getInputStream();
        } catch (SourceException e) {
            log.error(e.getMessage(), e);
            throw new UsecaseException(e.getMessage());
        }
        Repository rcRepo = realm.getRTIRepository();
        
        String newRCPath = PathUtil.getRCPath(path);
        if (log.isDebugEnabled()) log.debug(newRCPath);
        YarepUtil.addNodes(rcRepo, newRCPath, org.wyona.yarep.core.NodeType.RESOURCE);
        IOUtils.copy(rcTemplateIS, rcRepo.getNode(newRCPath.toString()).getOutputStream());
    } 
    
    private void addKey(String id, String lang) {
        Iterator<Image> it = images.iterator();
        while (it.hasNext()) {
            Image image = (Image) it.next();
            if (image.getId().equals(id)) {
                List<Key> keys = image.getKeys();
                Iterator<Key> keyIt = keys.iterator();
                while (keyIt.hasNext()) {
                    Key key = (Key) keyIt.next();
                    if (key.getLang().equals(lang)) {
                        return;
                    }
                }
                Key key = new Key();
                key.setLang(lang);
                keys.add(key);
            }
        }
        saveXml();
    }
    
    private static String generateUUID() {
        return UUID.randomUUID().toString();
    }    
    
    protected boolean modelXmlExists() {
        boolean exists = false;
        Repository repo;
        try {
            repo = getRealm().getRepository();
            exists = repo.existsNode(getModelXmlPath());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return exists;
    }
//    /**
//     * Allows to implement subclasses a custom view, which is executed if TYPE_CUSTOM
//     */
//    protected View renderCustomView(ConfigurableViewDescriptor viewDescriptor) throws UsecaseException {
//        try {
//            Repository repo = getRealm().getRepository();
//            InputStream is = repo.getNode(getResourceConfigProperty(RC_PROPERTY_YANEL_PATH)).getInputStream();
//            getXMLView(viewDescriptor.getId(), is);
//        } catch (Exception e) {
//            log.warn(e, e);
//        }
//        return null;
//    }
}
