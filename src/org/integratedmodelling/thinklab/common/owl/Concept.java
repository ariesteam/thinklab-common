package org.integratedmodelling.thinklab.common.owl;

import java.util.Collection;
import java.util.Set;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IKnowledge;
import org.integratedmodelling.thinklab.api.knowledge.IOntology;
import org.integratedmodelling.thinklab.api.knowledge.IProperty;
import org.integratedmodelling.thinklab.api.knowledge.query.IQuery;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * Just a proxy for a concept, can be used to create concepts that are automatically inserted in the
 * proxy knowledge manager. Ontologies can be extracted as lists and marshalled to a server for
 * actual knowledge creation.
 * 
 * @author Ferd
 *
 */
public class Concept implements IConcept {

	String _id;
	String _cs;
	
	OWLClass _owl;
	
	public Concept(String cs, String id) {
		this._cs = cs;
		this._id = id;
	}
	
	@Override
	public String getConceptSpace() {
		return _cs;
	}

	@Override
	public String getLocalName() {
		return _id;
	}

	@Override
	public boolean is(IKnowledge concept) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean is(String semanticType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addAnnotation(String property, String value) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getAnnotation(String property) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getURI() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IConcept> getParents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IConcept> getAllParents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IConcept> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IProperty> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IProperty> getAllProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IConcept> getPropertyRange(IProperty property)
			throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAbstract() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IConcept getParent() throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPropertiesCount(String property) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMinCardinality(IProperty property) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxCardinality(IProperty property) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<IProperty> getAnnotationProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IConcept getLeastGeneralCommonConcept(IConcept c) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IQuery getDefinition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<IConcept> getSemanticClosure() {
		// TODO Auto-generated method stub
		return null;
	}

}
