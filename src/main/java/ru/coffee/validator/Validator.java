package ru.coffee.validator;

import java.util.Map;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;

public class Validator {

    public static boolean validateAddress(HttpServletRequest request) {
        String address = (String) request.getParameter("address");
        if (address == null || address.isEmpty()) {
            request.setAttribute("error", "Input Address!");
            return false;
        } else {
            return true;
        }
    }

    public static boolean validateListCoffee(HttpServletRequest request) {
        boolean hasPositive = false;
        Map<String, String[]> parameters = request.getParameterMap();
        for (String key : parameters.keySet()) {
            String value = parameters.get(key)[0];
            if (!value.isEmpty()) {
                if ("lang".equals(key)) {
                    continue;
                }
                long id = Long.parseLong(key);
                try {
                    int quantity = Integer.parseInt(value);
                    if (quantity < 0) {
                        throw new NumberFormatException();
                    }
                    if (quantity > 0) {
                        hasPositive = true;
                    }
                } catch (NumberFormatException e) {
                    request.setAttribute("error", String.format("Quantitry %s for id=%d is not correct!", value, id));
                    return false;
                }
            }
        }
        if (!hasPositive) {
            ResourceBundle bundle = (ResourceBundle) request.getSession().getAttribute("bundle");
            request.setAttribute("error", bundle.getString("enter_positive_value"));
        }
        return hasPositive;
    }

}
