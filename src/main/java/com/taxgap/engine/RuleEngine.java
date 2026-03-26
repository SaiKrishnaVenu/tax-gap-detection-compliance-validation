package com.taxgap.engine;

import com.taxgap.domain.entity.TaxException;
import com.taxgap.domain.entity.TaxRule;
import com.taxgap.domain.entity.Transaction;
import com.taxgap.repository.TaxRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class RuleEngine {

    private final TaxRuleRepository ruleRepository;
    private final List<TaxRuleExecutor> executors;


    public List<TaxException> evaluate(Transaction transaction) {

        Map<String, TaxRuleExecutor> executorMap = executors.stream()
                .collect(Collectors.toMap(TaxRuleExecutor::getRuleName, Function.identity()));

        List<TaxRule> activeRules = ruleRepository.findByEnabledTrue();
        List<TaxException> exceptions = new ArrayList<>();

        for (TaxRule rule : activeRules) {
            TaxRuleExecutor executor = executorMap.get(rule.getRuleName());
            if (executor == null) {
                log.warn("No executor found for rule: {}", rule.getRuleName());
                continue;
            }
            log.debug("Executing rule {} for transaction {}", rule.getRuleName(), transaction.getTransactionId());
            executor.execute(transaction, rule).ifPresent(exceptions::add);
        }
        return exceptions;
    }
}
