package org.integratedmodelling.thinklab.common.owl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabRuntimeException;
import org.integratedmodelling.exceptions.ThinklabUnsupportedOperationException;
import org.integratedmodelling.lang.Axiom;
import org.integratedmodelling.lang.SemanticType;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IOntology;
import org.integratedmodelling.thinklab.api.knowledge.IProperty;
import org.integratedmodelling.thinklab.api.lang.IModelParser;
import org.integratedmodelling.thinklab.api.lang.IModelSerializer;
import org.integratedmodelling.thinklab.api.lang.IResolver;
import org.integratedmodelling.thinklab.api.modelling.INamespace;
import org.integratedmodelling.thinklab.api.modelling.parsing.INamespaceDefinition;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyCreationIOException;
import org.semanticweb.owlapi.io.OWLParser;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;


/**
 * Import concepts and properties from OWL ontologies.
 * 
 * TODO make this the client's FileKnowledgeRepository and have it store a registry of what
 * it parses. Ensure it's used as a singleton by the ModelManager when reading projects. Then
 * just use this in thinklab and send the rest to bed.
 * 
 * @author Ferd
 *
 */
public class OWL implements IModelParser, IModelSerializer {
	
	HashMap<String, INamespace> _namespaces = new HashMap<String, INamespace>();
	HashMap<String, INamespace> _resourceIndex = new HashMap<String, INamespace>();
	HashMap<String, INamespace> _iriIndex = new HashMap<String, INamespace>();
	HashMap<String, IOntology>  _ontologies = new HashMap<String, IOntology>();
	HashSet<IRI> _seen = new HashSet<IRI>();
	
	/*
	 * Singleton in this implementation, doesn't have to be - multiple managers would
	 * equal multiple knowledge spaces, we should explore that in a later version.
	 */
	static private OWL _this = null;
	
	public static OWL get() {
		if (_this == null) {
			_this = new OWL();
		}
		return _this;
	}
	
	IOntology requireOntology(String id, String prefix) {

		if (_ontologies.get(id) != null) {
			return _ontologies.get(id);
		}
		
		IOntology ret = null;
		try {
			OWLOntology o = manager.createOntology(IRI.create(prefix + "/" + id));
			ret = new Ontology(o, id);
			_ontologies.put(id, ret);
		} catch (OWLOntologyCreationException e) {
			throw new ThinklabRuntimeException(e);
		}
		
		return ret;
	}
	
	/*
	 * package-visible, never null.
	 */
	OWLOntologyManager manager = null;
	
	/*
	 * package-visible, may be null.
	 */
	OWLReasoner reasoner = null;
	
	public static String getFileName(String s) {

		String ret = s;

		int sl = ret.lastIndexOf(File.separator);
		if (sl < 0)
			sl = ret.lastIndexOf('/');
		if (sl > 0)
			ret = ret.substring(sl+1);
		
		return ret;
	}
	
	/**
	 * This one will create a manager, so all knowledge loaded is local to this
	 * parser. Using this object as a singleton will (for now) enforce this
	 * behavior.
	 */
	OWL() {
		manager = OWLManager.createOWLOntologyManager();
	}
	
	/**
	 * Use this to read knowledge into an existing manager
	 * @param manager
	 */
	public OWL(OWLOntologyManager manager) {
		this.manager = manager;
	}
	
	@Override
	public INamespace parseInNamespace(InputStream input, String namespace,
			IResolver resolver) throws ThinklabException {
		throw new ThinklabUnsupportedOperationException(
				"cannot parse OWL ontology fragments in namespace " + namespace + 
				": only direct ontology import is supported");
	}
	
