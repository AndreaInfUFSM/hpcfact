/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.refactoring;

import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.refactoring.DetectPossibleCriticalSections;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.core.vpg.refactoring.VPGRefactoring;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class DetectPossibleCriticalSectionsAction extends
    AbstractFortranRefactoringActionDelegate implements IWorkbenchWindowActionDelegate,
    IEditorActionDelegate
{
    public DetectPossibleCriticalSectionsAction()
    {
        super(DetectPossibleCriticalSections.class, DetectPossibleCriticalSectionsWizard.class);
    }

    @Override
    protected VPGRefactoring<IFortranAST, Token, PhotranVPG> getRefactoring(List<IFile> files)
    {
        DetectPossibleCriticalSections r = new DetectPossibleCriticalSections();
        r.initialize(getFortranEditor().getIFile(), getFortranEditor().getSelection());
        return r;
    }

    public static class DetectPossibleCriticalSectionsWizard extends
        AbstractFortranRefactoringWizard
    {
        protected DetectPossibleCriticalSections detectedPossibleCriticalSections;

        public DetectPossibleCriticalSectionsWizard(DetectPossibleCriticalSections r)
        {
            super(r);
            this.detectedPossibleCriticalSections = r;
        }

        @Override
        protected void doAddUserInputPages()
        {
            addPage(new UserInputWizardPage(detectedPossibleCriticalSections.getName())
            {
                protected Button checkVariaveis;

                protected Button checkDetalhes;

                public void createControl(Composite parent)
                {
                    Composite top = new Composite(parent, SWT.NONE);
                    initializeDialogUnits(top);
                    setControl(top);

                    top.setLayout(new GridLayout(2, false));

                    Composite group = top;
                    Label lbl = new Label(group, SWT.NONE);
                    lbl.setText(Messages.bind(
                        Messages.DetectPossibleCriticalSectionsAction_label, "")); //$NON-NLS-1$

                    new Label(group, SWT.NONE).setText("\n"); //$NON-NLS-1$

                    checkVariaveis = new Button(group, SWT.CHECK);
                    checkVariaveis
                        .setText(Messages.DetectPossibleCriticalSectionsAction_checkVariaveis);
                    checkVariaveis.setSelection(true);
                    checkVariaveis.addSelectionListener(new SelectionListener()
                    {
                        public void widgetDefaultSelected(SelectionEvent e)
                        {
                            widgetSelected(e);
                        }

                        public void widgetSelected(SelectionEvent e)
                        {
                            checkDetalhes.setSelection(false);
                            detectedPossibleCriticalSections.setTypeDetection(false);
                        }

                    });

                    new Label(group, SWT.NONE).setText(""); //$NON-NLS-1$

                    checkDetalhes = new Button(group, SWT.CHECK);
                    checkDetalhes
                        .setText(Messages.DetectPossibleCriticalSectionsAction_checkDetalhes);
                    checkDetalhes.setSelection(false);
                    checkDetalhes.addSelectionListener(new SelectionListener()
                    {
                        public void widgetDefaultSelected(SelectionEvent e)
                        {
                            widgetSelected(e);
                        }

                        public void widgetSelected(SelectionEvent e)
                        {
                            checkVariaveis.setSelection(false);
                            detectedPossibleCriticalSections.setTypeDetection(true);
                        }

                    });

                    checkVariaveis.setFocus();
                }
            });
        }
    }
}