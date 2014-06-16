/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.reflect;

import in.myrpc.annotation.RpcParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * This class acts as interop layer between the concrete java object with
 * methods to be called by myRpcIn, and the incoming serialized method calls.
 *
 * @author kguthrie
 */
public class RpcMethodReflection {

    private final Class rpcMethodContainerClass;
    private final Object rpcMethodContainerInstance;
    private final Map<String, Method> methodsByName;
    private final Map<String, Map<String, Integer>> methodArgumentOrderByName;

    public RpcMethodReflection(Object rpcMethodContainerInstance) {
        assert (rpcMethodContainerInstance != null);

        this.methodsByName = new HashMap<String, Method>();
        this.methodArgumentOrderByName
                = new HashMap<String, Map<String, Integer>>();
        this.rpcMethodContainerInstance = rpcMethodContainerInstance;
        this.rpcMethodContainerClass = rpcMethodContainerInstance.getClass();

        analyzeReflectedMethods();
    }

    /**
     * this method walks the methods in the rpc method container object and
     * makes them easy to call indirectly by name
     */
    private void analyzeReflectedMethods() {

        Method[] methods = rpcMethodContainerClass.getDeclaredMethods();

        for (Method method : methods) {
            methodsByName.put(method.getName().toLowerCase(), method);
            Object[] parameterTypes = method.getParameterTypes();

            // Arguments can only be Strings for now
            for (Object parameterType : parameterTypes) {
                assert (String.class == parameterType.getClass());
            }

            Map<String, Integer> paramNameToIndex
                    = new HashMap<String, Integer>();
            methodArgumentOrderByName.put(method.getName(), paramNameToIndex);

            Annotation[][] annotations = method.getParameterAnnotations();

            for (int i = 0; i < parameterTypes.length; i++) {
                String paramName = null;
                Annotation[] annotationsForParameterI = annotations[i];

                assert (annotationsForParameterI != null);

                for (Annotation a : annotationsForParameterI) {
                    if (a.annotationType().equals(RpcParam.class)) {
                        paramName = ((RpcParam) a).value();
                        break;
                    }
                }

                assert (paramName != null);
                paramNameToIndex.put(paramName.toLowerCase(), i);
            }
        }

    }

    /**
     * Call the method with the given name on the instance of the rpc method
     * container class with the given parameters.
     *
     * @param methodName
     * @param params
     * @return
     */
    public String call(String methodName, Map<String, String> params) {
        assert (methodName != null);
        assert (params != null);

        Method method = methodsByName.get(methodName.toLowerCase().trim());

        assert (method != null);

        Map<String, Integer> paramsIdxByName
                = methodArgumentOrderByName.get(methodName);
        assert (paramsIdxByName != null);

        Object[] paramerters = new Object[paramsIdxByName.size()];

        assert (paramerters.length == params.size());

        for (Map.Entry<String, String> paramEntry : params.entrySet()) {
            String paramName = paramEntry.getKey().toLowerCase();
            String paramValue = paramEntry.getValue();
            Integer paramIndex = paramsIdxByName.get(paramName);

            assert (paramIndex != null);

            paramerters[paramIndex] = paramValue;
        }

        Object result = null;

        try {
            result = method.invoke(
                    rpcMethodContainerInstance, paramerters);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return result == null ? null : result.toString();
    }
}
