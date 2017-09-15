<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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
                    errorMes.innerHTML = "Input Address!";
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
                    <tr><th colspan="2">${bundle.getString("delivery_information")}</th></tr>
                    <tr>
                        <th>${bundle.getString("name")}</th>
                        <td><input name="name"></td>
                    </tr>
                    <tr>
                        <th>${bundle.getString("address")}</th>
                        <td><input id="address" name="address"></td>
                    </tr>
                    <tr>
                        <td colspan="1"></td>
                        <td><input type="submit" value=${bundle.getString("create_order")}/></td>
                    </tr>
                </table>
            </form>   
            <div id="error" style="color:red">${error}</div>
            <table border="1" cellpadding="5">
                <tr>
                    <th>${bundle.getString("type")}</th>
                    <th>${bundle.getString("price")}</th>
                    <th>${bundle.getString("quantity")}</th>
                    <th>${bundle.getString("total")}</th>
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
                    <th colspan="3">${bundle.getString("cost")}</th>
                    <td><c:out value="${order.coffeeCost}" /></td>
                </tr>
                <tr>
                    <th colspan="3">${bundle.getString("delivery_cost")}</th>
                    <td><c:out value="${order.deliveryCost}" /></td>
                </tr>
                <tr>
                    <th colspan="3">${bundle.getString("total_cost")}</th>
                    <td><c:out value="${order.totalCost}" /></td>
                </tr>
            </table>
            <%@include file="footer.jspf" %>
        </div>
        
    </body>
</html>
