package org.integratedmodelling.thinklab.common.owl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabRuntimeException;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IKnowledge;
import org.integratedmodelling.thinklab.api.knowledge.IOntology;
import org.integratedmodelling.thinklab.api.knowledge.IProperty;
import org.integratedmodelling.thinklab.api.knowledge.query.IQuery;
import org.integratedmodelling.thinklab.api.metadata.IMetadata;
import org.integratedmodelling.thinklab.common.owl.OntologyUtilities.RestrictionVisitor;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLQuantifiedRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.NodeSet;

/**
 * Just a wrapper for an OWL concept. Metadata are redirected to annotation properties.
 * 
 * @author Ferd
 *
 */
public class Concept implements IConcept {

	String _id;
	String _cs;
	
	OWLClass _owl;
		
	Concept(OWLClass c) {
		_owl = c;
		_id = c.getIRI().getFragment();
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

		if (! (concept instanceof Concept))
			return false;
		
		Concept cc = (Concept)concept;
		
		if (cc.equals(this))
			return true;
		
		if (OWL.get().reasoner != null) {
			return OWL.get().reasoner.getSubClasses(_owl, false).containsEntity(cc._owl);
		}
	
		Collection<IConcept> collection = getAllParents();
		collection.add(this);

		return collection.contains(concept);
	}

	@Override
	public String getURI() {
		return _owl.getIRI().toString();
	}

	@Override
	public IOntology getOntology() {
		return OWL.get().getOntology(_cs);
	}

	@Override
	public Collection<IConcept> getParents() {

		Set<IConcept> concepts = new HashSet<IConcept>();
		synchronized (_owl) {
			Set<OWLClassExpression> set = _owl.getSuperClasses(OWL.get().manager.getOntologies());
		
			for (OWLClassExpression s : set) {
				if (!(s.isAnonymous() || s.isOWLNothing()))
					concepts.add(new Concept(s.asOWLClass()));
			}
		}

		// OWLAPI doesn't do this - only add owl:Thing if this is its direct subclass, i.e. has no 
		// parents in OWLAPI.
		if (concepts.isEmpty() && !OWL.get().getRootConcept().equals(this))
			concepts.add(OWL.get().getRootConcept());
		
		return concepts;
		
	}

	@Override
	public Collection<IConcept> getAllParents() {
		Set<IConcept> concepts = new HashSet<IConcept>();
		
		if (OWL.get().reasoner != null) {
			NodeSet<OWLClass> parents = OWL.get().reasoner.getSuperClasses(_owl, false);
			for (OWLClass cls : parents.getFlattened()) {
				concepts.add(new Concept(cls));
			}
				
			return concepts;

		} else {
			
			for (IConcept c : getParents()) {				
				concepts.add(c);
				concepts.addAll(c.getAllParents());
 				
			}
		}

		concepts.add(OWL.get().getRootConcept());

		return concepts;

	}

	@Override
	public Collection<IConcept> getChildren() {
		
		Set<IConcept> concepts = new HashSet<IConcept>();
		synchronized (_owl) {
			Set<OWLClassExpression> set = _owl.getSubClasses(OWL.get().manager.getOntologies());
			
			for (OWLClassExpression s : set) {
				if (!(s.isAnonymous() || s.isOWLNothing() || s.isOWLThing()))
					concepts.add(new Concept(s.asOWLClass()));
			}
			if (set.isEmpty() && ((OWLClass)_owl).isOWLThing()) {
				for (IOntology onto : OWL.get()._ontologies.values()) {
					concepts.addAll(onto.getConcepts());
				}
			}
		}
		return concepts;
	}

	@Override
	public Collection<IProperty> getProperties() {
		Collection<IProperty> props = getDirectProperties();
		ArrayList<Collection<IProperty>> psets = new ArrayList<Collection<IProperty>>();
		
		for (IProperty prop: props) 
			synchronized (prop) {
				psets.add(prop.getChildren());
			}
		
		for (Collection<IProperty> pp : psets) 
			props.addAll(pp);
		
		return props;
	}

	public Collection<IProperty> getDirectProperties() {
		
		OWLOntology ontology = ((Ontology)(getOntology()))._ontology;
		
		Set<IProperty> properties = new HashSet<IProperty>();
			synchronized (ontology) {
				for (OWLObjectProperty op : ontology.getObjectPropertiesInSignature(true)) {
					Set<OWLClassExpression> rang = op.getDomains(OWL.get().manager.getOntologies());
					if (rang.contains(_owl))
						properties.add(new Property(op));
				}
				for (OWLDataProperty op : ontology.getDataPropertiesInSignature(true)) {
					Set<OWLClassExpression> rang = op.getDomains(OWL.get().manager.getOntologies());
					if (rang.contains(_owl))
						properties.add(new Property(op));
				}
			}
		return properties;
	}
	
