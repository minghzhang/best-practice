package com.bestpractice.asyncplatform.runtime.registry;

import com.bestpractice.asyncplatform.core.contract.SingleMqHandler;

/**
 * Couples a runtime handler bean with the binding that activated it.
 *
 * @param binding binding metadata loaded from configuration
 * @param handler concrete business handler bean
 */
public record RegisteredHandler<T>(HandlerBinding binding, SingleMqHandler<T> handler) {
}
