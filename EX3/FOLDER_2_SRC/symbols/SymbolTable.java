package symbols;

import types.TYPE_FOR_SCOPE_BOUNDARIES;
import types.TYPE_FOR_SCOPE_BOUNDARIES.Scope;
import types.Type;
import types.TypeClass;
import types.TypeFunction;
import types.builtins.TypeArray;
import types.builtins.TypeInt;
import types.builtins.TypeString;
import types.builtins.TypeVoid;
import utils.NotNull;
import utils.Nullable;
import utils.Utils;

import java.io.PrintWriter;
import java.util.function.Predicate;

public class SymbolTable {
    private int hashArraySize = 13;
    private SymbolTableEntry[] table = new SymbolTableEntry[hashArraySize];
    private SymbolTableEntry top;
    private int topIndex = 0;
    private TypeClass enclosingClass;
    private TypeFunction enclosingFunction;
    private boolean isClassScanning = false;

    private int hash(String s) {
        if (s.charAt(0) == 'l') {
            return 1;
        }
        if (s.charAt(0) == 'm') {
            return 1;
        }
        if (s.charAt(0) == 'r') {
            return 3;
        }
        if (s.charAt(0) == 'i') {
            return 6;
        }
        if (s.charAt(0) == 'd') {
            return 6;
        }
        if (s.charAt(0) == 'k') {
            return 6;
        }
        if (s.charAt(0) == 'f') {
            return 6;
        }
        if (s.charAt(0) == 'S') {
            return 6;
        }
        return 12;
    }

    public void enter(String name, Type t) {
        enter(name, t, false);
    }

    public void enter(String name, Type t, boolean isVariableDeclaration) {
        int hashValue = hash(name);
        SymbolTableEntry next = table[hashValue];
        SymbolTableEntry e = new SymbolTableEntry(name, t, hashValue, next, top, topIndex++, isVariableDeclaration);
        top = e;
        table[hashValue] = e;

        PrintMe();
    }

    /**
     * Find the inner-most scope element with given name, returning null if not found
     */
    @Nullable
    public Type find(@NotNull String name) {
        return find(name, null);
    }

    /**
     * Find the inner-most scope element with given name, returning null if not found
     *
     * @param filter Applying filter on results, returns element only if accepted by the filter.
     */
    @Nullable
    public Type find(@NotNull String name, @Nullable Predicate<SymbolTableEntry> filter) {
        SymbolTableEntry e;

        for (e = table[hash(name)]; e != null; e = e.next) {
            if (name.equals(e.name) && (filter == null || filter.test(e))) {
                return e.type;
            }
        }

        return null;
    }

    /**
     * Find the inner-most scope element with given name, returning null if not found or if went outside an enclosing scope.
     */
    public Type findInCurrentEnclosingScope(@NotNull String name) {
        SymbolTableEntry e;

        for (e = table[hash(name)]; e != null; e = e.next) {
            if (name.equals(e.name)) {
                return e.type;
            } else if (e.type instanceof TYPE_FOR_SCOPE_BOUNDARIES) {
                Scope scope = ((TYPE_FOR_SCOPE_BOUNDARIES) e.type).scope;
                if (scope != Scope.Block)
                    return null;
            }
        }

        return null;
    }

    /**
     * Find the inner-most scope element with given name, returning null if not found or if went outside of the current scope.
     */
    public Type findInCurrentScope(@NotNull String name) {
        SymbolTableEntry e;

        for (e = table[hash(name)]; e != null; e = e.next) {
            if (name.equals(e.name)) {
                return e.type;
            } else if (e.type instanceof TYPE_FOR_SCOPE_BOUNDARIES) {
                    return null;
            }
        }

        return null;
    }

    /**
     * Find the inner-most scope element with name that is a function, returning null if not found.
     *
     * @param startSearchingOutsideClass Whether or not to skip a method that is defined inside class scope
     */
    @Nullable
    public TypeFunction findMethod(@NotNull String name, boolean startSearchingOutsideClass) {
        if (!startSearchingOutsideClass && enclosingClass != null) {
            TypeFunction type = enclosingClass.queryMethodRecursively(name);
            if (type != null)
                return type;
        }

        return (TypeFunction) find(name, entry -> !entry.isVariableDeclaration && entry.type.isFunction());
    }

    /**
     * Find the inner-most scope element with name that is a field, returning null if not found.
     *
     * @param startSearchingOutsideClass Whether or not to skip a method that is defined inside class scope
     */
    @Nullable
    public Type findField(@NotNull String name, boolean startSearchingOutsideClass) {
        if (!startSearchingOutsideClass && enclosingClass != null) {
            Type type = enclosingClass.queryFieldRecursively(name);
            if (type != null)
                return type;
        }

        return find(name, entry -> entry.isVariableDeclaration);
    }

    /**
     * Find a class with the given name, returning null if not found.
     */
    @Nullable
    public TypeClass findClassType(@NotNull String name) {
        return (TypeClass) find(name, entry -> !entry.isVariableDeclaration && entry.type.isClass());
    }

    /**
     * Find an array with the given name, returning null if not found.
     */
    @Nullable
    public TypeArray findArrayType(@NotNull String name) {
        return (TypeArray) find(name, entry -> !entry.isVariableDeclaration && entry.type.isArray());
    }


    /**
     * Find generalized type with the given name:
     * - string, int, void
     * - array type
     * - class type
     */
    @Nullable
    public Type findGeneralizedType(@NotNull String name) {
        if (name.equals(TypeInt.instance.name))
            return TypeInt.instance;
        else if (name.equals(TypeString.instance.name))
            return TypeString.instance;
        if (name.equals(TypeVoid.instance.name))
            return TypeVoid.instance;

        Type arrayType = findArrayType(name);
        if (arrayType != null)
            return arrayType;

        return findClassType(name);
    }

