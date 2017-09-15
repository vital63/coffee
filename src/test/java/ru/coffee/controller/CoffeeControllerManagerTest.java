package ru.coffee.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.coffee.dao.CoffeeDAOJDBC;
import ru.coffee.domain.CoffeeOrder;
import ru.coffee.domain.CoffeeOrderItem;
import ru.coffee.domain.CoffeeType;
import ru.coffee.filter.ServiceFilter;
import ru.coffee.service.CoffeeService;
import ru.coffee.dao.CoffeeDAO;

public class CoffeeControllerManagerTest {
    
    public CoffeeControllerManagerTest() {
        ApplicationContext context = new ClassPathXmlApplicationContext("SpringConfig.xml");
        controllerManager = (CoffeeControllerManager) context.getBean("coffeeControllerManager");
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    private CoffeeControllerManager controllerManager;
    
    @Test
    public void testListCoffee() throws Exception {
        HttpServletRequest request = new FakeHttpServletRequest();
        ServiceFilter.setMessageBundle(request);
        
        controllerManager.listCoffee(request, null);
        List<CoffeeType> coffeeList =(List<CoffeeType>)request.getAttribute("coffeeList");
        
        assertNotNull("coffeeList is null", coffeeList);
        assertEquals("Size of coffeeList is not right", 4, coffeeList.size());
        
        Map<Long, Float> idToPriceExpected = new HashMap<>();
        idToPriceExpected.put(1l, 20f);
        idToPriceExpected.put(2l, 15f);
        idToPriceExpected.put(3l, 10f);
        idToPriceExpected.put(4l, 12f);
        
        for(CoffeeType coffeeType: coffeeList){
            assertEquals("Price is not correct for ID: ", idToPriceExpected.get(coffeeType.getId()), coffeeType.getPrice());
        }
    }
    
    @Test
    public void testDelivery() throws Exception {
        doTestErrorDeliveryRequest(getErrorDeliveryRequest1(), "Referer", "enter_positive_value");
        doTestErrorDeliveryRequest(getErrorDeliveryRequest2(), "Referer", "quantity_not_correct", "-5", 4);
        doTestErrorDeliveryRequest(getErrorDeliveryRequest3(), "Referer", "enter_positive_value");
        doTestErrorDeliveryRequest(getErrorDeliveryRequest4(), "Referer", "quantity_not_correct", "not number", 4);

        FakeHttpServletRequest request = getCorrectDeliveryRequest();
        FakeHttpServletResponse response = new FakeHttpServletResponse();
        controllerManager.delivery(request, response);
        assertNull("Redirect is wrong: ", response.getRedirectLocation());
        assertNull("Error message must be null: ", request.getAttribute("error"));
        
        List<CoffeeOrderItem> orderItems = (List<CoffeeOrderItem>)request.getSession().getAttribute("orderItems");
        assertNotNull("orderItems is null", orderItems);
        assertEquals("orderItems size is not correct: ", 2, orderItems.size());
        
        Map<Long, CoffeeOrderItem> idToExpectedItems = new HashMap<>();
        idToExpectedItems.put(2l, new CoffeeOrderItem(2l, 2, 30f));
        idToExpectedItems.put(3l, new CoffeeOrderItem(3l, 5, 40f));
        
        for(CoffeeOrderItem item: orderItems){
            CoffeeOrderItem expectedItem = idToExpectedItems.get(item.getCoffeeType().getId());
            assertNotNull(expectedItem);
            assertEquals("Quantity is not correct: ", expectedItem.getQuantity(), item.getQuantity());
            assertEquals("Cost is not correct: ", expectedItem.getCost(), item.getCost(), 0.01f);
        }
        
        CoffeeOrder order = (CoffeeOrder)request.getSession().getAttribute("order");
        assertNotNull("order is null", order);
        assertEquals("Cost of order is not valid: ", 70f, order.getCoffeeCost(), 0.01f);
        assertEquals("Cost of delivery is not valid: ", 0f, order.getDeliveryCost(), 0.01f);
        assertEquals("Total cost is not valid: ", 70f, order.getTotalCost(), 0.01f);
    }

    @Test
    public void testCreateOrder() throws Exception {
        FakeHttpServletRequest request = getCorrectDeliveryRequest();
        FakeHttpServletResponse response = new FakeHttpServletResponse();
        controllerManager.delivery(request, response);
        
        testCreateOrderWithoutAddress(request, response);
        
        CoffeeService coffeeService = controllerManager.getCoffeeService();
        
        long nextID = coffeeService.getNextID("coffeeorder");
        testCreateOrderWithAddress(request, response);
        
        List<CoffeeOrderItem> orderItems = new ArrayList<>();
        CoffeeOrder coffeeOrder = coffeeService.getOrder(nextID, orderItems);
        assertNotNull("coffeeOrder is null: ", coffeeOrder);
        
        CoffeeOrder expectedOrder = (CoffeeOrder) request.getSession().getAttribute("order");
        Date expectedOrderDate = getDateRoundedToMinute(expectedOrder.getOrderDate());
        Date orderDate = getDateRoundedToMinute(coffeeOrder.getOrderDate());
        assertEquals("Order Date check: ", expectedOrderDate, orderDate);
        assertEquals("Order Name check: ", expectedOrder.getName(), coffeeOrder.getName());
        assertEquals("Order Address check: ", expectedOrder.getDeliveryAddress(), coffeeOrder.getDeliveryAddress());
        assertEquals("Order TotalCost check: ", expectedOrder.getTotalCost(), coffeeOrder.getTotalCost());

        final Comparator<CoffeeOrderItem> comparator = (CoffeeOrderItem o1, CoffeeOrderItem o2) -> (int)(o1.getId() - o2.getId());

        Collections.sort(orderItems, comparator);
        
        List<CoffeeOrderItem> expectedOrderItems = (List<CoffeeOrderItem>) request.getSession().getAttribute("orderItems");
        assertNotNull("expectedOrderItems is null!", expectedOrderItems);
        assertEquals("Size of colections of orderItems not conside: ", expectedOrderItems.size(), orderItems.size());
        Collections.sort(expectedOrderItems, comparator);

        for(int i = 0; i < expectedOrderItems.size(); i++){
            CoffeeOrderItem expItem = expectedOrderItems.get(i);
            CoffeeOrderItem item = orderItems.get(i);
            assertEquals("OrderItem ID check: ", expItem.getId(), item.getId());
            assertEquals("OrderItem typeID check: ", expItem.getCoffeeType().getId(), item.getCoffeeType().getId());
            assertEquals("OrderItem orderID check: ", expItem.getCoffeeOrder().getId(), item.getCoffeeOrder().getId());
            assertEquals("OrderItem quantity check: ", expItem.getQuantity(), item.getQuantity());
        }
    }
    
    private static Date getDateRoundedToMinute(Date date) {
        long ms = date.getTime();
        ms = ms - ms % (1000 * 60);
        return new Date(ms);
    }
    
    private void testCreateOrderWithoutAddress(FakeHttpServletRequest request, FakeHttpServletResponse response) throws IOException, ServletException {
        request.setParameter("name", "Vital");
        controllerManager.createOrder(request, response);
        assertEquals("Redirect is wrong: ", "Referer", response.getRedirectLocation());
        String expectedErrorMessage = getStringFromBundleByKey(request, "input_address");
        assertEquals("Error message is wrong: ", expectedErrorMessage, request.getAttribute("error"));
    }

    private void testCreateOrderWithAddress(FakeHttpServletRequest request, FakeHttpServletResponse response) throws IOException, ServletException {
        request.setAttribute("error", null);
        request.setParameter("address", "Ratomka Station");
        controllerManager.createOrder(request, response);
        assertEquals("Redirect is wrong: ", "Confirmation", response.getRedirectLocation());
        assertNull("Error message must be null: ", request.getAttribute("error"));
    }
    
    private FakeHttpServletRequest getDeliveryRequest() throws ServletException, IOException {
        FakeHttpServletRequest request = new FakeHttpServletRequest();
        ServiceFilter.setMessageBundle(request);
        request.setMethod("POST");
        return request;
    }

    private FakeHttpServletRequest getCorrectDeliveryRequest() throws ServletException, IOException {
        FakeHttpServletRequest request = getDeliveryRequest();
        request.setParameter("2", "2");
        request.setParameter("3", "5");
        return request;
    }

    //Empty params
    private FakeHttpServletRequest getErrorDeliveryRequest1() throws ServletException, IOException {
        return getDeliveryRequest();
    }

    //Contain negative params
    private FakeHttpServletRequest getErrorDeliveryRequest2() throws ServletException, IOException {
        FakeHttpServletRequest request = getCorrectDeliveryRequest();
        request.setParameter("4", "-5");
        return request;
    }

    //Contain zero params
    private FakeHttpServletRequest getErrorDeliveryRequest3() throws ServletException, IOException {
        FakeHttpServletRequest request = getDeliveryRequest();
        request.setParameter("4", "0");
        return request;
    }

    //Contain not number params
    private FakeHttpServletRequest getErrorDeliveryRequest4() throws ServletException, IOException {
        FakeHttpServletRequest request = getCorrectDeliveryRequest();
        request.setParameter("4", "not number");
        return request;
    }

    private void doTestErrorDeliveryRequest(FakeHttpServletRequest request, String expectedRedirect,
            String expectedErrorKey, Object... args) throws IOException, ServletException {
        FakeHttpServletResponse response = new FakeHttpServletResponse();
        controllerManager.delivery(request, response);
        assertEquals("Redirect is wrong: ", expectedRedirect, response.getRedirectLocation());

        String expectedErrorMessage = getStringFromBundleByKey(request, expectedErrorKey, args);
        assertEquals("Error message is wrong: ", expectedErrorMessage, request.getAttribute("error"));
    }

    private String getStringFromBundleByKey(HttpServletRequest request, String key, Object... args) {
        ResourceBundle bundle = (ResourceBundle) request.getSession().getAttribute("bundle");
        return String.format(bundle.getString(key), args);
    }
}
