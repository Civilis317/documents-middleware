package boip.vxcompany.nl.alfresco_client;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ErrorController extends ResponseEntityExceptionHandler {
    Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    @ExceptionHandler(MultipartException.class)
    @ResponseBody
    public ErrorResponse handleFileException(HttpServletRequest request, Throwable ex) {
        ErrorResponse errorResponse = new ErrorResponse(500, 5001, ex.getMessage());
        logger.error(errorResponse.toString());
        return errorResponse;
    }
}
