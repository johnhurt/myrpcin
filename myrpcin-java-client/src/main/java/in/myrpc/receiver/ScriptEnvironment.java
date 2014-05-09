package in.myrpc.receiver;

import com.google.common.collect.Maps;
import in.myrpc.MyRpcException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains the state information about the script being used to
 * receive and process messages.
 *
 * Grammar
 * instructionLine := {assignment} | {functionCall};
 * assignment := {result} = {expression}
 * result := {variableName} {optionalResult}
 * optionalResult := nil | , {result}
 * expression := {expressionTerm} {optionalExpressionTerm}
 * optionalExpressionTerm := nil | + {expression}
 * expressionTerm := {functionCall} | {stringConstant} | {variableName}
 * variableName := [a-zA-Z_][a-zA-Z_0-9]*
 * stringConstant := \"^\".*\"
 * functionCall := {functionName} ({arguments})
 * functionName := GET | POST | random | regex
 * arguments := nil | {mandantoryArguments}
 * mandantoryArguments := {argument} {optionalArguments}
 * argument := {stringConstant} | {variableName}
 * optionalArguments := nil | , {mandantoryArguments}
 *
 * @author kguthrie
 */
public class ScriptEnvironment implements Runnable {

    public static final int EOF = -1;
    public static final int VARIABLE_NAME = 0;
    public static final int QUOTED_STRING = 1;
    public static final int L_PAREN = 2;
    public static final int R_PAREN = 3;
    public static final int SEMI_COLON = 4;
    public static final int COMMA = 5;
    public static final int EQUALS = 6;
    public static final int PLUS = 7;
    public static final int GET = 8;
    public static final int POST = 9;
    public static final int RANDOM = 10;
    public static final int REGEX = 11;
    public static final int TOKEN = 12;
    public static final int INC = 13;
    public static final int IF = 14;
    public static final int LABEL = 15;
    public static final int GOTO = 16;
    public static final int DEC = 17;

    private final String fullScript;
    private final Map<String, String> variables;
    private final Operation[] instructions;
    private final Random random;
    private final Map<String, Pattern> regexCache;
    private final Map<String, Matcher> matcherCache;
    private final HttpClientProxy client;
    private final Map<String, Integer> labels;
    private int currentEvaluationPointer;
    private String token;

    private boolean stopRequested;

    public ScriptEnvironment(String scriptBody) throws MyRpcException {
        stopRequested = false;
        variables = Maps.newHashMap();
        regexCache = Maps.newHashMap();
        matcherCache = Maps.newHashMap();
        labels = Maps.newHashMap();
        client = new HttpClientProxy();
        this.fullScript = scriptBody;
        currentEvaluationPointer = 0;

        random = new Random(System.currentTimeMillis());

        try {
            instructions = parseInstructions();
        }
        catch(IOException ex) {
            throw new MyRpcException("Falied to parse script", ex);
        }
        catch (MyRpcException ex) {
            throw new MyRpcException("Falied to parse script", ex);
        }
    }

    /**
     * Get the value for the variable with the given name
     * @param name
     * @return
     */
    public StringBuilder getVariableValueBuilder(String name) {
        String result = variables.get(name);
        return result == null ? new StringBuilder() : new StringBuilder(result);
    }

    /**
     * Get the value for the variable with the given name
     * @param name
     * @return
     */
    public String getVariableValue(String name) {
        return variables.get(name);
    }

    /**
     * Set the value of the variable with the given name to the given value
     * @param variableName
     * @param value
     * @return the previous value of the variable or null if it didn't exist
     */
    public String setVariableValue(String variableName, String value) {

        if (value != null && value.length() > 0) {
            System.out.println(variableName + " => " + value + "\n");
        }

        return variables.put(variableName, value);
    }

