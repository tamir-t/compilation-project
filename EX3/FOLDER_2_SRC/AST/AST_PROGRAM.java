package ast;

import ast.declarations.AST_DEC;
import utils.NotNull;

import java.util.List;

public class AST_PROGRAM extends AST_Node {
    @NotNull
    public AST_DEC[] declarations;

    public AST_PROGRAM(@NotNull AST_DEC[] declarations) {
        this.declarations = declarations;
    }

    @NotNull
    @Override
    protected String name() {
        return "Program";
    }

    @Override
    public void printMe() {
        super.printMe();
        for (AST_DEC declaration : declarations) {
            printAndEdge(declaration);
        }
    }
}
