package com.redhat.camel.errorhandler;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class HandleFaultTest extends CamelTestSupport {

	/* 
	 * Basic Camel Unit Test consist in:
	 * 1)- Create a registry to link beans
	 * 2)- Do the different unit testing
	 * 3)- Create a Route to mock a scenario
	 */
	@Override
	protected JndiRegistry createRegistry() throws Exception {
		JndiRegistry jndi = super.createRegistry();
		jndi.bind("orderService", new OrderService());
		return jndi;
	}
	
	@Test
	public void testOrderOK() throws Exception {
		MockEndpoint mock = getMockEndpoint("mock:queue.order");
		mock.expectedMessageCount(1);
		
		template.sendBody("seda:queue.inbox", "amount=1,name=Camel in Action");
		
		assertMockEndpointsSatisfied();
	}
	
	@Test
	public void testOrderFail() throws Exception {
		MockEndpoint mock = getMockEndpoint("mock:queue.order");
		mock.expectedMessageCount(0);
		
		MockEndpoint dead = getMockEndpoint("mock:dead");
		dead.expectedMessageCount(1);
		
		// On the EXCEPTION_CAUGHT property we have the caused exception which we can
		// assert contains the fault message
        dead.message(0).property(Exchange.EXCEPTION_CAUGHT).convertTo(String.class).contains("ActiveMQ in Action is out of stock");

        template.sendBody("seda:queue.inbox","amount=1,name=ActiveMQ in Action");

        assertMockEndpointsSatisfied();
	}
	
	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				// we want this Camel error handler to handle SOAP faults
                errorHandler(deadLetterChannel("mock:dead"));

                // enable fault handling globally
                // getContext().setHandleFault(true);

                // or you can enable it specific per route as shown below
                from("seda:queue.inbox").handleFault()
                    .beanRef("orderService", "toSoap")
                    .to("mock:queue.order");
			}
		};
	}
	
	
}
