package com.redhat.camel.eip;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

public class MySplitterStrategy implements AggregationStrategy {

    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null) {
            // this is the first time so no existing aggregated exchange
            return newExchange;
        }

        // append the new word to the existing
        String body = newExchange.getIn().getBody(String.class);
        String existing = oldExchange.getIn().getBody(String.class);

        oldExchange.getIn().setBody(existing + "+" + body);
        return oldExchange;
    }
    
}