    /**
     * read the script body and return the parsed instructions as an array
     * @return
     */
    private Operation[] parseInstructions()
            throws IOException, MyRpcException {

        StringBuilder tokenBuffer = new StringBuilder();
        int[] cursor = new int[] {0};

        List<Operation> operations = new ArrayList<Operation>();
        Operation operation;

        currentEvaluationPointer = 0;

        while ((operation = parseInstruction(
                getFullScript(), cursor, tokenBuffer)) != null) {
            operations.add(operation);
            currentEvaluationPointer++;
        }

        Operation[] result = new Operation[operations.size()];
        result = operations.toArray(result);

        return result;
    }

    /**
     * Parse the line of script and return an executable instruction
     * @param source
     * @return
     */
    private Operation parseInstruction(String source,
            int[] cursor, StringBuilder tokenBuffer)
            throws MyRpcException {

        Operand lhs = parseLhs(source, cursor, tokenBuffer);
        Operand rhs = parseRhs(source, cursor, tokenBuffer);

        if (lhs == null && rhs == null) {
            return null; // (end of file)
        }

        int expectingSemiColon = consumeToken(source, cursor,
                tokenBuffer);

        if (expectingSemiColon != SEMI_COLON) {
            throw new MyRpcException("Missing semicolon");
        }

        if (lhs == null) {
            return rhs.getOpValue();
        }

        return new Operation(Operation.ASSIGN, lhs, rhs);
    }

    /**
     * parse the left hand side of an assignment.  The return of this method
     * will be a variable operand if there is one or null
     * @param source
     * @param cursor
     * @param buffer
     * @return
     */
    private Operand parseLhs(String source, int[] cursor,
            StringBuilder buffer) throws MyRpcException {
        int firstToken = peakToken(source, cursor, buffer);
        buffer.setLength(0);

        // If the first token is not a variable name there is nothing being
        // assigned, so this statement has no left hand side
        if (firstToken != VARIABLE_NAME) {
            return null;
        }

        consumeToken(source, cursor, buffer);

        String variableName = buffer.toString();

        buffer.setLength(0);

        int secondToken = consumeToken(source, cursor, buffer);

        // If the second token is not an equals, then this is not actually an
        // assignment even though it started out like one
        if (secondToken != EQUALS) {
            buffer.setLength(0);
            cursor[0] = 0;
            return null;
        }

        return new Operand(variableName, Operand.VARIABLE_NAME);
    }

    /**
     * parse the right hand side of an assignment.  The return the operation
     * whose result will be assigned to the left hand side
     * @param source
     * @param cursor
     * @param buffer
     * @return
     */
    private Operand parseRhs(String source, int[] cursor,
            StringBuilder buffer) throws MyRpcException {

        int firstToken = consumeToken(source, cursor, buffer);
        Operand firstOperand = null;

        switch (firstToken) {
            case EOF: {
                return null;
            }
            case VARIABLE_NAME: {
                String variableName = buffer.toString();
                buffer.setLength(0);
                firstOperand = new Operand(variableName, Operand.VARIABLE_NAME);
                break;
            }
            case QUOTED_STRING: {
                String constant = buffer.toString();
                buffer.setLength(0);
                firstOperand = new Operand(constant, Operand.STRING_CONSTANT);
                break;
            }
            case LABEL: {
                buffer.setLength(0);
                Operand arguments
                        = parseFunctionArguments(source, cursor, buffer);
                if (arguments == null
                        || arguments.getArrayValue().length != 1
                        || arguments.getArrayValue()[0].getType()
                                != Operand.STRING_CONSTANT) {
                    throw new MyRpcException("Parse error.  Label requires a "
                            + "single string constant as an arguemtn");
                }

                labels.put(arguments.getArrayValue()[0].getStrValue(),
                        currentEvaluationPointer);

                firstOperand = new Operand(new Operation(
                        Operation.CALL, new Operand(firstToken),
                                new Operand(new Operand[] {})));
                break;
            }
            case GET:
            case POST:
            case REGEX:
            case RANDOM:
            case TOKEN:
            case INC:
            case IF:
            case GOTO:
            case DEC: {
                buffer.setLength(0);
                Operand arguments
                        = parseFunctionArguments(source, cursor, buffer);
                firstOperand = new Operand(new Operation(
                        Operation.CALL, new Operand(firstToken), arguments));
                break;
            }
        }

        if (peakToken(source, cursor, buffer) == PLUS) {
            consumeToken(source, cursor, buffer);
            return new Operand(new Operation(Operation.CONCATENATE,
                    firstOperand, parseRhs(source, cursor, buffer)));
        }

        return firstOperand;
    }

