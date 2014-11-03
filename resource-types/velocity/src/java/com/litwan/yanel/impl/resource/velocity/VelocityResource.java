/*
 * Copyright 2011 litwan
 */

package com.litwan.yanel.impl.resource.velocity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.wyona.security.core.api.Identity;
import org.wyona.yanel.core.Resource;
import org.wyona.yanel.core.api.attributes.ViewableV2;
import org.wyona.yanel.core.attributes.viewable.View;
import org.wyona.yanel.core.attributes.viewable.ViewDescriptor;
import org.wyona.yanel.core.i18n.MessageManager;
import org.wyona.yanel.core.i18n.MessageProvider;
import org.wyona.yanel.core.i18n.MessageProviderFactory;
import org.wyona.yanel.core.source.SourceResolver;
import org.wyona.yanel.core.util.PathUtil;
import org.wyona.yanel.impl.resources.usecase.UsecaseException;
import org.wyona.yarep.core.Repository;

/**
 *
 */
public class VelocityResource extends Resource implements ViewableV2  {
    
    private static Logger log = Logger.getLogger(VelocityResource.class);
    
    private static final String RC_PROP_MIME_TYPE = "mime-type";
    
    public static final String RC_PROP_TEAMPLATE = "template";
    
    public static final String DEFAULT_VIEW_ID = "default";
    
    public static final String SOURCE_VIEW_ID = "source";
    

    @Override
    public View getView(String viewId) throws UsecaseException {
        if (viewId == null || viewId.length() == 0) {
            viewId = DEFAULT_VIEW_ID;
        }
        try {
            View view = new View();
            String mimeType = getMimeType(viewId);
            view.setMimeType(mimeType);

            String viewTemplateURI = getPath();

            if (viewId != null && viewId.equals(SOURCE_VIEW_ID)) {
                view.setInputStream(getRealm().getRepository().getNode(viewTemplateURI).getInputStream());
                return view;
            }
            InputStream velocityInputStream = getVelocityInputStream(viewId, viewTemplateURI);
            StringWriter errorWriter = new StringWriter();
            try {
                view.setInputStream(velocityInputStream);
                return view;
            } catch (Exception e) {
                log.error(e, e);
                log.error(e + " (" + getPath() + ", " + getRealm() + ")", e);
                String errorMsg;
                String transformationError = errorWriter.toString();
                if (transformationError != null) {
                    errorMsg = "Transformation error:\n" + transformationError;
                    log.error(errorMsg);
                } else {
                    errorMsg = e.getMessage();
                }
                throw new Exception(errorMsg);
            }
        } catch (Exception e) {
            String errorMsg = "Error generating view '" + viewId + "' of usecase resource: " + getPath() + ": " + e;
            log.error(errorMsg, e);
            throw new UsecaseException(errorMsg, e);
        }
    }
    
