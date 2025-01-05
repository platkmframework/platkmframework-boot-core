package org.platkmframework.comon.service.validator;

import java.util.Set;
import org.platkmframework.core.request.exception.RequestProcessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

/**
 *   Author:
 *     Eduardo Iglesias
 *   Contributors:
 *   	Eduardo Iglesias - initial API and implementation
 */
public class ValidatorUtil {

	
	/**
	 * Base64Util
	 */
	  private ValidatorUtil() {
	    throw new IllegalStateException("ValidatorUtil class");
	  }
	  
	  
    /**
     * Atributo factory
     */
    private static ValidatorFactory factory = null;

    /**
     * checkValidation
     * @param objBean objBean
     * @throws RequestProcessException RequestProcessException
     */
    public static void checkValidation(Object objBean) throws RequestProcessException {
        if (factory == null)
            factory = Validation.byDefaultProvider().configure().buildValidatorFactory();
        Set<ConstraintViolation<Object>> constraintViolations = factory.getValidator().validate(objBean);
        if (constraintViolations.size() > 0) {
            String msg = "";
            String coma = "";
            for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
                msg += coma + constraintViolation.getPropertyPath() + " " + constraintViolation.getMessage();
                coma = ", ";
            }
            throw new RequestProcessException(msg);
        }
    }
}
