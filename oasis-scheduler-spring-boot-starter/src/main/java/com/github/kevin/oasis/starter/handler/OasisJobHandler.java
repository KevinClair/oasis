package com.github.kevin.oasis.starter.handler;

import com.github.kevin.oasis.starter.context.OasisJobContext;
import com.github.kevin.oasis.starter.model.OasisJobResult;

/**
 * Business job handler contract.
 */
public interface OasisJobHandler {

    /**
     * Unique handler name configured in Oasis admin.
     */
    String handlerName();

    /**
     * Execute job business logic.
     */
    OasisJobResult execute(OasisJobContext context) throws Exception;
}
