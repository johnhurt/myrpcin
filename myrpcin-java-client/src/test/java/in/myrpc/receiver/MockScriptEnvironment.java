/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.receiver;

import in.myrpc.MyRpcException;

/**
 *
 * @author kguthrie
 */
public class MockScriptEnvironment extends ScriptEnvironment {

    public int getCount = 0;

    public MockScriptEnvironment(String scriptBody) throws MyRpcException {
        super(scriptBody);
    }

    @Override
    public StringBuilder get(String[] args) throws MyRpcException {

        getCount++;

        return new StringBuilder(args[0] + " " + args[1]  +
                (args.length > 2 ? " " + args[2] : ""));

    }

    @Override
    public StringBuilder post(String[] args) throws MyRpcException {

        return new StringBuilder(args[0] + " " + args[1] + " " + args[2]);

    }

    @Override
    public StringBuilder getToken() {
        return new StringBuilder("token");

    }

}
