package ru.coffee.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.coffee.service.CoffeeService;
import ru.coffee.validator.Validator;

@Controller
public class CoffeeControllerManager {

    @Autowired
    private CoffeeService coffeeService;
    
    @Autowired
    private Validator validator;

    @RequestMapping(value = "/")
    public void listCoffee(HttpServletRequest request, HttpServletResponse response, Locale locale) throws ServletException, IOException {
        try {
            coffeeService.listCoffee(request, locale);
            forwardToView(request, response, "/WEB-INF/pages/CoffeeList.jsp");
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }

    @RequestMapping(value = "/Delivery")
    public void delivery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("POST".equals(request.getMethod())) {
            if (!validator.validateListCoffee(request, Locale.getDefault())) {
                response.sendRedirect(request.getHeader("Referer"));
                return;
            }
            coffeeService.prepareOrder(request);
        }
        forwardToView(request, response, "/WEB-INF/pages/Delivery.jsp");
    }

    @RequestMapping(value = "/CreateOrder")
    public void createOrder(HttpServletRequest request, HttpServletResponse response, Locale locale) throws ServletException, IOException {
        if (!validator.validateAddress(request, locale)) {
            response.sendRedirect(request.getHeader("Referer"));
            return;
        }
        coffeeService.createOrder(request);
        response.sendRedirect("Confirmation");
    }

    @RequestMapping(value = "/Confirmation")
    public void confirm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        forwardToView(request, response, "/WEB-INF/pages/Confirmation.jsp");
    }

    private static void forwardToView(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher(path);
        rd.forward(request, response);
    }

    public CoffeeService getCoffeeService() {
        return coffeeService;
    }

    public void setCoffeeService(CoffeeService coffeeService) {
        this.coffeeService = coffeeService;
    }
}
