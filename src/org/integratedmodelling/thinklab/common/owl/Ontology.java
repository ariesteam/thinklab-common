package org.integratedmodelling.thinklab.common.owl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.thinklab.api.knowledge.IAxiom;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IOntology;
import org.integratedmodelling.thinklab.api.knowledge.IProperty;
import org.integratedmodelling.thinklab.api.lang.IList;
import org.integratedmodelling.thinklab.api.metadata.IMetadata;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.PrefixManager;

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
	ArrayList<IList> _axioms = new ArrayList<IList>();
	
	OWLOntology _ontology;
	PrefixManager _prefix;
	
	HashSet<String> _conceptIDs  = new HashSet<String>();
	HashSet<String> _propertyIDs = new HashSet<String>();
	
	Ontology(OWLOntology ontology, String id) {
		_id = id;
		_ontology = ontology;
		scan();
	}

	/*
	 * build a catalog of names, as there seems to be no way to quickly assess
	 * if an ontology contains a named entity or not. This needs to be kept in 
	 * sync with any changes, which is a pain.
	 */
	private void scan() {
		for (OWLClass c : _ontology.getClassesInSignature()) {
			_conceptIDs.add(c.getIRI().getFragment());
		}
		for (OWLProperty<?,?>  p : _ontology.getDataPropertiesInSignature()) {
			_propertyIDs.add(p.getIRI().getFragment());
		}
		for (OWLProperty<?,?>  p : _ontology.getObjectPropertiesInSignature()) {
			_propertyIDs.add(p.getIRI().getFragment());
		}
		for (OWLAnnotationProperty  p : _ontology.getAnnotationPropertiesInSignature()) {
			_propertyIDs.add(p.getIRI().getFragment());
		}		
	}

	@Override
	public IOntology getOntology() {
		return this;
	}
	
	@Override
	public Collection<IConcept> getConcepts() {
		
		ArrayList<IConcept> ret = new ArrayList<IConcept>();
		for (OWLClass c : _ontology.getClassesInSignature()) {
			ret.add(new Concept(c));
		}
 		return ret;
	}

	@Override
	public Collection<IProperty> getProperties() {
		ArrayList<IProperty> ret = new ArrayList<IProperty>();
		for (OWLProperty<?,?>  p : _ontology.getDataPropertiesInSignature()) {
			ret.add(new Property(p));
		}
		for (OWLProperty<?,?>  p : _ontology.getObjectPropertiesInSignature()) {
			ret.add(new Property(p));
		}
		for (OWLAnnotationProperty  p : _ontology.getAnnotationPropertiesInSignature()) {
			ret.add(new Property(p));
		}

 		return ret;
	}

	@Override
	public IConcept getConcept(String ID) {
		if (_conceptIDs.contains(ID)) {
			return new Concept(
				_ontology.getOWLOntologyManager().getOWLDataFactory().
					getOWLClass(":" + ID, _prefix));
		}
		return null;
	}


	@Override
	public IProperty getProperty(String ID) {
		return null;
	}

	@Override
	public String getConceptSpace() {
		return _id;
	}

	@Override
	public String getURI() {
		return _ontology.getOWLOntologyManager().getOntologyDocumentIRI(_ontology).toString();
	}

	@Override
	public boolean write(String uri) throws ThinklabException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void define(Collection<IAxiom> axioms) throws ThinklabException {
		for (IAxiom axiom : axioms) {
//		try {
//			OWLDataFactory factory = _ontology.getOWLOntologyManager().getOWLDataFactory();
//			if (axiom.is(IAxiom.CLASS_ASSERTION)) {
//			
//				URI uri = URI.create(getURI() + "#" + axiom.getArgument(0));
//				OWLClass newcl = factory.getOWLClass(uri);
//				_ontology.getOWLOntologyManager().addAxiom(_ontology, factory.getOWLDeclarationAxiom(newcl));
//			
//			} else if (axiom.is(IAxiom.SUBCLASS_OF)) {
//
//				IConcept p = findConcept(axiom.getArgument(1).toString());
//				IConcept c = findConcept(axiom.getArgument(0).toString());
//				OWLClass parent = (OWLClass) ((Concept)p).entity;
//				manager.addAxiom(ont, factory.getOWLSubClassAxiom((OWLClass)((Concept)c).entity, parent));
//			}
//			
//			/* TODO etc */
//			
//		} catch (OWLOntologyChangeException e) {
//			throw new ThinklabInternalErrorException(e);
//		}
		}
	}

	@Override
	public IMetadata getMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

}
