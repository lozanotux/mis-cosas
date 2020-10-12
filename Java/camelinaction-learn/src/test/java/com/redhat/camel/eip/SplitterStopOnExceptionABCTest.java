package com.redhat.camel.eip;

import org.apache.camel.CamelExchangeException;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class SplitterStopOnExceptionABCTest extends CamelTestSupport {
	
	@Test
	public void testSplitStopOnException() throws Exception {
		MockEndpoint split = getMockEndpoint("mock:split");
		// we expect 1 messages to be split since the 2nd message should fail
		split.expectedBodiesReceived("Camel rocks");
		
		// and no combined aggregated message since we stop on exception
		MockEndpoint result = getMockEndpoint("mock:result");
		result.expectedMessageCount(0);
		
		// now send a message with an unknown letter (F) which forces an exception to occur
		try {
			template.sendBody("direct:start", "A,F,C");
			fail("Should have thrown an exception");
		} catch(CamelExecutionException e) {
			CamelExchangeException cause = assertIsInstanceOf(CamelExchangeException.class, e.getCause());
            IllegalArgumentException iae = assertIsInstanceOf(IllegalArgumentException.class, cause.getCause());
            assertEquals("Key not a known word F", iae.getMessage());
		}
		
		assertMockEndpointsSatisfied();
	}

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		
		return new RouteBuilder() {
			public void configure() throws Exception {
				from("direct:start")
					.split(body(), new MyAggregationStrategy())
						// configure the splitter to stop on exception
						.stopOnException()
						// log each splitted message
	                    .log("Split line ${body}")
	                    // and have them translated into a quote
	                    .bean(WordTranslateBean.class)
	                    // and send it to a mock
	                    .to("mock:split")
					.end()
					// log the outgoing aggregated message
                    .log("Aggregated ${body}")
                    // and send it to a mock as well
                    .to("mock:result");
			}
		};
		
	}
	
}
