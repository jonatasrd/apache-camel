package br.com.caelum.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class RotaPedidosArquivo {

	public static void main(String[] args) throws Exception {

		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from("file:pedidos?delay=2s&noop=true").
				split().
					xpath("/pedido/itens/item").
				filter().
					xpath("/item/formato[text()='EBOOK']").
				log("${id}").
				marshal()
					.xmljson().
				log("${body}").
				setHeader(Exchange.FILE_NAME, simple("${file:name.noext}-${header.CamelSplitIndex}.json")).
				to("file:saida");
			}
		});
		
		context.start();
		Thread.sleep(20000L);
		context.stop();
	}
}


//setHeader("CamelFileName", simple("${file:name.noext}.json")).
//setHeader("CamelFileName", simple("${id}.json")).