package org.integratedmodelling.thinklab.common.owl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabInternalErrorException;
import org.integratedmodelling.thinklab.api.knowledge.IAxiom;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IOntology;
import org.integratedmodelling.thinklab.api.knowledge.IProperty;
import org.integratedmodelling.thinklab.api.lang.IList;
import org.integratedmodelling.thinklab.api.metadata.IMetadata;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLProperty;

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
	String _prefix;
	OWL _manager;
	HashSet<String> _conceptIDs  = new HashSet<String>();
	HashSet<String> _propertyIDs = new HashSet<String>();
	
	/*
	 * what a pain
	 */
	HashSet<String> _opropertyIDs = new HashSet<String>();
	HashSet<String> _dpropertyIDs = new HashSet<String>();
	HashSet<String> _apropertyIDs = new HashSet<String>();
	
	Ontology(OWLOntology ontology, String id, OWL manager) {
		_id = id;
		_ontology = ontology;
		_manager = manager;
		_prefix = ontology.getOntologyID().getDefaultDocumentIRI().toString();
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
			_dpropertyIDs.add(p.getIRI().getFragment());			
			_propertyIDs.add(p.getIRI().getFragment());
		}
		for (OWLProperty<?,?>  p : _ontology.getObjectPropertiesInSignature()) {
			_opropertyIDs.add(p.getIRI().getFragment());
			_propertyIDs.add(p.getIRI().getFragment());
		}
		for (OWLAnnotationProperty  p : _ontology.getAnnotationPropertiesInSignature()) {
			_apropertyIDs.add(p.getIRI().getFragment());
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
			ret.add(new Concept(c, _manager));
		}
 		return ret;
	}

	@Override
	public Collection<IProperty> getProperties() {
		ArrayList<IProperty> ret = new ArrayList<IProperty>();
		for (OWLProperty<?,?>  p : _ontology.getDataPropertiesInSignature()) {
			ret.add(new Property(p, _manager));
		}
		for (OWLProperty<?,?>  p : _ontology.getObjectPropertiesInSignature()) {
			ret.add(new Property(p, _manager));
		}
		for (OWLAnnotationProperty  p : _ontology.getAnnotationPropertiesInSignature()) {
			ret.add(new Property(p, _manager));
		}

 		return ret;
	}

	/**
	 * Special purpose function: return all concepts that have no parents that belong to
	 * this ontology - making them "top-level" in it. Optionally, just return those whose
	 * ONLY parent is owl:Thing.
	 * 
	 * @param noParent if true, only return those concept that derive directly and 
	 *        exclusively from owl:Thing.
	 * @return
	 */
	public List<IConcept> getTopConcepts(boolean noParent) {
		ArrayList<IConcept> ret = new ArrayList<IConcept>();
		for (IConcept c : getConcepts()) {
			Collection<IConcept> parents = c.getParents();
			boolean ok = true;
			if (noParent) {
				ok = parents.size() <= 1;
			} else {
				for (IConcept cc : parents) {
					if (cc.getConceptSpace().equals(_id)) {
						ok = false;
						break;
					}
				}
			}
			if (ok)
				ret.add(c);
		}
		return ret;
	}
	
	@Override
	public IConcept getConcept(String ID) {
		if (_conceptIDs.contains(ID)) {
			return new Concept(
				_ontology.getOWLOntologyManager().getOWLDataFactory().
					getOWLClass(IRI.create(_prefix + "#" + ID)), _manager);
		}
		return null;
	}
	
	@Override
	public IProperty getProperty(String ID) {
		if (_opropertyIDs.contains(ID)) {
			return new Property(_ontology.getOWLOntologyManager().getOWLDataFactory().
				getOWLObjectProperty(IRI.create(_prefix + "#" + ID)), _manager);
		}
		if (_dpropertyIDs.contains(ID)) {
			return new Property(_ontology.getOWLOntologyManager().getOWLDataFactory().
				getOWLDataProperty(IRI.create(_prefix + "#" + ID)), _manager);
		}
		if (_apropertyIDs.contains(ID)) {
			return new Property(_ontology.getOWLOntologyManager().getOWLDataFactory().
				getOWLAnnotationProperty(IRI.create(_prefix + "#" + ID)), _manager);
		}
		return null;

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
		
		/*
		 * ACHTUNG remember to add IDs to appropriate catalogs as classes and property assertions
		 * are encountered. This can be called incrementally, so better not to call scan() every time.
		 */
		
		for (IAxiom axiom : axioms) {
			try {
				OWLDataFactory factory = _ontology.getOWLOntologyManager().getOWLDataFactory();
				if (axiom.is(IAxiom.CLASS_ASSERTION)) {
					OWLClass newcl = factory.getOWLClass(IRI.create(_prefix + "#" + axiom.getArgument(0)));
					_ontology.getOWLOntologyManager().addAxiom(_ontology, factory.getOWLDeclarationAxiom(newcl));
					_conceptIDs.add(axiom.getArgument(0).toString());
				} else if (axiom.is(IAxiom.SUBCLASS_OF)) {
					OWLClass p = factory.getOWLClass(IRI.create(_prefix + "#" + axiom.getArgument(1)));
					OWLClass c = factory.getOWLClass(IRI.create(_prefix + "#" + axiom.getArgument(0)));
					_manager.manager.addAxiom(_ontology, factory.getOWLSubClassOfAxiom(p, c));
				}
			
				/* TODO etc */
			
			} catch (OWLOntologyChangeException e) {
				throw new ThinklabInternalErrorException(e);
			}
		}
	}

	@Override
	public IMetadata getMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getConceptSpace() {
		return _id;
	}

	@Override
	public int getConceptCount() {
		return _conceptIDs.size();
	}

	@Override
	public int getPropertyCount() {
		return _propertyIDs.size();
	}

}
