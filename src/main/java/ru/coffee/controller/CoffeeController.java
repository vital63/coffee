package ru.coffee.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CoffeeController extends HttpServlet {

    CoffeeControllerManager controllerManager = new CoffeeControllerManager();
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getServletPath();
        switch (action) {
            case "/Delivery":
                controllerManager.delivery(request, response);
                break;
            case "/CreateOrder":
                controllerManager.createOrder(request, response);
                break;
            case "/Confirmation":
                controllerManager.confirm(request, response);
                break;
            default:
                controllerManager.listCoffee(request, response);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
