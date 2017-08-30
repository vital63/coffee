<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Coffee List</title>
    </head>
    <body>
        <div align="center">
            <form action="Delivery">
                <table border="1" cellpadding="5">
                    <caption><h2>Choose Coffee</h2></caption>
                    <tr>
                        <th>ID</th>
                        <th>Type</th>
                        <th>Price</th>
                        <th>Quantity</th>
                    </tr>
                    <c:forEach var="coffee" items="${coffeeList}">
                        <tr>
                            <td><c:out value="${coffee.id}" /></td>
                            <td><c:out value="${coffee.type}" /></td>
                            <td><c:out value="${coffee.price}" /></td>
                            <td>
                                <input name="${coffee.id}">
                            </td>
                        </tr>
                    </c:forEach>
                    <tr>
                        <td colspan="3"></td>
                        <td><input type="submit" value="Create Order"/></td>
                    </tr>
                </table>
            </form>   
        </div>
    </body>
</html>
