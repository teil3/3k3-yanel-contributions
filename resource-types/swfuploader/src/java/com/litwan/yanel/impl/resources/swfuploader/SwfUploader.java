/*
 * Copyright 2007 Wyona
 */

package com.litwan.yanel.impl.resources.swfuploader;

import org.wyona.yanel.impl.resources.usecase.ExecutableUsecaseResource;
import org.wyona.yanel.impl.resources.usecase.UsecaseException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;


/**
 * A simple usecase which is based on ExecutableUsecaseResource
 */
public class SwfUploader extends ExecutableUsecaseResource {
    
    private static Logger log = Logger.getLogger(SwfUploader.class);
    
    public String getSessionId() {
        return getEnvironment().getRequest().getSession().getId();
    }
}