    @Override
    public ViewDescriptor[] getViewDescriptors() {
        try {
            ViewDescriptor[] vd = new ViewDescriptor[2];
            vd[0] = new ViewDescriptor(DEFAULT_VIEW_ID);
            vd[0].setMimeType(getMimeType(DEFAULT_VIEW_ID));
            vd[1] = new ViewDescriptor(SOURCE_VIEW_ID);
            vd[1].setMimeType(getMimeType(SOURCE_VIEW_ID));
            return vd;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean exists() throws Exception {
        return getRealm().getRepository().existsNode(getPath());
    }

    @Override
    public long getSize() throws Exception {
        return -1;
    }    
    
    public String getPath() {
        String viewTemplateURI;
        try {
            viewTemplateURI = getResourceConfigProperty(RC_PROP_TEAMPLATE);
        } catch (Exception e) {
            return super.getPath();
        }
        if (viewTemplateURI == null || viewTemplateURI.length() < 1) {
            return super.getPath();
        }
        return viewTemplateURI;
    }    
    
    private InputStream getVelocityInputStream(String viewId, String viewTemplate) throws UsecaseException {
        try {
            if (viewTemplate  == null || viewTemplate.length() < 1) {
                viewTemplate = getPath();
            }
            if (log.isDebugEnabled()) log.debug("viewTemplate: "+viewTemplate);
            Repository repo = getRealm().getRepository();
            
            InputStream templateInputStream = null;
            if (viewTemplate.startsWith("/")) {
                if (log.isDebugEnabled()) log.debug("Accessing view template directly from the repo (no protocol specified). View Template: " + viewTemplate);
                // for backwards compatibility. when not using a protocol
                templateInputStream = repo.getNode(viewTemplate).getInputStream();
            } else {
                if (log.isDebugEnabled()) log.debug("Accessing view template through the source-resolver (protocol specified). View Template: " + viewTemplate);
                SourceResolver resolver = new SourceResolver(this);
                Source templateSource = resolver.resolve(viewTemplate, null);
                templateInputStream = ((StreamSource)templateSource).getInputStream();
            }
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Writer w = new OutputStreamWriter(out);
            InputStreamReader isR = new InputStreamReader(templateInputStream);
            Velocity.setProperty( Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute" );
            String loggerName = log.getName();
            
            Velocity.setProperty("runtime.log.logsystem.log4j.logger", loggerName);
            Velocity.init();
            VelocityContext context = getContext();            
            Velocity.evaluate( context, w, "LOG", isR);
            w.flush();
            w.close();
            
            byte[] byteArray = out.toByteArray();
            
            return new ByteArrayInputStream(byteArray);
        } catch (MethodInvocationException e1) {
            log.error(e1.getMessage(), e1);
            String errorMsg = "Error creating 'velocity' view '" + viewId + "' of usecase resource: " + getPath() + ": " + e1;
            throw new UsecaseException(errorMsg, e1);
        } catch (ParseErrorException e2) {
            log.error(e2.getMessage(), e2);
            String errorMsg = "Error creating 'velocity' view '" + viewId + "' of usecase resource: " + getPath() + ": " + e2;
            throw new UsecaseException(errorMsg, e2);
        } catch (Exception e3) {
            String errorMsg = "Error creating 'velocity' view '" + viewId + "' of usecase resource: " + getPath() + ": " + e3;
            log.error(errorMsg, e3);
            throw new UsecaseException(errorMsg, e3);
        }
    }
    
    public String i18n(String key) {
        try {
            SourceResolver uriResolver = new SourceResolver(this);
            String[] i18nCatalogueNames = getI18NCatalogueNames();
            Locale locale = new Locale(getUserLanguage());
            Locale defaultLocale = new Locale(getRealm().getDefaultLanguage());
            MessageManager messageManager = new MessageManager(defaultLocale);
            for (int i = 0; i < i18nCatalogueNames.length; i++) {
                MessageProvider messageProvider = MessageProviderFactory.getMessageProvider(i18nCatalogueNames[i], uriResolver); 
                messageManager.addMessageProvider("catalogue-" + i, messageProvider);
            }        
            //set key as default text
            String text = messageManager.getText(key, locale, key);
            return text;
        } catch (Exception e) {
            log.error("Could not translate Text:" + e.getMessage(), e);
            return "";
        }
    }
 
    /**
     * Gets the names of the i18n message catalogues used for the i18n transformation.
     * Uses the following priorization:
     * 1. rc config properties named 'i18n-catalogue'.
     * 2. realm i18n-catalogue 
     * 3. 'global'
     * @return i18n catalogue name
     */
    protected String[] getI18NCatalogueNames() throws Exception {
        ArrayList<String> catalogues = new ArrayList<String>();
        String[] rcCatalogues = getResourceConfigProperties("i18n-catalogue");
        if (rcCatalogues != null) {
            for (int i = 0; i < rcCatalogues.length; i++) {
                catalogues.add(rcCatalogues[i]);
            }
        }
        String realmCatalogue = getRealm().getI18nCatalogue();
        if (realmCatalogue != null) {
            catalogues.add(realmCatalogue);
        }
        catalogues.add("global");
        return catalogues.toArray(new String[catalogues.size()]);
    }
    
    /**
     * Get user language (order: profile, browser, ...)
     */
    private String getUserLanguage() throws Exception {
        Identity identity = getEnvironment().getIdentity();
        String language = getRequestedLanguage();
        String userID = identity.getUsername();
        if (userID != null) {
            String userLanguage = getRealm().getIdentityManager().getUserManager().getUser(userID).getLanguage();
            //log.debug("User language: " + userLanguage);
            if(userLanguage != null) {
                language = userLanguage;
                log.debug("Use user profile language: " + language);
            } else {
                log.debug("Use requested language: " + language);
            }
        }
        return language;
    }
    
    /**
     * @see org.wyona.yanel.core.api.attributes.ViewableV2#getMimeType(java.lang.String)
     */
    public String getMimeType(String viewId) throws Exception {
        String mimeType = null;
        if (mimeType == null) {
            mimeType = this.getResourceConfigProperty(RC_PROP_MIME_TYPE);
        }
        if (mimeType == null) {
            String suffix = org.wyona.commons.io.PathUtil.getSuffix(getPath());
            if (suffix != null) {
                log.debug("SUFFIX: " + suffix);
                mimeType = getMimeTypeBySuffix(suffix);
            } else {
                log.warn("mime-type will be set to application/octet-stream, because no suffix for " + getPath());
                mimeType = "application/octet-stream";
            }
        }
        //log.debug("Mime type: " + mimeType + ", " + viewId);
        return mimeType;
    }    

    protected VelocityContext getContext() {
        VelocityContext context = new VelocityContext();            
        context.put("resource", this);
        context.put("r", this);
        context.put("request", getEnvironment().getRequest());
        context.put("yanelback2context", PathUtil.backToContext(realm, getPath()));
        context.put("yanel.back2realm", PathUtil.backToRealm(getPath()));
        context.put("yanel.globalHtdocsPath", PathUtil.getGlobalHtdocsPath(this));
        context.put("yanel.resourcesHtdocsPath", PathUtil.getResourcesHtdocsPathURLencoded(this));
        context.put("yanel.reservedPrefix", this.getYanel().getReservedPrefix());
        return context;
    }
    
    
    private String getMimeTypeBySuffix(String suffix) {
        // TODO: use MimeTypeUtil
        if (suffix.equals("html")) {
            return "text/html";
        } else if (suffix.equals("htm")) {
            return "text/html";
        } else if (suffix.equals("xhtml")) {
            return "application/xhtml+xml";
        } else if (suffix.equals("xml")) {
            return "application/xml";
        } else if (suffix.equals("xsd")) {
            return "application/xml";
            // TODO: Clarify ...
            //return "application/xsd+xml";
        } else if (suffix.equals("xsl")) {
            return "application/xml";
            // TODO: Clarify ...
            //return "application/xslt+xml";
        } else if (suffix.equals("css")) {
            return "text/css";
        } else if (suffix.equals("js")) {
            return "application/x-javascript";
        } else if (suffix.equals("svg")) {
            return "image/svg+xml";
        } else {
            log.warn("Could not determine mime-type from suffix '" + suffix + "' (path: " + getPath() + "). Return application/octet-stream!");
            return "application/octet-stream";
        }
    }    
}
