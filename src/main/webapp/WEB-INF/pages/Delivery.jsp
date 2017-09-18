<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <script type="text/javascript">
            function validate(){
                var errorMes = document.getElementById('error');
                var address = document.getElementById('address');
                if(address.value === ''){
                    errorMes.innerHTML = '<spring:message code="input_address"/>';
                    return false;
                }
                else
                    return true;
            }
        </script> 
    </head>
    <body>
        <div align="center">
            <form action="CreateOrder" method="POST" onsubmit="return validate()">
                <table border="1" cellpadding="5">
                    <tr><th colspan="2"><spring:message code="delivery_information"/></th></tr>
                    <tr>
                        <th><spring:message code="name"/></th>
                        <td><input name="name"></td>
                    </tr>
                    <tr>
                        <th><spring:message code="address"/></th>
                        <td><input id="address" name="address"></td>
                    </tr>
                    <tr>
                        <td colspan="1"></td>
                        <td><input type="submit" value=<spring:message code="create_order"/>/></td>
                    </tr>
                </table>
            </form>   
            <div id="error" style="color:red">${error}</div>
            <table border="1" cellpadding="5">
                <tr>
                    <th><spring:message code="type"/></th>
                    <th><spring:message code="price"/></th>
                    <th><spring:message code="quantity"/></th>
                    <th><spring:message code="total"/></th>
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
                    <th colspan="3"><spring:message code="cost"/></th>
                    <td><c:out value="${order.coffeeCost}" /></td>
                </tr>
                <tr>
                    <th colspan="3"><spring:message code="delivery_cost"/></th>
                    <td><c:out value="${order.deliveryCost}" /></td>
                </tr>
                <tr>
                    <th colspan="3"><spring:message code="total_cost"/></th>
                    <td><c:out value="${order.totalCost}" /></td>
                </tr>
            </table>
            <%@include file="footer.jspf" %>
        </div>
        
    </body>
</html>
