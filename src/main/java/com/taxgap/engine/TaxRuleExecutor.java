package com.taxgap.engine;

import com.taxgap.domain.entity.TaxException;
import com.taxgap.domain.entity.TaxRule;
import com.taxgap.domain.entity.Transaction;

import java.util.Optional;


public interface TaxRuleExecutor {

     String getRuleName();

      Optional<TaxException> execute(Transaction transaction, TaxRule rule);
}