    /**
     * Parse the arguments of a function accepting anything in the form:
     *
     * L_PAREN R_PAREN
     *
     * or
     *
     * L_PAREN RHS (COMMA RHS)* R_PAREN
     *
     * @param source
     * @param cursor
     * @param buffer
     * @return
     * @throws MyRpcException
     */
    private Operand parseFunctionArguments(String source, int[] cursor,
            StringBuilder buffer) throws MyRpcException {

        int firstToken = consumeToken(source, cursor, buffer);
        Operand currOperand;
        List<Operand> resultList = new ArrayList<Operand>();

        if (firstToken != L_PAREN) {
            throw new MyRpcException("Syntax error: expected L_PAREN in line "
                    + source);
        }

        int currToken = peakToken(source, cursor, buffer);

        // If the next token is a close paren, then there are no arguments.
        if (currToken == R_PAREN) {
            consumeToken(source, cursor, buffer);
            return new Operand(new Operand[] {});
        }

        buffer.setLength(0);

        do {
            currOperand = parseRhs(source, cursor, buffer);
            resultList.add(currOperand);
        } while (consumeToken(source, cursor, buffer) == COMMA);

        return new Operand(resultList.toArray(new Operand[] {}));
    }

    /**
     * Read the next token without moving the cursor
     * @param source
     * @param cursorLocation
     * @param target
     * @return
     * @throws MyRpcException
     */
    private int peakToken(String source, int[] cursorLocation,
            StringBuilder target) throws MyRpcException {
        int cursor = cursorLocation[0];
        int result = consumeToken(source, cursorLocation, target);
        cursorLocation[0] = cursor;

        return result;
    }

    /**
     * consume the next token from the given source string starting at the given
     * cursor location
     * @param source string fromwhich the token should be drawn
     * @param cursorLocation int position to start looking for a token
     * @param target token is consumed into this buffer
     * @return token type as a string
     */
    private int consumeToken(String source, int[] cursorLocation,
            StringBuilder target) throws MyRpcException {

        int c;
        char ch = 0;
        char prevCh;
        boolean inQuotes = false;
        boolean inToken = false;

        while ((cursorLocation[0] < source.length()) &&
                (c = source.charAt(cursorLocation[0]++)) >= 0) {

            prevCh = ch;
            ch = (char) c;

            // String tokens can have a letter or number or a '_' after the
            // first character
            if (inToken) {
                if (Character.isLetterOrDigit(ch) || ch == '_') {
                    target.append(ch);
                    continue;
                }

                if (inQuotes) {
                    if ('"' == ch && '\\' != prevCh) {
                        return QUOTED_STRING;
                    }
                    if ('\\' == ch) {
                        if ('\\' != prevCh) {
                            continue;
                        }
                        else {
                            ch = 0;
                            target.append('\\');
                            continue;
                        }
                    }
                    target.append(ch);
                    continue;
                }

                cursorLocation[0]--;
                String value = target.toString();

                if ("get".equalsIgnoreCase(value)) {
                    return GET;
                }
                if ("post".equalsIgnoreCase(value)) {
                    return POST;
                }
                if ("random".equalsIgnoreCase(value)) {
                    return RANDOM;
                }
                if ("regex".equalsIgnoreCase(value)) {
                    return REGEX;
                }
                if ("token".equalsIgnoreCase(value)) {
                    return TOKEN;
                }
                if ("inc".equalsIgnoreCase(value)) {
                    return INC;
                }
                if ("if".equalsIgnoreCase(value)) {
                    return IF;
                }
                if ("label".equalsIgnoreCase(value)) {
                    return LABEL;
                }
                if ("goto".equalsIgnoreCase(value)) {
                    return GOTO;
                }
                if ("dec".equalsIgnoreCase(value)) {
                    return DEC;
                }

                return VARIABLE_NAME;
            }

            if (Character.isWhitespace(ch) && !inQuotes) {
                continue;
            }

            // Don't worry about dealing with the target for these
            switch (ch) {
                case '(': return L_PAREN;
                case ')': return R_PAREN;
                case '+': return PLUS;
                case '=': return EQUALS;
                case ',': return COMMA;
                case ';': return SEMI_COLON;
            }

            if ('#' == ch) {
                consumeToEndOfLine(source, cursorLocation);
                continue;
            }

            // Muist be the start of a quoted string
            if (ch == '"') {
                if (!inQuotes) {
                    inToken = true;
                    inQuotes = true;
                    continue; // on to the next character, & don't add the quote
                }
                else {
                    throw new MyRpcException("Syntax Error.  Unexpected \" "
                            + "in instruction: " + source);
                }
            }

            // The start of a non quoted string has to be a letter or an _
            if (!inQuotes) {
                if (Character.isLetter(ch) || ch == '_') {
                    target.append(ch);
                    inToken = true;
                }
                else {
                    throw new MyRpcException("Syntax Error.  Invalid start of "
                            + "variable or or function in instruction: "
                            + source);
                }
            }
        }

        return EOF;
    }

