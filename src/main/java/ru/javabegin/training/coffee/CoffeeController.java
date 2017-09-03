package ru.javabegin.training.coffee;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//TODO: internationalization
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

    private static void setMessageBundle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        Locale locale;
        String lang = request.getParameter("lang");
        
        if(lang != null && !lang.isEmpty()){
            locale = Locale.forLanguageTag(lang);
        }else if(request.getSession().getAttribute("bundle") == null){
            locale = Locale.ENGLISH;
        }else
            return;
        
        ResourceBundle bundle = ResourceBundle.getBundle("locales.messages", locale);
        request.getSession().setAttribute("bundle", bundle);
    }
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setMessageBundle(request, response);
        
        String action = request.getServletPath();
        System.out.println("action: " + action);
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
            List<CoffeeType> coffeeList = coffeeDAO.listCoffeeType(false);
            request.setAttribute("coffeeList", coffeeList);
            forwardToView(request, response, "/CoffeeList.jsp");
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }
    
    private boolean validateListCoffee(HttpServletRequest request){
        boolean hasPositive = false;
        Map<String, String[]> parameters = request.getParameterMap();
        for (String key : parameters.keySet()) {
            String value = parameters.get(key)[0];
            if (!value.isEmpty()) {
                if("lang".equals(key))
                    continue;
                
                long id = Long.parseLong(key);
                try {
                    int quantity = Integer.parseInt(value);
                    
                    if(quantity < 0)
                        throw new NumberFormatException();
                    
                    if(quantity > 0)
                        hasPositive = true;
                } catch (NumberFormatException e) {
                    request.setAttribute("error", String.format("Quantitry %s for id=%d is not correct!", value,id));
                    return false;
                }
            }
        }
        
        if(!hasPositive){
            ResourceBundle bundle = (ResourceBundle)request.getSession().getAttribute("bundle");
            request.setAttribute("error", bundle.getString("enter_positive_value"));
        }
        
        return hasPositive;
    }
    
    private void delivery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if("POST".equals(request.getMethod())){
            if(!validateListCoffee(request)){
                response.sendRedirect(request.getHeader("Referer"));
                return;
            }
            prepareOrder(request);
        }
        
        forwardToView(request, response, "/Delivery.jsp");
    }    
    
    private void prepareOrder(HttpServletRequest request) throws ServletException{
        try {
            CoffeeOrder order = new CoffeeOrder();
            order.setOrderDate(new Date());
            List<CoffeeOrderItem> orderItems = new ArrayList<>();
            Map<String, String[]> parameters = request.getParameterMap();
            for (String key : parameters.keySet()) {
                if ("lang".equals(key)) 
                    continue;
                
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

    private boolean validateAddress(HttpServletRequest request) {
        String address = (String)request.getParameter("address");
        if(address == null || address.isEmpty()){
            request.setAttribute("error", "Input Address!");
            return false;
        }else
            return true;
    }
    
    private void createOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!validateAddress(request)) {
            response.sendRedirect(request.getHeader("Referer"));
            return;
        }
        
        List<CoffeeOrderItem> orderItems = (List<CoffeeOrderItem>)request.getSession().getAttribute("orderItems");
        CoffeeOrder order = (CoffeeOrder)request.getSession().getAttribute("order");
        
        order.setName(request.getParameter("name"));
        order.setDeliveryAddress(request.getParameter("address"));
        
        try {
            coffeeDAO.createOrder(order, orderItems);
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
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
