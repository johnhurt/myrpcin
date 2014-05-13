package in.myrpc.receiver;

import in.myrpc.MyRpcException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Message receiver that evaluates a script to receive and handle messages
 * @author kguthrie
 */
public class EvaluativeMessageReceiver extends MessageReceiver {

    private static ScriptEnvironment lastEnv = null;

    private ScriptEnvironment env;

    public EvaluativeMessageReceiver(String token, MessageHandler handler) {
        super(token, handler);
    }

    private String getScriptContent() throws MyRpcException {

        StringBuilder result = new StringBuilder();

        String line;
        int lineNum = 0;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    this.getClass().getResourceAsStream("/default.script")));

            while ((line = br.readLine()) != null) {
                if (lineNum++ > 0) {
                    result.append('\n');
                }
                result.append(line);
            }
        }
        catch (IOException ex) {
            throw new MyRpcException("Failed to read script", ex);
        }

        return result.toString();
    }

    @Override
    public void open() throws MyRpcException {

        String script = getScriptContent();

        if (script == null) {
            throw new MyRpcException("Failed to open script");
        }

        if (lastEnv != null && lastEnv.getFullScript().equals(script)) {
            env = lastEnv;
        }
        else {
            env = new ScriptEnvironment(script);
            lastEnv = env;
        }

        env.setToken(token);
        env.setMessageHandler(handler);
        Thread t = new Thread(env);

        t.start();
    }

    @Override
    public void close() throws MyRpcException {
        if (env != null) {
            env.stop();
        }
    }
}
