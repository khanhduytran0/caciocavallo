/*
 * Copyright 2009 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
package net.java.openjdk.awt.peer.sdl;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import sun.awt.peer.cacio.managed.FullScreenWindowFactory;

/**
 * SDL based implementation of the GraphicConfiguration.
 *
 * @author Mario Torre <neugens.limasoftware@gmail.com>
 */
class SDLGraphicsConfiguration extends GraphicsConfiguration {

    private SDLGraphicsDevice device;

    SDLGraphicsConfiguration(SDLGraphicsDevice device) {
        this.device = device;
    }

    static GraphicsConfiguration getDefaultConfiguration() {

        GraphicsEnvironment ge =
        GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        return gc;
    }

    @Override
    public GraphicsDevice getDevice() {
        
        return this.device;
    }

    @Override
    public ColorModel getColorModel() {

        return new DirectColorModel(32, 0x00FF0000, 0x0000FF00,
                                    0x000000FF);
    }

    @Override
    public ColorModel getColorModel(int transparency) {
    
        if (transparency == Transparency.OPAQUE) {
            return getColorModel();
        } else {
            return ColorModel.getRGBdefault();
        }
    }

    @Override
    public AffineTransform getDefaultTransform() {

        return new AffineTransform();
    }

    @Override
    public AffineTransform getNormalizingTransform() {

        return new AffineTransform();
    }

    @Override
    public Rectangle getBounds() {

        /* TODO: implement properly */
        return new Rectangle(FullScreenWindowFactory.getScreenDimension());
    }

}
