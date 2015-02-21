/*
 * Copyright 2015 Simon Litwan, Teil3
 */

package com.teil3.yanel.impl.resources.zxing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wyona.yanel.core.Resource;
import org.wyona.yanel.core.api.attributes.ViewableV2;
import org.wyona.yanel.core.attributes.viewable.View;
import org.wyona.yanel.core.attributes.viewable.ViewDescriptor;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;


/**
 *
 */
public class ZxingResource extends Resource implements ViewableV2 {
    
    private static Logger log = LogManager.getLogger(ZxingResource.class);
    
    private static String REQUEST_PARAMETER_CODE_TEXT = "code_text";
    private static String REQUEST_PARAMETER_CODE_COLOR = "code_color";
    private static String REQUEST_PARAMETER_BACKGROUND_COLOR = "background_color";
    private static String REQUEST_PARAMETER_CODE_SIZE = "code_size";
    
    private static final String VIEW_PNG = "png";
    private static final String VIEW_JPEG= "jpg";

 
    @Override
    public ViewDescriptor[] getViewDescriptors() {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public View getView(String viewId) throws Exception {
        View defaultView = new View(); 
        String path = getPath();
        String fileType = getPathExtension(path);
        if(!(fileType.equals(VIEW_PNG) || fileType.equals(VIEW_JPEG)))  {
            return defaultView;
        }
        try {
            Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(getCodeText(),BarcodeFormat.QR_CODE, getCodeSize(), getCodeSize(), hintMap);
            int width = byteMatrix.getWidth();
            BufferedImage image = new BufferedImage(width, width, BufferedImage.TYPE_INT_RGB);
            image.createGraphics();

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(getBackgroundColor());
            graphics.fillRect(0, 0, width, width);
            graphics.setColor(getCodeColor());

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < width; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            defaultView.setResponse(false);
            OutputStream os = getEnvironment().getResponse().getOutputStream();
            ImageIO.write(image, fileType, os);
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return defaultView;
    }
    @Override
    public boolean exists() throws Exception {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public long getSize() throws Exception {
        // TODO Auto-generated method stub
        return 0;
    }
    
    private Color getBackgroundColor() throws Exception {
        //backgroundColor
        Color color = Color.decode("#FFFFFF");
        String parameterBackgroundColor = getParameterAsString(REQUEST_PARAMETER_BACKGROUND_COLOR);
        if (parameterBackgroundColor != null && parameterBackgroundColor.length() > 0) {
            if (parameterBackgroundColor.startsWith("#")) {
                parameterBackgroundColor = parameterBackgroundColor.substring(1);
            }
            if (parameterBackgroundColor.matches("\\d+")) {
                String hex = "#" + parameterBackgroundColor;
                color = Color.decode(hex);
                return color;
            }
        }
        return color;
    }
    
    private Color getCodeColor() throws Exception {
        //code Color
        Color color = Color.decode("#00000");
        String parameterCodeColor = getParameterAsString(REQUEST_PARAMETER_CODE_COLOR);
        if (parameterCodeColor != null && parameterCodeColor.length() > 0) {
            if (parameterCodeColor.startsWith("#")) {
                parameterCodeColor = parameterCodeColor.substring(1);
            }
            if (parameterCodeColor.matches("\\d+")) {
                String hex = "#" + parameterCodeColor;
                color = Color.decode(hex);
                return color;
            }
        }
        return color;
    }

    private int getCodeSize() throws Exception {
        //code size
        int size = 125;
        String parameterSize = getParameterAsString(REQUEST_PARAMETER_CODE_SIZE);
        if (parameterSize != null && parameterSize.length() > 0 && parameterSize.matches("\\d+")) {
            size = Integer.parseInt(parameterSize);
            return size;
        }
        return size;
    }
    
    private String getCodeText() throws Exception {
        //code size
        String text = "teil3";
        String parameterText = getParameterAsString(REQUEST_PARAMETER_CODE_TEXT);
        if (parameterText != null && parameterText.length() > 0) {
            text = parameterText;
            return text;
        }
        return text;
    }
    
    private String getPathExtension(String path){
        String[] pathParts = path.split("\\.");
        return pathParts[pathParts.length - 1];
    }    
    
}
