package ru.coffee.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CoffeeController extends HttpServlet {

    private CoffeeControllerManager controllerManager;

    @Override
    public void init() throws ServletException {
        ApplicationContext context = new ClassPathXmlApplicationContext("SpringConfig.xml");
        controllerManager = (CoffeeControllerManager)context.getBean("coffeeControllerManager");
    }
    
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