    /**
     * Move the cursor until the end of the current line is found.
     * @param source
     * @param cursorLocation
     */
    public void consumeToEndOfLine(String source, int[] cursorLocation) {
        while (cursorLocation[0] < source.length() &&
                source.charAt(cursorLocation[0]++) != '\n') {}
    }

    /**
     * Evaluate the script in the current environment
     * @throws MyRpcException
     */
    public void evaluate() throws MyRpcException {

        for (currentEvaluationPointer = 0;
                currentEvaluationPointer < instructions.length;
                currentEvaluationPointer++) {
            if (stopRequested) {
                return;
            }

            instructions[currentEvaluationPointer].evaluate(this);
        }
    }

    /**
     * Run the current script with a new set of environment variables
     */
    public void run() {
        variables.clear();
        client.reset();
        stopRequested = false;

        try {
            evaluate();
        }
        catch (MyRpcException ex) {
            System.out.println("Error evaluting script: " + ex.getMessage());
            ex.printStackTrace();
        }

    }

    /**
     * Create a random string with some configurable options.
     *
     * args[0] -> int number of random characters (required)
     * args[1] -> boolean allow numbers (optional)
     * args[2] -> boolean allow uppercase (optional)
     * args[3] -> boolean allow lowercase (optional)
     *
     * @param args
     * @return
     * @throws in.myrpc.MyRpcException
     */
    public StringBuilder random(String[] args) throws MyRpcException {

        if (args == null || args.length < 1) {
            throw new MyRpcException("Method random requires one or more args");
        }

        int length = Integer.parseInt(args[0]);
        boolean allowNumbers = true;
        boolean allowUpperCase = true;
        boolean allowLowerCase = true;

        if (args.length > 1 && args[1] != null && args[1].trim().length() > 0) {
            char firstChar = Character.toLowerCase(args[1].trim().charAt(0));
            allowNumbers = !(firstChar == 'f' || firstChar == 'n');
        }

        if (args.length > 2 && args[2] != null && args[2].trim().length() > 0) {
            char firstChar = Character.toLowerCase(args[2].trim().charAt(0));
            allowUpperCase = !(firstChar == 'f' || firstChar == 'n');
        }

        if (args.length > 3 && args[3] != null && args[3].trim().length() > 0) {
            char firstChar = Character.toLowerCase(args[3].trim().charAt(0));
            allowLowerCase = !(firstChar == 'f' || firstChar == 'n');
        }


        int randMod = (allowLowerCase ? 26 : 0)
                + (allowNumbers ? 10 : 0)
                + (allowUpperCase ? 26 : 0);

        if (randMod == 0) {
            throw new MyRpcException(
                    "Random Strings need one allowed character class");
        }

        StringBuilder result = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int c = random.nextInt(randMod);

            if (allowLowerCase) {
                if (c < 26) {
                    result.append((char)('a' + c));
                    continue;
                }

                c -= 26;
            }

            if (allowNumbers) {
                if (c < 10) {
                    result.append((char)('0' + c));
                    continue;
                }
                c -= 10;
            }

            result.append((char)('A' + c));
        }

