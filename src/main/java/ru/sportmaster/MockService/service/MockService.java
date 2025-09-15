package ru.sportmaster.MockService.service;

import org.springframework.stereotype.Service;
import ru.sportmaster.MockService.model.MockConfig;
import jakarta.annotation.PostConstruct;

@Service
public class MockService {
    private final MockConfig mockConfig = new MockConfig();

    @PostConstruct
    public void init() {
        // Можно добавить начальные моки для тестирования
        mockConfig.addMock("/api/v2/products/31041630299", "{\"id\": 31041630299, \"name\": \"Test Product\", \"price\": 100}");
    }

    public MockConfig getMockConfig() {
        return mockConfig;
    }

    public void addMock(String path, String response) {
        mockConfig.addMock(path, response);
    }

    public void removeMock(String path) {
        mockConfig.removeMock(path);
    }

    public String getMockResponse(String path) {
        return mockConfig.getMockResponse(path);
    }

    public boolean hasMock(String path) {
        return mockConfig.hasMock(path);
    }
}
