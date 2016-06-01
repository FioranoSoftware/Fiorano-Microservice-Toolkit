/*
 * Copyright (c) Fiorano Software and affiliates. All rights reserved. http://www.fiorano.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package display;

import org.eclipse.osgi.util.NLS;

public class Messages_Display extends NLS {
	private static final String BUNDLE_NAME = "display.Messages_Display"; //$NON-NLS-1$
	public static String ConfigPage_1;
	public static String ConfigPage_2;
	public static String ConfigPage_3;
	public static String ConfigPage_4;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages_Display.class);
	}

	private Messages_Display() {
	}
}
