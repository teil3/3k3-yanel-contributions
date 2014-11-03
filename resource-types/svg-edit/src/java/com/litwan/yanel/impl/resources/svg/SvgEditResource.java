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
import java.io.OutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.wyona.yanel.core.Resource;
import org.wyona.yanel.core.api.attributes.ModifiableV2;
import org.wyona.yanel.core.api.attributes.VersionableV2;
import org.wyona.yanel.core.api.attributes.ViewableV2;
import org.wyona.yanel.core.attributes.viewable.View;
import org.wyona.yanel.core.util.PathUtil;
import org.wyona.yanel.core.util.ResourceAttributeHelper;
import org.wyona.yanel.impl.resources.usecase.ExecutableUsecaseResource;
import org.wyona.yanel.impl.resources.usecase.UsecaseException;



/**
 * A simple usecase which is based on ExecutableUsecaseResource
 */
public class SvgEditResource extends ExecutableUsecaseResource {
    
    private static Logger log = Logger.getLogger(SvgEditResource.class);
    
//    private static final String PARAMETER_EDIT_PATH = "yanel.resource.svg-edit.edit-path";
    private static final String PARAMETER_EDITOR_CONTENT = "yanel.resource.svg-edit.editor-content";
    private static final String PARAMETER_CONTINUE_PATH = "yanel.resource.svg-edit.continue-path";
    private static final String CONFIG_PROPERTY_MATCHER_EXTENSION = "matcher-extension";
    
    private String editorContent;
    private String resourceContent;
    private String editPath;
    private String contentToEdit;
    private Resource resToEdit;

    /* (non-Javadoc)
     * @see org.wyona.yanel.impl.resources.usecase.UsecaseResource#init()
     */
    protected void init() throws UsecaseException {
        String matcherExtension = "";
        try {
            matcherExtension = getResourceConfigProperty(CONFIG_PROPERTY_MATCHER_EXTENSION);
        } catch (Exception e) {
            log.error("Could not get Resource Configuration Property matcher-extension");
        }
        String path = getPath();
        editPath = getPath().substring(0, path.length() - matcherExtension.length());
        if (editPath == null || editPath.equals("")) {
            addError("Could not get paramter edit-path. Don't know what to edit.");
            return;
        }
        try {
            resToEdit = getYanel().getResourceManager().getResource(getEnvironment(), getRealm(), editPath);
        } catch (Exception e) {
            log.error("Exception: " + e);
        }
        if (resToEdit == null) {
            addError("Could not get Resource-Type to edit.");
            return;
        }
        String parameterEditorContent = getParameterAsString(PARAMETER_EDITOR_CONTENT);
        if(parameterEditorContent != null) {
            editorContent = new String(Base64.decodeBase64(parameterEditorContent.getBytes()));; 
        }
        if (ResourceAttributeHelper.hasAttributeImplemented(resToEdit, "Modifiable", "2")) {
            try {
                InputStream is = ((ModifiableV2) resToEdit).getInputStream();
                resourceContent = IOUtils.toString(is);
            } catch (Exception e) {
                log.error("Exception: " + e);
                addError("Could not get Resource-Type content.");
            }
        } else {
            addError("This resource can not be edited. ");
        }
        
    }
    
