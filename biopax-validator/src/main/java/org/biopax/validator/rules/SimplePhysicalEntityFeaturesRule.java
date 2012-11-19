package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * A rule to check if a Simple Physical Entity Feature is also a feature of
 * its Entity Reference. If not, fixes it by adding the missing feature
 * to the Entity Reference.
 *
 */
@Component
public class SimplePhysicalEntityFeaturesRule extends AbstractRule<SimplePhysicalEntity>{

    @Override
    public void check(final Validation validation, SimplePhysicalEntity thing) {
        EntityReference er = thing.getEntityReference();
        Set<EntityFeature> erefs = er.getEntityFeature(),
                           peefs = new HashSet<EntityFeature>();

        peefs.addAll(thing.getFeature());
        peefs.addAll(thing.getNotFeature());

        for(EntityFeature ef: peefs) {
            if(!erefs.contains(ef)) {
                if(validation.isFix())
                    er.addEntityFeature(ef);

                error(validation, thing, "improper.feature.use", validation.isFix(), ef.getRDFId(), er.getRDFId());
            }
        }
    }

    @Override
    public boolean canCheck(Object thing) {
        return thing instanceof SimplePhysicalEntity && ((SimplePhysicalEntity) thing).getEntityReference() != null;
    }
}