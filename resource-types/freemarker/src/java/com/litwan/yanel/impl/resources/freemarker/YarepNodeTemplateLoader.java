package com.litwan.yanel.impl.resources.freemarker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.wyona.yarep.core.Node;
import org.wyona.yarep.core.RepositoryException;

import freemarker.cache.TemplateLoader;

public class YarepNodeTemplateLoader implements TemplateLoader {
    
    Node node = null;
    
    public YarepNodeTemplateLoader(Node node){
        this.node = node;
    }

    public Reader getReader(Object templateSource, String encoding) throws IOException {
        try {
            return new BufferedReader(new InputStreamReader(node.getInputStream(), "UTF-8"));
        } catch (RepositoryException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public long getLastModified(Object templateSource) {
        return -1;
    }

    public Object findTemplateSource(String name) throws IOException {
        return node;
    }

    public void closeTemplateSource(Object templateSource) throws IOException {

    }

}
