/*
 * Copyright 2011 litwan
 */

package com.litwan.yanel.impl.resources.imagegallery;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;

import org.apache.log4j.Logger;
import org.wyona.security.core.AuthorizationException;
import org.wyona.yanel.core.attributes.viewable.View;
import org.wyona.yanel.core.util.PathUtil;
import org.wyona.yanel.impl.resources.usecase.UsecaseException;

import com.litwan.yanel.impl.resources.imagegallery.model.Image;
import com.litwan.yanel.impl.resources.imagegallery.model.Key;


/**
 * A simple usecase which is based on ExecutableUsecaseResource
 */
public class ImageGalleryResource extends ImageGalleryCUDResource {
    
private static Logger log = Logger.getLogger(ImageGalleryResource.class);
//    private static final int NUMBER_OF_DISPALYED_ITEMS = 10;
    
//    protected void init() throws UsecaseException {
//        try {
//        	Serializer serializer = new Persister();
//        	Repository repo = getRealm().getRepository();
//        	Gallery gallery = serializer.read(Gallery.class, repo.getNode(getResourceConfigProperty("yanel-path")).getInputStream());
//            images = gallery.getImages();
//        } catch (Exception e) {
//            log.warn(e, e);
//        }
//
//    }
    
    
    protected View processUsecase(String viewID) throws UsecaseException {
    	String uscaseParameter = getParameterAsString(REQUEST_PARAMTER_USECASE);
    	boolean authorized = isAuthorized();
		if (authorized) {
			if (uscaseParameter != null ) {
				return super.processUsecase(viewID);
			}
		}
		if (!modelXmlExists()) {
            View view = new View();
            view.setResponse(false); // this resource writes the response itself
            HttpServletResponse response = getEnvironment().getResponse();
            response.setStatus(404);
            return view;
		}
    	int currentPosition = getCurrentPosition();
		int numberOfImages = getNumberOfImages();
		if (currentPosition > numberOfImages && modelXmlExists()) {
            View view = new View();
            view.setResponse(false); // this resource writes the response itself
            String redirectPath = PathUtil.getParent(getPath()) + numberOfImages + "." + PathUtil.getSuffix(getPath());
            if (redirectPath.startsWith("/")) {
            	redirectPath = redirectPath.substring(1);
            }
            String redirectURL = PathUtil.backToRealm(getPath()) + redirectPath;
            HttpServletResponse response = getEnvironment().getResponse();
            response.setStatus(307);
            response.setHeader("Location", redirectURL);
            return view;
    	}
        return generateView(viewID);
    }

	private boolean isAuthorized() {
		boolean authorized = false;
		try {
			authorized = getRealm().getPolicyManager().authorize(getPath(), getEnvironment().getIdentity(), new org.wyona.security.core.api.Usecase("write"));
		} catch (AuthorizationException e) {
			log.warn(e.getMessage(),e);
		}
		return authorized;
	}    
    
    public int getNumberOfImages() {
        try {
            return images.size();
        } catch (Exception e) {
            log.warn(e, e);
            return 0;
        }
    }
    
    public int getCurrentPosition() {
    	if (getNumberOfImages() == 0) {
			return 0;
		}
    	int position = 1;
        String pathName = PathUtil.getName(getPath());
        try {
            String pathNameWithoutSuffix = pathName.split("\\.")[0];
            int currentPosition = Integer.parseInt(pathNameWithoutSuffix);
            position = currentPosition;
        } catch (Exception e) {
            log.warn(e, e);
        }
        if (position < 1) {
        	position = 1;
        }
        return position;
    }
    
    
    public Image getImageByPosition(String position) {
    	return images.get(Integer.parseInt(position) - 1);
    }
    
    public Image getCurrentImage() {
    	return images.get(getCurrentPosition() - 1);
    }
    
    public String getCurrentImagePath() {
        try {
            Image image = getCurrentImage();
            return image.getSrc();
        } catch (Exception e) {
            log.warn(e,e);
            return null;
        }
    }

