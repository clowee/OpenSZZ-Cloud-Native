package com.rest.szz.entities;


import gr.uom.java.xmi.UMLAttribute;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLJavadoc;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.decomposition.VariableDeclaration;
import gr.uom.java.xmi.diff.*;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RefactoringMiner {
    private GitHistoryRefactoringMiner miner;
    private Repository repository;

    public RefactoringMiner(Repository repository) {
        this.miner = new GitHistoryRefactoringMinerImpl();
        this.repository = repository;
    }
    public ArrayList<CodeRange> getRefactoringCodeRangesForTransaction(Transaction transaction) {
        ArrayList<CodeRange> refactoringChanges = new ArrayList<>(0);
        this.miner.detectAtCommit(this.repository, transaction.getId(), new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                for (Refactoring ref : refactorings) {
                    switch (ref.getRefactoringType()) {

                        // variable refactorings
                        case EXTRACT_VARIABLE: {
                            CodeRange codeRange = ((ExtractVariableRefactoring) ref).getExtractedVariableDeclarationCodeRange();
                            refactoringChanges.add(codeRange);
                            break;
                        }
                        case INLINE_VARIABLE: {
                            CodeRange codeRange = ((InlineVariableRefactoring) ref).getInlinedVariableDeclarationCodeRange();
                            refactoringChanges.add(codeRange);
                            break;
                        }
                        case REPLACE_VARIABLE_WITH_ATTRIBUTE: {
                            CodeRange codeRange = ((RenameVariableRefactoring) ref).getOriginalVariable().codeRange();
                            refactoringChanges.add(codeRange);
                            break;
                        }
                        case RENAME_VARIABLE: {
                            VariableDeclaration originalVariable = ((RenameVariableRefactoring) ref).getOriginalVariable();
                            VariableDeclaration renamedVariable = ((RenameVariableRefactoring) ref).getRenamedVariable();
                            if (areVariablesEqualIgnoringName(originalVariable, renamedVariable)) {
                                refactoringChanges.add(originalVariable.codeRange());
                                ((RenameVariableRefactoring) ref).getVariableReferences().forEach(r -> {
                                    refactoringChanges.add(r.getFragment1().codeRange());
                                });
                            }
                            break;
                        }

                        // attribute refactorings
                        case RENAME_ATTRIBUTE: {
                            UMLAttribute originalAttribute = ((RenameAttributeRefactoring) ref).getOriginalAttribute();
                            UMLAttribute renamedAttribute = ((RenameAttributeRefactoring) ref).getRenamedAttribute();
                            if (areAttributesEqualIgnoringNameFinalAndVisibility(originalAttribute, renamedAttribute)) {
                                refactoringChanges.add(originalAttribute.codeRange());
                                ((RenameAttributeRefactoring) ref).getAttributeRenames().forEach(r -> {
                                    r.getAttributeReferences().forEach(ar -> {
                                        refactoringChanges.add(ar.getFragment1().codeRange());
                                    });
                                });
                            }
                            break;
                        }
                        case EXTRACT_ATTRIBUTE: {
                            refactoringChanges.addAll(ref.leftSide());
                            break;
                        }
                        case MOVE_RENAME_ATTRIBUTE: {
                            UMLAttribute originalAttribute = ((MoveAndRenameAttributeRefactoring) ref).getOriginalAttribute();
                            UMLAttribute movedAttribute = ((MoveAndRenameAttributeRefactoring) ref).getMovedAttribute();
                            if (areAttributesEqualIgnoringNameFinalAndVisibility(originalAttribute, movedAttribute)) {
                                refactoringChanges.add(originalAttribute.codeRange());
                                ((MoveAndRenameAttributeRefactoring) ref).getAttributeRenames().forEach(r -> {
                                    r.getAttributeReferences().forEach(ar -> {
                                        refactoringChanges.add(ar.getFragment1().codeRange());
                                    });
                                });
                            }
                            break;
                        }
                        case MOVE_ATTRIBUTE: {
                            UMLAttribute originalAttribute = ((MoveAttributeRefactoring) ref).getOriginalAttribute();
                            UMLAttribute movedAttribute = ((MoveAttributeRefactoring) ref).getMovedAttribute();
                            if (areAttributesEqualIgnoringFinalAndVisibility(originalAttribute,movedAttribute)) {
                                refactoringChanges.add(originalAttribute.codeRange());
                            }
                            break;
                        }
                        case PULL_UP_ATTRIBUTE: {
                            UMLAttribute originalAttribute = ((PullUpAttributeRefactoring) ref).getOriginalAttribute();
                            UMLAttribute movedAttribute = ((PullUpAttributeRefactoring) ref).getMovedAttribute();
                            if (areAttributesEqualIgnoringFinalAndVisibility(originalAttribute,movedAttribute)) {
                                refactoringChanges.add(originalAttribute.codeRange());
                            }
                            break;
                        }
                        case PUSH_DOWN_ATTRIBUTE: {
                            UMLAttribute originalAttribute = ((PushDownAttributeRefactoring) ref).getOriginalAttribute();
                            UMLAttribute movedAttribute = ((PushDownAttributeRefactoring) ref).getMovedAttribute();
                            if (areAttributesEqualIgnoringFinalAndVisibility(originalAttribute,movedAttribute)) {
                                refactoringChanges.add(originalAttribute.codeRange());
                            }
                            break;
                        }

                        // parameter refactorings
                        case RENAME_PARAMETER: {
                            VariableDeclaration originalParameter = ((RenameVariableRefactoring) ref).getOriginalVariable();
                            VariableDeclaration renamedParameter = ((RenameVariableRefactoring) ref).getRenamedVariable();
                            if (areVariablesEqualIgnoringName(originalParameter, renamedParameter)
                                && originalParameter.codeRange().getStartColumn() == renamedParameter.codeRange().getStartColumn()
                            ) {
                                refactoringChanges.add(originalParameter.codeRange());
                                ((RenameVariableRefactoring) ref).getVariableReferences().forEach(r -> {
                                    refactoringChanges.add(r.getFragment1().codeRange());
                                });
                            }
                            break;
                        }
                        case REORDER_PARAMETER: {
                            ((ReorderParameterRefactoring) ref).getParametersBefore().forEach(p -> {
                                refactoringChanges.add(p.codeRange());
                            });
                            break;
                        }

                        // method refactorings
                        case EXTRACT_OPERATION: {
                            CodeRange codeRange = ((ExtractOperationRefactoring) ref).getExtractedCodeRangeFromSourceOperation();
                            refactoringChanges.add(codeRange);
                            break;
                        }
                        case RENAME_METHOD: {
                            UMLOperation originalOperation = ((RenameOperationRefactoring) ref).getOriginalOperation();
                            UMLOperation renamedOperation = ((RenameOperationRefactoring) ref).getRenamedOperation();
                            Boolean areSignaturesEqual = renamedOperation.equalSignatureIgnoringOperationName(originalOperation);
                            if (areSignaturesEqual) {
                                refactoringChanges.add(getOperationSignatureCodeRange(originalOperation));
                                // List<CodeRange> callReferencesCodeRanges = ((RenameOperationRefactoring) ref).getCallReferences()
                                //         .stream().map(c -> c.getInvokedOperationBefore().codeRange()).collect(Collectors.toList());
                                // refactoringChanges.addAll(callReferencesCodeRanges);
                            }
                            break;
                        }

                        // class refactorings
                        case RENAME_CLASS: {
                            UMLClass originalClass = ((RenameClassRefactoring) ref).getOriginalClass();
                            UMLClass renamedClass = ((RenameClassRefactoring) ref).getRenamedClass();
                            if (areClassesEqualIgnoringName(originalClass, renamedClass)) {
                                refactoringChanges.add(getClassSignatureCodeRange(originalClass));
                                originalClass
                                    .getOperations().stream()
                                    .filter(o -> o.isConstructor())
                                    .forEach(o -> refactoringChanges.add(getOperationSignatureCodeRange(o)));
                            }
                            break;
                        }
                    }
                }
            }
        });

        return removeCodeRangeDuplicates(refactoringChanges);
    }

    private CodeRange getOperationSignatureCodeRange(UMLOperation o) {
        if (o.getBody() == null) return null;
        CodeRange operationLocation = o.getBody().getCompositeStatement().codeRange();
        return copyCodeRange(operationLocation, operationLocation.getStartLine(), operationLocation.getStartLine());
    }

    private CodeRange getClassSignatureCodeRange(UMLClass c) {
        UMLJavadoc javadoc = c.getJavadoc();
        int annotationsStartLine = javadoc == null ? c.codeRange().getStartLine() : c.getJavadoc().codeRange().getEndLine();
        int classSignatureLine =  annotationsStartLine + c.getAnnotations().size();
        return copyCodeRange(c.codeRange(), classSignatureLine, classSignatureLine);
    }

    private CodeRange copyCodeRange(CodeRange codeRange, int startLine, int endLine) {
        return new CodeRange(
            codeRange.getFilePath(),
            startLine,
            endLine,
            codeRange.getStartColumn(),
            codeRange.getEndColumn(),
            codeRange.getCodeElementType()
        );
    }

    private ArrayList<CodeRange> removeCodeRangeDuplicates(ArrayList<CodeRange> refactoringChanges) {
        ArrayList<CodeRange> filteredList = new ArrayList<>();
        for (CodeRange a : refactoringChanges) {
            if (a == null) continue;
            Boolean contains = false;
            for (CodeRange b : filteredList)
                if (areCodeRangesEqual(a,b)) {
                    contains = true;
                    continue;
                }
            if(!contains) {
                filteredList.add(a);
            }
        }
        return filteredList;
    }

    private Boolean areCodeRangesEqual(CodeRange a, CodeRange b) {
        return Objects.equals(a.getStartLine(),b.getStartLine())
            && Objects.equals(a.getEndLine(),b.getEndLine())
            && Objects.equals(a.getFilePath(),b.getFilePath());
    }

    private Boolean areClassesEqualIgnoringName(UMLClass a, UMLClass b) {
        return Objects.equals(a.getTypeParameters(), b.getTypeParameters())
            && Objects.equals(a.getVisibility(),b.getVisibility())
            && Objects.equals(a.getImplementedInterfaces(),b.getImplementedInterfaces())
            && Objects.equals(a.getSuperclass(),b.getSuperclass());
    }

    private Boolean areVariablesEqualIgnoringName(VariableDeclaration a, VariableDeclaration b) {
        return Objects.equals(a.getType(),b.getType())
            && Objects.equals(a.getInitializer(),b.getInitializer())
            && Objects.equals(a.getAnnotations(),b.getAnnotations());
    }

    private Boolean areAttributesEqualIgnoringFinalAndVisibility(UMLAttribute a, UMLAttribute b) {
        return Objects.equals(a.getAnnotations(),b.getAnnotations())
            && Objects.equals(a.getName(),b.getName())
            && Objects.equals(a.getType(),b.getType())
            && Objects.equals(a.isStatic(), b.isStatic())
            && Objects.equals(a.getVariableDeclaration().getInitializer(),b.getVariableDeclaration().getInitializer());
    }

    private Boolean areAttributesEqualIgnoringNameFinalAndVisibility(UMLAttribute a, UMLAttribute b) {
        return Objects.equals(a.getAnnotations(),b.getAnnotations())
            && Objects.equals(a.getType(),b.getType())
            && Objects.equals(a.isStatic(), b.isStatic())
            && Objects.equals(a.getVariableDeclaration().getInitializer(),b.getVariableDeclaration().getInitializer());
    }
}
