/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.ArrayList;
import java.util.List;

/**
 * ASN.1 videotex string
 *
 * @author Kristof Szabados
 */
public class TitanVideotexString extends TitanUniversalCharString {
	public static TitanVideotexString TTCN_ISO2022_2_VideotexString(final TitanOctetString p_os) {
		final char osstr[] = p_os.getValue();
		final int len = osstr.length;
		final ArrayList<TitanUniversalChar> ucstr = new ArrayList<TitanUniversalChar>(len);

		for (int i = 0; i < len; i++) {
			ucstr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, osstr[i]));
		}

		return new TitanVideotexString(ucstr);
	}

	public TitanVideotexString() {
		//intentionally empty
	}

	public TitanVideotexString(final TitanUniversalCharString aOtherValue) {
		super(aOtherValue);
	}

	public TitanVideotexString(final List<TitanUniversalChar> aOtherValue) {
		super(aOtherValue);
	}

	public TitanVideotexString(final TitanVideotexString aOtherValue) {
		super(aOtherValue);
	}
}
