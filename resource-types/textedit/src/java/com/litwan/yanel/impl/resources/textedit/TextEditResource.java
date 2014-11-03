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

package com.litwan.yanel.impl.resources.textedit;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.wyona.yanel.core.Environment;
import org.wyona.yanel.core.Resource;
import org.wyona.yanel.core.ResourceConfiguration;
import org.wyona.yanel.core.api.attributes.CreatableV2;
import org.wyona.yanel.core.api.attributes.ModifiableV2;
import org.wyona.yanel.core.api.attributes.VersionableV2;
import org.wyona.yanel.core.api.attributes.ViewableV2;
import org.wyona.yanel.core.attributes.viewable.View;
import org.wyona.yanel.core.map.Realm;
import org.wyona.yanel.core.navigation.Node;
import org.wyona.yanel.core.navigation.Sitetree;
import org.wyona.yanel.core.source.SourceResolver;
import org.wyona.yanel.core.util.PathUtil;
import org.wyona.yanel.core.util.ResourceAttributeHelper;

import org.wyona.yanel.core.attributes.viewable.ViewDescriptor;
import org.wyona.yanel.impl.resources.BasicXMLResource;
import org.wyona.yanel.impl.resources.usecase.ExecutableUsecaseResource;
import org.wyona.yanel.impl.resources.usecase.UsecaseException;
import org.wyona.yanel.impl.resources.xml.ConfigurableViewDescriptor;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.io.IOUtils;


/**
 *
 */
public class TextEditResource extends ExecutableUsecaseResource {
    
    private static Logger log = Logger.getLogger(TextEditResource.class);
    
    private static final String PARAMETER_CONTINUE_PATH = "continue-path";

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
        try {
            String matcherExtension = getResourceConfigProperty(CONFIG_PROPERTY_MATCHER_EXTENSION);
            if (matcherExtension != null && matcherExtension.length() > 0) {
                editPath = getPath().substring(0, getPath().length() - matcherExtension.length());
            } else {
                log.warn("No 'matcher-extension' resource type property configured. try to use the path.");
                editPath = getPath();
            }
        } catch(Exception e) {
            log.error(e, e);
            throw new UsecaseException(e.getMessage());
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
        editorContent = getParameterAsString(editPath); 
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
        String editorContent = getEditorContent();
        String checkoutUserID = getResToEditCheckoutUserID();
        String resourceContent = getResourceContent();
        
        if (getParameter(PARAM_CANCEL) != null) {
            cancel();
            return generateView(VIEW_CANCEL);
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
                            versionable.checkin("Updated with textEdit");
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
     * used by textEdit.jelly
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
    public String escapeXML(String string) {
        return StringEscapeUtils.escapeXml(string);
    }
    
    public String getResToEditMimeType() {
        String mimeType = null;
        try {
            mimeType = getEnvironment().getRequest().getSession().getServletContext().getMimeType(getEditPath());
        } catch (NoSuchElementException e) {
            log.warn("mimetype util could not guess the mimetype.");
        }

        if (mimeType == null) {
            try {
                mimeType = getRealm().getRepository().getNode(getEditPath()).getMimeType();
            } catch (Exception e) {
                log.error(e.getMessage(),e);
            }
        }
        return mimeType;
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
