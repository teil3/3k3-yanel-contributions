package com.litwan.yanel.servlet.menu.impl;

import org.wyona.yanel.core.Resource;
import org.wyona.yanel.core.api.attributes.ViewableV2;
import org.wyona.yanel.core.attributes.viewable.View;
import org.wyona.yanel.core.i18n.MessageManager;
import org.wyona.yanel.core.i18n.MessageProvider;
import org.wyona.yanel.core.i18n.MessageProviderFactory;
import org.wyona.yanel.core.map.Map;
import org.wyona.yanel.core.source.SourceResolver;
import org.wyona.yanel.core.util.PathUtil;
import org.wyona.yanel.core.util.ResourceAttributeHelper;

import org.wyona.yanel.servlet.menu.Menu;
import org.wyona.yanel.servlet.menu.impl.DefaultMenu;
import org.wyona.yanel.servlet.menu.impl.RevisionsWorkflowMenuItem;

import org.wyona.security.core.api.Identity;
import org.wyona.security.core.api.User;
import org.wyona.security.core.api.UserManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Locale;

import org.apache.log4j.Logger;


/**
 *
 */
public class ResourceTypeDemoMenu extends DefaultMenu {

    /**
     * Get generic file menu
     */
    public String getFileMenu(Resource resource) throws Exception {
        UserManager userManager = resource.getRealm().getIdentityManager().getUserManager();
        Identity identity = resource.getEnvironment().getIdentity();
        User user = userManager.getUser(identity.getUsername());
        StringBuilder sb = new StringBuilder();
        sb.append("<ul><li>");
        sb.append("<div id=\"yaneltoolbar_menutitle\">File</div>");
        sb.append("<ul>");
        if (user != null) {
            sb.append("<li>");
            sb.append("<a href=\"create-new-page.html?resource-type=http%3A%2F%2Fwww.wyona.org%2Fyanel%2Fresource%2F1.0%3A%3Axml\">New HTML Document</a>");
            sb.append("</li>");
            sb.append("<li>");
            sb.append("<a href=\"create-new-page.html?resource-type=http%3A%2F%2Fwww.litwan.com%2Fyanel%2Fresource%2F1.0%3A%3Asvg\">New SVG Document</a>");
            sb.append("</li>");
            sb.append(new RevisionsWorkflowMenuItem().getMenuItem(resource));
            if (ResourceAttributeHelper.hasAttributeImplemented(resource, "Modifiable", "2")) {
                sb.append("<li><a href=\"?yanel.resource.usecase=delete\">Delete this page</a></li>");
            }
        }
        sb.append("<li><a href=\"?yanel.resource.meta\">Info</a></li>");
        
        sb.append("</ul>");
        sb.append("</li></ul>");
        
        return sb.toString();
    }    
    
    /**
     *  edit menu
     */
    public String getEditMenu(Resource resource) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul><li>");
        sb.append("<div id=\"yaneltoolbar_menutitle\">Edit</div>");
        sb.append("<ul>");
        String backToRealm = org.wyona.yanel.core.util.PathUtil.backToRealm(resource.getPath());
        if (ResourceAttributeHelper.hasAttributeImplemented(resource, "Modifiable", "2")) {
            View view = ((ViewableV2)resource).getView("default");
            if (view.getMimeType().contains("html")) {
                sb.append("<li><a href=\"" + backToRealm + "usecases/xinha.html?edit-path=" + resource.getPath() + "\">Edit page with Xinha&#160;&#160;&#160;</a></li>");
                sb.append("<li><a href=\"" + backToRealm + "usecases/tinymce.html?edit-path=" + resource.getPath() + "\">Edit page with tinyMCE&#160;&#160;&#160;</a></li>");
            }
            if (view.getMimeType().contains("svg")) {
                sb.append("<li><a href=\"" + backToRealm + resource.getPath().substring(1) + ".svg-edit.html\">Edit page with svg-edit&#160;&#160;&#160;</a></li>");
            }
        }
        sb.append("</ul>");
        sb.append("</li>");
        sb.append("</ul>");
        return sb.toString();
    }
    
    public String getAdminMenu(Resource resource, HttpServletRequest request, Map map, String reservedPrefix) throws ServletException, IOException, Exception {
        UserManager userManager = resource.getRealm().getIdentityManager().getUserManager();
        Identity identity = resource.getEnvironment().getIdentity();
        User user = userManager.getUser(identity.getUsername());
        
        if (user != null) {
            return super.getAdminMenu(resource, request, map, reservedPrefix);
        }
        return "";
    }
}