    /**
     * Return the enclosing class for the current state of the symbol table, returning null if no such class.
     */
    @Nullable
    public TypeClass getEnclosingClass() {
        return enclosingClass;
    }

    /**
     * Return the enclosing function for the current state of the symbol table, returning null if no such function.
     */
    @Nullable
    public TypeFunction getEnclosingFunction() {
        return enclosingFunction;
    }


    public void beginScope(@NotNull Scope scope, @Nullable Type enclosingType) {
        beginScope(scope, enclosingType, "NONE");
    }
    /**
     * Begins a new scope by adding <SCOPE-BOUNDARY> to the Hashmap.
     * In addition, if it is a class or function scope, update the enclosing field.
     */
    public void beginScope(@NotNull Scope scope, @Nullable Type enclosingType, String debugInfo) {
        enter("SCOPE-BOUNDARY", new TYPE_FOR_SCOPE_BOUNDARIES(debugInfo, scope));

        if (scope == Scope.Function) {
            enclosingFunction = (TypeFunction) enclosingType;
        } else if (scope == Scope.Class) {
            enclosingClass = (TypeClass) enclosingType;
        } else if (scope == Scope.ClassScan) {
            isClassScanning = true;
            enclosingClass = (TypeClass) enclosingType;

        }

        PrintMe();
    }

    /**
     * Ending the current scope by popping all the symbols in the current scope.
     * If the current scope with {@link Scope#ClassScan} it will also register all the fields and method to the class.
     */
    public void endScope() {
        // Pop elements from the symbol table stack until a SCOPE-BOUNDARY is hit */
        while (!top.name.equals("SCOPE-BOUNDARY")) {
            if (isClassScanning) {
                // register the method/field into the class type.
                Type declaration = top.type;
                if (declaration.isFunction()) {
                    enclosingClass.registerMethod(top.name, (TypeFunction) declaration);
                } else {
                    enclosingClass.registerField(top.name, declaration);
                }
            }

            table[top.index] = top.next;
            topIndex = topIndex - 1;
            top = top.prevtop;
        }

        Scope scope = ((TYPE_FOR_SCOPE_BOUNDARIES) top.type).scope;
        if (scope == Scope.Function) {
            enclosingFunction = null;
        } else if (scope == Scope.Class || scope == Scope.ClassScan) {
            enclosingClass = null;
        }

        table[top.index] = top.next;
        topIndex = topIndex - 1;
        top = top.prevtop;

        PrintMe();
    }

    public static int n = 0;

    public void PrintMe() {
        int i = 0;
        int j = 0;
        String dirname = "./FOLDER_5_OUTPUT/";
        String filename = String.format("SYMBOL_TABLE_%d_IN_GRAPHVIZ_DOT_FORMAT.txt", n++);

        try {
            /*******************************************/
            /* [1] Open Graphviz text file for writing */
            /*******************************************/
            PrintWriter fileWriter = new PrintWriter(dirname + filename);

            /*********************************/
            /* [2] Write Graphviz dot prolog */
            /*********************************/
            fileWriter.print("digraph structs {\n");
            fileWriter.print("rankdir = LR\n");
            fileWriter.print("node [shape=record];\n");

            /*******************************/
            /* [3] Write Hash Table Itself */
            /*******************************/
            fileWriter.print("hashTable [label=\"");
            for (i = 0; i < hashArraySize - 1; i++) {
                fileWriter.format("<f%d>\n%d\n|", i, i);
            }
            fileWriter.format("<f%d>\n%d\n\"];\n", hashArraySize - 1, hashArraySize - 1);

            /****************************************************************************/
            /* [4] Loop over hash table array and print all linked lists per array cell */
            /****************************************************************************/
            for (i = 0; i < hashArraySize; i++) {
                if (table[i] != null) {
                    /*****************************************************/
                    /* [4a] Print hash table array[i] -> entry(i,0) edge */
                    /*****************************************************/
                    fileWriter.format("hashTable:f%d -> node_%d_0:f0;\n", i, i);
                }
                j = 0;
                for (SymbolTableEntry it = table[i]; it != null; it = it.next) {
                    /*******************************/
                    /* [4b] Print entry(i,it) node */
                    /*******************************/
                    fileWriter.format("node_%d_%d ", i, j);
                    fileWriter.format("[label=\"<f0>%s|<f1>%s|<f2>prevtop=%d|<f3>next\"];\n",
                            it.name,
                            it.type.name,
                            it.prevtop_index);

                    if (it.next != null) {
                        /***************************************************/
                        /* [4c] Print entry(i,it) -> entry(i,it.next) edge */
                        /***************************************************/
                        fileWriter.format(
                                "node_%d_%d -> node_%d_%d [style=invis,weight=10];\n",
                                i, j, i, j + 1);
                        fileWriter.format(
                                "node_%d_%d:f3 -> node_%d_%d:f0;\n",
                                i, j, i, j + 1);
                    }
                    j++;
                }
            }
            fileWriter.print("}\n");
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SymbolTable instance = null;

    protected SymbolTable() {
    }


    public static SymbolTable getInstance() {
        if (instance == null) {
            instance = new SymbolTable();

            instance.enter("int", TypeInt.instance);
            instance.enter("string", TypeString.instance);
            instance.enter(
                    "PrintInt",
                    new TypeFunction(
                            "PrintInt",
                            TypeVoid.instance,
                            Utils.mutableListOf(TypeInt.instance)));

        }
        return instance;
    }
}
