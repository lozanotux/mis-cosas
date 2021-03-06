package com.redhat.camel.transformation.bindy;

import java.math.BigDecimal;
import java.util.Locale;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.dataformat.BindyType;
import org.junit.Test;

import junit.framework.TestCase;

public class PurchaseOrderBindyTest extends TestCase {

	@Test
	public void testBindy() throws Exception {
		final Locale locale = Locale.getDefault();
		
		try {
			locale.setDefault(Locale.US);
			CamelContext context = new DefaultCamelContext();
			context.addRoutes(createRoutes());
			context.start();
			
			MockEndpoint mock = context.getEndpoint("mock:result", MockEndpoint.class);
			mock.expectedBodiesReceived("Camel in Action;39.95;1\n");
			
			PurchaseOrder order = new PurchaseOrder();
			order.setAmount(1);
			order.setPrice(new BigDecimal("39.95"));
			order.setName("Camel in Action");
			
			ProducerTemplate template = context.createProducerTemplate();
			template.sendBody("direct:toCsv", order);
			
			mock.assertIsSatisfied();
		} finally {
			Locale.setDefault(locale);
		}
	}
	
	public RouteBuilder createRoutes() {
		return new RouteBuilder() {
			public void configure() {
				from("direct:toCsv")
					.marshal().bindy(BindyType.Csv, PurchaseOrder.class)
					.to("mock:result");
			}
		};
	}
	
}
