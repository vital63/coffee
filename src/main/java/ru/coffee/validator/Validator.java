package ru.coffee.validator;

import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class Validator {

    @Autowired
    MessageSource messageSource;
    
    public boolean validateAddress(HttpServletRequest request, Locale locale) {
        String address = (String) request.getParameter("address");
        if (address == null || address.isEmpty()) {
            request.setAttribute("error", messageSource.getMessage("input_address", null, locale));
            return false;
        } else {
            return true;
        }
    }

    public boolean validateListCoffee(HttpServletRequest request, Locale locale) {
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
                    String error = messageSource.getMessage("quantity_not_correct", new Object[] {value, id}, locale);
                    request.setAttribute("error", error);
                    return false;
                }
            }
        }
        if (!hasPositive) {
            request.setAttribute("error", messageSource.getMessage("enter_positive_value", null, locale));
        }
        return hasPositive;
    }

}
