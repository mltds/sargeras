package org.mltds.sargeras.api.model;

public class ParamInfo {
    private Class<?> parameterType;
    private String parameterTypeStr;
    private String parameterName;
    private Object parameter;
    private byte[] parameterByte;

    public Class<?> getParameterType() {
        return parameterType;
    }

    public void setParameterType(Class<?> parameterType) {
        this.parameterType = parameterType;
    }

    public String getParameterTypeStr() {
        return parameterTypeStr;
    }

    public void setParameterTypeStr(String parameterTypeStr) {
        this.parameterTypeStr = parameterTypeStr;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public Object getParameter() {
        return parameter;
    }

    public void setParameter(Object parameter) {
        this.parameter = parameter;
    }

    public byte[] getParameterByte() {
        return parameterByte;
    }

    public void setParameterByte(byte[] parameterByte) {
        this.parameterByte = parameterByte;
    }

    @Override
    public String toString() {
        return "ParamInfo{" +
                "parameterTypeStr='" + parameterTypeStr + '\'' +
                ", parameterName='" + parameterName + '\'' +
                '}';
    }
}