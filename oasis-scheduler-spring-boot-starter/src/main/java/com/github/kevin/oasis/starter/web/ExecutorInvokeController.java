package com.github.kevin.oasis.starter.web;

import com.github.kevin.oasis.starter.model.ExecutorInvokeRequest;
import com.github.kevin.oasis.starter.service.OasisExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Invoke entrypoint for Oasis admin.
 */
@RestController
@RequestMapping("${oasis.scheduler.server.context-path:/oasis-executor}")
@RequiredArgsConstructor
public class ExecutorInvokeController {

    private final OasisExecutionService oasisExecutionService;

    @PostMapping("/invoke")
    public Map<String, Object> invoke(@Valid @RequestBody ExecutorInvokeRequest request) {
        boolean accepted = oasisExecutionService.submit(request);
        return Map.of("accepted", accepted, "fireLogId", request.getFireLogId());
    }
}
