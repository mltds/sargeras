package org.mltds.sargeras.worker.spi.network;

import org.mltds.sargeras.common.exception.SagaNetworkException;
import org.mltds.sargeras.common.rpc.RpcRequest;
import org.mltds.sargeras.common.rpc.RpcResponse;

/**
 * @author sunyi.
 */
public interface Network {

    RpcResponse request(RpcRequest rpcRequest) throws SagaNetworkException;

}
