package ast.variables;

import ir.commands.arithmetic.IRBinOpRightConstCommand;
import ir.commands.arithmetic.Operation;
import ir.commands.memory.IRLoadCommand;
import ir.commands.memory.IRStoreCommand;
import ir.registers.Register;
import ir.utils.IRContext;
import symbols.Symbol;
import symbols.SymbolTable;
import types.TypeClass;
import utils.NotNull;
import utils.errors.SemanticException;

import java.util.function.Supplier;

public class AST_VAR_FIELD extends AST_VAR {
    @NotNull
    public AST_VAR var;
    @NotNull
    public String fieldName;
    private Symbol symbol;

    public AST_VAR_FIELD(@NotNull AST_VAR var, @NotNull String fieldName) {
        this.var = var;
        this.fieldName = fieldName;
    }

    @NotNull
    @Override
    protected String name() {
        return "VAR(_." + fieldName + ")";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(var);
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        var.semant(symbolTable);
        if (!var.getType().isClass()) {
            throwSemantic("Trying to access the field \"" + fieldName + "\" on non-class type: " + var.getType());
        }

        symbol = ((TypeClass) var.getType()).queryFieldRecursively(fieldName);
        if (symbol == null) {
            throwSemantic("Trying to access the non-existent field \"" + fieldName + "\" on type: " + var.getType());
        }
        type = symbol.type;
    }

    @Override
    public @NotNull Register irMe(IRContext context) {
        assert symbol != null && symbol.instance != null;

        Register instance = var.irMe(context);
        context.checkNotNull(instance);

        int fieldOffset = context.getFieldOffset(symbol);
        Register temp1 = context.newRegister();
        context.command(new IRBinOpRightConstCommand(temp1, instance, Operation.Plus, fieldOffset)); // temp1 hold address of variable
        Register temp2 = context.newRegister();
        context.command(new IRLoadCommand(temp2, temp1)); // temp2 hold variable
        return temp2;
    }

    @Override
    public void irAssignTo(IRContext context, Supplier<Register> data) {
        assert symbol != null;

        Register instance = var.irMe(context);
        context.checkNotNull(instance);

        Register content = data.get();

        int fieldOffset = context.getFieldOffset(symbol);
        Register temp1 = context.newRegister();

        context.command(new IRBinOpRightConstCommand(temp1, instance, Operation.Plus, fieldOffset)); // temp1 hold address of variable
        context.command(new IRStoreCommand(temp1, content));

    }
}