	String importOntology(OWLOntology ontology, IResolver resolver, String resource, boolean imported) throws ThinklabException {

		String namespace = 
				getFileName(ontology.getOntologyID().getOntologyIRI().toURI().toString());
		
		/*
		 * clean up namespace ID if necessary
		 */
		while (namespace.startsWith(".")) {
			namespace = namespace.substring(1);
		}
		if (namespace.endsWith(".owl")) {
			namespace = namespace.substring(0, namespace.lastIndexOf('.'));
		}
		
		if (!_ontologies.containsKey(namespace)) {
			_ontologies.put(namespace, new Ontology(ontology, namespace));
		}
		
		/*
		 * seen already?
		 */
		if (_namespaces.containsKey(namespace))	{
			return namespace;
		}
		
		INamespaceDefinition ns = (INamespaceDefinition) resolver.newLanguageObject(INamespace.class);
		ns.setId(namespace);
		ns.setResourceUrl(resource);

		/*
		 * TODO sync model objects with ontology
		 */
		
		resolver.onNamespaceDeclared();
		
		/*
		 * import all axioms into namespace
		 */
		for (OWLAxiom axiom : ontology.getAxioms()) {
			addAxiom(axiom, ns);
		}
		
		resolver.onNamespaceDefined();
		
		_namespaces.put(namespace, ns);
		
		return namespace;
	}

	private void addAxiom(OWLAxiom axiom, INamespaceDefinition ns) {
		
		if (axiom instanceof OWLDeclarationAxiom) {

			OWLEntity entity = ((OWLDeclarationAxiom)axiom).getEntity();
			String id = entity.getIRI().getFragment();
			
			if (entity instanceof OWLObjectProperty) {
				ns.addAxiom(Axiom.ObjectPropertyAssertion(id));
			} else if (entity instanceof OWLDataProperty) {
				ns.addAxiom(Axiom.DataPropertyAssertion(id));				
			} else if (entity instanceof OWLAnnotationProperty) {
				ns.addAxiom(Axiom.AnnotationPropertyAssertion(id));
			} else if (entity instanceof OWLClass) {
				ns.addAxiom(Axiom.ClassAssertion(id));				
			}
			
		} else if (axiom instanceof OWLDisjointClassesAxiom) {
			
			ArrayList<String> cls = new ArrayList<String>();
			for (OWLClassExpression e : ((OWLDisjointClassesAxiom)axiom).getClassExpressionsAsList())  {
				
				/*
				 * TBC
				 * assuming that whatever subclass we have has been declared 
				 * in this ontology.
				 */
				cls.add(e.asOWLClass().getIRI().getFragment());
			}
			ns.addAxiom(Axiom.DisjointClasses(cls.toArray(new String[cls.size()])));
			
		} else if (axiom instanceof OWLSubClassOfAxiom) {
			
			OWLClassExpression sup = ((OWLSubClassOfAxiom)axiom).getSuperClass();
			OWLClassExpression sub = ((OWLSubClassOfAxiom)axiom).getSubClass();
			
			/*
			 * axioms will change according to subclass type
			 */
			if (sup instanceof OWLRestriction<?, ?, ?>) {
				/*
				 * import restrictions we support
				 */
			} else {
				
				/*
				 * TBC
				 * assuming that whatever subclass we have has been declared 
				 * in this ontology.
				 */
				String subId = sub.asOWLClass().getIRI().getFragment();
				
				/*
				 * super may be in another ontology, so ensure we have the same namespace or get the proper one.
				 */
			}
			
		} else if (axiom instanceof OWLAnnotationAssertionAxiom) {
			
		}
		
		/*
		 * TODO - for now: ignore inverse properties, equivalent classes
		 */
	}

