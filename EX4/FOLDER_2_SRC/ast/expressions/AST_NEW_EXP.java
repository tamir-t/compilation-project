package ast.expressions;

import symbols.SymbolTable;
import types.Type;
import utils.NotNull;
import utils.errors.SemanticException;

public class AST_NEW_EXP extends AST_EXP {
    @NotNull
    public String className;

    public AST_NEW_EXP(@NotNull String className) {
        this.className = className;
    }

    @NotNull
    @Override
    protected String name() {
        return "new " + className;
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        Type classType = symbolTable.findClassType(className);
        if (classType == null) {
            throwSemantic("Trying to create a new expression of non class type: \"" + className + "\".");
        } else {
            type = classType;
        }
    }
}
