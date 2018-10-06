/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;


/**
 * ASN.1 general string template
 *
 * @author Kristof Szabados
 */
public class TitanGeneralString_template extends TitanUniversalCharString_template {
	public TitanGeneralString_template() {
		//intentionally empty
	}

	public TitanGeneralString_template(final template_sel otherValue) {
		super(otherValue);
		checkSingleSelection(otherValue);
	}

	public TitanGeneralString_template(final TitanGeneralString aOtherValue) {
		super(aOtherValue);
	}

	public TitanGeneralString_template(final TitanGeneralString_template aOtherValue) {
		super(aOtherValue);
	}

	public TitanGeneralString valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific `general string' template.");
		}

		return new TitanGeneralString(single_value);
	}
}
