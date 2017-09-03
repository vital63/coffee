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
                        errorMes.innerHTML = '${bundle.getString("quantity")} ' + input.value + ' ${bundle.getString("for_id")}=' +
                               input.name + ' ${bundle.getString("is_not_correct")}!';
                        return false;
                    } else if(input.value > 0) {
                        hasPositive = true;
                    }
                }
                if(!hasPositive)
                    errorMes.innerHTML = "${bundle.getString("enter_positive_value")}";
                return hasPositive;
            }
        </script>
    </head>
    <body>
        <div align="center">
            <form action="Delivery" method="POST" onsubmit="return validate()">
                <table border="1" cellpadding="5">
                    <caption><h2>${bundle.getString("choose_coffee")}</h2></caption>
                    <tr>
                        <th>ID</th>
                        <th>${bundle.getString("type")}</th>
                        <th>${bundle.getString("price")}</th>
                        <th>${bundle.getString("quantity")}</th>
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
                        <td><input type="submit" value=${bundle.getString("create_order")}/></td>
                    </tr>
                </table>
            </form>   
            <%@include file="footer.jspf" %>
        </div>
    </body>
</html>
