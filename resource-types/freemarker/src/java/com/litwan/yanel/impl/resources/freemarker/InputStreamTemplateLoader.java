package com.litwan.yanel.impl.resources.freemarker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.wyona.yarep.core.Node;

import freemarker.cache.TemplateLoader;

public class InputStreamTemplateLoader implements TemplateLoader {
    
    private InputStream templateInputStream;

    public InputStreamTemplateLoader(InputStream templateInputStream) {
        this.templateInputStream = templateInputStream;
    }

    public Reader getReader(Object templateSource, String encoding) throws IOException {
        return new BufferedReader(new InputStreamReader(templateInputStream, "UTF-8"));
    }

    public long getLastModified(Object templateSource) {
        return -1;
    }

    public Object findTemplateSource(String name) throws IOException {
        return templateInputStream;
    }

    public void closeTemplateSource(Object templateSource) throws IOException {

    }


}
