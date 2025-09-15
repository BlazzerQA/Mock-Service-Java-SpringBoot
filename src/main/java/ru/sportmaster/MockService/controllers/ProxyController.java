package ru.sportmaster.MockService.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import ru.sportmaster.MockService.service.MockService;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

@RestController
public class ProxyController {

    @Value("${backend.url}")
    private String backendUrl; //URL реального бэкенда (берётся из конфигурации)

    private final MockService mockService;
    private final RestTemplate restTemplate; //Клиент для HTTP-запросов к бэкенду

    public ProxyController(MockService mockService) {
        this.mockService = mockService;
        this.restTemplate = new RestTemplate();

        this.restTemplate.getInterceptors().add(
                (request, body, execution) -> {
                    ClientHttpResponse response = execution.execute(request, body);
                    response.getHeaders().set("Accept-Encoding", "gzip");
                    return response;
                });
    }

    @RequestMapping("/**") // - Перехватчик ВСЕХ HTTP-запросов
    public ResponseEntity<byte[]> proxyRequest(HttpServletRequest request,
                                               @RequestBody(required = false) byte[] body) {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        // Проверяем, есть ли мок для этого пути и включен ли режим моков
        if (mockService.getMockConfig().isEnabled() && mockService.hasMock(requestUri)) {
            String mockResponse = mockService.getMockResponse(requestUri);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(mockResponse.getBytes(StandardCharsets.UTF_8));
        }

        // Иначе проксируем запрос на настоящий бэкенд
        try {
            URI targetUri = new URI(backendUrl + requestUri + getQueryString(request));

            // Создаем заголовки
            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (!headerName.equalsIgnoreCase("host") &&
                        !headerName.equalsIgnoreCase("content-length")) {
                    headers.add(headerName, request.getHeader(headerName));
                }
            }

            // Создаем сущность запроса
            HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

            // Выполняем запрос
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    targetUri,
                    HttpMethod.valueOf(method),
                    entity,
                    byte[].class
            );

            //Копируем заголовки ответа
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.putAll(response.getHeaders());

            if (responseHeaders.getContentType()==null) {
                responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }

            return new ResponseEntity<>(response.getBody(),responseHeaders,response.getStatusCode());

        } catch (Exception e) {
            //Возвращаем ошибку в виде текста
            String errorMessage = "Ошибка при проксировании запроса: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(errorMessage.getBytes(StandardCharsets.UTF_8));
        }
    }

    //Вспомогательный метод обработки параметров запроса (чтобы query не потерялись при запросе к бэку)
    private String getQueryString(HttpServletRequest request) {
        String queryString = request.getQueryString();
        return queryString != null ? "?" + queryString : "";
    }
}
