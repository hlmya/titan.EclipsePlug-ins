/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.actions;

import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.preferences.PreferenceConstantValues;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * @author Kristof Szabados
 * */
public final class IndentationSupport {

	private static String indentationString = null;

	private IndentationSupport() {
		// Hide constructor
	}

	/** clear the indentation string */
	public static void clearIndentString() {
		indentationString = null;
	}

	/** @return the current indentation string */
	public static String getIndentString() {
		if (indentationString != null) {
			return indentationString;
		}

		final String tabPolicy = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.INDENTATION_TAB_POLICY);
		final String indentSizeString = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.INDENTATION_SIZE);
		int indentSize;
		// This checking is leaved for sure.
		if (indentSizeString.isEmpty()) {
			indentSize = 2;
		} else {
			indentSize = Integer.parseInt(indentSizeString);
		}
		if (PreferenceConstantValues.TAB_POLICY_1.equals(tabPolicy)) {
			indentationString = "\t";
		} else if (PreferenceConstantValues.TAB_POLICY_2.equals(tabPolicy)) {
			final StringBuilder sb = new StringBuilder(8);
			for (int i = 0; i < indentSize; i++) {
				sb.append(' ');
			}
			indentationString = sb.toString();
		} else {
			indentationString = "  ";
		}

		return indentationString;
	}
}
