package in.myrpc.receiver;

/**
 * Representation of a single operand for an operator
 * @author kguthrie
 */
public class Operand {

    public static final int OPERATION = 1;
    public static final int STRING_CONSTANT = 2;
    public static final int VARIABLE_NAME = 3;
    public static final int ARRAY = 4;
    public static final int INTEGER = 5;

    private final int type;
    private final int intVal;
    private final String strValue;
    private final Operand[] arrayValue;
    private final Operation opValue;

    /**
     * Constructor for an integer constant
     * @param value
     */
    public Operand(int value) {
        this(INTEGER, null, null, null, value);
    }

    /**
     * Constructor for variable name or string constant
     * @param value
     * @param type
     */
    public Operand(String value, int type) {
        this(type, value, null, null, -1);
    }

    /**
     * Constructor for operand with an array list
     * @param value
     */
    public Operand(Operand[] value) {
        this(ARRAY, null, value, null, -1);
    }

    /**
     * Constructor for an operand that is a sub operation
     * @param operation
     */
    public Operand(Operation operation) {
        this(OPERATION, null, null, operation, -1);
    }


    /**
     * private complete constructor
     * @param type
     * @param strValue
     * @param arrayValue
     * @param opValue
     */
    private Operand(int type, String strValue, Operand[] arrayValue,
            Operation opValue, int intVal) {
        this.type = type;
        this.strValue = strValue;
        this.arrayValue = arrayValue;
        this.opValue = opValue;
        this.intVal = intVal;
    }



    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @return the strValue
     */
    public String getStrValue() {
        return strValue;
    }

    /**
     * @return the arrayValue
     */
    public Operand[] getArrayValue() {
        return arrayValue;
    }

    /**
     * @return the opValue
     */
    public Operation getOpValue() {
        return opValue;
    }

    /**
     * @return the intVal
     */
    public int getIntVal() {
        return intVal;
    }

}
