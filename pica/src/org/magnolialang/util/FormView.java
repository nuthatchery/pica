/**************************************************************************
 * Copyright (c) 2011-2012 Anya Helene Bagge
 * Copyright (c) 2011-2012 University of Bergen
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. See http://www.gnu.org/licenses/
 * 
 * 
 * See the file COPYRIGHT for more information.
 * 
 * Contributors:
 * * Anya Helene Bagge
 * 
 *************************************************************************/
package org.magnolialang.util;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;
import org.magnolialang.errors.ImplementationError;

public class FormView extends ViewPart implements ISelectionListener {
	private FormToolkit toolkit;
	private ScrolledForm form;


	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.setText("Hello, Eclipse Forms");
		getViewSite().getPage().addSelectionListener(this);
	}


	/**
	 * Disposes the toolkit
	 */
	@Override
	public void dispose() {
		if(toolkit == null) {
			throw new ImplementationError("Not initialized");
		}
		getViewSite().getPage().removeSelectionListener(this);
		toolkit.dispose();
		super.dispose();
	}


	/**
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if(form == null) {
			throw new ImplementationError("Not initialized");
		}
		if(selection instanceof ITextSelection) {
			ITextSelection sel = (ITextSelection) selection;
			form.setText(sel.getText());
		}
	}


	/**
	 * Passing the focus request to the form.
	 */
	@Override
	public void setFocus() {
		if(form == null) {
			throw new ImplementationError("Not initialized");
		}
		form.setFocus();
	}

}
