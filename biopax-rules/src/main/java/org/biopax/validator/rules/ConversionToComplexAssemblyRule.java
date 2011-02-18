package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.*;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * A rule to check if the Conversion can be converted
 * to a ComplexAssembly.
 *
 * Some Conversions are not cast as ComplexAssembly,
 * although there is no modification to the PEs
 * and there is a clear Complex formation
 * throughout the process.
 *
 */
@Component
public class ConversionToComplexAssemblyRule extends AbstractRule<Conversion> {

    public void check(Conversion thing, boolean fix) {
        Set<PhysicalEntity> left = getPEsRecursively(thing.getLeft()),
                            right = getPEsRecursively(thing.getRight());

        left.removeAll(right);

        int complexDiff = getComplexCount(thing.getLeft()) - getComplexCount(thing.getRight());
        if( left.isEmpty()
                && (complexDiff != 0 || thing.getLeft().size() - thing.getRight().size() != 0 ))
            error(thing, "wrong.conversion.class", false, thing.getModelInterface());

    }

    private int getComplexCount(Set<PhysicalEntity> pes) {
        int count = 0;

        for(PhysicalEntity pe: pes) {
            if(pe instanceof Complex)
                count++;
        }

        return count;
    }

    private Set<PhysicalEntity> getPEsRecursively(Set<PhysicalEntity> pes) {
        Set<PhysicalEntity> pool = new HashSet<PhysicalEntity>();
        for(PhysicalEntity pe: pes) {
            if(pe instanceof Complex)
                pool.addAll(getPEsRecursively(((Complex) pe).getComponent()));
            else
                pool.add(pe);
        }

        return pool;
    }

    public boolean canCheck(Object thing) {
        return thing instanceof Conversion
                && !(thing instanceof ComplexAssembly);
    }
}