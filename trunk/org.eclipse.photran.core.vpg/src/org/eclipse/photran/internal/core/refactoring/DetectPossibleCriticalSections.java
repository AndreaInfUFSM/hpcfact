/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UFSM - Universidade Federal de Santa Maria (www.ufsm.br)     
 *******************************************************************************/

/**
 * @author Dionatan Kitzmann Tietzmann
 */

package org.eclipse.photran.internal.core.refactoring;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.loops.ASTProperLoopConstructNode;
import org.eclipse.photran.internal.core.analysis.loops.GenericASTVisitorWithLoops;
import org.eclipse.photran.internal.core.analysis.loops.LoopReplacer;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTAssignmentStmtNode;
import org.eclipse.photran.internal.core.parser.ASTCallStmtNode;
import org.eclipse.photran.internal.core.parser.ASTElseConstructNode;
import org.eclipse.photran.internal.core.parser.ASTEntityDeclNode;
import org.eclipse.photran.internal.core.parser.ASTIfConstructNode;
import org.eclipse.photran.internal.core.parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.ASTModuleNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineArgNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTTypeDeclarationStmtNode;
import org.eclipse.photran.internal.core.parser.ASTUseStmtNode;
import org.eclipse.photran.internal.core.parser.ASTVarOrFnRefNode;
import org.eclipse.photran.internal.core.parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.reindenter.Reindenter;
import org.eclipse.photran.internal.core.reindenter.Reindenter.Strategy;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import br.ufsm.inf.viewCriticalSection.*;
import br.ufsm.inf.viewCriticalSection.views.CriticalSectionEvent;
import br.ufsm.inf.viewCriticalSection.views.CriticalSectionModel;


public class DetectPossibleCriticalSections extends FortranEditorRefactoring
{
    private StatementSequence sequenceCode = null;

    private boolean detectDetails = false;

    private List<Definition> variables = new LinkedList<Definition>();

    private List<String> ocorNodes = new LinkedList<String>();

    private List<IFile> files = new LinkedList<IFile>();

    private Token Gvar = null;

    private Token GvarCritical = null;

    private List<Token> listGvarPrivate = new LinkedList<Token>();

    private List<Token> listGvar = new LinkedList<Token>();

    private Integer Gid = 1;

    private List<Integer> listGid = new LinkedList<Integer>();

    private boolean indCreateListModules = false;

    private List<ASTModuleNode> listModules = new LinkedList<ASTModuleNode>();

    private List<Definition> listGlobalVarModified = new LinkedList<Definition>();

    private List<Definition> listGlobalVarRead = new LinkedList<Definition>();

    private List<Token> listGlobalVarDetected = new LinkedList<Token>();

    private List<Definition> listGlobalVarDetectedDefinition = new LinkedList<Definition>();

    private List<Definition> listVarModule = new LinkedList<Definition>();

    private int NumberLoop;

    CriticalSectionModel model = ViewCriticalSectionPlugin.getDefault().getModel();

    public void setTypeDetection(Boolean details)
    {
        this.detectDetails = details;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////
    // Rotinas - Photran
    // /////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getName()
    {
        return Messages.DetectPossibleCriticalSections_Name;
    }

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);
        LoopReplacer.replaceAllLoopsIn(this.astOfFileInEditor.getRoot());

        if (this.selectedRegionInEditor.getText().trim().length() == 0)
            fail(Messages.DetectPossibleCriticalSections_CommandsWarnning);