    public String getCurrentText() {
        try {
        	Image image = getCurrentImage();
            String contentLanguage = getContentLanguage();
            List<Key> keys = image.getKeys();
            Iterator<Key> it = keys.iterator();
            while (it.hasNext()) {
				Key key = (Key) it.next();
				if (key.getLang().equals(contentLanguage)) {
					return key.getText();
				}
			}
        } catch (Exception e) {
            log.warn(e,e);
        }
        return null;
    }
    
    public String getText(int position, String lang) {
    	try {
    		Image image = images.get(position);
    		List<Key> keys = image.getKeys();
    		Iterator<Key> it = keys.iterator();
    		while (it.hasNext()) {
    			Key key = (Key) it.next();
    			if (key.getLang().equals(lang)) {
    				return key.getText();
    			}
    		}
    	} catch (Exception e) {
    		log.warn(e,e);
    	}
    	return null;
    }
//    
//    public List<Image> getImages() {
//		return images;
//	}
    
/*    
 * TODO it should not fail if there is no file under the path of getTextPath() 
 * 
 * public String getText() {
        String textPath = "yanelresource:" + getTextPath();
        if (textPath != null) {
           try {
               SourceResolver uriResolver = new SourceResolver(this);
               Source textSource = uriResolver.resolve(textPath, textPath);
               textSource.
               
               ViewableV1 textResource = (ViewableV1) getYanel().getResourceManager().getResource(getEnvironment(), getRealm(), textPath);
               textResource.
           } catch (Exception e) {
               log.warn(e,e);
               return null;
           }
        }
    }*/
    
//    public Collection<String> getItems() {
//        int currentPosition = getCurrentPosition();
////        long x = (currentPosition / (NUMBER_OF_ITEMS -1)) * 10;
////        float test = x * 10;
//////        int startPosition = (Math.round(x * 10)) + 1;
////        int startPosition = new Double(x).intValue() + 1;
////        if(hasLess()){
////            startPosition = startPosition - 2;
////        }
////        if (currentPosition > numberOfItems) {
////            numberOfItems = numberOfItems - 1;
////        } else {
////            startPosition = Math.abs((currentPosition / numberOfItems)) * 10 + 1;
////        }
//        int startPosition = getCurrentPosition() - ((NUMBER_OF_DISPALYED_ITEMS / 2)-1);
//        if (startPosition < 1) {
//            startPosition = 1;
//        }
//        ArrayList<String> items = new ArrayList<String>();
//
////        int i = 0;
////        for (Iterator iterator = images.iterator(); iterator.hasNext();) {
////            i++;
////            Image image = (Image) iterator.next();
////            if (i <= startPosition + numberOfItems  || i <= getNumberOfImages()) {
////                
////            }
////        }
////        int i;
//        for (int i = startPosition; i <= (startPosition -1) + NUMBER_OF_DISPALYED_ITEMS  && i <= getNumberOfImages(); i++) {
//            items.add(String.valueOf(i));
//        }
//        return items;
//    }
    
//    public boolean hasMore(){
//       int numberOfImages = getNumberOfImages();
//       int currentPosition = getCurrentPosition();
//       Collection<String> items = getItems();
//       if(numberOfImages > currentPosition && items.size() >= NUMBER_OF_DISPALYED_ITEMS){
//           return true;
//       }
//       return false;
//    }
//    
//    public boolean hasLess(){
//        int startPosition = getCurrentPosition() - ((NUMBER_OF_DISPALYED_ITEMS / 2)-1);
//        if (startPosition <= 1) {
//            return false;
//        }
//        return true;
//    }
    
    /**
     * Pass parameters to xslt transformer.
     * @param transformer
     * @throws Exception
     */
    protected void passTransformerParameters(Transformer transformer) throws Exception {      
        transformer.setParameter("yanel.path.parent", org.wyona.commons.io.PathUtil.getParent(getPath()));
        transformer.setParameter("session.id", getEnvironment().getRequest().getSession().getId());
        transformer.setParameter("current.position", getCurrentPosition());
        // Set general parameters
        super.passTransformerParameters(transformer);
    }    
}
