package org.mltds.sargeras.api.model;

import java.lang.reflect.Method;

public class MethodInfo {

    private Class<?> cls;
    private String clsName;

    private Method method;
    private String methodName;

    private Class[] parameterTypes;
    private String parameterTypesStr;

    private String[] parameterNames;
    private String parameterNamesStr;

    private Object[] parameters;

        public Class<?> getCls() {
                return cls;
        }

        public void setCls(Class<?> cls) {
                this.cls = cls;
        }

        public String getClsName() {
                return clsName;
        }

        public void setClsName(String clsName) {
                this.clsName = clsName;
        }

        public Method getMethod() {
                return method;
        }

        public void setMethod(Method method) {
                this.method = method;
        }

        public String getMethodName() {
                return methodName;
        }

        public void setMethodName(String methodName) {
                this.methodName = methodName;
        }

        public Class[] getParameterTypes() {
                return parameterTypes;
        }

        public void setParameterTypes(Class[] parameterTypes) {
                this.parameterTypes = parameterTypes;
        }

        public String getParameterTypesStr() {
                return parameterTypesStr;
        }

        public void setParameterTypesStr(String parameterTypesStr) {
                this.parameterTypesStr = parameterTypesStr;
        }

        public String[] getParameterNames() {
                return parameterNames;
        }

        public void setParameterNames(String[] parameterNames) {
                this.parameterNames = parameterNames;
        }

        public String getParameterNamesStr() {
                return parameterNamesStr;
        }

        public void setParameterNamesStr(String parameterNamesStr) {
                this.parameterNamesStr = parameterNamesStr;
        }

        public Object[] getParameters() {
                return parameters;
        }

        public void setParameters(Object[] parameters) {
                this.parameters = parameters;
        }
}