    /* (non-Javadoc)
     * @see org.wyona.yanel.impl.resources.usecase.ExecutableUsecaseResource#processUsecase(java.lang.String)
     */
    protected View processUsecase(String viewID) throws UsecaseException {
        String userID = getEnvironment().getIdentity().getUsername();
        String checkoutUserID = getResToEditCheckoutUserID();
        String resourceContent = getResourceContent();
        
        if (getParameter(PARAM_CANCEL) != null) {
            cancel();
            return generateView(VIEW_DONE);
        } 
        if (hasErrors() || !checkPreconditions()) {
            String editPath =  getEditPath();
            String continuePath;
            String referer = getEnvironment().getRequest().getHeader("referer");
            if (editPath != null && editPath.length() > 1) {
                continuePath = PathUtil.backToRealm(getPath()) + editPath.substring(1);
            } else if (referer != null)  {
                continuePath = referer;
            } else {
                continuePath = PathUtil.backToRealm(getPath());
            }
            setParameter(PARAMETER_CONTINUE_PATH, continuePath);
            return generateView(VIEW_CANCEL);
        }
        if (isResToEditCheckedOut() && !(getParameter(PARAM_SUBMIT) != null)) {
            addError("Resource is checked out ");
            if (checkoutUserID != null) {
                if(checkoutUserID.equals(userID)) {
                    addError("by you (User: " + userID + ")! ");
                } else if (!checkoutUserID.equals("null")) {
                    addError("by user: " + checkoutUserID + " ");
                    return generateView(VIEW_CANCEL);
                } else if (checkoutUserID.equals("null")) {
                    addError("by a not loged in user. ");
                }
            }
        }
        if (getParameter(PARAM_SUBMIT) != null) {
            execute();
            return generateView(VIEW_DONE);
        }         
        contentToEdit = resourceContent;
        try {
            if (isResToEditVersionableV2() && !isResToEditCheckedOut()) {
                VersionableV2 versionable = (VersionableV2) getResToEdit();
                if (!versionable.isCheckedOut()) {
                    versionable.checkout(userID);
                }
            }
        } catch (Exception e) {
            log.warn("Could not checkout resource: " + getResToEdit().getPath() + " " + e.getMessage());
        }
        return generateView(viewID); // this will show the default view if the param is not set
    }
    
