package br.com.caelum.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.impl.DefaultCamelContext;

//Staged Event Driven Architecture
public class RotaPedidosSEDA {
	
	public static void main(String[] args) throws Exception {

		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				
				//lendo arquivos e delegando para outras rotas
				from("file:pedidos?delay=2s&noop=true").
					routeId("rota-pedidos").
					to("seda:http"). //delegando
					to("seda:soap"); //delegando
				
				from("seda:http").
					routeId("rota-http").
					setProperty("pedidoId", xpath("/pedido/id/text()")).
				    setProperty("clienteId", xpath("/pedido/pagamento/email-titular/text()")).
					split().
						xpath("/pedido/itens/item").
					filter().
						xpath("/item/formato[text()='EBOOK']").
					setProperty("ebookId", xpath("/item/livro/codigo/text()")).
					marshal()
						.xmljson().
					log("${id} - ${body}").
					setHeader(Exchange.HTTP_METHOD, HttpMethods.GET).
					setHeader(Exchange.HTTP_QUERY,   
							simple("clienteId=${exchangeProperty.clienteId}&pedidoId=${exchangeProperty.pedidoId}&ebookId=${exchangeProperty.ebookId}")).
				to("http4://localhost:8080/webservices/ebook/item");
				
				from("seda:soap").
					routeId("rota-soap").
					log("chamando servico soap").
				to("mock:soap");
			}
		});
		
		context.start();
		Thread.sleep(20000L);
		context.stop();
	}
}
