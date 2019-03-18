package org.mltds.sargeras.rpc.httpjson.manager.jdkproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import org.mltds.sargeras.api.exception.SagaException;
import org.mltds.sargeras.common.rpc.RpcRequest;
import org.mltds.sargeras.common.rpc.RpcResponse;
import org.mltds.sargeras.utils.Utils;
import org.mltds.sargeras.api.SagaApplication;
import org.mltds.sargeras.rpc.httpjson.network.Network;

/**
 * @author sunyi.
 */
public class JdkProxyServerFacade implements InvocationHandler {

    private AtomicLong reqId = new AtomicLong();
    private String localIP = Utils.getLocalIPv4();
    private Network network = SagaApplication.getNetwork();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        RpcRequest request = buildRequest(method, args);

        // request
        RpcResponse response = network.request(request);

        if (response.isSuccess()) {
            return response.getResult();
        } else {
            throw new SagaException("Server 响应异常：" + response.getErrorMessage());
        }
    }

    private RpcRequest buildRequest(Method method, Object[] args) {
        RpcRequest request = new RpcRequest();

        request.setReqId(reqId.addAndGet(1L));
        request.setMethod(method.getName());

        if (args != null && args.length > 0) {
            Class<?>[] parameterTypes = new Class<?>[args.length];
            request.setParameterTypes(parameterTypes);
        }
        request.setArgs(args);

        request.getAttachment().put("CLIENT_IP", localIP);

        return request;
    }

}
