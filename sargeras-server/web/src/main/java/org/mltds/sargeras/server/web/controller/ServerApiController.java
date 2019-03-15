package org.mltds.sargeras.server.web.controller;

import org.mltds.sargeras.common.rpc.RpcRequest;
import org.mltds.sargeras.common.rpc.RpcResponse;
import org.mltds.sargeras.server.facade.ServerFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.esotericsoftware.reflectasm.MethodAccess;

/**
 * @author sunyi.
 */
@Controller
public class ServerApiController {

    private static final Logger logger = LoggerFactory.getLogger(ServerApiController.class);

    @Autowired
    private ServerFacade serverFacade;

    private MethodAccess methodAccess = MethodAccess.get(ServerFacade.class);

    @ResponseBody
    @RequestMapping("/api/server")
    public RpcResponse api(@RequestBody RpcRequest request) {

        String method = request.getMethod();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] args = request.getArgs();

        RpcResponse response = new RpcResponse();
        response.setReqId(request.getReqId());

        try {
            Object result = methodAccess.invoke(serverFacade, method, parameterTypes, args);
            response.setSuccess(true);
            response.setResult(result);
        } catch (Exception e) {
            response.setSuccess(true);
            response.setErrorMessage(e.getMessage());
            logger.error("Facade Api 调用时发生异常", e);
        }

        return response;

    }
}
