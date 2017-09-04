package ru.coffee.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import ru.coffee.dao.CoffeeDAO;
import ru.coffee.domain.CoffeeOrder;
import ru.coffee.domain.CoffeeOrderItem;
import ru.coffee.domain.CoffeeType;

public class CoffeeService {

    private CoffeeDAO coffeeDAO = new CoffeeDAO();
    
    public void prepareOrder(HttpServletRequest request) throws ServletException {
        try {
            CoffeeOrder order = new CoffeeOrder();
            order.setOrderDate(new Date());
            List<CoffeeOrderItem> orderItems = new ArrayList<>();
            Map<String, String[]> parameters = request.getParameterMap();
            for (String key : parameters.keySet()) {
                if ("lang".equals(key)) {
                    continue;
                }
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
            request.getSession().setAttribute("orderItems", orderItems);
            request.getSession().setAttribute("order", order);
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }

    public void createOrder(HttpServletRequest request) throws ServletException {
        List<CoffeeOrderItem> orderItems = (List<CoffeeOrderItem>) request.getSession().getAttribute("orderItems");
        CoffeeOrder order = (CoffeeOrder) request.getSession().getAttribute("order");

        order.setName(request.getParameter("name"));
        order.setDeliveryAddress(request.getParameter("address"));

        try {
            coffeeDAO.createOrder(order, orderItems);
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }
    
    public void listCoffee(HttpServletRequest request) throws SQLException {
        ResourceBundle bundle = (ResourceBundle) request.getSession().getAttribute("bundle");
        Locale locale = bundle == null ? null : bundle.getLocale();
        List<CoffeeType> coffeeList = coffeeDAO.listCoffeeType(locale, false);
        request.setAttribute("coffeeList", coffeeList);
    }
}
