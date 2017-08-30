<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <div align="center">
            <form action="CreateOrder">
                <table border="1" cellpadding="5">
                    <tr><th colspan="2">Delivery Information</th></tr>
                    <tr>
                        <th>Name</th>
                        <td><input name="name"></td>
                    </tr>
                    <tr>
                        <th>Address</th>
                        <td><input name="address"></td>
                    </tr>
                    <tr>
                        <td colspan="1"></td>
                        <td><input type="submit" value="Create Order"/></td>
                    </tr>
                </table>
            </form>   

            <table border="1" cellpadding="5">
                <tr>
                    <th>Coffee Type</th>
                    <th>Price</th>
                    <th>Quantity</th>
                    <th>Total</th>
                </tr>
                
                <c:forEach var="orderItem" items="${orderItems}">
                    <tr>
                        <td><c:out value="${orderItem.coffeeType.type}" /></td>
                        <td><c:out value="${orderItem.coffeeType.price}" /></td>
                        <td><c:out value="${orderItem.quantity}" /></td>
                        <td><c:out value="${orderItem.cost}" /></td>
                    </tr>
                </c:forEach>
                
                <tr>
                    <th colspan="3">Cost</th>
                    <td><c:out value="${order.coffeeCost}" /></td>
                </tr>
                <tr>
                    <th colspan="3">Delivery Cost</th>
                    <td><c:out value="${order.deliveryCost}" /></td>
                </tr>
                <tr>
                    <th colspan="3">Total Cost</th>
                    <td><c:out value="${order.totalCost}" /></td>
                </tr>
            </table>
            
        </div>
        
    </body>
</html>
