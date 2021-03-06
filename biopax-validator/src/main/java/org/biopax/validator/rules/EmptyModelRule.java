package org.biopax.validator.rules;


import org.biopax.paxtools.model.Model;
import org.biopax.validator.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

/**
 * Checks whether the model contains any objects.
 */
@Component
public class EmptyModelRule extends AbstractRule<Model> {
	
	public boolean canCheck(Object thing) {
		return thing instanceof Model;
	}

	public void check(final Validation validation, Model model) {
		if(model.getObjects().isEmpty())
			error(validation, model, "empty.biopax.model", false, model.getLevel().toString());
	}

}
