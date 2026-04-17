package com.bestpractice.asyncplatform.runtime.registry;

import com.bestpractice.asyncplatform.core.contract.SingleMqHandler;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds the runtime dispatch model from configuration bindings and discovered handlers.
 *
 * <p>The registry groups bindings by topic + consumer group, then maps sub-topics
 * to concrete handlers inside each dispatch group.</p>
 */
public class HandlerRegistry {
    /**
     * Dispatch groups indexed by topic + consumer-group.
     */
    private final Map<String, DispatchGroup> dispatchGroups;

    /**
     * Builds the runtime registry from configuration bindings and discovered handler beans.
     *
     * @param bindings binding definitions loaded from configuration
     * @param handlers handler beans discovered from the application context
     */
    public HandlerRegistry(Collection<HandlerBinding> bindings, Collection<SingleMqHandler<?>> handlers) {
        Map<String, SingleMqHandler<?>> handlerIndex = handlers.stream()
                .collect(Collectors.toMap(SingleMqHandler::handlerName, handler -> handler, (left, right) -> left, LinkedHashMap::new));
        this.dispatchGroups = buildDispatchGroups(bindings, handlerIndex);
    }

    /**
     * Returns all dispatch groups built by the registry.
     */
    public Collection<DispatchGroup> dispatchGroups() {
        return dispatchGroups.values();
    }

    /**
     * Groups bindings by topic + consumer-group and registers sub-topic routes inside each group.
     */
    private Map<String, DispatchGroup> buildDispatchGroups(Collection<HandlerBinding> bindings,
                                                           Map<String, SingleMqHandler<?>> handlerIndex) {
        Map<String, DispatchGroup> groups = new LinkedHashMap<>();
        for (HandlerBinding binding : bindings) {
            if (!binding.isEnabled()) {
                continue;
            }
            SingleMqHandler<?> handler = handlerIndex.get(binding.getHandlerName());
            if (handler == null) {
                throw new IllegalStateException("No handler bean found for binding: " + binding.getHandlerName());
            }
            validate(binding, handler);
            String key = binding.getTopic() + "::" + binding.getGroup();
            // All bindings that share topic + group are consumed by the same runtime container.
            DispatchGroup group = groups.computeIfAbsent(key,
                    ignored -> new DispatchGroup(binding.getTopic(), binding.getGroup(), binding.getConcurrency()));
            if (group.getConcurrency() != binding.getConcurrency()) {
                throw new IllegalStateException("All bindings sharing topic/group must use the same concurrency: " + key);
            }
            RegisteredHandler<?> registeredHandler = new RegisteredHandler<>(binding, handler);
            for (String subTopic : binding.getSubTopics()) {
                group.register(subTopic, registeredHandler);
            }
        }
        return groups;
    }

    /**
     * Validates that one binding is structurally correct and compatible with the target handler.
     */
    private void validate(HandlerBinding binding, SingleMqHandler<?> handler) {
        if (binding.getTopic() == null || binding.getTopic().isBlank()) {
            throw new IllegalStateException("Binding topic must not be blank for " + binding.getName());
        }
        if (binding.getGroup() == null || binding.getGroup().isBlank()) {
            throw new IllegalStateException("Binding group must not be blank for " + binding.getName());
        }
        if (binding.getSubTopics() == null || binding.getSubTopics().isEmpty()) {
            throw new IllegalStateException("Binding subTopics must not be empty for " + binding.getName());
        }
        Set<String> supported = handler.supportedSubTopics();
        if (!supported.isEmpty() && !supported.containsAll(binding.getSubTopics())) {
            List<String> unsupported = binding.getSubTopics().stream()
                    .filter(subTopic -> !supported.contains(subTopic))
                    .filter(Objects::nonNull)
                    .toList();
            if (!unsupported.isEmpty()) {
                throw new IllegalStateException("Binding " + binding.getName() + " declares unsupported subTopics: " + unsupported);
            }
        }
    }
}
