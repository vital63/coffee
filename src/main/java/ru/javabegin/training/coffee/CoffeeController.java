package ru.javabegin.training.coffee;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//TODO: internationalization
//TODO: validation
public class CoffeeController extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    CoffeeDAO coffeeDAO = new CoffeeDAO();
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getServletPath();
        System.out.println("action: " + action);
        switch (action) {
            case "/Delivery":
                delivery(request, response);
                break;
            case "/CreateOrder":
                createOrder(request, response);
                break;
            default:
                listCoffee(request, response);
        }
    }
    
    private void listCoffee(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            RequestDispatcher rd = request.getRequestDispatcher("/CoffeeList.jsp");
            List<CoffeeType> coffeeList = coffeeDAO.listCoffeeType(false);
            request.setAttribute("coffeeList", coffeeList);
            rd.forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }
    
    private void delivery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        prepareOrder(request);
        RequestDispatcher rd = request.getRequestDispatcher("/Delivery.jsp");
        rd.forward(request, response);
    }    
    
    private void prepareOrder(HttpServletRequest request) throws ServletException{
        try {
            CoffeeOrder order = new CoffeeOrder();
            order.setOrderDate(new Date());
            List<CoffeeOrderItem> orderItems = new ArrayList<>();
            Map<String, String[]> parameters = request.getParameterMap();
            for (String key : parameters.keySet()) {
                String value = parameters.get(key)[0];
                if (!value.isEmpty()) {
                    CoffeeOrderItem orderItem = new CoffeeOrderItem();
                    CoffeeType coffeeType = coffeeDAO.getCoffeeTypeById(Long.parseLong(key));
                    orderItem.setCoffeeType(coffeeType);
                    orderItem.setQuantity(Integer.parseInt(value));
                    orderItem.setCoffeeOrder(order);
                    orderItems.add(orderItem);
                }
            }
            coffeeDAO.calculateCost(order, orderItems);
            System.out.println("orderItems.size: " + orderItems.size());
            request.getSession().setAttribute("orderItems", orderItems);
            request.getSession().setAttribute("order", order);
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }

    private void createOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<CoffeeOrderItem> orderItems = (List<CoffeeOrderItem>)request.getSession().getAttribute("orderItems");
        CoffeeOrder order = (CoffeeOrder)request.getSession().getAttribute("order");
        
        order.setName(request.getParameter("name"));
        order.setDeliveryAddress(request.getParameter("address"));
        
        try {
            coffeeDAO.createOrder(order, orderItems);
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
        RequestDispatcher rd = request.getRequestDispatcher("/Confirmation.jsp");
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
