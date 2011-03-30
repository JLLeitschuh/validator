package psidev.ontology_manager.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;

import psidev.ontology_manager.Ontology;
import psidev.ontology_manager.OntologyManager;
import psidev.ontology_manager.OntologyTermI;

import java.io.File;
import java.net.*;
import java.util.*;

/**
 * Central access to configured Ontology.
 *
 * @author Florian Reisinger
 * @Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author rodche (baderlab.org) - re-factored for the BioPAX Validator
 * @since 2.0.0
 */
public class OntologyManagerImpl implements OntologyManager {

    public static final Log log = LogFactory.getLog( OntologyManagerImpl.class );
   
    
    /**
     * The Map that holds the Ontologies.
     * The key is the ontology ID and the value is a ontology inplementing the Ontology interface.
     */
    private Map<String, Ontology> ontologies;

    /**
     * Create a new OntologyManagerImpl with no configuration (no associated ontologies).
     */
    public OntologyManagerImpl() {
        ontologies = new HashMap<String, Ontology>();
    }

    /** 
     * Creates a new OntologyManagerImpl managing the ontologies specified in the config map.
     *
     * @param cfg configuration map for the manager (ID->resource location).
     * @throws OntologyLoaderException if the config file could not be parsed or the loading of a ontology failed.
     */
	public OntologyManagerImpl(Map<String, Resource> cfg)
			throws OntologyLoaderException 
	{
		this();
		loadOntologies(cfg);
		if (log.isDebugEnabled())
			log.debug("Successfully created and configured new OntologyManagerImpl.");
	}


    public Ontology putOntology( String ontologyID, Ontology ontology ) {
        if ( ontologies.containsKey( ontologyID ) ) {
            if ( log.isWarnEnabled() )log.warn( "Ontology with the ID '" + ontologyID + "' already exists. Overwriting!" );
        }
        return ontologies.put( ontologyID, ontology );
    }


    public Set<String> getOntologyIDs() {
        return ontologies.keySet();
    }


    public Ontology getOntology( String ontologyID ) {
        return ontologies.get( ontologyID );
    }


    public void setOntologyDirectory( File ontologyDirectory ) {
        OntologyManagerContext.getInstance().setOntologyDirectory( ontologyDirectory );
    }


    public boolean containsOntology( String ontologyID ) {
        return ontologies.containsKey( ontologyID );
    }

    
    public void loadOntologies( Map<String, Resource> configMap ) 
    	throws OntologyLoaderException 
    {
        if ( configMap != null && !configMap.isEmpty()) {
            for ( String ontId : configMap.keySet() ) 
            {
                try {
                	Resource source = configMap.get(ontId);
                	URI uri = source.getURI();

                	if ( log.isInfoEnabled() ) {
                		log.info( "Loading ontology: ID= " + 
                			ontId + ", uri=" + uri);
                	}

                    Ontology oa = fetchOntology( ontId, "OBO", uri );
                    putOntology(ontId, oa);
                } catch ( Exception e ) {
                    throw new OntologyLoaderException( "Failed loading ontology source.", e );
                }
            }
        } else {
        	throw new OntologyLoaderException("Ontology configuration map is missing or empty (map)!");
        }
    }
    

    protected Ontology fetchOntology( String ontologyID, String format, URI uri ) 
    	throws OntologyLoaderException {
    	Ontology oa = null;
    	
        // check the format
        if ( "OBO".equals( format ) ) {
            if ( uri == null ) {
                throw new IllegalArgumentException( "The given CvSource doesn't have a URL" );
            } else {
                URL url;
                try {
                    url = uri.toURL();
                } catch ( MalformedURLException e ) {
                    throw new IllegalArgumentException( "The given CvSource doesn't have a valid URL: " + uri );
                }

                // parse the URL and load the ontology
                OboLoader loader = new OboLoader( );
                try {
                    if ( log.isDebugEnabled() )
                        log.debug( "Parsing URL: " + url );
                    
                    oa = loader.parseOboFile( url, ontologyID );
                    oa.setName(ontologyID);
                } catch ( OntologyLoaderException e ) {
                    throw new OntologyLoaderException( "OboFile parser failed with Exception: ", e );
                }
            }
        } else {
            throw new OntologyLoaderException( "Unsupported ontology format: " + format );
        }

        if ( log.isInfoEnabled() ) {
            log.info( "Successfully created OntologyImpl from values: ontology="
                      + ontologyID + " format=" + format + " location=" + uri );
        }
        
        return oa;
    }
    

	public Set<OntologyTermI> searchTermByName(String name) {
		Set<OntologyTermI> found  = new HashSet<OntologyTermI>();
		
		for(String ontologyId: getOntologyIDs()) {
			Ontology oa = getOntology(ontologyId);
			for(OntologyTermI term : oa.getOntologyTerms()) {
				if(term.getPreferredName().equalsIgnoreCase(name)) {
					found.add(term);
				} else {
					for(String syn : term.getNameSynonyms()) {
						if(syn.equalsIgnoreCase(name)) {
							found.add(term);
						}
					}
				}
			}
		}
		
		return found;
	}
	
	
	public OntologyTermI findTermByAccession(String acc) {
		OntologyTermI term = null;
		
		for(String ontologyId : getOntologyIDs()) {
			term = getOntology(ontologyId).getTermForAccession(acc);
			if(term != null) 
				break;
		}
		
		return term;
	}

}