	@Override
	public INamespace parse(String namespace, String resource, IResolver resolver) throws ThinklabException {
		
		String ns = "__not__found";
		InputStream input = null;
		
		Throwable exception = null;
		
		try {
			
			if (_resourceIndex.containsKey(resource)) {
				return _resourceIndex.get(resource);
			}
			
			input = resolver.openStream();
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(input);
			input.close();
			
			/*
			 * import ontology and all its imports. Return namespace ID.
			 */
			ns = importOntology(ontology, resolver, resource, false);
			
			
			/*
			 * do not load this again, it's in the manager.
			 */
			_resourceIndex.put(resource, _namespaces.get(ns));
			
		} catch (OWLOntologyCreationIOException e) {
			// IOExceptions during loading get wrapped in an
			// OWLOntologyCreationIOException
			exception = e;
			
			IOException ioException = e.getCause();
			if (ioException instanceof FileNotFoundException) {
				System.out.println("Could not load ontology. File not found: "
						+ ioException.getMessage());
			} else if (ioException instanceof UnknownHostException) {
				System.out.println("Could not load ontology. Unknown host: "
						+ ioException.getMessage());
			} else {
				System.out.println("Could not load ontology: "
						+ ioException.getClass().getSimpleName() + " "
						+ ioException.getMessage());
			}
		} catch (UnparsableOntologyException e) {
			
			exception = e;
			
			// If there was a problem loading an ontology because there are
			// syntax errors in the document (file) that
			// represents the ontology then an UnparsableOntologyException is
			// thrown
			System.out.println("Could not parse the ontology: "
					+ e.getMessage());
			// A map of errors can be obtained from the exception
			Map<OWLParser, OWLParserException> exceptions = e.getExceptions();
			// The map describes which parsers were tried and what the errors
			// were
			for (OWLParser parser : exceptions.keySet()) {
				System.out.println("Tried to parse the ontology with the "
						+ parser.getClass().getSimpleName() + " parser");
				System.out.println("Failed because: "
						+ exceptions.get(parser).getMessage());
			}
		} catch (UnloadableImportException e) {
			
			exception = e;
			
			// If our ontology contains imports and one or more of the imports
			// could not be loaded then an
			// UnloadableImportException will be thrown (depending on the
			// missing imports handling policy)
			System.out.println("Could not load import: "
					+ e.getImportsDeclaration());
			// The reason for this is specified and an
			// OWLOntologyCreationException
			OWLOntologyCreationException cause = e
					.getOntologyCreationException();
			System.out.println("Reason: " + cause.getMessage());
		} catch (OWLOntologyAlreadyExistsException e) {

			/*
			 * OK, then wrap it and screw that.
			 */
			OWLOntology ontology = manager.getOntology(e.getOntologyID().getOntologyIRI());
			if (ontology != null) {
				ns = importOntology(ontology, resolver, resource, false);
				_resourceIndex.put(resource, _namespaces.get(ns));
			}

		} catch (OWLOntologyCreationException e) {
			exception = e;
			System.out.println("Could not load ontology: " + e.getMessage());
		} catch (IOException e) {
			exception = e;
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (IOException e) {
					exception = e;
				}
		}

		if (exception != null) {
			resolver.onException(exception, 0);
		}

		INamespace nns = _namespaces.get(ns);
		((INamespaceDefinition)nns).setResourceUrl(resource);
		((INamespaceDefinition)nns).setId(namespace);

		/*
		 * TODO create concept objects in NS?
		 */
		
		return _namespaces.get(ns);
	}

	@Override
	public void writeNamespace(INamespace namespace, File outputFile)
			throws ThinklabException {
		/*
		 * TODO save all axioms in namespace as OWL axioms
		 */
		
		/*
		 * TODO turn the model structure into axioms?
		 */
		
	}

	
	/*
	 * the three knowledge manager methods we implement, so we can serve as delegate to
	 * a KM for these.
	 */
	
	
	public IConcept getConcept(String concept) {

		if (SemanticType.validate(concept)) {
			SemanticType st = new SemanticType(concept);
			IOntology o = _ontologies.get(st.getConceptSpace());
			return o.getConcept(st.getLocalName());
		}
		return null;
	}

	public IProperty getProperty(String concept) {

		if (SemanticType.validate(concept)) {
			SemanticType st = new SemanticType(concept);
			IOntology o = _ontologies.get(st.getConceptSpace());
			return o.getProperty(st.getLocalName());
		}
		return null;
	}

	public IConcept getLeastGeneralCommonConcept(Collection<IConcept> cc) {
		
		IConcept ret = null;
		Iterator<IConcept> ii = cc.iterator();

		if (ii.hasNext()) {		
			
		  ret = ii.next();
		  
		  if (ret != null)
			while (ii.hasNext()) {
		      ret = ret.getLeastGeneralCommonConcept(ii.next());
		      if (ret == null)
		    	  break;
		    }
		}
		
		return ret;
	}

	/**
	 * Return the ontology for the given namespace ID (short name).
	 * @param _cs
	 * @return
	 */
	public IOntology getOntology(String ns) {
		return _ontologies.get(ns);
	}

	public IConcept getRootConcept() {
		return new Concept(manager.getOWLDataFactory().getOWLThing());
	}

}
