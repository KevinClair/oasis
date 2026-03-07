package com.github.kevin.oasis.services;

import com.github.kevin.oasis.models.entity.Application;

public interface ExecutorInvokeClient {

    DispatchResult invoke(String address,
                          Application app,
                          Long fireLogId,
                          Integer attemptNo,
                          String handlerName,
                          String triggerParam);
}
