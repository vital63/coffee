<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <div align="center">
            <table border="1" cellpadding="5">
                <tr>
                    <th><spring:message code="confirmation"/></th>
                </tr>
                <tr>
                    <td><spring:message code="order_received"/></td>
                </tr>
            </table>
            <%@include file="footer.jspf" %>
        </div>
    </body>
</html>
