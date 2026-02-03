package app.giftify.shared.api.response;

// API 공통 응답 규격
public final class RsData<T> {
    private final Result result;
    private final T data;
    private final String message;
    private final String errorCode;

    private RsData(Result result, T data, String message, String errorCode) {
        this.result = result;
        this.data = data;
        this.message = message;
        this.errorCode = errorCode;
    }

    public enum Result {
        SUCCESS, FAIL
    }

    public static <T> RsData<T> success(T data) {
        return new RsData<>(Result.SUCCESS, data, null, null);
    }

    public static <T> RsData<T> success(T data, String message) {
        return new RsData<>(Result.SUCCESS, data, message, null);
    }

    public static RsData<Void> fail(String message, String errorCode) {
        return new RsData<>(Result.FAIL, null, message, errorCode);
    }

    public Result getResult() {
        return result;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}