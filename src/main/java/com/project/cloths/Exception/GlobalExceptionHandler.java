package com.project.cloths.Exception;

import com.project.cloths.Exception.GlobalException;
import com.project.cloths.Model.ResponseModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ResponseModel> handleGlobalException(GlobalException ex) {
        Map<String, String> resCode = new HashMap<>();
        resCode.put("error_code", ex.getErrorCode());
        resCode.put("error_desc", ex.getErrorDesc());

        Map<String, Object> data = new HashMap<>();
        data.put("ErrorMess", ex.getMessage());
        data.put("ErrorType", "SYSTEM_ERROR");
        data.put("ErrorCode", ex.getErrorCode());

        ResponseModel response = new ResponseModel(resCode, data);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseModel> handleOtherExceptions(Exception ex) {
        Map<String, String> resCode = new HashMap<>();
        resCode.put("error_code", "500");
        resCode.put("error_desc", "Internal Server Error");

        Map<String, Object> data = new HashMap<>();
        data.put("ErrorMess", ex.getMessage());
        data.put("ErrorType", "UNKNOWN_ERROR");
        data.put("ErrorCode", "ERR500");

        ResponseModel response = new ResponseModel(resCode, data);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