        return result;
    }

    /**
     * Regular expression method to extract the first capturing group of a
     * regular expression from a given string.  The arguments expected for this
     * method are:
     *
     * args[0] string regular expression to be used.
     * args[1] string to be searched for the given pattern
     * args[2] boolean stream search string for multiple matches (default false)
     *
     * All regular expressions are compiled with case insensitive multiline
     * options.
     *
     *
     * @param args
     * @return
     * @throws in.myrpc.MyRpcException
     */
    public StringBuilder regex(String[] args) throws MyRpcException {

        if (args == null || args.length < 2) {
            throw new MyRpcException("regexCompile expects at least 2 "
                    + "argument");
        }

        String patternString = args[0];
        String searchString = args[1];

        int groupNumber = 1;
        boolean stream = false;

        if (patternString == null || patternString.length() == 0) {
            throw new MyRpcException("regex requires valid pattern");
        }

        if (args.length > 2) {
            stream = args[2] != null
                    && args[2].length() > 0
                    &&  (Character.toLowerCase(args[2].charAt(0)) == 't'
                            || Character.toLowerCase(args[2].charAt(0)) == 'y');
        }

        if (searchString == null) {
            return new StringBuilder();
        }

        Matcher matcher = null;
        String matcherKey = patternString + searchString;
        if (stream) {
            matcher = matcherCache.get(matcherKey);
        }

        if (matcher == null) {

            Pattern pattern = regexCache.get(patternString);

            if (pattern == null) {
                pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE +
                        Pattern.MULTILINE);

                regexCache.put(patternString, pattern);
            }

            matcher = pattern.matcher(searchString);

            if (stream) {
                matcherCache.put(matcherKey, matcher);
            }
        }

        String matchResult = null;

        if (matcher.find()) {
            matchResult = matcher.group(groupNumber);
        }
        else if (stream) {
            matcherCache.remove(matcherKey);
        }

        return new StringBuilder(matchResult != null ? matchResult : "");
    }

    /**
     * Perform an http get operation and return the response body.  The
     * arguments excepted by this method are:
     *
     * args[0] = Url to GET from
     * args[1] = referer url
     * args[2] = minimum number of bytes to read from (default: 0 - no limit)
     * args[3] = maximum number of lines to read from (default: 0 - no limit)
     *
     * @param args
     * @return
     * @throws MyRpcException
     */
    public StringBuilder get(String[] args) throws MyRpcException {

        if (args == null || args.length < 2) {
            throw new MyRpcException("get expects at least 2 arguments");
        }

        String url = args[0];
        String referer = args[1];
        int minBytes = 0;
        int maxLines = 0;

        if (url == null || url.length() == 0
                || referer == null || referer.length() == 0) {
            throw new MyRpcException("Get requires valid url and referer url");
        }

        if (args.length > 2) {
            minBytes = Integer.parseInt(args[2]);
        }

        if (args.length > 3) {
            maxLines = Integer.parseInt(args[3]);
        }

        try {
            return client.get(url, referer, minBytes, maxLines);
        }
        catch (IOException e) {
            throw new MyRpcException("Get failed", e);
        }
    }

    /**
     * Perform an http post operation and return the response body.  The
     * arguments excepted by this method are:
     *
     * args[0] = Url to POST to
     * args[1] = referer url
     * args[2] = post content
     * args[3] = minimum number of bytes to read from (default: 0 - no limit)
     * args[4] = maximum number of lines to read from (default: 0 - no limit)
     *
     * @param args
     * @return
     * @throws MyRpcException
     */
    public StringBuilder post(String[] args) throws MyRpcException {

        if (args == null || args.length < 3) {
            throw new MyRpcException("post expects at least 2 arguments");
        }

        String url = args[0];
        String referer = args[1];
        String content = args[2];
        int minBytes = 0;
        int maxLines = 0;

        if (url == null || url.length() == 0
                || referer == null || referer.length() == 0) {
            throw new MyRpcException("Post requires valid url and referer url");
        }

        if (args.length > 3) {
            minBytes = Integer.parseInt(args[3]);
        }

        if (args.length > 4) {
            maxLines = Integer.parseInt(args[4]);
        }

        try {
            return client.post(url, referer, content,
                    minBytes, maxLines);
        }
        catch (IOException e) {
            throw new MyRpcException("Post failed", e);
        }
    }

    /**
     * Return 1 plus the given number
     *
     * args[0] number to be incremented
     *
     * @param args
     * @return
     * @throws in.myrpc.MyRpcException
     */
    public StringBuilder increment(String[] args) throws MyRpcException {
        if (args == null || args.length != 1) {
            throw new MyRpcException("increment expects 1 argument");
        }

        int num = Integer.parseInt(args[0]) + 1;

        return new StringBuilder(Integer.toString(num));
    }

    /**
     * return 1 minus the given number
     *
     * args[0] number to be decremented
     *
     * @param args
     * @return
     * @throws in.myrpc.MyRpcException
     */
    public StringBuilder decrement(String[] args) throws MyRpcException {
        if (args == null || args.length != 1) {
            throw new MyRpcException("increment expects 1 argument");
        }

        int num = Integer.parseInt(args[0]) - 1;

        return new StringBuilder(Integer.toString(num));
    }

    /**
     * @return the fullScript
     */
    public String getFullScript() {
        return fullScript;
    }

    /**
     * @return the token
     */
    public StringBuilder getToken() {
        return new StringBuilder(token);
    }

    /**
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Request for the scripting environment to stop running
     */
    public void stop() {
        stopRequested = true;
    }

    /**
     * carry out an if control statement.  The expected arguments are:
     *
     * args[0] = String to evaluate.  If this argument is not null and not empty
     *           then it is interpreted as true.  Otherwise, false
     * args[1] = return result if args[0] is interpreted as true
     * args[2] = return result if args[0] is interpreted as false
     *
     * @param args
     * @return
     * @throws in.myrpc.MyRpcException
     */
    public StringBuilder doIf(String[] args) throws MyRpcException {
        if (args == null || args.length != 3) {
            throw new MyRpcException("If expects 3 arguments");
        }

        if (args[0] == null || args[0].length() == 0) {
            return new StringBuilder(args[2]);
        }
        else {
            return new StringBuilder(args[1]);
        }
    }

    /**
     * Instructs the script to move the current evaluation pointer to the
     * label with the given name.  The expected arguments are:
     *
     * args[0] name of the label to move the script evaluation pointer
     *
     * if the given label name is blank, then this line does nothing
     *
     * @param args
     * @return
     * @throws MyRpcException
     */
    public StringBuilder doGoto(String[] args) throws MyRpcException {

        if (args == null || args.length != 1 || args[0] == null) {
            throw new MyRpcException("Goto expects 1 argument");
        }

        if (args[0].length() == 0) {
            return new StringBuilder();
        }

        Integer location = labels.get(args[0]);

        if (location == null) {
            throw new MyRpcException("Cannot go to label \"" + args[0]
                    + "\".  No such label");
        }

        currentEvaluationPointer = location;

        return new StringBuilder();
    }
}
