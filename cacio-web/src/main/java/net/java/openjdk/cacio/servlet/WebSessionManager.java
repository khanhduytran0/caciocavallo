/*
 * Copyright (c) 2011, Clemens Eisserer, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package net.java.openjdk.cacio.servlet;

import java.lang.ref.*;
import java.util.*;
import java.util.logging.*;

import javax.servlet.http.*;

import net.java.openjdk.awt.peer.web.*;
import sun.awt.*;

/**
 * Session-Manager with sub-session functionality. Browser often assign the same
 * session-cookie to different tabs, although for Caciocavallo-Web different
 * WebScreen instances running in different tabs are completly seperated and
 * isolated sessions should be represented by seperated Sessions. This has been
 * implemented by "subsessions". Additionally to the http-session-cookie, each
 * existing instance gets its own subsession-id.
 * 
 * @author Clemens Eisserer <linuxhippy@gmail.com>
 */
public abstract class WebSessionManager {
    private static Logger logger = Logger.getLogger(WebSessionManager.class.getName());

    static final String SESSION_KEY = "WEBSessionState";

    private static final WebSessionManager instance;

    static {
	WebSessionManager sessionMan = null;
	
	String sessionManagerClsName = System.getProperty("CacioSessionManager");
	if(sessionManagerClsName != null) {
	    try {
		sessionMan = (WebSessionManager) Class.forName(sessionManagerClsName).newInstance();
	    } catch (Exception ex) {
		logger.log(Level.WARNING, "Specified SessionManager "+sessionManagerClsName+" could not be loaded, falling back to default", ex);
	    }
	}
	
	if(sessionMan == null) {
	    sessionMan = new WebSessionManagerImpl();
	}

	instance = sessionMan;
    }

    ThreadLocal<WeakReference<WebSessionState>> threadStateHolder = new ThreadLocal<>();

    public static WebSessionManager getInstance() {
	return instance;
    }

    /**
     * Register at the current HttpSession, and issue a new subsession-id.
     * 
     * @param session
     * @return the WebSessionState generated for the current subsession
     */
    public abstract WebSessionState register(HttpSession session);

    /**
     * Builds links between AppContext and WebSessionState, so that the
     * AppContext can be accessed by using WebSessionState and vice-versa.
     * 
     * @param context
     * @param state
     */
    public void registerAppContext(AppContext context, WebSessionState state) {
	state.setAppContext(context);
	AppContext.getAppContext().put(SESSION_KEY, state);
	registerSessionAtCurrentThread(state);
    }

    /**
     * Returns the WebSessionState of the currently active AppContext. This is
     * called from AWT threads, where the only way to get to the WebSessionState
     * is through AppContext.
     * 
     * @return the WebSessionState
     */
    public WebSessionState getCurrentStateAWT() {
	return (WebSessionState) AppContext.getAppContext().get(SESSION_KEY);
    }

    /**
     * Can be called if a method is called by the servlet-thread as well as by
     * AWT/App threads.
     * 
     * @return
     */
    public WebSessionState getCurrentState() {
	WebSessionState state = getCurrentStateAWT();

	if (state == null) {
	    state = threadStateHolder.get().get();
	}

	return state;
    }

    /**
     * Returns the WebSessionState of the HttpSessiont. This is called from
     * servlet threads.
     * 
     * @param session
     * @param subSessionID
     * @return the WebSessionState
     */
    public abstract WebSessionState getCurrentState(HttpSession session, int subSessionID);

    /**
     * Registers the state for the current thread. Has to be called before
     * calling into cacio-web from a servlet!
     * 
     * @param state
     */
    public void registerSessionAtCurrentThread(WebSessionState state) {
	threadStateHolder.set(new WeakReference<>(state));
    }

    /**
     * Disposes the session, as well as the assiciated AppContext. This will
     * also shut down the applications contained in that AppContext as well as
     * all its threads.
     * 
     * @param session
     */
    public abstract void disposeSession(HttpSession session);

    public synchronized void disposeSessionState(HttpSession session, WebSessionState state) {
	HashMap<Integer, WebSessionState> subSessionMap = state.getSubSessionMap();
	subSessionMap.remove(state.getSubSessionID());
	state.dispose();
    }

    public abstract WebSessionState getSessionState(HttpServletRequest request);
}