        try
        {
            sequenceCode = findEnclosingStatementSequence(this.astOfFileInEditor,
                this.selectedRegionInEditor);

            if (sequenceCode == null) 
                fail(Messages.DetectPossibleCriticalSections_CommandsWarnning);

        }
        catch (Exception e)
        {
            // TODO: handle exception
            e.printStackTrace();
            fail(Messages.DetectPossibleCriticalSections_CommandsWarnning);
        }
    }

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {

    }

    @SuppressWarnings("null")
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        
        model.clear();

        try
        {
        
            CreateListFiles();

            CreateListModules();

            CreateListVarModule(listVarModule);

            boolean isLoop = false;
            if (sequenceCode.selectedStmts.size() == 1)
                if (sequenceCode.selectedStmts.get(0) instanceof ASTProperLoopConstructNode)
                    isLoop = true;

            if (isLoop)
            {
                NumberLoop = 0;
            }
            else
            {
                NumberLoop = 1;
            }

            for (IASTNode stmt : sequenceCode.selectedStmts)
            {
                stmt.accept(new GenericASTVisitorWithLoops()
                {
                    @Override
                    public void visitASTNode(IASTNode node)
                    {
                        boolean globalVarDetected = false;

                        if (node instanceof ASTAssignmentStmtNode)
                        {
                            Gvar = ((ASTAssignmentStmtNode)node).getLhsVariable().getName();
                            GvarCritical = Gvar;

                            globalVarDetected = false;
                            if (!listGlobalVarDetectedDefinition.contains(Gvar.resolveBinding()
                                .get(0)) || detectDetails)
                            {
                                if (!listGlobalVarDetectedDefinition.contains(Gvar.resolveBinding()
                                    .get(0)) || detectDetails)
                                {
                                    if (findDefinitionInListModule(Gvar.resolveBinding().get(0)))
                                    {
                                        addGlobalVarList(Gvar.resolveBinding().get(0),
                                            listGlobalVarModified);
                                        if (listGlobalVarRead
                                            .contains(Gvar.resolveBinding().get(0)))
                                        {
                                            globalVarDetected = true;
                                            listGlobalVarDetected.add(Gvar);
                                            listGlobalVarDetectedDefinition.add(Gvar
                                                .resolveBinding().get(0));
                                            addOcor(
                                                GvarCritical,
                                                GvarCritical,
                                                "Global variable", getDefinitionInListModule(Gvar.resolveBinding().get(0)).toString()); //$NON-NLS-1$
                                            Gid++;
                                            node = addOpenMPCriticalClause(node);
                                            Reindenter.reindent(node,
                                                vpg.acquirePermanentAST(fileInEditor),
                                                Strategy.REINDENT_EACH_LINE);
                                        }
                                    }
                                }
                            }

                            if (globalVarDetected == false)
                            {
                                int indexGlobalVar = listGlobalVarDetected.size();
                                if ((detectDetails) || (!CheckVariableList(GvarCritical)))
                                {
                                    if (isCriticalSection(sequenceCode.selectedStmts, Gvar, false,
                                        null))
                                    {
                                        Gid++;
                                        node = addOpenMPCriticalClause(node);
                                    }
                                }
                                if (indexGlobalVar < listGlobalVarDetected.size())
                                    node = addListVariables(node, indexGlobalVar,
                                        listGlobalVarDetected, false);

                            }
                        }

                        if (node instanceof ASTTypeDeclarationStmtNode)
                        {

                            for (ASTEntityDeclNode entityDeclList : ((ASTTypeDeclarationStmtNode)node)
                                .getEntityDeclList())
                            {
                                if (entityDeclList.getInitialization() != null)
                                {

                                    Gvar = entityDeclList.getObjectName().getObjectName();
                                    GvarCritical = Gvar;

                                    if ((detectDetails) || (!CheckVariableList(GvarCritical)))
                                    {

                                        if (isCriticalSection(sequenceCode.selectedStmts, Gvar,
                                            false, null))
                                        {
                                            Gid++;
                                            node = addOpenMPCriticalClause(node);

                                        }
                                    }
                                }
                            }
                        }

                        if (node instanceof ASTProperLoopConstructNode)
                        {
                            NumberLoop++;
                            if (((ASTProperLoopConstructNode)node).getIndexVariable() != null)
                            {
                                Gvar = ((ASTProperLoopConstructNode)node).getIndexVariable();
                                GvarCritical = Gvar;

                                globalVarDetected = false;
                                if (!listGlobalVarDetectedDefinition.contains(Gvar.resolveBinding()
                                    .get(0)) || detectDetails)
                                {
                                    if (findDefinitionInListModule(Gvar.resolveBinding().get(0)))
                                    {
                                        addGlobalVarList(Gvar.resolveBinding().get(0),
                                            listGlobalVarModified);
                                        if (listGlobalVarRead
                                            .contains(Gvar.resolveBinding().get(0)))
                                        {
                                            globalVarDetected = true;
                                            listGlobalVarDetected.add(Gvar);
                                            listGlobalVarDetectedDefinition.add(Gvar
                                                .resolveBinding().get(0));
                                            addOcor(
                                                GvarCritical,
                                                GvarCritical,
                                                "Global variable", getDefinitionInListModule(Gvar.resolveBinding().get(0)).toString()); //$NON-NLS-1$
                                            Gid++;
                                            node = addOpenMPCriticalClause(node);

                                        }
                                    }
                                }

                                if (globalVarDetected == false)
                                {
                                    if ((detectDetails) || (!CheckVariableList(GvarCritical)))
                                    {
                                        if (isCriticalSection(sequenceCode.selectedStmts, Gvar,
                                            false, null))
                                        {
                                            Gid++;
                                            if (NumberLoop > 1)
                                            {
                                                listGvarPrivate.add(GvarCritical);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (node instanceof ASTCallStmtNode)
                        {
                            int indexVar = listGvar.size();
                            int indexGlobalVar = listGlobalVarDetected.size();
                            if (callIsCriticalSection((ASTCallStmtNode)node, null, true, true, true))
                            {
                                Gid++;
                                node = addListVariables(node, indexVar, listGvar, true);
                            }
                            if (indexGlobalVar < listGlobalVarDetected.size())
                                node = addListVariables(node, indexGlobalVar,
                                    listGlobalVarDetected, true);
                        }

                        traverseChildren(node);
                    }

                    

                });
            }

            
            Gid = 0;
            List<String> res = new LinkedList<String>();
            for (int i = 0; i <= listGid.size() - 1; i++)
            {

                if (Gid != listGid.get(i))
                {
                    Gid = listGid.get(i);
                    if (res == null)
                        res.add(listGvar.get(i).getText());
                    else if (res.contains(listGvar.get(i).getText()) == false)
                        res.add(listGvar.get(i).getText());

                }

            }

            if (!res.isEmpty())
            {
                String warning = sequenceCode.selectedStmts.get(0).findFirstToken()
                    .getWhiteBefore().trim();
                String warningPrivate = ""; //$NON-NLS-1$


                if (!listGvarPrivate.isEmpty())
                {
                    warningPrivate += " PRIVATE("; //$NON-NLS-1$
                    for (Token t : listGvarPrivate)
                        warningPrivate += t.getText() + ","; //$NON-NLS-1$ //"

                    warningPrivate = warningPrivate.substring(0, warningPrivate.length() - 1) + ")"; //$NON-NLS-1$
                }

                if (warning.length() > 0) warning = "\n" + warning + "\n"; //$NON-NLS-1$ //$NON-NLS-2$

                if (isLoop)
                    warning = "!$OMP PARALLEL DO" + warningPrivate + warning; //$NON-NLS-1$
                else
                    warning = "!$OMP PARALLEL" + warningPrivate + warning; //$NON-NLS-1$

                warning += "\n"; //$NON-NLS-1$
                sequenceCode.selectedStmts.get(0).findFirstToken().setWhiteBefore(warning);

                if (isLoop)
                    sequenceCode.lastToken().setWhiteAfter(
                        sequenceCode.lastToken().getWhiteAfter().trim()
                            + "\n!$OMP END PARALLEL DO \n"); //$NON-NLS-1$
                else
                    sequenceCode.lastToken()
                        .setWhiteAfter(
                            sequenceCode.lastToken().getWhiteAfter().trim()
                                + "\n!$OMP END PARALLEL \n"); //$NON-NLS-1$

                Reindenter.reindent(sequenceCode.selectedStmts.get(0),
                    vpg.acquirePermanentAST(fileInEditor), Strategy.REINDENT_EACH_LINE);
            }

            this.addChangeFromModifiedAST(this.fileInEditor, pm);
            vpg.releaseAllASTs();

        }
        finally
        {
            vpg.releaseAllASTs();
        }

    }

    // /////////////////////////////////////////////////////////////////////////////////////////
    // Rotinas - Identificação de SC
    // /////////////////////////////////////////////////////////////////////////////////////////

    private final boolean isCriticalSection(List<IASTNode> code, final Token var,
        final boolean checkAssignment, final List<List<Definition>> listVarModule)
    {
        class VisitorToken extends GenericASTVisitor
        {
            private boolean result = false;

            public boolean read;

            @Override
            public void visitToken(Token token)
            {
                if (token.getTerminal() == Terminal.T_IDENT)
                {
                    for (Definition def : token.resolveBinding())
                    {

                        if ((!listGlobalVarDetectedDefinition.contains(def)) || (detectDetails))
                        {
                            if (((read) && !(listGlobalVarRead.contains(def)))
                                || (!(read) && !(listGlobalVarModified.contains(def))))
                            {
                                if (findDefinitionInListModule(def))
                                {
                                    Token auxGvarCritical = GvarCritical;
                                    GvarCritical = token;
                                    if (read)
                                    {
                                        addGlobalVarList(def, listGlobalVarRead);
                                        if (listGlobalVarModified.contains(def))
                                        {
                                            listGlobalVarDetected.add(token);
                                            listGlobalVarDetectedDefinition.add(def);
                                            addOcor(token, token, "Global variable", "Routine: " //$NON-NLS-1$ //$NON-NLS-2$
                                                + token.getEnclosingScope().getName());
                                            Gid++;
                                        }
                                    }
                                    else
                                    {
                                        addGlobalVarList(def, listGlobalVarModified);
                                        if (listGlobalVarRead.contains(def))
                                        {
                                            listGlobalVarDetected.add(token);
                                            listGlobalVarDetectedDefinition.add(def);
                                            addOcor(token, token, "Global variable", token //$NON-NLS-1$
                                                .getEnclosingScope().getName());
                                            Gid++;
                                        }
                                    }
                                    GvarCritical = auxGvarCritical;
                                }
                            }
                        }
                    }
                }

                if (!result && var != null)
                {

                    if (token.getTerminal() == Terminal.T_IDENT)
                    {
                        if (!listGlobalVarDetected.contains(token))
                        {
                            for (Definition def : token.resolveBinding())
                            {
                                if (!result)
                                {
                                    if (def.isLocalVariable())
                                    {
                
                                        if (token.resolveBinding().equals(var.resolveBinding())
                                            && !(token.equals(var)))
                                        {
                                            if (!checkAssignment)
                                            {
                                                addOcor(token, var, null, null);
                                            }

                                            result = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

        class VisitorNode extends GenericASTVisitor
        {
            private boolean result = false;

            List<IASTNode> checkedNodes = new LinkedList<IASTNode>();

            boolean check = true;

            @Override
            public void visitASTNode(IASTNode node)
            {
                if (check)
                {
                    if (!(node instanceof ASTListNode) && !(node instanceof ASTElseConstructNode))
                    {
                        if (node instanceof ASTAssignmentStmtNode)
                        {
                            VisitorToken v = new VisitorToken();
                            if (checkAssignment)
                            {
                                v.read = false;
                                ((ASTAssignmentStmtNode)node).getLhsVariable().accept(v);

                            }
                            else
                            {
                                v.read = true;
                                ((ASTAssignmentStmtNode)node).getRhs().accept(v);
                                if((v.result==false) && (((ASTAssignmentStmtNode)node).getLhsExprList()!=null))
                                    ((ASTAssignmentStmtNode)node).getLhsExprList().accept(v);
                                
                            }
                            if (v.result)
                            {
                                result = true;
                            }

                            checkedNodes.add(node);

                        }
                        else if (node instanceof ASTIfConstructNode)
                        {
                            if (!(checkAssignment))
                            {
                                VisitorToken v = new VisitorToken();
                                v.read = true;
                                ((ASTIfConstructNode)node).getIfThenStmt().accept(v);
                                if (v.result) result = true;
                            }
                            checkedNodes.add(((ASTIfConstructNode)node).getIfThenStmt());
                        }
                        else if (node instanceof ASTTypeDeclarationStmtNode)
                        {
                            VisitorToken v = new VisitorToken();
                            for (ASTEntityDeclNode entityDeclList : ((ASTTypeDeclarationStmtNode)node)
                                .getEntityDeclList())
                            {
                                if (checkAssignment)
                                {
                                    if (entityDeclList.getInitialization() != null)
                                        entityDeclList.accept(v);
                                    v.read = false;
                                }

                                if (v.result)
                                {
                                    result = true;
                                }

                            }
                            checkedNodes.add(node);
                        }
                        else if (node instanceof ASTUseStmtNode)
                        {
                            checkedNodes.add(node);
                        }
                        else if (node instanceof ASTCallStmtNode)
                        {
                            if (callIsCriticalSection((ASTCallStmtNode)node, var, false, false,
                                checkAssignment))
                            {
                                result = true;
                            }
                            checkedNodes.add(node);
                        }
                        else if (node instanceof ASTProperLoopConstructNode)
                        {
                            VisitorToken v = new VisitorToken();
                            if (checkAssignment)
                            {
                                v.read = false;
                                ((ASTProperLoopConstructNode)node).getLoopHeader().getLoopControl()
                                    .getVariableName().accept(v);

                            }
                            else
                            {
                                v.read = true;
                                ((ASTProperLoopConstructNode)node).getLoopHeader().getLoopControl()
                                    .getUb().accept(v);
                                if((v.result==false) && (((ASTProperLoopConstructNode)node).getLoopHeader().getLoopControl().getLb()!=null))
                                    ((ASTProperLoopConstructNode)node).getLoopHeader().getLoopControl().getLb().accept(v);
                            }
                            if (v.result)
                            {
                                result = true;
                            }

                            checkedNodes.add(((ASTProperLoopConstructNode)node).getLoopHeader());
                        }
                        else if (!(nodeInListNode(checkedNodes, node)))
                        {
                            VisitorToken v = new VisitorToken();
                            v.read = true;
                            node.accept(v);
                            if (v.result)
                            {
                                result = true;
                            }
                        }
                    }

                    if ((result) && ((checkAssignment) || (!detectDetails)))
                    {
                        check = false;
                    }
                }

                traverseChildren(node);

            }

        }

        boolean result = false;

        if ((!result) || (detectDetails))
        {
            for (IASTNode stmt : code)
            {
                if (stmt != null)
                {
                    VisitorNode node = new VisitorNode();
                    stmt.accept(node);
                    if (node.result)
                    {
                        result = true;
                        if ((!detectDetails) || (checkAssignment)) return result;
                    }
                }
            }
        }
        return result;
    }

    private boolean nodeInListNode(List<IASTNode> checkedNodes, final IASTNode node)
    {
        class VisitorNode extends GenericASTVisitor
        {
            boolean result = false;

            @Override
            public void visitASTNode(IASTNode n)
            {
                if (n.equals(node)) result = true;
                traverseChildren(n);
            }
        }
        for (IASTNode stmt : checkedNodes)
        {
            if (stmt != null)
            {
                VisitorNode n = new VisitorNode();
                stmt.accept(n);
                if (n.result) return true;
            }
        }
        return false;
    }

    private final boolean callIsCriticalSection(ASTCallStmtNode call, final Token var,
        boolean addVar, boolean firstCheck, final boolean checkAssignment)
    {
        boolean result = false;

        final Token subName = call.getSubroutineName();

        ASTSubroutineSubprogramNode sub = FindSubroutine(subName);

        if (sub != null)
        {

            List<IASTNode> subrotineCode = new LinkedList<IASTNode>();
            subrotineCode.addAll(sub.getBody());

            final List<ASTSubroutineArgNode> listParCall = new LinkedList<ASTSubroutineArgNode>();
            final List<Token> listParSubroutine = new LinkedList<Token>();

            if (call.getArgList() != null)
            {
                for (int i = 0; i <= call.getArgList().size() - 1; i++)
                {
                    if (call.getArgList().get(i).getExpr() instanceof ASTVarOrFnRefNode)
                    {
                        if (((var != null) && (call.getArgList().get(i).getExpr().toString()
                            .equals(var.getText()))) || ((var == null) && (firstCheck)))
                        {
                            listParCall.add(call.getArgList().get(i));
                            listParSubroutine.add(sub.getSubroutineStmt().getSubroutinePars()
                                .get(i).getVariableName());
                        }
                    }
                }
            }

            if (!indCreateListModules)
            {
                CreateListFiles();
                CreateListModules();
            }

            List<String> listMod = new LinkedList<String>();
            FindUseModule(sub, listMod);
            List<List<Definition>> listVarMod = CreateListVarUseModule(listMod);

            if (!(listParCall.isEmpty()))
            {
                for (int i = 0; i <= listParCall.size() - 1; i++)
                {
                    if (var != null)
                    {
                        if (addVar)
                        {
                            Gvar = sub.getSubroutineStmt().getSubroutinePars().get(i)
                                .getVariableName();
                            if (firstCheck)
                                GvarCritical = call.getArgList().get(i).findFirstToken();
                        }
                        if ((detectDetails) || (!CheckVariableList(GvarCritical)))
                        {
                            if (firstCheck)
                            {
                                if (isCriticalSection(subrotineCode, listParSubroutine.get(i),
                                    checkAssignment, listVarMod))
                                    if (isCriticalSection(subrotineCode, listParSubroutine.get(i),
                                        false, listVarMod))
                                    {
                                        Gid++;
                                        result = true;
                                    }
                            }
                            else if (isCriticalSection(subrotineCode, listParSubroutine.get(i),
                                checkAssignment, listVarMod))
                            {
                                Gid++;
                                result = true;
                            }
                        }
                    }
                    else
                    {
                        Gvar = listParSubroutine.get(i);
                        if (firstCheck)
                            GvarCritical = listParCall.get(i).getExpr().findFirstToken();

                        if ((detectDetails) || (!CheckVariableList(GvarCritical)))
                        {
                            if (firstCheck)
                            {
                                if (isCriticalSection(subrotineCode, listParSubroutine.get(i),
                                    checkAssignment, listVarMod))
                                {
                                    if (isCriticalSection(sequenceCode.selectedStmts, listParCall
                                        .get(i).getExpr().findFirstToken(), false, listVarMod))
                                    {
                                        Gid++;
                                        result = true;
                                    }

                                    if ((detectDetails) || (!result))
                                    {
                                        if (isCriticalSection(subrotineCode,
                                            listParSubroutine.get(i), false, listVarMod))
                                        {
                                            Gid++;
                                            result = true;
                                        }
                                    }
                                }
                            }
                            else
                            {
                                if (isCriticalSection(subrotineCode, listParSubroutine.get(i),
                                    checkAssignment, listVarMod))
                                {
                                    Gid++;
                                    result = true;
                                }
                            }
                        }
                    }
                }
            }
            else
            {
                if (isCriticalSection(subrotineCode, null, checkAssignment, listVarMod))
                    result = true;
            }
            listParCall.clear();
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private void addOcor(Token tkn, Token var, String type, String detail)
    {

        boolean add = false;
        if (detectDetails) add = true;
        if (listGid.size() == 0)
            add = true;
        else if ((!add) && (!detectDetails) && (listGid.get(listGid.size() - 1) != Gid))
            add = true;

        if (add)
        {
            listGid.add(Gid);
            listGvar.add(GvarCritical);
            variables.addAll(var.resolveBinding());
            ocorNodes.add(tkn.getEnclosingScope().getName() + " - " //$NON-NLS-1$
                + tkn.getPhysicalFile().toString() + " - linha:" //$NON-NLS-1$
                + tkn.getLine());

            CriticalSectionEvent event = new CriticalSectionEvent();
            event.setProjectName(GvarCritical.getText());

            if ((var.resolveBinding().get(0).isArray())
                || (var.resolveBinding().get(0).isDerivedType()))
                event.setImage("W"); //$NON-NLS-1$
            else
                event.setImage("E"); //$NON-NLS-1$

            if (type != null)
                event.setType(type);
            else
                event.setType(GvarCritical.resolveBinding().get(0).describeClassification());

            if (!detectDetails)
                event.setFileName(GvarCritical.getPhysicalFile().toString());
            else
                event.setFileName(tkn.getPhysicalFile().toString());

            if (detail != null)
                event.setDetails(detail);
            else if (!detectDetails)
                event.setDetails(Gvar.getText());
            else
                event.setDetails(tkn.getText());

            // PhotranVPG
            PhotranTokenRef tr = tkn.getTokenRef();
            IFile file = tr == null ? null : tr.getFile();
            IResource res = file == null ? ResourcesPlugin.getWorkspace().getRoot() : file;
            IMarker marker;
            try
            {
                marker = res.createMarker("org.eclipse.photran.core.vpg.warningMarker"); //$NON-NLS-1$
                @SuppressWarnings("rawtypes")
                Map attribs = new HashMap(5);

                if (tr != null)
                {
                    if (!detectDetails)
                    {

                        attribs.put(IMarker.CHAR_START, GvarCritical.getStreamOffset());
                        attribs.put(IMarker.CHAR_END, GvarCritical.getStreamOffset()
                            + GvarCritical.getText().length());
                    }
                    else
                    {
                        attribs.put(IMarker.CHAR_START, tkn.getStreamOffset());
                        attribs.put(IMarker.CHAR_END, tkn.getStreamOffset()
                            + tkn.getText().length());
                    }

                }
                attribs.put(IMarker.USER_EDITABLE, false);
                attribs.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);

                marker.setAttributes(attribs);

                event.setMaker(marker);
            }
            catch (CoreException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            model.addEvent(event);

        }
    }

    private ASTSubroutineSubprogramNode FindSubroutine(final Token subName)
    {
        class VisitorNode extends GenericASTVisitor
        {
            private boolean result = false;

            private ASTSubroutineSubprogramNode subrotine = null;

            @Override
            public void visitASTSubroutineSubprogramNode(ASTSubroutineSubprogramNode sub)
            {

                if (sub.getSubroutineStmt().getSubroutineName().getSubroutineName().getText()
                    .equals(subName.getText()))
                {
                    subrotine = sub;
                    result = true;
                }
            }
        }

        ASTSubroutineSubprogramNode subroutine = null;

        VisitorNode v = new VisitorNode();
        IFortranAST ast = vpg.acquirePermanentAST(this.fileInEditor);
        ast.accept(v);
        if (v.result)
        {
            subroutine = v.subrotine;
        }
        else
        {
            if (files.isEmpty())
            {
                CreateListFiles();
            }

            for (final IFile file : files)
            {
                v = new VisitorNode();
                ast = vpg.acquirePermanentAST(file);
                ast.accept(v);
                if (v.result)
                {
                    subroutine = v.subrotine;
                    break;
                }
            }
        }
        return subroutine;

    }

    // /////////////////////////////////////////////////////////////////////////////////////////
    // Rotinas - Arquivos do projeto
    // /////////////////////////////////////////////////////////////////////////////////////////
    private void CreateListFiles()
    {
        class ResourceVisitor implements IResourceVisitor
        {
            public boolean visit(IResource res)
            {
                if (res.getFileExtension() != null)
                {
                    if (res.getFileExtension().equals(fileInEditor.getFileExtension()))
                    {
                        files.add((IFile)res);
                    }
                }
                return true;
            }
        }
        IProject project = this.fileInEditor.getProject();
        try
        {
            project.accept(new ResourceVisitor());
        }
        catch (CoreException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    // /////////////////////////////////////////////////////////////////////////////////////////
    // Rotinas - Módulos
    // /////////////////////////////////////////////////////////////////////////////////////////

    private static final class ModuleVisitor extends GenericASTVisitor
    {
        private List<ASTModuleNode> modules = new LinkedList<ASTModuleNode>();

        @Override
        public void visitASTNode(IASTNode node)
        {
            if (node instanceof ASTModuleNode) modules.add((ASTModuleNode)node);
            traverseChildren(node);
        }
    }

    private void CreateListModules()
    {
        for (IFile file : files)
        {
            indCreateListModules = true;

            IFortranAST ast = vpg.acquirePermanentAST(file);
            ModuleVisitor modules = new ModuleVisitor();
            ast.accept(modules);
            listModules.addAll(modules.modules);
        }
    }

    private void CreateListVarModule(List<Definition> listVarModule)
    {
        for (ASTModuleNode mod : listModules)
            listVarModule.addAll(mod.getAllDefinitions());
    }

    private boolean FindUseModule(ScopingNode scopo, final List<String> list)
    {
        class VisitorNode extends GenericASTVisitor
        {
            private boolean result = false;

            @Override
            public void visitASTNode(IASTNode node)
            {
                if (node instanceof ASTUseStmtNode)
                {
                    list.add(((ASTUseStmtNode)node).getName().getText());
                    result = true;
                }
                traverseChildren(node);
            }

        }

        VisitorNode v = new VisitorNode();
        scopo.accept(v);

        return v.result;

    }

    private List<List<Definition>> CreateListVarUseModule(List<String> modules)
    {
        List<List<Definition>> listVar = new LinkedList<List<Definition>>();

        for (ASTModuleNode mod : listModules)
        {
            if (modules.contains(mod.getName()))
            {
                listVar.add(mod.getAllDefinitions());
            }
        }
        return listVar;

    }

    private boolean findDefinitionInListModule(Definition def)
    {
        if (listGlobalVarModified.contains(def)) return true;
        if (listGlobalVarRead.contains(def)) return true;
        if (listGlobalVarDetectedDefinition.contains(def)) return true;
        if (listVarModule.contains(def)) return true;
        return false;
    }

    private Definition getDefinitionInListModule(Definition def)
    {
        for (ASTModuleNode mod : listModules)
            for (Definition modDef : mod.getAllDefinitions())
                if (modDef.equals(def)) return modDef;
        return null;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////
    // Rotinas - Listas Globais de variáveis
    // /////////////////////////////////////////////////////////////////////////////////////////

    private boolean CheckVariableList(Token var)
    {
        for (int i = 0; i <= listGvar.size() - 1; i++)
        {
            if (var.getText().equals(listGvar.get(i).getText())) return true;
        }
        return false;
    }

    private void addGlobalVarList(Definition def, List<Definition> listGlobalVar)
    {
        if (!(listGlobalVar.contains(def))) listGlobalVar.add(def);
    }

    private IASTNode addOpenMPCriticalClause(IASTNode node)
    {
        String warning = node.findFirstToken().getWhiteBefore().trim();
        if (warning.length() > 0) warning = "\n" + warning + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
        warning += "!$OMP CRITICAL \n"; //$NON-NLS-1$
        node.findFirstToken().setWhiteBefore(warning);

        node.findLastToken().setWhiteAfter(
            node.findLastToken().getWhiteAfter().trim() + "!$OMP END CRITICAL \n"); //$NON-NLS-1$

        Reindenter.reindent(node, vpg.acquirePermanentAST(fileInEditor),
            Strategy.REINDENT_EACH_LINE);

        return node;
    }

    private IASTNode addListVariables(IASTNode node, int index, List<Token> listVar, boolean isCall)
    {
        String warning = node.findFirstToken().getWhiteBefore().trim();
        if (warning.length() > 0) warning = "\n" + warning + "\n"; //$NON-NLS-1$ //$NON-NLS-2$

        if (isCall)
        {
            if (listVar.equals(listGlobalVarDetected))
                warning += "!*** Warning ***\n!Critical global variables identified in the call: "; //$NON-NLS-1$
            else
                warning += "!*** Warning ***\n!Critical variables identified in the call: "; //$NON-NLS-1$ 
        }
        else
            warning += "!*** Warning ***\n!Critical global variables identified: "; //$NON-NLS-1$

        for (; index <= listVar.size() - 1; index++)
        {
            warning += listVar.get(index).getText();
            if (index < listVar.size() - 1)
                warning += ", "; //$NON-NLS-1$
            else
                warning += "\n"; //$NON-NLS-1$
        }
  
        node.findFirstToken().setWhiteBefore(warning);
  
        Reindenter.reindent(node, vpg.acquirePermanentAST(fileInEditor),
            Strategy.REINDENT_EACH_LINE);

        return node;
    }
}