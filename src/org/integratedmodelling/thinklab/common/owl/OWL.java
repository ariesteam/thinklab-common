package org.integratedmodelling.thinklab.common.owl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabIOException;
import org.integratedmodelling.exceptions.ThinklabRuntimeException;
import org.integratedmodelling.exceptions.ThinklabUnsupportedOperationException;
import org.integratedmodelling.lang.SemanticType;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IOntology;
import org.integratedmodelling.thinklab.api.knowledge.IProperty;
import org.integratedmodelling.thinklab.api.lang.IModelParser;
import org.integratedmodelling.thinklab.api.lang.IModelSerializer;
import org.integratedmodelling.thinklab.api.lang.IResolver;
import org.integratedmodelling.thinklab.api.modelling.INamespace;
import org.integratedmodelling.thinklab.api.modelling.parsing.INamespaceDefinition;
import org.integratedmodelling.thinklab.common.utils.CamelCase;
import org.integratedmodelling.thinklab.common.utils.CopyURL;
import org.integratedmodelling.thinklab.common.utils.Debug;
import org.integratedmodelling.thinklab.common.utils.MiscUtilities;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyCreationIOException;
import org.semanticweb.owlapi.io.OWLParser;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.AutoIRIMapper;


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
	
	private HashMap<String, INamespace> _namespaces = new HashMap<String, INamespace>();
	private HashMap<String, INamespace> _resourceIndex = new HashMap<String, INamespace>();
	HashMap<String, IOntology>  _ontologies = new HashMap<String, IOntology>();
	
	HashMap<String, String> _iri2ns = new HashMap<String, String>();
	HashMap<SemanticType, OWLClass> _systemConcepts = new HashMap<SemanticType, OWLClass>();
	HashMap<String, IConcept> _xsdMappings = new HashMap<String, IConcept>();
	
	public IOntology requireOntology(String id, String prefix) {

		if (_ontologies.get(id) != null) {
			return _ontologies.get(id);
		}
		
		IOntology ret = null;
		try {
			OWLOntology o = manager.createOntology(IRI.create(prefix + "/" + id));
			ret = new Ontology(o, id, this);
			_ontologies.put(id, ret);
			_iri2ns.put(o.getOntologyID().getDefaultDocumentIRI().toString(), id);
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
	public OWL() {
		manager = OWLManager.createOWLOntologyManager();
		initialize();
	}
	
	/**
	 * Use this to read knowledge into an existing manager
	 * @param manager
	 */
	public OWL(OWLOntologyManager manager) {
		this.manager = manager;
		initialize();
	}
	
	private void initialize() {
		
		/*
		 * TODO insert basic datatypes as well
		 */
		_systemConcepts.put(new SemanticType("owl:Thing"), manager.getOWLDataFactory().getOWLThing());
		_systemConcepts.put(new SemanticType("owl:Nothing"), manager.getOWLDataFactory().getOWLNothing());
	}

	@Override
	public INamespace parseInNamespace(InputStream input, String namespace,
			IResolver resolver) throws ThinklabException {
		throw new ThinklabUnsupportedOperationException(
				"cannot parse OWL ontology fragments in namespace " + namespace + 
				": only direct ontology import is supported");
	}
	
	String importOntology(OWLOntology ontology, IResolver resolver, String resource, String namespace, boolean imported) throws ThinklabException {
		
		
		if (!_ontologies.containsKey(namespace)) {
			_ontologies.put(namespace, new Ontology(ontology, namespace, this));
		}
		
		/*
		 * seen already?
		 */
		if (_namespaces.containsKey(namespace))	{
			return namespace;
		}
		
		INamespaceDefinition ns = (INamespaceDefinition) resolver.newLanguageObject(INamespace.class);
		resolver.onNamespaceDeclared();
		ns.setOntology(_ontologies.get(namespace));
		resolver.onNamespaceDefined();
		
		_namespaces.put(namespace, ns);
		
		return namespace;
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
			ns = importOntology(ontology, resolver, resource, namespace, false);
			
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
				ns = importOntology(ontology, resolver, resource, namespace, false);
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

		/*
		 * get the finished namespace (with the ontology in it) and set the remaining
		 * fields.
		 */
		INamespace nns = _namespaces.get(ns);
		((INamespaceDefinition)nns).setResourceUrl(resource);
		((INamespaceDefinition)nns).setId(namespace);
		
		OWLOntologyID oid = ((Ontology)(nns.getOntology()))._ontology.getOntologyID();
		_iri2ns.put(oid.getDefaultDocumentIRI().toString(), namespace);

		
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
			
			if (o == null && _systemConcepts.containsKey(st)) {
				return new Concept(_systemConcepts.get(st), this, st.getConceptSpace());
			}
			
			return o == null ? null : o.getConcept(st.getLocalName());
		}
		return null;
	}

	public IProperty getProperty(String concept) {

		if (SemanticType.validate(concept)) {
			SemanticType st = new SemanticType(concept);
			IOntology o = _ontologies.get(st.getConceptSpace());
			if (o == null)
				return null;
			
			if (concept.toString().equals("aries.locations.test:hasGroundElevation")) {
				System.out.println("tartarpm");
			}
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
		return new Concept(manager.getOWLDataFactory().getOWLThing(), this, "owl");
	}

	public String getConceptSpace(IRI iri) {

		String r = MiscUtilities.removeFragment(iri.toURI()).toString();
		String ret = _iri2ns.get(r);
		
		if (ret == null) {
			/*
			 * happens, whenever we depend on a concept from a server ontology
			 * not loaded yet. Must find a way to deal with this.
			 */
			System.out.println("NULL: " + iri);
			ret = MiscUtilities.getNameFromURL(r);
		}
		
		return ret;
	}
	
	public void extractCoreOntologies(File dir) throws ThinklabIOException {
		
		dir.mkdirs();
		
		Reflections reflections = new Reflections(new ConfigurationBuilder()
      	    .setUrls(ClasspathHelper.forPackage("knowledge"))
      	    .setScanners(new ResourcesScanner()));
		
		for (String of : reflections.getResources(Pattern.compile(".*\\.owl"))) {

			/*
			 * remove knowledge/ prefix for output
			 */
			String oof = of;
			if (oof.startsWith("knowledge/"))
				oof = oof.substring("knowledge/".length());
			
			File output = new File(dir + File.separator + oof);
			new File(MiscUtilities.getFilePath(output.toString())).mkdirs();
			CopyURL.copy(this.getClass().getClassLoader().getResource(of), output);
		}
	}

	/**
	 * Load OWL files from given directory and in its
	 * subdirectories, using a prefix mapper to resolve URLs internally and
	 * deriving ontology names from the relative paths.
	 *
	 * This does not use a resolver and does not create namespaces for now. It's only meant
	 * for core knowledge not seen by users.
	 * 
	 * @param kdir
	 * @throws ThinklabIOException 
	 */
	public void load(File kdir) throws ThinklabException {

		AutoIRIMapper imap = new AutoIRIMapper(kdir, true);
		manager.addIRIMapper(imap);

		for (File fl : kdir.listFiles()) {
			loadInternal(fl, "");
		}
	}
	
	private void loadInternal(File f, String path) throws ThinklabException {

		String pth = 
				path == null ? 
					"" : 
					(path + (path.isEmpty() ? "" : ".") + CamelCase.toLowerCase(MiscUtilities.getFileBaseName(f.toString()), '-'));
						
		if (f. isDirectory()) {

			for (File fl : f.listFiles()) {
				loadInternal(fl, pth);
			}
			
		} else if (MiscUtilities.getFileExtension(f.toString()).equals("owl")) {

			InputStream input;
			
			try {
				input = new FileInputStream(f);
				OWLOntology ontology = manager.loadOntologyFromOntologyDocument(input);
				input.close();
				Ontology ont = new Ontology(ontology, pth, this);
				ont.setResourceUrl(f.toURI().toURL().toString());
				_ontologies.put(pth, ont);
				_iri2ns.put(ontology.getOntologyID().getDefaultDocumentIRI().toString(), pth);

			} catch (OWLOntologyAlreadyExistsException e) {

				/*
				 * already imported- wrap it and use it as is.
				 */
				OWLOntology ont = manager.getOntology(e.getOntologyID().getOntologyIRI());
				if (ont != null && _ontologies.get(pth) == null) {
					_ontologies.put(pth, new Ontology(ont, pth, this));
					_iri2ns.put(ont.getOntologyID().getDefaultDocumentIRI().toString(), pth);
				}
				
			} catch (Exception e) {
				
				/*
				 * everything else is probably an error
				 */
				throw new ThinklabIOException(e);
			}
		}
	}


	public IOntology refreshOntology(URL url, String id) throws ThinklabException {
	
		InputStream input;
		Ontology ret = null;
		
		try {
			input = url.openStream();
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(input);
			input.close();
			ret = new Ontology(ontology, id, this);
			ret.setResourceUrl(url.toString());
			_ontologies.put(id, ret);
			_iri2ns.put(ontology.getOntologyID().getDefaultDocumentIRI().toString(), id);

		} catch (OWLOntologyAlreadyExistsException e) {

			/*
			 * already imported- wrap it and use it as is.
			 */
			OWLOntology ont = manager.getOntology(e.getOntologyID().getOntologyIRI());
			if (ont != null && _ontologies.get(id) == null) {
				_ontologies.put(id, new Ontology(ont, id, this));
				_iri2ns.put(ont.getOntologyID().getDefaultDocumentIRI().toString(), id);
			}
			
		} catch (Exception e) {
			
			/*
			 * everything else is probably an error
			 */
			throw new ThinklabIOException(e);
		}
		
		return ret;
	}
	
	public IConcept getDatatypeMapping(String string) {
		return _xsdMappings.get(string);
	}

	public IConcept registerDatatypeMapping(String xsd, IConcept concept) {
		return _xsdMappings.put(xsd, concept);
	}
	
	public void releaseOntology(IOntology ontology) {
		
		// TODO remove from _csIndex - should be harmless to leave for now
		INamespace ns = _namespaces.get(ontology.getConceptSpace());
		if (ns != null) {
			_resourceIndex.remove(ns.getResourceUrl());
		}
		_namespaces.remove(ontology.getConceptSpace());
		_ontologies.remove(ontology.getConceptSpace());
		_iri2ns.remove(((Ontology)ontology)._ontology.getOntologyID().getDefaultDocumentIRI().toString());
		manager.removeOntology(((Ontology)ontology)._ontology);
	}

	
	public void clear() {
		Collection<String> keys = new HashSet<String>(_ontologies.keySet());
		for (String o : keys)
			releaseOntology(getOntology(o));
	}

	public Collection<IOntology> getOntologies() {
		return _ontologies.values();
	}
	
}
