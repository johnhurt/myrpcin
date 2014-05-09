/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.receiver;

import in.myrpc.MyRpcException;

/**
 * Interface that defines the methods for receiving messages from a central
 * messaging server
 * @author kguthrie
 */
public abstract class MessageReceiver {

    protected final MessageHandler handler;
    protected final String token;

    public MessageReceiver(String token, MessageHandler handler) {
        this.token = token;
        this.handler = handler;
    }

    public abstract void open() throws MyRpcException;

    public abstract void close() throws MyRpcException;

}