    /* (non-Javadoc)
     * @see org.wyona.yanel.impl.resources.usecase.ExecutableUsecaseResource#checkPreconditions()
     */
    public boolean checkPreconditions() throws UsecaseException {
        Resource resToEdit = getResToEdit();
        if (!ResourceAttributeHelper.hasAttributeImplemented(resToEdit, "Modifiable", "2")) {
            addError("The resource you wanted to edit does not implement ModifiableV2 and is therefor not editable with this editor. ");
            return false;
        }
        if (ResourceAttributeHelper.hasAttributeImplemented(resToEdit, "Viewable", "2")) {
            try {
                View view = ((ViewableV2)resToEdit).getView(DEFAULT_VIEW_ID);
                if (!view.getMimeType().contains("svg")) {
                    addError("Mime-Type not supported: " + view.getMimeType() + ". Only edit svg documents with svg-edit. ");
                    return false;
                }
            } catch (Exception e) {
                addError("Could not find out mime-type. " + e.getMessage() + " ");
                return false;
            }
        } else {
            addError("Could not find out mime-type. ");
            return false;
        }
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.wyona.yanel.impl.resources.usecase.ExecutableUsecaseResource#execute()
     */
    public void execute() throws UsecaseException {
        final String content = getEditorContent();
        final Resource resToEdit = getResToEdit();
        if (log.isDebugEnabled()) log.debug("saving content: " + content);
            if (ResourceAttributeHelper.hasAttributeImplemented(resToEdit, "Modifiable", "2")) {
                try {
                    OutputStream os = ((ModifiableV2) resToEdit).getOutputStream();
                    IOUtils.write(content, os);
                    addInfoMessage("Succesfully saved resource " + resToEdit.getPath() + ". ");
                    if (isResToEditVersionableV2()) {
                        VersionableV2 versionable  = (VersionableV2)resToEdit;
                        try {
                            versionable.checkin("Updated with svg-edit");
                            addInfoMessage("Succesfully checked in resource " + resToEdit.getPath() + ". ");
                        } catch (Exception e) {
                            String msg = "Could not check in resource: " + resToEdit.getPath() + " " + e.getMessage() + ". ";
                            log.error(msg, e);
                            addError(msg);
                            throw new UsecaseException(msg, e);
                        }
                    }
                } catch (Exception e) {
                    log.error("Exception: " + e);
                    throw new UsecaseException(e.getMessage(), e);
                }
            } else {
                addError("Could not save the document. ");
            }
            setParameter(PARAMETER_CONTINUE_PATH, PathUtil.backToRealm(getPath()) + getEditPath().substring(1)); // allow jelly template to show link to new event
    }
    
    /* (non-Javadoc)
     * @see org.wyona.yanel.impl.resources.usecase.ExecutableUsecaseResource#cancel()
     */
    public void cancel() throws UsecaseException {
        addInfoMessage("Canceled. ");
        final Resource resToEdit = getResToEdit();
        if (isResToEditVersionableV2()) {
            VersionableV2 versionable  = (VersionableV2)resToEdit;
            try {
                versionable.cancelCheckout();
                addInfoMessage("Released lock for: " + resToEdit.getPath() + ". ");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                addInfoMessage("Releasing of lock failed because of: " + resToEdit.getPath() 
                        + " " + e.getMessage() + ". ");
            }
        }
        setParameter(PARAMETER_CONTINUE_PATH, PathUtil.backToRealm(getPath()) + getEditPath().substring(1)); // allow jelly template to show link to new event
    }
    
    /**
     * Get the String containing the path to the resource which is going to be edited
     * @return String
     */
    public String getEditPath() {
        return editPath;
    }

    /**
     * Get the content proposed to edit
     * used by svg-edit.jelly
     * @return String
     */
    public String getContentToEdit() {
        return contentToEdit;
    }

    /**
     * escape xml
     * @param String to escape
     * @return String escaped
     */
    public String encodeBase64(String string) {
        return new String(Base64.encodeBase64(string.getBytes()));
        
    }
    

    /**
     * Checks if InputStream is wellformed
     * @return boolean true if wellformed, false if not
     * @param InputStream which is checked if wellformed
     * @throws UsecaseException
     */
    private boolean isWellformed(InputStream is) throws UsecaseException {
        try {
            //TODO: code borrowed from YanelServlet.java r40436. see line 902. 1. maybe there is a better way to do so. 2. this code could maybe be refactored into a some xml.util lib. 
            javax.xml.parsers.DocumentBuilderFactory dbf= javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder parser = dbf.newDocumentBuilder();
            // NOTE: DOCTYPE is being resolved/retrieved (e.g. xhtml schema from w3.org) also
            //       if isValidating is set to false.
            //       Hence, for performance and network reasons we use a local catalog ...
            //       Also see http://www.xml.com/pub/a/2004/03/03/catalogs.html
            //       resp. http://xml.apache.org/commons/components/resolver/
            // TODO: What about a resolver factory?
            parser.setEntityResolver(new CatalogResolver());
            parser.parse(is);
            return true;
        } catch (org.xml.sax.SAXException e) {
            addError("Document is not wellformed: " + e.getMessage() + " ");
            return false;
        } catch (Exception e) {
            addError(e.getMessage());
            return false;
        }
    }
    
    /**
     * Get the String with the content of the resource which is going to be edited
     * @return String 
     */
    private String getResourceContent() throws UsecaseException {
        return resourceContent;
    }
    
    /**
     * Get the String with the content of the editor
     * @return String 
     */
    private String getEditorContent() {
        return editorContent;
    }
    
    /**
     * Get the Resource which is going to be edited
     * @return Resource
     */
    private Resource getResToEdit() throws UsecaseException {
        return resToEdit;
    }

    private boolean isResToEditVersionableV2() {
        try {
            if (ResourceAttributeHelper.hasAttributeImplemented(getResToEdit(), "Versionable", "2")) {
                return true;     
            }
        } catch (Exception e) {
            return false;     
        }
        return false;     
    }
    
    private boolean isResToEditCheckedOut()  {
        try {
            if (isResToEditVersionableV2()) {
                VersionableV2 versionable = (VersionableV2) getResToEdit();
                if (versionable.isCheckedOut()) {
                    return true;
                }
            }     
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    
    /**
     * Returns the user id which was supplied when calling checkout(). can be null if not known, or the resource doesn't implement VersionableV2, or resource is not checked out yet. or no way to find out.
     * @return String
     */
    private String getResToEditCheckoutUserID() {
        try {
            if (isResToEditVersionableV2() && isResToEditCheckedOut()) {
                final Resource resToEdit = getResToEdit();
                VersionableV2 versionable = (VersionableV2)resToEdit;
                if (versionable.isCheckedOut()) {
                    return versionable.getCheckoutUserID();
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
