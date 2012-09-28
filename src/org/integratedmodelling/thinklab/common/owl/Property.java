package org.integratedmodelling.thinklab.common.owl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.integratedmodelling.exceptions.ThinklabRuntimeException;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IKnowledge;
import org.integratedmodelling.thinklab.api.knowledge.IOntology;
import org.integratedmodelling.thinklab.api.knowledge.IProperty;
import org.integratedmodelling.thinklab.api.metadata.IMetadata;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;

public class Property implements IProperty {

	String _id;
	String _cs;
	OWLEntity _owl;
	OWL _manager;
		
	public Property(OWLEntity p, OWL manager, String cs) {
		_owl = p;
		_id = _owl.getIRI().getFragment();
		_manager = manager;
		_cs = cs;
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
	public boolean is(IKnowledge p) {
		/*
		 * TODO use reasoner as appropriate
		 */
		return p instanceof Property && (p.equals(this) || getAllParents().contains(p));
	}

	@Override
	public String getURI() {
		return _owl.getIRI().toString();
	}


	@Override
	public IOntology getOntology() {
		return _manager.getOntology(getConceptSpace());
	}

	@Override
	public boolean isClassification() {
		// FIXME hmmm. Do we still need this?
		return false;
	}

	@Override
	public boolean isLiteralProperty() {
		return _owl.isOWLDataProperty() || _owl.isOWLAnnotationProperty();
	}

	@Override
	public boolean isObjectProperty() {
		return _owl.isOWLObjectProperty();
	}

	@Override
	public boolean isAnnotation() {
		return _owl.isOWLAnnotationProperty();
	}

	@Override
	public IProperty getInverseProperty() {
		Property ret = null;
		
		synchronized (_owl) {
			if (_owl.isOWLObjectProperty()) {
			
				Set<OWLObjectPropertyExpression> dio = 
					_owl.asOWLObjectProperty().getInverses(ontology());
			
				if (dio.size() > 1) 
					throw new ThinklabRuntimeException(
							"taking the inverse of property " + 
							this	 + 
							", which has multiple inverses");
			
				if (dio.size() > 0) {
					OWLObjectProperty op = dio.iterator().next().asOWLObjectProperty();
					ret = new Property(op, _manager, _manager.getConceptSpace(op.getIRI()));
				}
			}
		}
		return ret;
	}

	@Override
	public Collection<IConcept> getRange() {
		Set<IConcept> ret = new HashSet<IConcept>();
		synchronized (_owl) {
			if (_owl.isOWLDataProperty()) {

				for (OWLDataRange c : _owl.asOWLDataProperty().getRanges(
						_manager.manager.getOntologies())) {

					if (c.isDatatype()) {
//						OWL2DataType dtype = (OWL2Datatype) c;
//						// FIXME! complete this
//						IConcept tltype = Thinklab.get().getXSDMapping(dtype.getURI().toString());
//						if (tltype != null) {
//							ret.add(tltype);
//						}
					}
				}
			} else if (_owl.isOWLObjectProperty()) {
				for (OWLClassExpression c : _owl.asOWLObjectProperty().getRanges(
						_manager.manager.getOntologies())) {
					if (!c.isAnonymous())
						ret.add(new Concept(c.asOWLClass(), _manager, _manager.getConceptSpace(c.asOWLClass().getIRI())));
				}
			}
		}
		return ret;
	}

	@Override
	public Collection<IConcept> getDomain() {
		
		Set<IConcept> ret = new HashSet<IConcept>();
		synchronized (this._owl) {
			if (_owl.isOWLDataProperty()) {
				for (OWLClassExpression c : _owl.asOWLDataProperty().getDomains(
						_manager.manager.getOntologies())) {
					ret.add(new Concept(c.asOWLClass(), _manager, _manager.getConceptSpace(c.asOWLClass().getIRI())));
				}
			} else if (_owl.isOWLObjectProperty()) {
				for (OWLClassExpression c : _owl.asOWLObjectProperty().getDomains(
						_manager.manager.getOntologies())) {
					ret.add(new Concept(c.asOWLClass(), _manager, _manager.getConceptSpace(c.asOWLClass().getIRI())));
				}
			}
		}
		return ret;
	}

	@Override
	public boolean isAbstract() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IProperty getParent()  {
		
		Collection<IProperty> pars = getParents();
		
		if (pars.size() > 1)
			throw new ThinklabRuntimeException("asking for single parent of multiple-inherited property " + this);

		return pars.size() == 0 ? null : pars.iterator().next();
	}

	@Override
	public Collection<IProperty> getParents() {
		
		Set<IProperty> ret = new HashSet<IProperty>();
		Set<OWLOntology> onts = 
			_manager.manager.getOntologies();

		/*
		 * TODO use reasoner as appropriate
		 */
		synchronized (_owl) {
			if (_owl.isOWLDataProperty()) {
				for (OWLOntology o : onts)  {
					for (OWLDataPropertyExpression p : 
						_owl.asOWLDataProperty().getSuperProperties(o)) {
						ret.add(new Property(p.asOWLDataProperty(), _manager, _manager.getConceptSpace(p.asOWLDataProperty().getIRI())));
					}
				}
			} else if (_owl.isOWLObjectProperty()) {
				for (OWLOntology o : onts)  {
					for (OWLObjectPropertyExpression p : 
						_owl.asOWLObjectProperty().getSuperProperties(o)) {
						ret.add(new Property(p.asOWLObjectProperty(), _manager, _manager.getConceptSpace(p.asOWLObjectProperty().getIRI())));
					}
				}
			}
		}

		return ret;
	}

	@Override
	public Collection<IProperty> getAllParents() {
		Set<IProperty> ret = new HashSet<IProperty>();

		synchronized (_owl) {
			if (_manager.reasoner != null) {

//				try {
//					if (_owl.isOWLObjectProperty()) {
//						Set<Set<OWLObjectProperty>> parents = 
//							KR().getPropertyReasoner()
//								.getAncestorProperties(entity
//										.asOWLObjectProperty());
//						Set<OWLObjectProperty> subClses = OWLReasonerAdapter
//								.flattenSetOfSets(parents);
//						for (OWLObjectProperty cls : subClses) {
//							ret.add(new Property(cls));
//						}
//					} else if (entity.isOWLDataProperty()) {
//						Set<Set<OWLDataProperty>> parents = 
//							KR().getPropertyReasoner()
//								.getAncestorProperties(entity
//										.asOWLDataProperty());
//						Set<OWLDataProperty> subClses = OWLReasonerAdapter
//								.flattenSetOfSets(parents);
//						for (OWLDataProperty cls : subClses) {
//							ret.add(new Property(cls));
//						}
//					}
//					return ret;
//
//				} catch (OWLException e) {
//					// just continue to dumb method
//				}

			} else {

				for (IProperty c : getParents()) {
					ret.add(c);
					ret.addAll(c.getAllParents());
				}
			}
		}
		return ret;
	}

	@Override
	public Collection<IProperty> getChildren() {
		
		Set<IProperty> ret = new HashSet<IProperty>();
		
		Set<OWLOntology> onts = 
			_manager.manager.getOntologies();
		
		if (_owl.isOWLDataProperty()) {
			for (OWLOntology o : onts)  {
				synchronized (this._owl) {
					for (OWLDataPropertyExpression p : 
						_owl.asOWLDataProperty().getSubProperties(o)) {
						ret.add(new Property(p.asOWLDataProperty(), _manager, _manager.getConceptSpace(p.asOWLDataProperty().getIRI())));
					}
				}
			}
		} else if (_owl.isOWLObjectProperty()) {
			for (OWLOntology o : onts)  {
				synchronized (this._owl) {
					for (OWLObjectPropertyExpression p : 
							_owl.asOWLObjectProperty().getSubProperties(o)) {
						ret.add(new Property(p.asOWLObjectProperty(), _manager, _manager.getConceptSpace(p.asOWLObjectProperty().getIRI())));
					}
				}
			}
		}
		
		return ret;
	}

	@Override
	public Collection<IProperty> getAllChildren() {
		
		Set<IProperty> ret = new HashSet<IProperty>();
		for (IProperty c : getChildren()) {
			
			ret.add(c);
			for (IProperty p : c.getChildren()) {
				ret.addAll(p.getAllChildren());
			}
		}
		
		return ret;
	}

	@Override
	public boolean isFunctional() {
		return  _owl.isOWLDataProperty() ?
				_owl.asOWLDataProperty().isFunctional(ontology()) :
				_owl.asOWLObjectProperty().isFunctional(ontology());
	}

	@Override
	public IMetadata getMetadata() {
		return new OWLMetadata(_owl);
	}

	
	private OWLOntology ontology() {
		return ((Ontology)getOntology())._ontology;
	}

	@Override
	public boolean equals(Object obj) {
		return  obj instanceof Property ? toString().equals(obj.toString()) : false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		return getConceptSpace() + ":" + _id;
	}
}
