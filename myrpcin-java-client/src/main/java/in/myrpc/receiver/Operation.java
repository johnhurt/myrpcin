package in.myrpc.receiver;

import in.myrpc.MyRpcException;

/**
 * Representation of an programmatic operation of an operator on two operands
 * @author kguthrie
 */
public class Operation {

    public static final int CONCATENATE = 1;
    public static final int ASSIGN = 2;
    public static final int CALL = 3;

    private final int operator;
    private final Operand left;
    private final Operand right;

    public Operation(int operator, Operand left, Operand right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    public StringBuilder evaluate(ScriptEnvironment env)
            throws MyRpcException {
        switch(operator) {
            case ASSIGN: {
                return assign(env);
            }
            case CONCATENATE: {
                return concat(env);
            }
            case CALL: {
                return call(env);
            }
        }

        throw new MyRpcException("Unknown operation type: " + operator);
    }

    /**
     * evaluate the value of the given operand
     * @param operand
     * @return
     */
    private StringBuilder evaluateOperandAsString(Operand operand,
            ScriptEnvironment env) throws MyRpcException {

        switch (operand.getType()) {
            case Operand.OPERATION: {
                return operand.getOpValue().evaluate(env);
            }
            case Operand.STRING_CONSTANT: {
                return new StringBuilder(operand.getStrValue());
            }
            case Operand.VARIABLE_NAME: {
                return env.getVariableValueBuilder(operand.getStrValue());
            }
        }

        throw new MyRpcException("Scripting runtime exception.  Cannot "
                + "evaluate operand as a string " + operand.toString());
    }

    /**
     * execute the given operation as an assignment
     * @param env
     */
    private StringBuilder assign(ScriptEnvironment env) throws MyRpcException {
        String variableName = left.getStrValue();
        StringBuilder result = evaluateOperandAsString(right, env);

        env.setVariableValue(variableName, result.toString());

        return result;
    }

    /**
     * evaluate the concatenation of the left and right operands
     * @param env
     * @return
     * @throws MyRpcException
     */
    private StringBuilder concat(ScriptEnvironment env) throws MyRpcException {
        StringBuilder result = evaluateOperandAsString(left, env);
        result.append(evaluateOperandAsString(right, env));

        return result;

    }

    /**
     * evaluate the this operand as a function call with the left indicating the
     * function and the right indicating the array of operands making up the
     * arguments
     * @return
     */
    private StringBuilder call(ScriptEnvironment env) throws MyRpcException {
        int function = left.getIntVal();

        String[] arguments = null;

        if (right != null) {
            switch (right.getType()) {
                case Operand.ARRAY: {
                    Operand[] opArgs = right.getArrayValue();
                    arguments = new String[opArgs.length];

                    for (int i = 0; i < arguments.length; i++) {
                        StringBuilder curr =
                                evaluateOperandAsString(opArgs[i], env);
                        arguments[i] = curr == null ? null : curr.toString();
                    }

                    break;
                }
                case Operand.STRING_CONSTANT:
                case Operand.VARIABLE_NAME:
                case Operand.OPERATION: {
                    StringBuilder value = evaluateOperandAsString(right, env);

                    arguments = new String[] {
                        (value != null ? value.toString() : null)
                    };

                    break;
                }
            }
        }

        switch (function) {
            case ScriptEnvironment.RANDOM: {
                return env.random(arguments);
            }
            case ScriptEnvironment.REGEX: {
                return env.regex(arguments);
            }
            case ScriptEnvironment.GET: {
                return env.get(arguments);
            }
            case ScriptEnvironment.POST: {
                return env.post(arguments);
            }
            case ScriptEnvironment.TOKEN: {
                return env.getToken();
            }
            case ScriptEnvironment.INC: {
                return env.increment(arguments);
            }
            case ScriptEnvironment.IF: {
                return env.doIf(arguments);
            }
            case ScriptEnvironment.LABEL: {
                return new StringBuilder();
            }
            case ScriptEnvironment.GOTO: {
                return env.doGoto(arguments);
            }
            case ScriptEnvironment.DEC: {
                return env.decrement(arguments);
            }
            case ScriptEnvironment.ON_OPEN: {
                return env.onOpen();
            }
            case ScriptEnvironment.ON_MESSAGE: {
                return env.onMessage(arguments);
            }
            case ScriptEnvironment.ON_ERROR: {
                return env.onError(arguments);
            }
        }

        throw new MyRpcException("Uknown function " + function);
    }
}
