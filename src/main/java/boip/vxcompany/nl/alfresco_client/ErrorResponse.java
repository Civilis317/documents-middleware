package boip.vxcompany.nl.alfresco_client;

public class ErrorResponse {
    private int status;
    private int code;
    private String message;

    public ErrorResponse(int status, int code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    // getters
    public int getStatus() {
        return status;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" + "status=" + status + ", code=" + code + ", message=" + message + '}';
    }
}
