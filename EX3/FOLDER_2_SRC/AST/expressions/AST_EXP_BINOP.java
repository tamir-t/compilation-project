package ast.expressions;

import symbols.SymbolTable;
import types.TypeClass;
import types.TypeError;
import types.builtins.TypeInt;
import types.builtins.TypeNil;
import types.builtins.TypeString;
import utils.NotNull;
import utils.SemanticException;

public class AST_EXP_BINOP extends AST_EXP {
    @NotNull
    public Op op;
    @NotNull
    public AST_EXP left;
    @NotNull
    public AST_EXP right;


    public AST_EXP_BINOP(@NotNull AST_EXP left, @NotNull AST_EXP right, @NotNull Op op) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    @NotNull
    @Override
    protected String name() {
        return op.text;
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(left);
        printAndEdge(right);
    }

    public enum Op {
        Plus("+"), Minus("-"), Times("*"), Divide("/"), LT("<"), GT(">"), EQ("=");
        @NotNull
        String text;

        Op(@NotNull String text) {
            this.text = text;
        }
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        left.semant(symbolTable);
        right.semant(symbolTable);

        if (left.type == TypeError.instance || right.type == TypeError.instance) {
            type = TypeError.instance;
        }

        if (op == Op.EQ) {
            /* Section 3.5 in manual
             * NIL <=> array, class
             * array <=> same type of array
             * class <=> assignable class
             * int <=> int
             * str <=> str
             *
             * returns int
             */
            if (!left.getType().isAssignableFrom(right.getType()) && !right.getType().isAssignableFrom(left.getType())) {
                throwSemantic("Trying to compare an object of type " + left.getType() + " to object of type: " + right.getType());
            }
            type = TypeInt.instance;
        } else if (op != Op.Plus) {
            /* Section 3.6 in manual
             * int OP int => int
             */
            if (left.type != TypeInt.instance) {
                throwSemantic("Trying to apply binary operation " + op.text + " but left expression is not int: " + left);
            } else if (right.type != TypeInt.instance) {
                throwSemantic("Trying to apply binary operation " + op.text + " but right expression is not int: " + right);
            } else {
                type = TypeInt.instance;
            }
        } else {
            /* Section 3.6 in manual
             * int + int => int
             * str + str => str
             */
            if (left.type == TypeInt.instance && right.type == TypeInt.instance) {
                type = TypeInt.instance;
            } else if (left.type == TypeString.instance && right.type == TypeString.instance) {
                type = TypeString.instance;
            } else {
                throwSemantic("Trying to apply binary operation + but typing is incorrect: left is " + left.type + " and right is " + right.type);
            }
        }
    }
}
