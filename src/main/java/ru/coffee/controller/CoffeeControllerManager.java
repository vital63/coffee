package ru.coffee.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import ru.coffee.service.CoffeeService;
import ru.coffee.validator.Validator;

@Controller
public class CoffeeControllerManager {

    @Autowired
    private CoffeeService coffeeService;
    
    public void listCoffee(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            coffeeService.listCoffee(request);
            forwardToView(request, response, "/CoffeeList.jsp");
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }

    public void delivery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("POST".equals(request.getMethod())) {
            if (!Validator.validateListCoffee(request)) {
                response.sendRedirect(request.getHeader("Referer"));
                return;
            }
            coffeeService.prepareOrder(request);
        }
        forwardToView(request, response, "/Delivery.jsp");
    }

    public void createOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Validator.validateAddress(request)) {
            response.sendRedirect(request.getHeader("Referer"));
            return;
        }
        coffeeService.createOrder(request);
        response.sendRedirect("Confirmation");
    }

    public void confirm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        forwardToView(request, response, "/Confirmation.jsp");
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
