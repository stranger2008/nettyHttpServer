package com.lean.netty.http;

public class AjaxResult {
    private int code;
    private String message;
    private Object body;

    public static AjaxResult SUCCESS = new AjaxResult(0, "success");
    public static AjaxResult FAILURE = new AjaxResult(1, "failure");

    /**
     * <pre>
     * @param code
     * @param message
     * </pre>
     */
    private AjaxResult(int code, String message) {
        super();
        this.code = code;
        this.message = message;
    }

    /**
     * <pre>
     * @return the body
     * </pre>
     */
    public Object getBody() {
        return body;
    }

    /**
     * <pre>
     * @return the code
     * </pre>
     */
    public int getCode() {
        return code;
    }

    /**
     * <pre>
     * @return the message
     * </pre>
     */
    public String getMessage() {
        return message;
    }

    /**
     * <pre>
     * @param body the body to set
     * </pre>
     */
    public AjaxResult setBody(Object body) {
        this.body = body;
        return this;
    }

    /**
     * <pre>
     * @param code the code to set
     * </pre>
     */
    public AjaxResult setCode(int code) {
        this.code = code;
        return this;
    }

    /**
     * <pre>
     * @param message the message to set
     * </pre>
     */
    public AjaxResult setMessage(String message) {
        this.message = message;
        return this;
    }

}


