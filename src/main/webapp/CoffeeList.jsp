<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Coffee List</title>
        <script type="text/javascript">
            function validate(){
                var errorMes = document.getElementById('error');
                var elements = document.getElementsByTagName('input');
                var hasPositive = false;
                for (var i = 0; i < elements.length; i++) {
                    var input = elements[i];
                    if((input.type === 'submit') || (input.value === ''))
                        continue;
                    
                    var intValue = parseInt(input.value); 
                    if (isNaN(intValue) || (input.value < 0)){
                        errorMes.innerHTML = 'Quantitry ' + input.value + ' for id=' + input.name + ' is not correct!';
                        return false;
                    }
                    if(input.value > 0)
                        hasPositive = true;
                }
                if(!hasPositive)
                    errorMes.innerHTML = "Enter positive value for some Coffee!";
                return hasPositive;
            }
        </script>
    </head>
    <body>
        <div align="center">
            <form action="Delivery" onsubmit="return validate()">
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
                        <td id="error" colspan="3" style="color:red">${error}</td>
                        <td><input type="submit" value="Create Order"/></td>
                    </tr>
                </table>
            </form>   
        </div>
    </body>
</html>
