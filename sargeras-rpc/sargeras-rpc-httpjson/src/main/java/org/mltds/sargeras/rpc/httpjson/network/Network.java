package org.mltds.sargeras.rpc.httpjson.network;

import org.mltds.sargeras.api.exception.SagaNetworkException;
import org.mltds.sargeras.common.rpc.RpcRequest;
import org.mltds.sargeras.common.rpc.RpcResponse;

/**
 * @author sunyi.
 */
public interface Network {

    RpcResponse request(RpcRequest rpcRequest) throws SagaNetworkException;

}
