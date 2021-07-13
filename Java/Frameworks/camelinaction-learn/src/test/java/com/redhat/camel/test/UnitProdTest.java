package com.redhat.camel.test;

import java.io.File;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class UnitProdTest extends CamelTestSupport {

	private String inboxDir;
	private String outboxDir;
	
	@EndpointInject(uri = "file:{{file.inbox}}")
	private ProducerTemplate inbox;
	
	@Override
	protected CamelContext createCamelContext() throws Exception {
		CamelContext context = super.createCamelContext();
		
		// setup the properties component to use the production file
		PropertiesComponent prop = context.getComponent("properties", PropertiesComponent.class);
		prop.setLocation("classpath:rider-prod.properties");
		
		return context;
	}
	
	public void setUp() throws Exception {
		super.setUp();
		
		// lookup these endpoints from the properties file using Camel property placeholders - {{key}}
        inboxDir = context.resolvePropertyPlaceholders("{{file.inbox}}");
        outboxDir = context.resolvePropertyPlaceholders("{{file.outbox}}");

        // delete directories so we have a clean start
        deleteDirectory(inboxDir);
        deleteDirectory(outboxDir);
	}
	
	@Test
	public void testMoveFile() throws Exception {
		context.setTracing(true);
		
		// create a new file in the inbox folder with the name hello.txt and containing Hello World as body
        inbox.sendBodyAndHeader("Hello World", Exchange.FILE_NAME, "hello.txt");

        // wait a while to let the file be moved
        Thread.sleep(2000);

        // test the file was moved
        File target = new File(outboxDir + "/hello.txt");
        assertTrue("File should have been moved", target.exists());

        // test that its content is correct as well
        String content = context.getTypeConverter().convertTo(String.class, target);
        assertEquals("Hello World", content);
	}
	
	@Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // the route is very simple, notice it uses the placeholders
                from("file:{{file.inbox}}")
                    .to("file:{{file.outbox}}");
            }
        };
    }
	
}
