package ru.sportmaster.MockService.model;

import java.util.HashMap;
import java.util.Map;

public class MockConfig {

    private final Map<String, String> pathMocks = new HashMap<>();
    private boolean enabled = true;

    //Добавить мок - с переменной "путь" и "ответ"
    public void addMock(String path, String response) {
        pathMocks.put(path, response);
    }

    //Удалить мок по пути
    public void removeMock(String path) {
        pathMocks.remove(path);
    }


    public String getMockResponse(String path) {
        return pathMocks.get(path);
    }

    // Проверка - существует ли мок по пути (да/нет)
    public boolean hasMock(String path) {
        return pathMocks.containsKey(path);
    }

    public Map<String, String> getAllMocks() {
        return new HashMap<>(pathMocks);
    }

    public boolean isEnabled() {
        return enabled;
    }

    //Включить или выключить мок
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
