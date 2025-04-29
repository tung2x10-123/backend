package com.project.cloths.Model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class ResponseModel<T> {
    private Map<String, String> res_code;
    private Map<String, Object> data;

    public ResponseModel(Map<String, String> res_code, Map<String, Object> data) {
        this.res_code = res_code;
        this.data = data;
    }

}
