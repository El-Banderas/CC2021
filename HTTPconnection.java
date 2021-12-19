import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

public class HTTPconnection implements Runnable {
    private LogInformation log;

    public HTTPconnection(LogInformation log) {
        this.log = log;
    }

    /**
     * From here, we start receiving http requests.
     * For eacho connection, we create a new thread.
     */
    @Override
    public void run() {
        try {
            log.writeStartHTTP();
            System.out.println("[HTTP request] : Começou");
            HttpServer server = HttpServer.create(new InetSocketAddress( Constantes.OfficialPort), 0);
            server.createContext("/", new  MyHttpHandler());
            //server.createContext("/test", new  MyHttpHandler());
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class MyHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String requestParamValue=null;
            //Obtem qual a palavra inserida a seguir
            if("GET".equals(httpExchange.getRequestMethod())) {
                requestParamValue = handleGetRequest(httpExchange);
            }
           // System.out.println("[HTTP Req] Recebeu um pedido "+requestParamValue);
            if (requestParamValue == null) {
                System.out.println("[HTTP Req] recebeu null");
                handleResponse404(httpExchange);
            }
            else {
                if (requestParamValue.equals("log")) {
                    System.out.println("[HTTP Req] recebeu log");
                    handleResponseLog(httpExchange);
                }
                else {
                    if (requestParamValue.equals("info")) {
                        System.out.println("[HTTP Req] recebeu info");
                        handleResponseInfo(httpExchange);
                    }
                    else {
                        System.out.println("[HTTP Req] recebeu algo que não está previsto");
                        handleResponse404(httpExchange,requestParamValue);
                    }
                }
            }
        }

        /**
         * Creates the html file, and sends, with the following information:
         * Current files in this directory, and last time the file was modified.
         * @param httpExchange Connection http
         * @throws IOException
         */
        private void handleResponseInfo(HttpExchange httpExchange) throws IOException {
            OutputStream outputStream = httpExchange.getResponseBody();
            StringBuilder htmlBuilder = new StringBuilder();

            htmlBuilder.append("<html>").
                    append("<body>").
                    append("<h1>").
                    append("Recebeu um Info ")
                    .append("</h1>")
                    .append("</body>")
                    .append("</html>");
            // encode HTML content
            //String htmlResponse = StringEscapeUtils.escapeHtml4(htmlBuilder.toString());
            // this line is a must
            httpExchange.sendResponseHeaders(200, htmlBuilder.length());
            outputStream.write(htmlBuilder.toString().getBytes());
            outputStream.flush();
            outputStream.close();
        }

        /**
         * Creates the html file, and sends, with error message
         * @param httpExchange
         * @param requestParamValue
         * @throws IOException
         */
        private void handleResponse404(HttpExchange httpExchange, String requestParamValue) throws IOException {
            OutputStream outputStream = httpExchange.getResponseBody();
            StringBuilder htmlBuilder = new StringBuilder();

            htmlBuilder.append("<html>").
                    append("<body>").
                    append("<h1>").
                    append("Recebeu um Nada correto !!!! ")
                    .append("</h1>")
                    .append("</body>")
                    .append("</html>");
// encode HTML content
            //String htmlResponse = StringEscapeUtils.escapeHtml4(htmlBuilder.toString());
            // this line is a must
            httpExchange.sendResponseHeaders(200, htmlBuilder.length());
            outputStream.write(htmlBuilder.toString().getBytes());
            outputStream.flush();
            outputStream.close();
        }
        private void handleResponse404(HttpExchange httpExchange) throws IOException {
            OutputStream outputStream = httpExchange.getResponseBody();
            StringBuilder htmlBuilder = new StringBuilder();

            htmlBuilder.append("<html>").
                    append("<body>").
                    append("<h1>").
                    append("Não sei o que recebi sem argumentos")
                    .append("</h1>")
                    .append("</body>")
                    .append("</html>");
            // encode HTML content
            //String htmlResponse = StringEscapeUtils.escapeHtml4(htmlBuilder.toString());
            // this line is a must
            httpExchange.sendResponseHeaders(200, htmlBuilder.length());
            outputStream.write(htmlBuilder.toString().getBytes());
            outputStream.flush();
            outputStream.close();
        }

        private String handleGetRequest(HttpExchange httpExchange) {
            String x = httpExchange.
            getRequestURI()
                    .toString()
                    //.split("\\?")[1]
                    .split("/")[1];
            System.out.println("[HTTP req] A seguir à barra está: "+x);
            return x;
        }

        /**
         * Creates the html file, and sends, with the following information:
         * Number of active threads.
         * @param httpExchange
         * @throws IOException
         *
         * Log Menu:
         * Currenct active threads: nº
         */
        private void handleResponseLog(HttpExchange httpExchange)  throws  IOException {
            //Para ter o número atual de threads:
            int nThreads = log.getNThreads();
            OutputStream outputStream = httpExchange.getResponseBody();
            StringBuilder htmlBuilder = new StringBuilder();

            htmlBuilder.append("<html>").
                    append("<body>").
                    append("<h1>").
                    append("Recebeu um LOG !!!! ")
                    .append("</h1>")
                    .append("</body>")
                    .append("</html>");
            // encode HTML content
            //String htmlResponse = StringEscapeUtils.escapeHtml4(htmlBuilder.toString());
            // this line is a must
            httpExchange.sendResponseHeaders(200, htmlBuilder.length());
            outputStream.write(htmlBuilder.toString().getBytes());
            outputStream.flush();
            outputStream.close();
        }
    }


}
