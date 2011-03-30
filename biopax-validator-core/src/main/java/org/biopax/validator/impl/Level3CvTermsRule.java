package org.biopax.validator.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import javax.annotation.PostConstruct;

import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level3.Level3Element;
import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.validator.utils.BiopaxValidatorUtils;
import org.biopax.validator.utils.Normalizer;
import org.springframework.beans.factory.annotation.Configurable;

import psidev.ontology_manager.*;


/**
 * An abstract class for CV terms checking (BioPAX Level3).
 * 
 * @author rodch
 */
@Configurable
public abstract class Level3CvTermsRule<T extends Level3Element> 
	extends AbstractCvRule<T> {
  
    /**
     * Constructor.
     * 
     * TODO allow using properties path as the 'property' name parameter, i.e., "modificationFeature/modificationType"
     * 
     * @param domain
     * @param property
     * @param restrictions
     */
    public Level3CvTermsRule(Class<T> domain, String property, CvTermRestriction... restrictions)
    {
    	super(domain, property, restrictions);
    }
   
    @PostConstruct
    public void init() {
    	super.init();
		this.editor = (property != null && !ControlledVocabulary.class.isAssignableFrom(domain)) 
			? BiopaxValidatorUtils.EDITOR_MAP_L3.getEditorForProperty(property, this.domain)
			: null;    	
    };
    
    
	public void check(T thing, boolean fix) {
		// a set of CVs for this rule to validate
		Collection<ControlledVocabulary> vocabularies = new HashSet<ControlledVocabulary>();
		
		// if the editor is null, we expect a ControlledVocabulary object!
		if(editor == null) {
			vocabularies.add((ControlledVocabulary)thing); 
		} else if(editor.isMultipleCardinality()) {
			vocabularies = (Collection<ControlledVocabulary>) editor.getValueFromBean(thing);
		} else {
			ControlledVocabulary value = (ControlledVocabulary) editor.getValueFromBean(thing);
			if(value != null) vocabularies.add(value);
		}
		
		// shortcut
		if(vocabularies.isEmpty()) return;
		
		// text to report in any CV error case
		String cvRuleInfo = ((editor != null) ? " property: "
				+ property : "") + " " + restrictions.toString();
		
		// check each CV terms against the restrictions
		for (ControlledVocabulary cv : vocabularies) 
		{
			if (cv == null) {
				logger.warn(thing
					+ " referes to 'null' controlled vocabulary (bug!): "
					+ ", domain: " + domain + ", property: " + property);
			} else if(cv.getTerm().isEmpty()) {
				/* won't report/fix what other rules (e.g., 'controlledVocabularyTermCRRule') 
				 * or Normalizercan do! */
			} 
			else {
				//TODO (advanced feature, can/must be a separate rule) check if multiple terms are, in fact, synonyms/equivalent...
				
				final Set<String> badTerms = new HashSet<String>(); // initially - none
				final Set<String> noXrefTerms = new HashSet<String>(cv.getTerm()); // initially - all
				
				// first, check terms (names) are valid
				for(String name : cv.getTerm()) 
				{
					if(!getValidTerms().contains(name.toLowerCase())) {
						// save to report/delete/replace the invalid term later
						badTerms.add(name);
						noXrefTerms.remove(name); // - exclude invalid terms from extra checks (below)
					}
				}
				
				// second, check valid terms have uni.xrefs
				for(String name : cv.getTerm()) 
				{
					// only for valid terms
					if(getValidTerms().contains(name.toLowerCase())) {
						// check if there is the corresponding unification xref
						Set<OntologyTermI> ots = ((OntologyManager) ontologyManager).searchTermByName(name.toLowerCase());
						assert(!ots.isEmpty()); // shouldn't be, because the above getValidTerms() contains the name
						for(OntologyTermI term : ots) {
							String prefname = term.getPreferredName();
							String ontId = term.getOntologyId(); // e.g., "GO" 
							String db = ((OntologyManager) ontologyManager).getOntology(ontId).getName();
							String id = term.getTermAccession();
							// search for the xref with the same xref.id
							for (UnificationXref x : new ClassFilterSet<UnificationXref>(
									cv.getXref(), UnificationXref.class)) {
								/// exclude names "matching" uni.xrefs from the "no Xref" set
								if(id.equalsIgnoreCase(x.getId()))  {
									noXrefTerms.remove(name);
								}
							}
						}
					}
				}
				
				// note: at this point, 'noXrefTerms' (valid terms only) set is defined...
				
				if (!noXrefTerms.isEmpty()) {		
					String noXrefTermsInfo = noXrefTerms.toString();
					boolean fixed = false;
					
					if(fix) {
					/*
					 * However, it's not so trivial to fix by adding the xrefs, because:
					 * 1) no reference to the parent Model here available
					 *    (thus the validator must detect and add new objects automatically! [done!])
					 * 2) having the chance of creating several xrefs with the same RDFId requires 
					 *    a special care or follow-up merging, as simply adding them to a model will 
					 *    throw the "already have this element" exception!); and other rules 
					 *    can also generate duplicates...
					 * 3) risk that a rule generating/adding a new element may cause  
					 *    other rules to interfere via AOP and prevent changes in quite 
					 *    unpredictable manner (...bites its own tail)
					 * 4) multiple terms (accession numbers) can result from searching by (synonym) name
					 *    
					 *    Well, let's try to fix, anyway (and modifying ValidatorImpl as well)!
					 *    That's awesome!!!
					 */
						Set<OntologyTermI> validTermIs = ontologyManager.getValidTerms(this);
						for (String name : noXrefTerms) {
							//search for the ontology term(s) by name
							Set<OntologyTermI> ots = ((OntologyManager) ontologyManager)
									.searchTermByName(name.toLowerCase());
							//get only top (parent) valid terms
							Set<OntologyTermI> topvalids = new HashSet<OntologyTermI>();
							for (OntologyTermI term : ots) {
								// skip terms that are not applicable although having the same synonym name
								if(validTermIs.contains(term)) {
									Ontology ont = ((OntologyManager)ontologyManager).getOntology(term.getOntologyId());
									//if term's parents does not contain any of these terms
									if(Collections.disjoint(ots, ont.getAllParents(term))) {
										topvalids.add(term);
									}
								}
							}
							for (OntologyTermI term : topvalids) {								
								String ontId = term.getOntologyId();
								String db = ((OntologyManager) ontologyManager)
										.getOntology(ontId).getName();
								String id = term.getTermAccession();
								// auto-create and add the xref to the cv
								String rdfid;
								try {
									rdfid = Normalizer.BIOPAX_URI_PREFIX + "UnificationXref:" 
										+ URLEncoder.encode(db + "_" + id,"UTF-8")
										.toUpperCase(); // important!
								} catch (UnsupportedEncodingException e) {
									throw new RuntimeException(e);
								}
								
								UnificationXref ux = BioPAXLevel.L3.getDefaultFactory()
									.create(UnificationXref.class, rdfid);
								ux.setDb(db);
								ux.setId(id);
								cv.addXref(ux);
								fixed = true; // 99% true ;-)
								noXrefTermsInfo += "; " + id + " added!";
							}
						}
					}
					
					// report					
					error(thing, "no.xref.cv.terms", fixed, 
						noXrefTermsInfo, cvRuleInfo);
				}
				
				/* check if valid terms that can be inferred from the xref.id, 
				 * and report 'illegal.cv.xref' otherwise!
				 */
				final Set<UnificationXref> badXrefs = new HashSet<UnificationXref>(); // initially - none
				for (UnificationXref x : new ClassFilterSet<UnificationXref>(
						cv.getXref(), UnificationXref.class)) {
					OntologyTermI ot = ((OntologyManager) ontologyManager).findTermByAccession(x.getId());
					if(ot == null || !getValidTerms().contains(ot.getPreferredName().toLowerCase())) {
						badXrefs.add(x);
					}
				}
				
				// fix / cleanup and report wrong uni.xrefs (important: before fixing wrong terms!)
				if(!badXrefs.isEmpty()) {
					String bads = badXrefs.toString();
					if(fix) {
						cv.getXref().removeAll(badXrefs);
						bads += " were removed!";
						error(thing, "illegal.cv.xref", true, // fixed!
								bads, cvRuleInfo);
					} else {
						error(thing, "illegal.cv.xref", false, // not fixed!
								bads, cvRuleInfo);
					}
				}
									
				// fix / report wrong terms
				if (!badTerms.isEmpty()) {	
					boolean fixed = false;
					String badTermInfo = badTerms.toString();
					if (fix) {
						/* remove illegal terms only if addTerms (to add) is not empty,
						 * otherwise - do not fix!
						 */
						// try infer term names from the valid unification xrefs
						Set<String> addTerms = createTermsFromUnificationXrefs(cv);
						if (!addTerms.isEmpty()) {
							cv.getTerm().removeAll(badTerms);
							badTermInfo += " were removed";
							cv.getTerm().addAll(addTerms);
							badTermInfo += "; terms added " +
								"(inferred from the unification xref(s)): "
									+ addTerms.toString();
							fixed = true;
						}
					}
					// report
					error(thing, "illegal.cv.term", fixed, badTermInfo, cvRuleInfo);
				}

			} 
		}
	}
		
	
	private Set<String> createTermsFromUnificationXrefs(
			ControlledVocabulary cv) 
	{		
		Set<String> inferred = new HashSet<String>();
		for (UnificationXref x : new ClassFilterSet<UnificationXref>(
				cv.getXref(), UnificationXref.class)) 
		{
			OntologyTermI ot = ((OntologyManager) ontologyManager)
					.findTermByAccession(x.getId());
			if (ot != null) {
				inferred.add(ot.getPreferredName());
			} 
			else {
				if (logger.isWarnEnabled())
					logger.warn("Could not find a term by the xref.id: " + x.getId());
			}
		}
		
		return inferred;
	}       
	
}
