package ru.coffee.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ru.coffee.service.CoffeeService;
import ru.coffee.validator.Validator;

public class CoffeeController extends HttpServlet {

    private CoffeeService coffeeService = new CoffeeService();
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getServletPath();
        switch (action) {
            case "/Delivery":
                delivery(request, response);
                break;
            case "/CreateOrder":
                createOrder(request, response);
                break;
            case "/Confirmation":
                confirm(request, response);
                break;
            default:
                listCoffee(request, response);
        }
    }
    
    private void listCoffee(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            coffeeService.listCoffee(request);
            forwardToView(request, response, "/CoffeeList.jsp");
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }
    
    private void delivery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if("POST".equals(request.getMethod())){
            if(!Validator.validateListCoffee(request)){
                response.sendRedirect(request.getHeader("Referer"));
                return;
            }
            coffeeService.prepareOrder(request);
        }
        forwardToView(request, response, "/Delivery.jsp");
    }    
    
    private void createOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Validator.validateAddress(request)) {
            response.sendRedirect(request.getHeader("Referer"));
            return;
        }
        coffeeService.createOrder(request);
        response.sendRedirect("Confirmation");
    }
    
    private void confirm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        forwardToView(request, response, "/Confirmation.jsp");
    }
    
    private static void forwardToView(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException{
        RequestDispatcher rd = request.getRequestDispatcher(path);
        rd.forward(request, response);   
    }
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
