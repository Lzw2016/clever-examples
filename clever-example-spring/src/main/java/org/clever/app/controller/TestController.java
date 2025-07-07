package org.clever.app.controller;

import org.clever.core.model.response.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 作者：lizw <br/>
 * 创建时间：2024/10/02 16:05 <br/>
 */
@RestController
public class TestController {
    @GetMapping("/ok")
    public R<?> ok() {
        return R.success();
    }
}
