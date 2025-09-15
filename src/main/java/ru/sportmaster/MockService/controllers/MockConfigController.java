package ru.sportmaster.MockService.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.sportmaster.MockService.service.MockService;

@Controller
public class MockConfigController {

    private final MockService mockService;

    public MockConfigController(MockService mockService) {
        this.mockService = mockService;
    }

    @GetMapping("/")
    public String showConfigPage(Model model) {
        model.addAttribute("mocks", mockService.getMockConfig().getAllMocks());
        model.addAttribute("enabled", mockService.getMockConfig().isEnabled());
        return "config";
    }

    @PostMapping("/addMock")
    public String addMock(@RequestParam String path,
                          @RequestParam String response,
                          Model model) {
        mockService.addMock(path, response);
        return "redirect:/";
    }

    @PostMapping("/removeMock")
    public String removeMock(@RequestParam String path, Model model) {
        mockService.removeMock(path);
        return "redirect:/";
    }

    @PostMapping("/toggle")
    public String toggleEnabled(Model model) {
        boolean current = mockService.getMockConfig().isEnabled();
        mockService.getMockConfig().setEnabled(!current);
        return "redirect:/";
    }
}
