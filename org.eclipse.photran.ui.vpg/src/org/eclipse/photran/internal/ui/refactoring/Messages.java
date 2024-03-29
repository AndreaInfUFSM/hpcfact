/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.refactoring;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Button;

/**
 * Externalized strings.
 */
public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.eclipse.photran.internal.ui.refactoring.messages"; //$NON-NLS-1$

 /*
  * Adicionar as linhas abaixo no arquivo
  */
    public static String DetectPossibleCriticalSectionsAction_label;
    public static String DetectPossibleCriticalSectionsAction_checkVariaveis;
    public static String DetectPossibleCriticalSectionsAction_checkDetalhes;
 /*
  * fim
  */
    
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
