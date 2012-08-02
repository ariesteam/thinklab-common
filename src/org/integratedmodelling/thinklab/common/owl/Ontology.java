package org.integratedmodelling.thinklab.common.owl;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.thinklab.api.knowledge.IAxiom;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IOntology;
import org.integratedmodelling.thinklab.api.knowledge.IProperty;
import org.integratedmodelling.thinklab.api.lang.IList;

/**
 * A proxy for an ontology. Holds a list of concepts and a list of axioms. Can be
 * turned into a list and marshalled to a server for actual knowledge creation. Contains
 * no instances, properties or restrictions directly, just concepts for indexing and axioms for
 * the actual stuff.
 * 
 * @author Ferd
 *
 */
public class Ontology implements IOntology {

	String _id;
	String _uriFragment;
	ArrayList<IList> _axioms = new ArrayList<IList>();
	HashMap<String, IConcept> _concepts = new HashMap<String, IConcept>();

	public Ontology(String id) {
		this._id = id;
		this._uriFragment = id.replace('.', '/');
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLabel(String languageCode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription(String languageCode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addDescription(String desc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDescription(String desc, String language) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addLabel(String desc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addLabel(String desc, String language) {
		// TODO Auto-generated method stub

	}

	@Override
	public IOntology getOntology() {
		return this;
	}
	
	@Override
	public Collection<IConcept> getConcepts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IProperty> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Create any concept that isn't available
	 */
	@Override
	public IConcept getConcept(String ID) {
		IConcept ret = _concepts.get(ID);
		if (ret == null) {
			_concepts.put(ID, ret = new Concept(_id, ID));
		}
		return ret;
	}


	@Override
	public IProperty getProperty(String ID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getConceptSpace() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getURI() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public long getLastModificationDate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean write(String uri) throws ThinklabException {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public void define(Collection<IAxiom> axioms) throws ThinklabException {
		// TODO Auto-generated method stub
		
	}

}
