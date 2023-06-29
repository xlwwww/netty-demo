package test.netty.demo.message;

import lombok.Data;

/**
 * rpc 请求消息
 */
@Data
public class RpcRequestMessage extends Message {
    private int sequence_id;
    // 全限定类名
    private String interfaceName;
    // 请求的方法名
    private String methodName;
    // 方法的返回类型
    private Class returnType;
    // 请求的方法参数类型
    private Class[] parameterTypes;
    // 请求的方法参数值
    private Object[] parameterValues;

    public RpcRequestMessage(int sequence_id, String interfaceName, String methodName, Class returnType, Class[] parameterTypes, Object[] parameterValues) {
        this.sequence_id = sequence_id;
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.parameterValues = parameterValues;
    }


    @Override
    public byte getMessageType() {
        return RpcRequestMessage;
    }
}