	@Override
	public Collection<IProperty> getAllProperties() {
		Set<IProperty> props = (Set<IProperty>) getProperties();
		for(IConcept c: getAllParents()){
			props.addAll(c.getProperties());
		}
		return props;
	}

	@Override
	public Collection<IConcept> getPropertyRange(IProperty property)
			throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAbstract() {
		// TODO this requires a project-wide choice of property. Maybe the thinklab ontology (and only 
		// that) should be part of THIS package.
		return false;
	}

	@Override
	public IConcept getParent() {
		Collection<IConcept> pp = getParents();
		if (pp.size() > 1) {
			throw new ThinklabRuntimeException(
					"Concept " + this + " has more than one parent: cannot call getParent() on it.");
		}
		return pp.iterator().next();
	}

	@Override
	public int getPropertiesCount(String property) {
		return getProperties().size();
	}

	@Override
	public int[] getCardinality(IProperty property) {
		
		if (property.isFunctional()) 
			return new int[]{1,1};
		
		int min = -1, max = -1;
		
		if (property.isObjectProperty()) {
			for (OWLQuantifiedRestriction<?, ?, ?> r : getRestrictions().getObjectRestrictions()) {
				if (r instanceof OWLObjectExactCardinality) {
					min = max = ((OWLObjectExactCardinality)r).getCardinality();
				} else if (r instanceof OWLObjectMaxCardinality) {
					max = ((OWLObjectMaxCardinality)r).getCardinality();
				} else if (r instanceof OWLObjectMinCardinality) {
					min = ((OWLObjectMinCardinality)r).getCardinality();
				}
			}
		} else {
			for (OWLQuantifiedRestriction<?, ?, ?> r : getRestrictions().getDataRestrictions()) {
				if (r instanceof OWLDataExactCardinality) {
					min = max = ((OWLDataExactCardinality)r).getCardinality();
				} else if (r instanceof OWLDataMaxCardinality) {
					max = ((OWLDataMaxCardinality)r).getCardinality();
				} else if (r instanceof OWLDataMinCardinality) {
					min = ((OWLDataMinCardinality)r).getCardinality();
				}
			}
		}
		
		return new int[]{min, max};
	}


	@Override
	public IConcept getLeastGeneralCommonConcept(IConcept otherConcept) {
		IConcept ret = null;
		if (otherConcept == null)
			return this;
		if (is(otherConcept))
			ret = otherConcept;
		else if (otherConcept.is(this))
			ret = this;
		else {
			for (IConcept pp : getParents()) {
				IConcept c1 = pp.getLeastGeneralCommonConcept(otherConcept);
				if (c1 != null) {
					ret = c1;
					break;
				}
			}
		}
		return ret;
	}

	@Override
	public IQuery getDefinition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<IConcept> getSemanticClosure() {

		if (OWL.get().reasoner != null) {
			
			HashSet<IConcept> ret = new HashSet<IConcept>();
			NodeSet<OWLClass> cset = OWL.get().reasoner.getSubClasses(_owl, false);
			for (OWLClass cl : cset.getFlattened()) {
				ret.add(new Concept(cl));
			}
			return ret;
		}
		
		Set<IConcept> ret = collectChildren(new HashSet<IConcept>());
		ret.add(this);
		
		return ret;
	}
	

	private Set<IConcept> collectChildren(Set<IConcept> hashSet) {

		for (IConcept c : getChildren()) {
			if (!hashSet.contains(c))
				((Concept)c).collectChildren(hashSet);
			hashSet.add(c);
		}			
		return hashSet;
	}

	@Override
	public IMetadata getMetadata() {
		return new OWLMetadata(_owl);
	}
	
	public RestrictionVisitor getRestrictions() {
		RestrictionVisitor visitor = new RestrictionVisitor(OWL.get().manager.getOntologies());
		for (OWLSubClassOfAxiom ax : ((Ontology)(getOntology()))._ontology.getSubClassAxiomsForSubClass(_owl)) {
			ax.getSuperClass().accept((OWLClassExpressionVisitor)visitor);
		}
		return visitor;
	}
	
	public Map<IProperty, String> getAnnotations() {
		HashMap<IProperty, String> ret = new HashMap<IProperty, String>();
		
		for (OWLAnnotation annotation : _owl.getAnnotations(((Ontology)getOntology())._ontology)) {
			OWLLiteral l = (OWLLiteral) annotation.getValue();
			ret.put(new Property(annotation.getProperty()), l.getLiteral());
		}
		return ret;
	}

}
